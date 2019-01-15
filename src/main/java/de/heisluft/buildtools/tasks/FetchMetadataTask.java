package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.utils.BuildInfo;
import de.heisluft.buildtools.utils.Utils;
import de.heisluft.buildtools.BuildToolsExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskAction;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class FetchMetadataTask extends BuildToolsTask {

  private BuildInfo info;

  void setInfo(BuildInfo info) {
    this.info = info;
  }

  public Provider<BuildInfo> getInfo() {
    return getProject().provider(() -> info);
  }

  @TaskAction
  public void download() throws IOException, ParseException {
    info = BuildInfo.fromJSON(
        (JSONObject) JSONValue.parseWithException(Utils
            .readURL("https://hub.spigotmc.org/versions/" + getProject().getExtensions().getByType(BuildToolsExtension.class).getMcVersion() + ".json")));
  }


}