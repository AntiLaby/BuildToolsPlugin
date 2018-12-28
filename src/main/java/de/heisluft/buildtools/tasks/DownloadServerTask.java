package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.utils.BuildInfo;
import de.heisluft.buildtools.utils.Utils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;

public class DownloadServerTask extends DefaultTask {



  @TaskAction
  public void downloadMinecraftServerTask() {
    BuildInfo.MCInfo mcInfo = Utils.<FetchMetadataTask>getTask(getProject(), "fetchMetadata").getInfo().get().mcInfo;
    Path to = Utils.getBasePath(getProject()).resolve(mcInfo.gameVersion + "/vanilla.jar");
    if(!Files.exists(to)) try {
      Files.createFile(to);
      ReadableByteChannel rbc = Channels.newChannel(new URL(mcInfo.serverUrl).openStream());
      FileChannel c = FileChannel.open(to, StandardOpenOption.WRITE);
      c.transferFrom(rbc, 0, Long.MAX_VALUE);
      rbc.close();
      c.close();
    } catch(IOException e) {
      throw new RuntimeException("could not download vanilla jar", e);
    }

    if(mcInfo.mcHash != null) try {
      String md5 = Utils.MD5(Files.readAllBytes(to));
      if(!mcInfo.mcHash.equals(md5)) throw new RuntimeException(
          "Vanilla MD5 invalid, expected " + mcInfo.mcHash + " but is " + md5);
    } catch(IOException | NoSuchAlgorithmException e) {
      throw new RuntimeException("Could not check vanilla jars MD5", e);
    }
  }
}
