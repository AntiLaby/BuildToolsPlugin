package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.BuildToolsExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;

import java.nio.file.Path;

public abstract class BuildToolsTask extends DefaultTask {

  Path getBasePath() {
    return getProject().getGradle().getGradleUserHomeDir().toPath().resolve("caches/buildtools/");
  }

  BuildToolsExtension getExtension() {
    return getProject().getExtensions().getByType(BuildToolsExtension.class);
  }

  @SuppressWarnings("unchecked")
  <T extends Task> T getTask(String name) {
    return (T) getProject().getTasksByName(name, false).iterator().next();
  }
}
