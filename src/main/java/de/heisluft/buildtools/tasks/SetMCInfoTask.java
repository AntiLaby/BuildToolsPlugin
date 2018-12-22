package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.utils.BuildInfo;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class SetMCInfoTask extends DefaultTask {
  @TaskAction
  public void update() {
    FetchMetadataTask t = (FetchMetadataTask) getProject().getTasksByName("fetchMetadata", false)
        .iterator().next();
    BuildInfo info = t.getInfo().get();
    try {
      info.mcInfo = BuildInfo.MCInfo.fromJSON((JSONObject) JSONValue.parseWithException(new String(
          Files.readAllBytes(
              ((SetupReposTask) getProject().getTasksByName("setupRepos", false).iterator().next()).getBuildDataGit().get().getRepository().getDirectory().toPath()
                  .getParent().resolve("info.json")), StandardCharsets.UTF_8)));
    } catch(ParseException | IOException e) {
      throw new RuntimeException("Could not set mc info", e);
    }
    t.setInfo(info);
  }
}
