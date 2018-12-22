package de.heisluft.buildtools.utils;

import org.json.simple.JSONObject;

public class BuildInfo {

  public final String BuildData;
  public final String Bukkit;
  public final String CraftBukkit;
  public final String Spigot;
  public MCInfo mcInfo;

  public BuildInfo(String BuildData, String Bukkit, String CraftBukkit, String Spigot) {
    this.BuildData = BuildData;
    this.Bukkit = Bukkit;
    this.CraftBukkit = CraftBukkit;
    this.Spigot = Spigot;
  }

  public static BuildInfo fromJSON(JSONObject json) {
    JSONObject refs = (JSONObject) json.get("refs");
    return new BuildInfo((String) refs.get("BuildData"), (String) refs.get("Bukkit"), (String) refs.get("CraftBukkit"), (String) refs.get("Spigot"));
  }

  public static class MCInfo {

    public static final MCInfo FALLBACK = new MCInfo("1.8", "bukkit-1.8.at", "bukkit-1.8-cl.csrg", "bukkit-1.8-members.csrg", "package.srg", null, null);

    public final String gameVersion;
    public final String ats;
    public final String clMappings;
    public final String memberMappings;
    public final String pkgMappings;
    public final String mcHash;
    public String decompCmd;
    public final String serverUrl;


    public MCInfo(String gameVersion, String ats, String clMappings, String memberMappings, String pkgMappings, String mcHash, String serverUrl) {
      this(gameVersion, ats, clMappings, memberMappings, pkgMappings, mcHash, serverUrl, null);
    }

    public static MCInfo fromJSON(JSONObject json) {
      return new MCInfo((String) json.get("minecraftVersion"), (String) json.get("accessTransforms"), (String) json.get("classMappings"), (String) json.get("memberMappings"), (String) json.get("packageMappings"), (String) json.get("minecraftHash"), (String) json.get("serverUrl"));
    }

    MCInfo(String gameVersion, String ats, String clMappings, String memberMappings, String pkgMappings, String mcHash, String serverUrl, String decompCmd) {
      this.serverUrl = serverUrl != null ? serverUrl : "https://s3.amazonaws.com/Minecraft.Download/versions/" + gameVersion + "/minecraft_server." + gameVersion + ".jar";
      this.gameVersion = gameVersion;
      this.ats = ats;
      this.clMappings = clMappings;
      this.memberMappings = memberMappings;
      this.pkgMappings = pkgMappings;
      this.mcHash = mcHash;
      this.decompCmd = decompCmd;
    }
  }
}