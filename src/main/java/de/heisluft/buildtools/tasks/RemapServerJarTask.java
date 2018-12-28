package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.utils.BuildInfo;
import de.heisluft.buildtools.utils.Utils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class RemapServerJarTask extends DefaultTask {

  @TaskAction
  public void remapJar() throws IOException, ReflectiveOperationException, GitAPIException {
    String mcVersion = Utils.getExtension(getProject()).getMcVersion();
    SetupReposTask reposTask = Utils.getTask(getProject(), "setupRepos");
    Path mappingsPath = reposTask.getBuildDataGit().get().getRepository().getDirectory().toPath()
        .resolveSibling("mappings").toAbsolutePath();
    BuildInfo buildInfo = Utils.<FetchMetadataTask>getTask(getProject(), "fetchMetadata").getInfo()
        .get();
    String commitSHA1 = reposTask.getBuildDataGit().get().log()
        .addPath("mappings/" + buildInfo.mcInfo.ats)
        .addPath("mappings/" + buildInfo.mcInfo.clMappings)
        .addPath("mappings/" + buildInfo.mcInfo.memberMappings)
        .addPath("mappings/" + buildInfo.mcInfo.pkgMappings).setMaxCount(1).call().iterator().next()
        .getName();

    String mJarNameBase = "-mapped-" + commitSHA1 + ".jar";
    Path base = getProject().getGradle().getGradleUserHomeDir().toPath()
        .resolve("caches/buildtools/" + mcVersion).toAbsolutePath();

    Path finalMJ = base.resolve("mapped.jar");
    if(Files.exists(finalMJ)) return;
    Path clMJ = base.resolve("cl" + mJarNameBase);
    Path memberMJ = base.resolve("m" + mJarNameBase);

    Path jp = base.resolve("BuildData/bin");
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
        .resolve(buildInfo.mcInfo.clMappings).toString(), "-o", clMJ.toString()});
    if(!Files.exists(memberMJ)) ss2Main.invoke(null,
        (Object) new String[]{"map", "-i", clMJ.toString(), "-m", mappingsPath
            .resolve(buildInfo.mcInfo.memberMappings).toString(), "-o", memberMJ.toString()});
    ssMain.invoke(null,
        (Object) new String[]{"--kill-lvt", "-i", memberMJ.toString(), "--access-transformer",
            mappingsPath
            .resolve(buildInfo.mcInfo.ats).toString(), "-m", mappingsPath
            .resolve(buildInfo.mcInfo.pkgMappings).toString(), "-o", finalMJ.toString()});
    ulc.close();
  }
}