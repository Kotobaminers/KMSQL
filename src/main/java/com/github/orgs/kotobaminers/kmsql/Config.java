package com.github.orgs.kotobaminers.kmsql;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	public static String USER = "";
	public static String PASS = "";
	public static String DSN = "";

	private static boolean enable = false;
	public static String REGISTER_PAGE = "";
	public static List<String> MESSAGES = new ArrayList<String>();

	enum Setting {
		USER("username"),
		PASS("password"),
		DATABASE("database"),
		MESSAGE("message"),
		ENABLE("enable"),
		DSN("dsn"),
		REGISTER_PAGE("registration"),
		;

		private String setting = "";
		private Setting(String setting) {
			this.setting = setting;
		}
	}

	private static final File CONFIG_FILE = new File(KMSQL.plugin.getDataFolder() + "//config.yml");
	private static final YamlConfiguration CONFIG = YamlConfiguration.loadConfiguration(CONFIG_FILE);

	public static void importConfig() {
		enable = CONFIG.getBoolean(Setting.ENABLE.setting);
		USER = CONFIG.getString(Setting.DATABASE.setting + "." + Setting.USER.setting);
		PASS = CONFIG.getString(Setting.DATABASE.setting + "." + Setting.PASS.setting);
		DSN = CONFIG.getString(Setting.DATABASE.setting + "." + Setting.DSN.setting);
		REGISTER_PAGE = CONFIG.getString(Setting.REGISTER_PAGE.setting);
		MESSAGES = CONFIG.getStringList(Setting.MESSAGE.setting);
	}

	public static boolean isEnable() {
		return enable;
	}
}
