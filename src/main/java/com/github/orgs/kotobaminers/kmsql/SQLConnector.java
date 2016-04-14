package com.github.orgs.kotobaminers.kmsql;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class SQLConnector implements Listener{
	public static Connection connection;
	private static Map<UUID, Long> logins = new HashMap<UUID, Long>();

	public static void printRegistration(Player player, String key) {
		Config.MESSAGES.stream().forEach(msg -> player.sendMessage(msg));
		player.sendMessage("Registration: " + Config.REGISTER_PAGE.replaceAll("{key}", key));
	}

	public synchronized static void openConnection() {
		try {
			connection = DriverManager.getConnection(Config.DSN + "?user=" + Config.USER + "&password=" + Config.PASS);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized static void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public synchronized static boolean playerDataContainsPlayer(Player player) {
		try {
			PreparedStatement sql = connection.prepareCall("SELECT * FROM `stopspam` WHERE uid=?;");
			sql.setString(1, player.getUniqueId().toString());
			ResultSet resultSet = sql.executeQuery();
			boolean containsPlayer = resultSet.next();

			sql.close();
			resultSet.close();

			return containsPlayer;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		openConnection();
		try {
			if (!playerDataContainsPlayer(event.getPlayer())) {
				String uuid = event.getPlayer().getUniqueId().toString();
				MessageDigest md = MessageDigest.getInstance("MD5");
				String buff = uuid + String.valueOf(System.currentTimeMillis());
				StringBuffer sb = new StringBuffer();
				for (byte b : md.digest(buff.getBytes())) {
					sb.append(String.format("%02x", b & 0xff));
				}
				PreparedStatement newPlayer = connection.prepareStatement("INSERT INTO `stopspam` values(?,?,?,?,0,0,0);");
				newPlayer.setString(1, event.getPlayer().getName());
				newPlayer.setString(2, uuid);
				newPlayer.setString(3, sb.toString());
				newPlayer.setString(4, "Minecraft");
				newPlayer.execute();
				newPlayer.close();
			}

			//TODO to check if the player register on the forum or not.

			PreparedStatement sql = connection.prepareStatement("SELECT onlinecount FROM `stopspam` WHERE uid=?;");
			sql.setString(1,  event.getPlayer().getUniqueId().toString());
			ResultSet result = sql.executeQuery();
			result.next();

			int previousLogins = result.getInt("onlinecount");
			PreparedStatement loginsUpdate = connection.prepareStatement("UPDATE `stopspam` SET onlinecount=? WHERE uid=?;");
			loginsUpdate.setInt(1,  previousLogins + 1);
			loginsUpdate.setString(2, event.getPlayer().getUniqueId().toString());
			loginsUpdate.executeUpdate();

			loginsUpdate.close();
			sql.close();
			result.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
}
