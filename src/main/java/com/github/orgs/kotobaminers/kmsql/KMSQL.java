package com.github.orgs.kotobaminers.kmsql;

import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;


public class KMSQL extends JavaPlugin {
	public static KMSQL plugin;

	@Override
	public void onEnable() {
		plugin = this;
		getServer().getPluginManager().registerEvents(new SQLConnector(), this);
		Config.load();
	}

	@Override
	public void onDisable() {
		try {
			if(SQLConnector.connection != null && !SQLConnector.connection.isClosed()) {
				SQLConnector.connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}