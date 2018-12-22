package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.utils.BuildInfo;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DecompileMCTask extends DefaultTask {

  @TaskAction
  public void decompileMC() {
    BuildInfo.MCInfo mcInfo = ((FetchMetadataTask) getProject()
        .getTasksByName("fetchMetadata", false).iterator().next()).getInfo().get().mcInfo;
    Path base = getProject().getGradle().getGradleUserHomeDir().toPath().resolve("caches/buildtools/" + mcInfo.gameVersion)
        .toAbsolutePath();
    Path decompiled = base.resolve("decompiled");
    if(!Files.exists(decompiled)) try {
      Files.createDirectory(decompiled);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
    ConsoleDecompiler.main(new String[]{"-dgs=1", "-hdc=0", "-rbr=0", "-asc=1", "-udv=0", base
        .resolve("vanilla.jar").toString(), decompiled.toString()});
  }
}
