package com.cyprias.AdminNotes;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.xml.sax.SAXException;

import com.cyprias.AdminNotes.command.CommandManager;
import com.cyprias.AdminNotes.command.CreateCommand;
import com.cyprias.AdminNotes.command.InfoCommand;
import com.cyprias.AdminNotes.command.ListCommand;
import com.cyprias.AdminNotes.command.NotifyCommand;
import com.cyprias.AdminNotes.command.RemoveCommand;
import com.cyprias.AdminNotes.command.SearchCommand;
import com.cyprias.AdminNotes.configuration.Config;
import com.cyprias.AdminNotes.database.Database;
import com.cyprias.AdminNotes.database.MySQL;
import com.cyprias.AdminNotes.database.SQLite;
import com.cyprias.AdminNotes.listeners.PlayerListener;

public class Plugin extends JavaPlugin {
	// static PluginDescriptionFile description;
	private static Plugin instance = null;

	public void onLoad() {
		// description = getDescription();
	}

	public static Database database;

	public void onEnable() {
		instance = this;

		// File dataFolder = getDataFolder();
		// configFile = new File(dataFolder, "config.yml");
		getConfig().options().copyDefaults(true);
		saveConfig();

		if (Config.getString("properties.db-type").equalsIgnoreCase("mysql")) {
			database = new MySQL();
		} else if (Config.getString("properties.db-type").equalsIgnoreCase("sqlite")) {
			database = new SQLite();
		} else {
			Logger.severe("No database selected (" + Config.getString("properties.db-type") + "), unloading plugin...");
			instance.getPluginLoader().disablePlugin(instance);
			return;
		}

		if (!database.init()) {
			Logger.severe("Failed to initilize database, unloading plugin...");
			instance.getPluginLoader().disablePlugin(instance);
			return;
		}

		registerListeners(new PlayerListener());

		CommandManager cm = new CommandManager().registerCommand("create", new CreateCommand()).registerCommand("info", new InfoCommand())
			.registerCommand("list", new ListCommand()).registerCommand("notify", new NotifyCommand()).registerCommand("search", new SearchCommand())
			.registerCommand("remove", new RemoveCommand());

		this.getCommand("notes").setExecutor(cm);

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
		}

		if (Config.getBoolean("properties.check-new-version")) {
			try {
				VersionChecker version = new VersionChecker(this, "http://dev.bukkit.org/server-mods/adminnotes/files.rss");

				VersionChecker.versionInfo info = (version.versions.size() > 0) ? version.versions.get(0) : null;
				if (info == null)
					return;
				
				String curVersion = getDescription().getVersion();

				int compare = VersionChecker.compareVersions(curVersion, info.getTitle());
				// plugin.info("curVersion: " + curVersion +", title: " +
				// info.getTitle() + ", compare: " + compare);
				if (compare < 0) {
					Logger.warning("We're running v" + curVersion + ", v" + info.getTitle() + " is available");
					Logger.warning(info.getLink());
				}

			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}

		}

		Logger.info("enabled.");
	}

	private static class getVersionInfoTask implements Runnable {
		private Object[] args;
		private String pluginName, curseRSS;
		private PluginManager pm;

		public getVersionInfoTask(PluginManager pm, String pluginName, String curseRSS) {
			this.pm = pm;
			this.pluginName = pluginName;
			this.curseRSS = curseRSS;
		}

		public void setArgs(Object... args) {
			this.args = args;
		}

		@Override
		public void run() {

		}
	}

	private void registerListeners(Listener... listeners) {
		PluginManager manager = getServer().getPluginManager();

		for (Listener listener : listeners) {
			manager.registerEvents(listener, this);
		}
	}

	public void onDisable() {
		Logger.info("disabled.");
		instance = null;
	}

	public static void reload() {
		instance.reloadConfig();
	}

	public static void disable() {
		instance.getServer().getPluginManager().disablePlugin(instance);
	}

	static public boolean hasPermission(CommandSender sender, Perm permission) {
		if (sender != null) {
			if (sender.hasPermission(permission.getPermission())) {
				return true;
			} else {
				Perm parent = permission.getParent();
				return (parent != null) ? hasPermission(sender, parent) : false;
			}
		}
		return false;
	}

	public static final Plugin getInstance() {
		return instance;
	}

	public static double getUnixTime() {
		return (System.currentTimeMillis() / 1000D);
	}

	public static String getFinalArg(final String[] args, final int start) {
		final StringBuilder bldr = new StringBuilder();
		for (int i = start; i < args.length; i++) {
			if (i != start) {
				bldr.append(" ");
			}
			bldr.append(args[i]);
		}
		return bldr.toString();
	}

	public static boolean isInt(final String sInt) {
		try {
			Integer.parseInt(sInt);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean isDouble(final String sDouble) {
		try {
			Double.parseDouble(sDouble);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static <T> T[] concat(T[] first, T[]... rest) {

		// Read rest
		int totalLength = first.length;
		for (T[] array : rest) {
			totalLength += array.length;
		}

		// Concat with arraycopy
		T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;

	}
}
