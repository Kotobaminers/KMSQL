package com.github.orgs.kotobaminers.kmsql;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SQLConnector implements Listener{
	public static Connection connection;
	private static Map<UUID, Long> logins = new HashMap<UUID, Long>();

	private synchronized static void openConnection() {
		try {
			connection = DriverManager.getConnection(Config.DSN + "?user=" + Config.USER + "&password=" + Config.PASS);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private synchronized static void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void printRegistrationLink(Player player) {
		Config.MESSAGES.stream().forEach(msg -> player.sendMessage(msg));
		openConnection();
		try {
			updateRegisterLink(player);
			player.sendMessage("Registration: " + Config.REGISTER_PAGE.replaceAll("\\*REPLACE\\*", fetchKey(player)));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	private String fetchKey(Player player) throws SQLException{
		PreparedStatement sql = connection.prepareStatement("SELECT hash FROM `stopspam` WHERE uuid=?;");
		sql.setString(1,  player.getUniqueId().toString());
		ResultSet result = sql.executeQuery();
		result.next();
		return result.getString("hash");
	}

	//Not Working
	public static void refreshData() throws SQLException {
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement(
					"UPDATE FROM `stopspam` "
					+ "SET uuid = `KAITEST`, nickname = `KAITEST`"
					+ "WHERE nickname=Yukari_ModDev ;"
					);
			ResultSet result = sql.executeQuery();
			result.next();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		logins.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		openConnection();
		try {
			updateOnQuit(event.getPlayer());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
		removeOfflinePlayerTime();
	}

	private long calculateOnlineTime(Player player) {
		long time = 0;
		if (logins.containsKey(player.getUniqueId())) {
			time = (System.currentTimeMillis() - logins.get(player.getUniqueId())) / 1000;
		}
		return time;
	}

	private void removeOfflinePlayerTime() {
		List<UUID> onlines = Bukkit.getOnlinePlayers().stream().map(player -> player.getUniqueId()).collect(Collectors.toList());
		logins.keySet().stream().filter(uuid -> !onlines.contains(uuid)).forEach(uuid -> logins.remove(uuid));
	}

	private String generateHash(Player player) {
		String init = "DEFAULT";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			String buff = player.getUniqueId() + String.valueOf(System.currentTimeMillis());
			StringBuffer sb = new StringBuffer();
			for (byte b : md.digest(buff.getBytes())) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return init;
	}

	private synchronized void updateOnQuit(Player player) throws Exception {
		//Primary Key == uuid
		PreparedStatement data = connection.prepareStatement(
				"INSERT INTO `stopspam` (`uuid`, `forum_uuid`, `nickname`, `type`, `hash`, `onlinetime`, `onlinecount`, `created`, `lastupdate`)" +
				"VALUES(?, NULL, ?, ?, ?, ?, 1, NULL, NULL) " +
				"ON DUPLICATE KEY UPDATE onlinecount = onlinecount + 1, onlinetime = onlinetime + " + calculateOnlineTime(player) + " ;"
				);
		data.setString(1, player.getUniqueId().toString());
		data.setString(2, player.getName());
		data.setString(3, "Minecraft");
		data.setString(4, generateHash(player));
		data.setObject(5, calculateOnlineTime(player));
		data.execute();
		data.close();
	}

	private synchronized void updateRegisterLink(Player player) throws Exception {
		PreparedStatement data = connection.prepareStatement(
				"INSERT INTO `stopspam` (`uuid`, `forum_uuid`, `nickname`, `type`, `hash`, `onlinetime`, `onlinecount`, `created`, `lastupdate`)" +
				"VALUES(?, NULL, ?, ?, ?, 0, 0, NULL, NULL) " +
				"ON DUPLICATE KEY UPDATE hash = '" +  generateHash(player) + "';"
				);
		data.setString(1, player.getUniqueId().toString());
		data.setString(2, player.getName());
		data.setString(3, "Minecraft");
		data.setString(4, generateHash(player));
		data.execute();
		data.close();
	}



//	private synchronized static boolean containsPlayerData(Player player) {
//		try {
//			PreparedStatement sql = connection.prepareCall("SELECT * FROM `stopspam` WHERE uid=?;");
//			sql.setString(1, player.getUniqueId().toString());
//			ResultSet resultSet = sql.executeQuery();
//			boolean containsPlayer = resultSet.next();
//			sql.close();
//			resultSet.close();
//			return containsPlayer;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//
//		}
//	}

//	private synchronized void updatePlayerDataOld(Player player) throws Exception {
//	PreparedStatement sql = connection.prepareStatement("SELECT onlinecount FROM `stopspam` WHERE uid=?;");
//	sql.setString(1,  player.getUniqueId().toString());
//	ResultSet result = sql.executeQuery();
//	result.next();
//	int previousLogins = result.getInt("onlinecount");
//	PreparedStatement loginsUpdate = connection.prepareStatement("UPDATE `stopspam` SET onlinecount=? WHERE uid=?;");
//	loginsUpdate.setInt(1,  previousLogins + 1);
//	loginsUpdate.setString(2, player.getUniqueId().toString());
//	loginsUpdate.executeUpdate();
//	loginsUpdate.close();
//	sql.close();
//	result.close();
//}


}
