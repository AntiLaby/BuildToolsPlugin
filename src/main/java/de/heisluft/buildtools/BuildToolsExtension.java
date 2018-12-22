package de.heisluft.buildtools;

import de.heisluft.buildtools.utils.Server;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;

import java.util.HashMap;
import java.util.Map;

public class BuildToolsExtension {

	private final HashMap<String, String> pluginYML = new HashMap<>();
	/**Default fallback version: Bukkit 1.8*/
	private Server buildOn = new Server(Server.APIType.BUKKIT, "1.8");

	public void setPluginYML(Action<? super HashMap<String, String>> action) {
		action.execute(pluginYML);
	}

	public Map<String, String> getPluginYML() {
		return pluginYML;
	}

	/**
	 * Get the server's {@link Server.APIType API type}
	 * @return {@link Server#type}
	 */
	public Server.APIType getType() {
		return buildOn.type;
	}

	/**
	 * Set the server's {@link Server.APIType API type}
	 * @param type the new type
	 */
	public void setType(Server.APIType type) {
		buildOn = new Server(type, buildOn.mcVersion);
	}
	/**
	 * Get the server's {@link Server#mcVersion minecraft version}
	 * @return {@link Server#mcVersion}
	 */
	public String getMcVersion() {
		return buildOn.mcVersion;
	}
	/**
	 * Set the server's {@link Server#mcVersion minecraft version}
	 * @param mcVersion the new version
	 */
	public void setMcVersion(String mcVersion) {
	  buildOn = new Server(buildOn.type, mcVersion);
	}

	public void validate() {
		boolean a = pluginYML.containsKey("name");
		boolean b = pluginYML.containsKey("version");
		boolean c = pluginYML.containsKey("main");
		if(!a|| !b || !c)
			throw new InvalidUserDataException("yml is missing properties: [" + (a ? "" : (!(b || c) ? "'name, '" : "'name'")) + (b ? "" : (!c ? "'version'" : "'version', ")) + (c ? "" : "main") + "]");
	}
}
