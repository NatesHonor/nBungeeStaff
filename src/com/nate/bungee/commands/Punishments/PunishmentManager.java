package com.nate.bungee.commands.Punishments;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import com.nate.bungee.Core;
import com.nate.bungee.commands.Punishments.SQLStatements.Mutes;

public class PunishmentManager implements Listener {

    private String databaseURL = "jdbc:mysql://localhost:3306/fakenetwork";
    private String databaseUsername = "root";
    private String databasePassword = "";
    private Map<String, MuteInfo> mutedPlayers = new HashMap<>();
    Mutes mutes = new Mutes();

    public PunishmentManager() {
        Core.getInstance().getProxy().getPluginManager().registerListener(Core.getInstance(), this);
        createTables();
        mutes.loadMutedPlayers(this);
    }

    private void createTables() {
        try (Connection connection = DriverManager.getConnection(databaseURL, databaseUsername, databasePassword);
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS punishments_mutes ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "player_name VARCHAR(255),"
                    + "reason VARCHAR(255),"
                    + "mute_time TIMESTAMP,"
                    + "unmute_time TIMESTAMP,"
                    + "unmuted ENUM('yes', 'no') DEFAULT 'no'"
                    + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, MuteInfo> getMutedPlayers() {
        return mutedPlayers;
    }

    public void putMap(String playername, String reason, Long unmuteTime) {
        MuteInfo muteInfo = new MuteInfo(reason, unmuteTime);

        mutedPlayers.put(playername, muteInfo);
    }

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String playerName = player.getName();

        if (isPlayerMuted(playerName)) {
            MuteInfo muteInfo = mutedPlayers.get(playerName);
            long currentTime = System.currentTimeMillis();

            if (muteInfo.unmuteTime <= currentTime) {
                mutes.setPlayerUnmuted(playerName);
                mutedPlayers.remove(playerName);
            } else {
                if (!muteInfo.muteMessageSent) {
                    String muteMessage = String.format(
                            "You have been muted for %s for %s. If your time has expired, please rejoin. You can also appeal at (sample appeal url)",
                            formatDuration(muteInfo.unmuteTime - currentTime),
                            muteInfo.reason);
                    player.sendMessage(new TextComponent(muteMessage));
                    muteInfo.muteMessageSent = true;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(ChatEvent e) {
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        String playerName = player.getName();
        String message = e.getMessage();
        if (isPlayerMuted(playerName)) {
            if (!message.startsWith("/")) {
                e.setCancelled(true);
            }
        }
    }

    private boolean isPlayerMuted(String playerName) {
        MuteInfo muteInfo = mutedPlayers.get(playerName);
        if (muteInfo == null) {
            return mutes.isPlayerMuted(playerName);
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime >= muteInfo.unmuteTime) {
            mutedPlayers.remove(playerName);
            return false;
        }
        return true;
    }

    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        return String.format("%d days, %d hours, %d minutes", days, hours % 24, minutes % 60);
    }

    public static class MuteInfo {
        public String reason;
        public long unmuteTime;
        boolean muteMessageSent = false;

        public MuteInfo(String reason, long unmuteTime) {
            this.reason = reason;
            this.unmuteTime = unmuteTime;
        }
    }

}
