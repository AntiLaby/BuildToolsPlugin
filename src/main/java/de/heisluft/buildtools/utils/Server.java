package de.heisluft.buildtools.utils;

import javax.annotation.Nonnull;

/**
 * This class stores information about a server configuration. A configuration is composed of an
 * {@link #type API type} and a {@link #mcVersion minecraft version}. It is used to provide the
 * right dependencies for a project.
 */
public class Server {

  /**
   * The servers APIType, one of {@link APIType#BUKKIT Bukkit}, {@link APIType#SPIGOT Spigot} or
   * {@link APIType#PAPERSPIGOT PaperSpigot}
   *
   * @see APIType
   */
  public final APIType type;
  /**
   * The minecraft version the server is running on, (e.g. 1.12). It is used to provide the right
   * dependency versions and the right minecraft server
   */
  public final String mcVersion;

  /**
   * Construct a new Server instance
   *
   * @param type
   *     the server's {@link #type API type}
   * @param mcVersion,
   *     the server's {@link #mcVersion minecraft version}
   */
  public Server(@Nonnull APIType type, @Nonnull String mcVersion) {
    this.type = type;
    this.mcVersion = mcVersion;
  }

  /**
   * The API type a server uses. The API Type is responsible for patching the mc server with the
   * right set of patches and for providing the right dependencies
   */
  public enum APIType {/** <a href="https://bukkit.org/">Bukkit</a> */ BUKKIT("CraftBukkit"),
    /** <a href="https://spigotmc.org/">Spigot</a> */ SPIGOT("Spigot"),
    /** <a href="https://papermc.io/">PaperSpigot</a> */ PAPERSPIGOT("Paper");

    private final String name;

    APIType(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }}
}