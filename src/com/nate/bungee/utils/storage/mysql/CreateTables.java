package com.nate.bungee.utils.storage.mysql;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import com.nate.bungee.Core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CreateTables {
    Core core = Core.getInstance();

    public void createAllTables() {
        createAcceptedReportsTable();
        createLevelsTable();
        createReportsTable();
        createBansTable();
        createDeniedReportsTable();
        createWarnsTable();
    }

    private void createLevelsTable() {
        try {
            Statement statement = core.getConnection().createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS levels (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "playername VARCHAR(255) NOT NULL," +
                    "playeruuid VARCHAR(36) NOT NULL," +
                    "level INT NOT NULL," +
                    "exp INT," +
                    "eventlevel VARCHAR(255)" +
                    ")");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createReportsTable() {
        try {
            Statement statement = core.getConnection().createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS reports (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "reporter_name VARCHAR(255) NOT NULL," +
                    "reporter_uuid VARCHAR(36) NOT NULL," +
                    "reported_name VARCHAR(255) NOT NULL," +
                    "reported_uuid VARCHAR(36) NOT NULL," +
                    "reason TEXT" +
                    ")");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createBansTable() {
        try {
            Statement statement = core.getConnection().createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS punishments_bans (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "uuid VARCHAR(36) NULL," +
                    "name VARCHAR(16) NOT NULL," +
                    "reason TEXT," +
                    "banned_by VARCHAR(16)," +
                    "ban_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "unban_time TIMESTAMP NULL," +
                    "unbanned ENUM('yes', 'no') DEFAULT 'no'," +
                    "is_active BOOLEAN DEFAULT TRUE," +
                    "exact_duration VARCHAR(255) NOT NULL" +
                    ")");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void createWarnsTable() {
        try {
            Statement statement = core.getConnection().createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS punishments_warns ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "player_name VARCHAR(255),"
                    + "reason VARCHAR(255),"
                    + "timestamp TIMESTAMP"
                    + ")");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPlayerToLevelsTable(UUID playerUUID, String playerName, int level, int eventLevel) {
        try {
            PreparedStatement checkStatement = core.getConnection().prepareStatement(
                    "SELECT playername FROM levels WHERE playeruuid = ?");
            checkStatement.setString(1, playerUUID.toString());
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                resultSet.close();
                checkStatement.close();
                return;
            }

            resultSet.close();
            checkStatement.close();

            PreparedStatement insertStatement = core.getConnection().prepareStatement(
                    "INSERT INTO levels (playername, playeruuid, level, eventlevel) VALUES (?, ?, ?, ?)");
            insertStatement.setString(1, playerName);
            insertStatement.setString(2, playerUUID.toString());
            insertStatement.setInt(3, level);
            insertStatement.setInt(4, eventLevel);
            insertStatement.executeUpdate();
            insertStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createAcceptedReportsTable() {
        try {
            Statement statement = core.getConnection().createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS accepted_reports (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "reporter_name VARCHAR(255) NOT NULL," +
                    "reported_name VARCHAR(255) NOT NULL," +
                    "reason TEXT" +
                    ")");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createDeniedReportsTable() {
        try {
            Statement statement = core.getConnection().createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS denied_reports (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "reporter_name VARCHAR(255) NOT NULL," +
                    "reported_name VARCHAR(255) NOT NULL," +
                    "reason TEXT" +
                    ")");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
