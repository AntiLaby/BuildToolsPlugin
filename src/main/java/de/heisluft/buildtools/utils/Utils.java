package de.heisluft.buildtools.utils;


import de.heisluft.buildtools.BuildToolsExtension;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Utils {
  public static final String JSR_305 = "com.google.code.findbugs:jsr305:3.0.2";
  public static final Map<String, Map<String, String>> DEPS = new HashMap<>();
  private final static char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

  static {
    DEPS.put("1.8.8",
        map("netty", "4.0.23.Final", "troveOrFU", "net.sf.trove4j:trove4j:3.0.3", "guava", "17.0",
            "gson", "2.2.4", "comIO", "2.4", "comCod", "1.9", "comLan3", "3.3.2", "log4j", "2.0"));
    DEPS.put("1.9.4",
        map("netty", "4.0.23.Final", "troveOrFU", "it.unimi.dsi:fastutil:7.0.12", "guava", "17.0",
            "gson", "2.2.4", "comIO", "2.4", "comCod", "1.9", "comLan3", "3.3.2", "log4j", "2.0"));
    DEPS.put("1.10.2",
        map("netty", "4.0.23.Final", "troveOrFU", "it.unimi.dsi:fastutil:7.0.12", "guava", "17.0",
            "gson", "2.2.4", "comIO", "2.4", "comCod", "1.9", "comLan3", "3.3.2", "log4j", "2.0"));
    DEPS.put("1.11.2",
        map("netty", "4.0.23.Final", "troveOrFU", "it.unimi.dsi:fastutil:7.0.12", "guava", "17.0",
            "gson", "2.2.4", "comIO", "2.4", "comCod", "1.9", "comLan3", "3.3.2", "log4j", "2.0"));
    DEPS.put("1.12.2",
        map("netty", "4.1.9.Final", "troveOrFU", "it.unimi.dsi:fastutil:7.1.0", "guava", "21.0",
            "gson", "2.8.0", "comIO", "2.5", "comCod", "1.10", "comLan3", "3.5", "log4j", "2.8.1"));
    DEPS.put("1.13.2",
        map("netty", "4.1.25.Final", "troveOrFU", "it.unimi.dsi:fastutil:8.2.0", "guava", "21.0",
            "gson", "2.8.0", "comIO", "2.5", "comCod", "1.10", "comLan3", "3.5", "log4j", "2.8.1"));
  }

  private Utils() {}

  public static String MD5(byte[] bytes) throws NoSuchAlgorithmException {
    byte[] bytes1 = MessageDigest.getInstance("MD5").digest(bytes);
    char[] hexChars = new char[bytes1.length * 2];
    for(int j = 0; j < bytes1.length; j++) {
      int v = bytes1[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static String getLastMinor(String mcVersion) {
    if(mcVersion.startsWith("1.8")) return "1.8.8";
    if(mcVersion.startsWith("1.9")) return "1.9.4";
    if(mcVersion.startsWith("1.10")) return "1.10.2";
    if(mcVersion.startsWith("1.11")) return "1.11.2";
    if(mcVersion.startsWith("1.12")) return "1.12.2";
    if(mcVersion.startsWith("1.13")) return "1.13.2";
    throw new RuntimeException("Project can't be built against version " + mcVersion + "!");
  }

  private static <K, V> Map<K, V> map(K k, V v, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
      K k6, V v6, K k7, V v7, K k8, V v8) {
    Map<K, V> m = new HashMap<>(7);
    m.put(k, v);
    m.put(k2, v2);
    m.put(k3, v3);
    m.put(k4, v4);
    m.put(k5, v5);
    m.put(k6, v6);
    m.put(k7, v7);
    m.put(k8, v8);
    return m;
  }

  public static String readURL(String url) throws IOException {
    try(Scanner scanner = new Scanner(new URL(url).openStream(),
        StandardCharsets.UTF_8.toString())) {
      scanner.useDelimiter("\\A");
      return scanner.hasNext() ? scanner.next() : "";
    }
  }

  public static Path getBasePath(Project p) {
    return p.getGradle().getGradleUserHomeDir().toPath().resolve("caches/buildtools/");
  }

  public static BuildToolsExtension getExtension(Project p) {
    return p.getExtensions().getByType(BuildToolsExtension.class);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Task> T getTask(Project p, String name) {
    return (T) p.getTasksByName("name", false).iterator().next();
  }
}
