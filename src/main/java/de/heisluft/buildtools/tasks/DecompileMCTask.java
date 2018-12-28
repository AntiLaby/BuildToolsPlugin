package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.utils.Utils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class DecompileMCTask extends DefaultTask {

  @TaskAction
  public void decompileMC() {
    Path base = Utils.getBasePath(getProject()).resolve(Utils.getExtension(getProject()).getMcVersion()).toAbsolutePath();
    Path decompiled = base.resolve("decompiled");
    if(Files.exists(decompiled.resolve("mapped.jar"))) return;

    if(!Files.exists(decompiled)) try {
      Files.createDirectory(decompiled);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
    try {
      URLClassLoader urlcl = new URLClassLoader(
          new URL[]{base.resolve("BuildData/bin/fernflower.jar").toUri().toURL()}, getClass().getClassLoader());
      Method ffMain = Class
          .forName("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler", true, urlcl)
          .getMethod("main", String[].class);

      ffMain.invoke(null,
          (Object) new String[]{"-dgs=1", "-hdc=0", "-rbr=0", "-asc=1", "-udv=0", base
              .resolve("mapped.jar").toString(), decompiled.toString()});
      urlcl.close();
    } catch(IOException | ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
