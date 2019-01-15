package de.heisluft.buildtools.tasks;

import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class DecompileMCTask extends BuildToolsTask {

  @OutputFile
  public File getDecompiledJar() {
    return this.<RemapServerJarTask>getTask("remapServerJar").getMappedJar().get()
        .resolveSibling("decompiled/final-mapped.jar").toFile();
  }

  @TaskAction
  public void decompileMC() {
    Path finalMappedJar = this.<RemapServerJarTask>getTask("remapServerJar").getMappedJar().get();
    Path decompiled = finalMappedJar.resolveSibling("decompiled");
    if(Files.exists(decompiled.resolve("final-mapped.jar"))) return;
    try {
      if(!Files.exists(decompiled)) Files.createDirectory(decompiled);

      URLClassLoader urlcl = new URLClassLoader(new URL[]{getBasePath()
          .resolve(getExtension().getMcVersion() + "/BuildData/bin/fernflower.jar")
          .toUri().toURL()}, getClass().getClassLoader());
      Method ffMain = Class
          .forName("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler", true, urlcl)
          .getMethod("main", String[].class);

      ffMain.invoke(null,
          (Object) new String[]{"-dgs=1", "-hdc=0", "-rbr=0", "-asc=1", "-udv=0",
              finalMappedJar.toString(), decompiled.toString()});
      urlcl.close();
    } catch(IOException | ReflectiveOperationException e) {
      throw new RuntimeException("Could not decompile mc", e);
    }
  }
}
