package de.heisluft.buildtools.tasks;

import de.heisluft.buildtools.utils.BuildInfo;
import de.heisluft.buildtools.utils.Server;
import de.heisluft.buildtools.utils.Utils;
import de.heisluft.buildtools.BuildToolsExtension;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SetupReposTask extends DefaultTask {

  private Git buildDataGit;
  private Git craftBukkitGit;
  private Git spigotGit;
  private Git paperGit;

  private static Git setupRepo(Path basePath, String downloadURL, Server.APIType type,
      String refOrBranch) throws GitAPIException, IOException {
    Path folder = basePath.resolve(type != null ? type.getName() : "BuildData");
    boolean updateRepo = Files.exists(folder) && Files.newDirectoryStream(folder).iterator().hasNext();
    if(!updateRepo) {
      if(!Files.isDirectory(folder)) Files.createDirectories(folder);
      if(type == Server.APIType.PAPERSPIGOT)
        return new CloneCommand().setURI(downloadURL).setDirectory(folder.toFile())
            .setBranch(refOrBranch).call();
      else {
        Git git = new CloneCommand().setDirectory(folder.toFile()).setURI(downloadURL).call();
        git.reset().setRef(refOrBranch).setMode(ResetCommand.ResetType.HARD).call();
        return git;
      }
    }
    Git git = Git.open(folder.toFile());
    if(type != Server.APIType.PAPERSPIGOT)
     git.reset().setRef(refOrBranch).setMode(ResetCommand.ResetType.HARD).call();
    git.fetch().call();

    return git;
  }

  public Provider<Git> getCraftBukkitGit() {
    return getProject().provider(() -> craftBukkitGit);
  }

  public Provider<Git> getBuildDataGit() {
    return getProject().provider(() -> buildDataGit);
  }

  public Provider<Git> getSpigotGit() {
    return getProject().provider(() -> spigotGit);
  }

  public Provider<Git> getPaperGit() {
    return getProject().provider(() -> paperGit);
  }

  @TaskAction
  public void executeTask() {
    BuildToolsExtension ex = Utils.getExtension(getProject());
    Path basePath = Utils.getBasePath(getProject()).resolve(ex.getMcVersion());
    BuildInfo buildInfo = Utils.<FetchMetadataTask>getTask(getProject(), "fetchMetadata").getInfo().get();
    try {
      buildDataGit = setupRepo(basePath,
          "https://hub.spigotmc.org/stash/scm/spigot/builddata", null,
          buildInfo.BuildData);
      craftBukkitGit = setupRepo(basePath,
          "https://hub.spigotmc.org/stash/scm/spigot/craftbukkit",
          Server.APIType.BUKKIT, buildInfo.CraftBukkit);
      if(ex.getType() == Server.APIType.SPIGOT || ex.getType() == Server.APIType.PAPERSPIGOT)
        spigotGit = setupRepo(basePath,
            "https://hub.spigotmc.org/stash/scm/spigot/spigot.git",
            Server.APIType.SPIGOT, buildInfo.Spigot);
      if(ex.getType() == Server.APIType.PAPERSPIGOT)
        paperGit = setupRepo(basePath, "https://github.com/PaperMC/Paper.git",
            Server.APIType.PAPERSPIGOT, "ver/" + Utils.getLastMinor(ex.getMcVersion()));
    } catch(GitAPIException | IOException e) {
      e.printStackTrace();
    }
  }
}