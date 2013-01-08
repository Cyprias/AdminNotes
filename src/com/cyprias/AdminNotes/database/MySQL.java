package com.cyprias.AdminNotes.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.command.CommandSender;

import com.cyprias.AdminNotes.Logger;
import com.cyprias.AdminNotes.configuration.Config;

public class MySQL implements Database {


	public void list(CommandSender sender) {
		
	}

	static String prefix;
	public Boolean init() {
		if (!canConnect()){
			Logger.info("Failed to connect to MySQL!");
			return false;
		}
		prefix = Config.getString("mysql.prefix");
		
		try {
			createTables();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public void createTables() throws SQLException{
		Connection con = getConnection();
		
		if (tableExists(prefix+ "Notes") == false) {
			Logger.info("Creating Notes table.");
			con.prepareStatement("CREATE TABLE " + prefix+ "Notes (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, `time` BIGINT NOT NULL, `notify` BOOLEAN NOT NULL DEFAULT '0', `writer` VARCHAR(32) NOT NULL, `player` VARCHAR(32) NOT NULL, `note` TEXT NOT NULL) ENGINE = InnoDB").executeUpdate();
		}
		
	}
	
	public static boolean tableExists(String tableName) throws SQLException {
		boolean exists = false;
		Connection con = getConnection();
		PreparedStatement statement = con.prepareStatement("show tables like '" + tableName + "'");
		ResultSet result = statement.executeQuery();
		result.last();
		if (result.getRow() != 0) 
			exists = true;
		con.close();
		return exists;
	}

	
	private static String getURL(){
		return "jdbc:mysql://" + Config.getString("mysql.hostname") + ":" + Config.getInt("mysql.port") + "/" + Config.getString("mysql.database");
	}
	
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(getURL(), Config.getString("mysql.username"), Config.getString("mysql.password"));
	}
	
	private Boolean canConnect(){
		try {
			Connection con = getConnection();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
}
