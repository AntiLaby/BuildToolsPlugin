package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.BuildToolsExtension;
import de.heisluft.buildtools.utils.Utils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ExcludeDependenciesTask extends DefaultTask {

  private static <K, V> Map<K, V> map(K key, V value) {
    Map<K, V> m = new HashMap<>(1);
    m.put(key, value);
    return m;
  }

  private static void deleteRec(Path p) throws IOException {
    if(!Files.exists(p)) return;
    for(Path p1 : Files.walk(p).sorted((a, b) -> b.startsWith(a) ? 1 : a.startsWith(b) ? -1 : 0)
        .collect(Collectors.toCollection(ArrayList::new)))
      if(!p.toString().equals(p1.toString())) Files.delete(p1);
    Files.delete(p);
  }

  @TaskAction
  public void excludeDeps() {
    String mcVersion = Utils.getExtension(getProject()).getMcVersion();
    Path from = Utils.getBasePath(getProject()).resolve(mcVersion + "/vanilla.jar");
    Path to = from.getParent().resolve("vanilla-nodeps.jar");
    try(FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:/" +
        (Files.exists(to) ? to : Files.copy(from, to)).toAbsolutePath().toString()
            .replace('\\', '/')), map("create", "true"))) {
      deleteRec(fs.getPath("io"));
      // 1.8 uses trove4j
      if(mcVersion.equals("1.8")) deleteRec(fs.getPath("gnu"));
      // 1.8.x does not use fastutil
      if(!mcVersion.startsWith("1.8")) deleteRec(fs.getPath("it"));
      // 1.13 uses jopt-simple
      if(mcVersion.startsWith("1.13")) deleteRec(fs.getPath("joptsimple"));
      deleteRec(fs.getPath("org"));
      deleteRec(fs.getPath("com"));
      deleteRec(fs.getPath("javax"));
      // Delete all log4j files except for the custom log4j2.xml config
      if(mcVersion.startsWith("1.13") || mcVersion.startsWith("1.12")) {
        deleteRec(fs.getPath("META-INF/org"));
        Files.delete(fs.getPath("Log4j-levels.xsd"));
      }
      Files.delete(fs.getPath("META-INF/log4j-provider.properties"));
      Files.delete(fs.getPath("Log4j-events.xsd"));
      Files.delete(fs.getPath("Log4j-config.xsd"));
      Files.delete(fs.getPath("Log4j-events.dtd"));
      // Cleanup Netty files
      deleteRec(fs.getPath("META-INF/native"));
      Files.delete(fs.getPath("META-INF/io.netty.versions.properties"));
      Files.delete(fs.getPath("META-INF/INDEX.LIST"));
    } catch(IOException e) {
      throw new RuntimeException("could not exclude dependencies", e);
    }
  }
}
