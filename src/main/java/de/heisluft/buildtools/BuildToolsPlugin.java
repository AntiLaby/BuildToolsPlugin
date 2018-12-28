package de.heisluft.buildtools;

import de.heisluft.buildtools.tasks.ExcludeDependenciesTask;
import de.heisluft.buildtools.tasks.ExtractMCSourcesTask;
import de.heisluft.buildtools.tasks.FetchMetadataTask;
import de.heisluft.buildtools.tasks.RemapServerJarTask;
import de.heisluft.buildtools.tasks.SetupReposTask;
import de.heisluft.buildtools.utils.Server;
import de.heisluft.buildtools.utils.Utils;
import de.heisluft.buildtools.tasks.DecompileMCTask;
import de.heisluft.buildtools.tasks.DownloadServerTask;
import de.heisluft.buildtools.tasks.SetMCInfoTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.TaskContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BuildToolsPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    TaskContainer tasks = project.getTasks();
    project.getExtensions().add("mcConfiguration", BuildToolsExtension.class);
    FetchMetadataTask fmt = tasks.create("fetchMetadata", FetchMetadataTask.class);
    SetupReposTask srt = tasks.create("setupRepos", SetupReposTask.class);
    SetMCInfoTask umt = tasks.create("setMCInfo", SetMCInfoTask.class);
    DownloadServerTask dst = tasks.create("downloadServer", DownloadServerTask.class);
    ExcludeDependenciesTask edt = tasks.create("excludeDeps", ExcludeDependenciesTask.class);
    RemapServerJarTask rsjt = tasks.create("remapServerJar", RemapServerJarTask.class);
    DecompileMCTask dmct = tasks.create("decompileMC", DecompileMCTask.class);
    ExtractMCSourcesTask est = tasks.create("extractMCSources", ExtractMCSourcesTask.class);
    umt.setDependsOn(Collections.singleton(srt));
    srt.setDependsOn(Collections.singleton(fmt));
    dst.setDependsOn(Collections.singleton(umt));
    edt.setDependsOn(Collections.singleton(dst));
    rsjt.setDependsOn(Collections.singleton(edt));
    dmct.setDependsOn(Collections.singleton(rsjt));
    est.setDependsOn(Collections.singleton(dmct));
    project.getRepositories().mavenCentral();
    project.getGradle().addListener(new DRL(project));
  }

  private static class DRL implements DependencyResolutionListener {

    private Project project;

    private DRL(Project project) {
      this.project = project;
    }

    private static ModuleDependency create(String from, DependencyHandler handler) {
      return (ModuleDependency) handler.create(from);
    }

    @Override
    public void beforeResolve(ResolvableDependencies dependencies) {
      BuildToolsExtension ex = project.getExtensions().getByType(BuildToolsExtension.class);
      project.getRepositories().maven(repo -> repo.setUrl(ex.getType() ==
          Server.APIType.PAPERSPIGOT ? "https://papermc.io/repo/repository/maven-public/" :
          "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"));
      project.getRepositories()
          .maven(repo -> repo.setUrl("https://oss.sonatype.org/content/repositories/snapshots/"));

      DependencySet o = project.getConfigurations().getByName("compile").getDependencies();
      DependencyHandler h = project.getDependencies();

      // Compile vanilla dependencies
      Map<String, String> vanillaDeps = Utils.DEPS.get(Utils.getLastMinor(ex.getMcVersion()));
      System.out.println(vanillaDeps);
      if(Utils.getLastMinor(ex.getMcVersion()).equals("1.13.2"))
        o.add(create("net.sf.jopt-simple:jopt-simple:5.0.3", h));
      // jna is 1.8.3 only (oshi 1.1, too BUT oshi 1.1 is not on maven (ノಠ益ಠ)ノ彡┻━┻)
      if(ex.getMcVersion().equals("1.8.3"))
        o.add(create("net.java.dev.jna:jna:jar:3.4.0", h));
      o.add(create(Utils.JSR_305, h));
      o.add(create(vanillaDeps.get("troveOrFU"), h));
      o.add(create("org.apache.logging.log4j:log4j-core:" + vanillaDeps.get("log4j"), h));
      o.add(create("org.apache.commons:commons-lang3:" + vanillaDeps.get("comLan3"), h));
      o.add(create("commons-io:commons-io:" + vanillaDeps.get("comIO"), h));
      o.add(create("commons-codec:commons-codec:" + vanillaDeps.get("comCod"), h));
      o.add(create("com.google.code.gson:gson:" + vanillaDeps.get("gson"), h));
      o.add(create("com.google.guava:guava:" + vanillaDeps.get("guava"), h));
      o.add(create("io.netty:netty-all:" + vanillaDeps.get("netty"), h));

      // Compile right api
      String dependencyVersion = ex.getMcVersion() + "-R0.1-SNAPSHOT";
      Map<String, String> noJUnitKThxBye = new HashMap<>(1);
      noJUnitKThxBye.put("group", "junit");
      switch(ex.getType()) {
        case PAPERSPIGOT:
          o.add(create("com.destroystokyo.paper:paper-api:" + dependencyVersion, h)
              .exclude(noJUnitKThxBye));
          break;
        case SPIGOT:
          o.add(create("org.spigotmc:spigot-api:" + dependencyVersion, h).exclude(noJUnitKThxBye));
          break;
        case BUKKIT:
          o.add(create("org.bukkit:bukkit:" + dependencyVersion, h).exclude(noJUnitKThxBye));
          break;
      }
      project.getGradle().removeListener(this);
    }

    @Override
    public void afterResolve(ResolvableDependencies dependencies) {
    }
  }
}