package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.utils.BuildInfo;
import de.heisluft.buildtools.utils.Utils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class RemapServerJarTask extends DefaultTask {
  @TaskAction
  public void remapJar() throws IOException, ReflectiveOperationException, GitAPIException {
    String mcVersion = Utils.getExtension(getProject()).getMcVersion();
    SetupReposTask reposTask = Utils.getTask(getProject(), "setupRepos");
    BuildInfo buildInfo = Utils.<FetchMetadataTask>getTask(getProject(), "fetchMetadata").getInfo().get();
    RevCommit mappings = reposTask.getBuildDataGit().get().log().addPath("mappings/" + buildInfo.mcInfo.ats)
        .addPath("mappings/" + buildInfo.mcInfo.clMappings)
        .addPath("mappings/" + buildInfo.mcInfo.memberMappings)
        .addPath("mappings/" + buildInfo.mcInfo.pkgMappings).setMaxCount(1).call().iterator().next();

    Path base = getProject().getGradle().getGradleUserHomeDir().toPath().resolve("caches/buildtools/" + mcVersion);
    Path jp = base.resolve("BuildData/bin");
    URLClassLoader ulc = new URLClassLoader(new URL[] {
        new URL(jp.resolve("SpecialSource-2.jar").toUri().toString()),
        new URL(jp.resolve("SpecialSource.jar").toUri().toString())
    }, getClass().getClassLoader());
    Class.forName("net.md_5.ss.SpecialSource").getDeclaredMethod("main", String[].class).invoke(null, (Object) new String[] {"", ""});
  }
}