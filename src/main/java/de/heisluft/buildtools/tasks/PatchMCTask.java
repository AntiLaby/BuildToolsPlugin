package de.heisluft.buildtools.tasks;

import difflib.DiffUtils;
import difflib.Patch;
import org.gradle.api.tasks.TaskAction;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PatchMCTask extends BuildToolsTask {
  @TaskAction
  @SuppressWarnings("unchecked")
  public void patchMC() throws Exception {
    Path patchesDir = getBasePath()
        .resolve(getExtension().getMcVersion() + "/CraftBukkit/nms-patches");
    Path mappedDir = this.<RemapServerJarTask>getTask("remapServerJar").getMappedJar().get()
        .getParent();
    Path patchedCB = mappedDir.resolve("patched-cb");
    if(!Files.exists(patchedCB.resolve("src/main/java/net/minecraft/server")))
      Files.createDirectories(patchedCB.resolve("src/main/java/net/minecraft/server"));

    for(Path patchFile : Files.newDirectoryStream(patchesDir)) {
      if(!patchFile.getFileName().toString().endsWith(".patch")) continue;

      String targetFile =
          "src/main/java/net/minecraft/server/" + patchFile.getFileName().toString().replace(".patch", ".java");

      Path clean = mappedDir.resolve("decompiled/" + targetFile);
      Path to = mappedDir.resolve("patched-cb/" + targetFile);

      if(Files.exists(to)) continue;

      List<String> patchLines = Files.readAllLines(patchFile, StandardCharsets.UTF_8);

      // Manually append prelude if it is not found in the first few lines.
      boolean preludeFound = false;
      for(int i = 0; i < Math.min(3, patchLines.size()); i++) {
        if(patchLines.get(i).startsWith("+++")) {
          preludeFound = true;
          break;
        }
      }
      if(!preludeFound) {
        patchLines.add(0, "+++");
      }
      System.out.println("attempting to patch " + patchFile.getFileName());
      Patch parsedPatch = DiffUtils.parseUnifiedDiff(patchLines);
      Files.write(to, (List<String>) DiffUtils
          .patch(Files.readAllLines(clean, StandardCharsets.UTF_8), parsedPatch));
    }
  }
}
