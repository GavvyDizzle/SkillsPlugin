package com.github.gavvydizzle.skillsplugin.storage;

import com.github.gavvydizzle.skillsplugin.SkillsPlugin;
import com.github.gavvydizzle.skillsplugin.player.SkillInformation;
import com.github.gavvydizzle.skillsplugin.player.SkillPlayer;
import com.github.gavvydizzle.skillsplugin.skill.SkillType;
import com.github.mittenmc.serverutils.UUIDConverter;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

public abstract class Database {

    private final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS skills(" +
            "uuid BINARY(16) PRIMARY KEY NOT NULL," +
            "archeryXP BIGINT DEFAULT 0 NOT NULL," +
            "excavationXP BIGINT DEFAULT 0 NOT NULL," +
            "farmingXP BIGINT DEFAULT 0 NOT NULL," +
            "fishingXP BIGINT DEFAULT 0 NOT NULL," +
            "foragingXP BIGINT DEFAULT 0 NOT NULL," +
            "hunterXP BIGINT DEFAULT 0 NOT NULL," +
            "miningXP BIGINT DEFAULT 0 NOT NULL," +
            "slayerXP BIGINT DEFAULT 0 NOT NULL," +
            "archeryAbility BIGINT DEFAULT 0 NOT NULL," +
            "excavationAbility BIGINT DEFAULT 0 NOT NULL," +
            "farmingAbility BIGINT DEFAULT 0 NOT NULL," +
            "fishingAbility BIGINT DEFAULT 0 NOT NULL," +
            "foragingAbility BIGINT DEFAULT 0 NOT NULL," +
            "hunterAbility BIGINT DEFAULT 0 NOT NULL," +
            "miningAbility BIGINT DEFAULT 0 NOT NULL," +
            "slayerAbility BIGINT DEFAULT 0 NOT NULL" +
            ");";

    private final String LOAD_PLAYER = "SELECT * FROM skills WHERE uuid=?;";

    private final String UPSERT_PLAYER = "INSERT OR REPLACE INTO skills(uuid," +
            " archeryXP, excavationXP, farmingXP, fishingXP, foragingXP, hunterXP, miningXP, slayerXP," +
            " archeryAbility, excavationAbility, farmingAbility, fishingAbility, foragingAbility, hunterAbility, miningAbility, slayerAbility" +
            ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

    SkillsPlugin plugin;
    Connection connection;

    public Database(SkillsPlugin instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    /**
     * Creates the tables if they do not already exist
     */
    public void createTables() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(CREATE_TABLE);
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * @param uuid The player's UUID
     * @return The player's SkillInformation or a blank one if an error occurs
     */
    @Nullable
    public SkillInformation loadPlayerInfo(UUID uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        SkillInformation skillInformation = new SkillInformation();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(LOAD_PLAYER);
            ps.setBytes(1, UUIDConverter.convert(uuid));
            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                skillInformation.setTotalXP(SkillType.ARCHERY, resultSet.getLong(2));
                skillInformation.setTotalXP(SkillType.EXCAVATION, resultSet.getLong(3));
                skillInformation.setTotalXP(SkillType.FARMING, resultSet.getLong(4));
                skillInformation.setTotalXP(SkillType.FISHING, resultSet.getLong(5));
                skillInformation.setTotalXP(SkillType.FORAGING, resultSet.getLong(6));
                skillInformation.setTotalXP(SkillType.HUNTER, resultSet.getLong(7));
                skillInformation.setTotalXP(SkillType.MINING, resultSet.getLong(8));
                skillInformation.setTotalXP(SkillType.SLAYER, resultSet.getLong(9));

                skillInformation.setLastUseTime(SkillType.ARCHERY, resultSet.getLong(10));
                skillInformation.setLastUseTime(SkillType.EXCAVATION, resultSet.getLong(11));
                skillInformation.setLastUseTime(SkillType.FARMING, resultSet.getLong(12));
                skillInformation.setLastUseTime(SkillType.FISHING, resultSet.getLong(13));
                skillInformation.setLastUseTime(SkillType.FORAGING, resultSet.getLong(14));
                skillInformation.setLastUseTime(SkillType.HUNTER, resultSet.getLong(15));
                skillInformation.setLastUseTime(SkillType.MINING, resultSet.getLong(16));
                skillInformation.setLastUseTime(SkillType.SLAYER, resultSet.getLong(17));

                skillInformation.updateLevels();
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            skillInformation = null;
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return skillInformation;
    }

    /**
     * Updates this player's info in the database
     * @param skillPlayer The SkillPlayer
     */
    public void updatePlayerInfo(@Nullable SkillPlayer skillPlayer) {
        if (skillPlayer == null) return;

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(UPSERT_PLAYER);
            ps.setBytes(1, UUIDConverter.convert(skillPlayer.getPlayer().getUniqueId()));
            SkillInformation skillInformation = skillPlayer.getSkillInformation();
            ps.setLong(2, skillInformation.getTotalXP(SkillType.ARCHERY));
            ps.setLong(3, skillInformation.getTotalXP(SkillType.EXCAVATION));
            ps.setLong(4, skillInformation.getTotalXP(SkillType.FARMING));
            ps.setLong(5, skillInformation.getTotalXP(SkillType.FISHING));
            ps.setLong(6, skillInformation.getTotalXP(SkillType.FORAGING));
            ps.setLong(7, skillInformation.getTotalXP(SkillType.HUNTER));
            ps.setLong(8, skillInformation.getTotalXP(SkillType.MINING));
            ps.setLong(9, skillInformation.getTotalXP(SkillType.SLAYER));
            ps.setLong(10, skillInformation.getLastUseTime(SkillType.ARCHERY));
            ps.setLong(11, skillInformation.getLastUseTime(SkillType.EXCAVATION));
            ps.setLong(12, skillInformation.getLastUseTime(SkillType.FARMING));
            ps.setLong(13, skillInformation.getLastUseTime(SkillType.FISHING));
            ps.setLong(14, skillInformation.getLastUseTime(SkillType.FORAGING));
            ps.setLong(15, skillInformation.getLastUseTime(SkillType.HUNTER));
            ps.setLong(16, skillInformation.getLastUseTime(SkillType.MINING));
            ps.setLong(17, skillInformation.getLastUseTime(SkillType.SLAYER));
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }/**
     * Updates a list of players
     * @param skillPlayers A Collection of SkillPlayers
     */
    public void updatePlayerInfo(@Nullable Collection<SkillPlayer> skillPlayers) {
        if (skillPlayers == null || skillPlayers.isEmpty()) return;

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(UPSERT_PLAYER);

            for (SkillPlayer skillPlayer : skillPlayers) {
                if (skillPlayer == null) continue;

                ps.setBytes(1, UUIDConverter.convert(skillPlayer.getPlayer().getUniqueId()));
                SkillInformation skillInformation = skillPlayer.getSkillInformation();
                ps.setLong(2, skillInformation.getTotalXP(SkillType.ARCHERY));
                ps.setLong(3, skillInformation.getTotalXP(SkillType.EXCAVATION));
                ps.setLong(4, skillInformation.getTotalXP(SkillType.FARMING));
                ps.setLong(5, skillInformation.getTotalXP(SkillType.FISHING));
                ps.setLong(6, skillInformation.getTotalXP(SkillType.FORAGING));
                ps.setLong(7, skillInformation.getTotalXP(SkillType.HUNTER));
                ps.setLong(8, skillInformation.getTotalXP(SkillType.MINING));
                ps.setLong(9, skillInformation.getTotalXP(SkillType.SLAYER));
                ps.setLong(10, skillInformation.getLastUseTime(SkillType.ARCHERY));
                ps.setLong(11, skillInformation.getLastUseTime(SkillType.EXCAVATION));
                ps.setLong(12, skillInformation.getLastUseTime(SkillType.FARMING));
                ps.setLong(13, skillInformation.getLastUseTime(SkillType.FISHING));
                ps.setLong(14, skillInformation.getLastUseTime(SkillType.FORAGING));
                ps.setLong(15, skillInformation.getLastUseTime(SkillType.HUNTER));
                ps.setLong(16, skillInformation.getLastUseTime(SkillType.MINING));
                ps.setLong(17, skillInformation.getLastUseTime(SkillType.SLAYER));
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }
}