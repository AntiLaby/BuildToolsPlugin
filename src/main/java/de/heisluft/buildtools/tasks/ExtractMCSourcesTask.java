package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.utils.Utils;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ExtractMCSourcesTask extends BuildToolsTask {

  private static String rmTrailingSlash(Path p) {
    String s = p.toString();
    return s.startsWith("/") ? s.substring(1) : s;
  }

  @TaskAction
  public void extractMCSources() {
    Path base = this.<RemapServerJarTask>getTask("remapServerJar").getMappedJar().get()
        .resolveSibling("decompiled").toAbsolutePath();
    Path mJar = base.resolve("final-mapped.jar");
    try(FileSystem fs = FileSystems
        .newFileSystem(URI.create("jar:file:/" + mJar.toString().replace('\\', '/')),
            Utils.map("create", "true"))) {
      Path srcMainJavaP = base.resolve("src/main/java");
      if(!Files.exists(srcMainJavaP)) Files.createDirectories(srcMainJavaP);
      Path srcMainResP = srcMainJavaP.resolveSibling("resources");
      if(!Files.exists(srcMainResP)) Files.createDirectory(srcMainResP);
      for(Path p : Files.walk(fs.getPath("/")).filter((p) -> !Files.isDirectory(p))
          .collect(Collectors.toCollection(ArrayList::new))) {
        Path to = p.getFileName().toString().endsWith("java") ? srcMainJavaP
            .resolve(rmTrailingSlash(p)) : srcMainResP.resolve(rmTrailingSlash(p));
        if(!Files.isDirectory(to.getParent())) Files.createDirectories(to.getParent());
        if(!Files.exists(to)) Files.copy(p, to);
      }
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}