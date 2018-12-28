package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.utils.BuildInfo;
import de.heisluft.buildtools.utils.Utils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class RemapServerJarTask extends DefaultTask {

  private Path mappedJar;

  public Provider<Path> getMappedJar() {
    return getProject().provider(() -> mappedJar);
  }

  @TaskAction
  public void remapJar() {
    BuildInfo.MCInfo info = Utils.<FetchMetadataTask>getTask(getProject(), "fetchMetadata")
        .getInfo().get().mcInfo;
    Path base = Utils.getBasePath(getProject())
        .resolve(Utils.getExtension(getProject()).getMcVersion());
    Path mappedDir;
    try {
      mappedDir = base.resolve("mapped-" +
          Utils.<SetupReposTask>getTask(getProject(), "setupRepos").getBuildDataGit().get().log()
              .addPath("mappings/" + info.ats).addPath("mappings/" + info.clMappings)
              .addPath("mappings/" + info.memberMappings).addPath("mappings/" + info.pkgMappings)
              .setMaxCount(1).call().iterator().next().getName());
    } catch(GitAPIException e) {
      throw new RuntimeException("Could not resolve rev commit", e);
    }
    Path jp = base.resolve("BuildData/bin");
    Path mappingsPath = jp.resolveSibling("mappings");
    mappedJar = mappedDir.resolve("final-mapped.jar");
    if(Files.exists(mappedJar)) return;
    Path clMJ = mappedDir.resolve("cl-mapped.jar");
    Path memberMJ = mappedDir.resolve("m-mapped.jar");

    try {
      if(!Files.exists(mappedDir)) Files.createDirectory(mappedDir);
      //Load SpecialSource, SpecialSource2 jars
      URLClassLoader ulc = new URLClassLoader(
          new URL[]{new URL(jp.resolve("SpecialSource-2.jar").toUri().toString()), new URL(
              jp.resolve("SpecialSource.jar").toUri().toString())}, getClass().getClassLoader());
      // Resolve main methods
      Method ss2Main = Class.forName("net.md_5.ss.SpecialSource", true, ulc)
          .getDeclaredMethod("main", String[].class);
      Method ssMain = Class.forName("net.md_5.specialsource.SpecialSource", true, ulc)
          .getDeclaredMethod("main", String[].class);

      // invoke main methods => remap
      if(!Files.exists(clMJ)) ss2Main.invoke(null, (Object) new String[]{"map", "-i", base
          .resolve("vanilla-nodeps.jar").toString(), "-m", mappingsPath
          .resolve(info.clMappings).toString(), "-o", clMJ.toString()});
      if(!Files.exists(memberMJ)) ss2Main.invoke(null,
          (Object) new String[]{"map", "-i", clMJ.toString(), "-m", mappingsPath
              .resolve(info.memberMappings).toString(), "-o", memberMJ.toString()});
      ssMain.invoke(null,
          (Object) new String[]{"--kill-lvt", "-i", memberMJ.toString(), "--access-transformer",
              mappingsPath
              .resolve(info.ats).toString(), "-m", mappingsPath
              .resolve(info.pkgMappings).toString(), "-o", mappedJar.toString()});
      ulc.close();
    } catch(IOException | ReflectiveOperationException e) {
      throw new RuntimeException("Could not remap jar", e);
    }
  }
}