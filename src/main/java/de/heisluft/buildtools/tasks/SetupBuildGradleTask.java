package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.utils.Utils;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetupBuildGradleTask extends BuildToolsTask {

  @OutputFile
  public File getOutputFile() {
    return getBasePath().resolve(getExtension().getMcVersion() + "/gradle.template").toFile();
  }

  private static final Pattern RPLC_PATTERN = Pattern.compile("\\$\\{([^{}]+)\\}");

  private static List<MatchResult> allResults(Matcher m) {
    List<MatchResult> toReturn = new ArrayList<>();
    while(m.find()) toReturn.add(m.toMatchResult());
    return toReturn;
  }

  @TaskAction
  public void setupBuildGradle() throws IOException {
    byte[] bytes = new byte[631]; //631 because that's the file size
    InputStream is = getClass().getClassLoader().getResourceAsStream("gradle.template");
    { //new scope for temp vars
      int i = 0;
      int cur;
      while((cur = is.read()) != -1) bytes[i++] = (byte) cur;
    }
    String file = new String(bytes, StandardCharsets.UTF_8);//.replace("\0", "");
    Matcher m = RPLC_PATTERN.matcher(file);
    String mcVersion = getExtension().getMcVersion();
    Map<String, String> dependencies = Utils.DEPS.get(Utils.getLastMinor(mcVersion));
    for(MatchResult r : allResults(m)) {
      file = file.replace(r.group(0), dependencies.get(r.group(1)));
    }
    Files.write(getBasePath().resolve(mcVersion + "/gradle.template"), file.getBytes(StandardCharsets.UTF_8));
  }
}
