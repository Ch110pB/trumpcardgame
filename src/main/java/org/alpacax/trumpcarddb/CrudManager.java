package org.alpacax.trumpcarddb;

import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

@UtilityClass
public class CrudManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String JDBC = "jdbc:sqlite:";
    private static final String DB_LOC = "src/main/resources/players.db";
    public static final String NULL_CONN_MESSAGE = "The connection is null.";

    public static void createNewDatabase() {

        final File file = new File(DB_LOC);
        if (file.exists()) {
            LOGGER.info("The player database already exists.");
        } else {
            try (Connection conn = DriverManager.getConnection(JDBC + DB_LOC)) {
                if (conn != null) {
                    LOGGER.info("The player database has been created.");
                }
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void createIdTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS ids (
                id INTEGER PRIMARY KEY,
                playerPageId TEXT NOT NULL,
                updatedAt TEXT
                );
                """;

        try (Connection conn = DriverManager.getConnection(JDBC + DB_LOC);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("IDs table has been created.");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void createStatTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS stats (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                country TEXT NOT NULL,
                matches INTEGER,
                runs INTEGER,
                notOuts INTEGER,
                highestScore INTEGER,
                highestScoreFlag INTEGER,
                batAvg REAL,
                batStrikeRate REAL,
                hundreds INTEGER,
                fifties INTEGER,
                overs REAL,
                wickets INTEGER,
                bowlAvg REAL,
                bowlEconRate REAL,
                bbiWickets INTEGER,
                bbiRuns INTEGER,
                catches INTEGER,
                imageUrl TEXT,
                updatedAt TEXT
                );
                """;

        try (Connection conn = DriverManager.getConnection(JDBC + DB_LOC);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Stats table has been created.");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void createRankTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS ranks (
                id INTEGER PRIMARY KEY,
                playerRank INTEGER NOT NULL,
                updatedAt TEXT
                );
                """;

        try (Connection conn = DriverManager.getConnection(JDBC + DB_LOC);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Ranks table has been created.");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static Connection connect() {

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(JDBC + DB_LOC);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }

        return conn;
    }

    public static void updateIdTable(PlayerId pid) {

        String sql = """
                INSERT INTO ids
                (id, playerPageId, updatedAt)
                VALUES (?,?,?)
                ON CONFLICT (id)
                DO UPDATE SET
                playerPageId = excluded.playerPageId,
                updatedAt = excluded.updatedAt
                """;
        Connection conn = Preconditions.checkNotNull(connect(), NULL_CONN_MESSAGE);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pid.getId());
            stmt.setString(2, pid.getPlayerPageId());
            stmt.setString(3, String.valueOf(LocalDate.now()));
            stmt.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void updateStatTable(Player player) {

        String sql = """
                INSERT INTO stats
                (id, name, country, matches, runs, notOuts, highestScore,
                highestScoreFlag, batAvg, batStrikeRate, hundreds, fifties, overs, wickets,
                bowlAvg, bowlEconRate, bbiWickets, bbiRuns, catches, imageUrl, updatedAt)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                ON CONFLICT (id)
                DO UPDATE SET
                name = excluded.name, country = excluded.country, matches = excluded.matches,
                runs = excluded.runs, notOuts = excluded.notOuts,
                highestScore = excluded.highestScore,
                highestScoreFlag = excluded.highestScoreFlag, batAvg = excluded.batAvg,
                batStrikeRate = excluded.batStrikeRate, hundreds = excluded.hundreds,
                fifties = excluded.fifties, overs = excluded.overs,
                wickets = excluded.wickets, bowlAvg = excluded.bowlAvg,
                bowlEconRate = excluded.bowlEconRate,
                bbiWickets = excluded.bbiWickets, bbiRuns = excluded.bbiRuns,
                catches = excluded.catches, imageUrl = excluded.imageUrl,
                updatedAt = excluded.updatedAt
                """;
        Connection conn = Preconditions.checkNotNull(connect(), NULL_CONN_MESSAGE);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, player.getId());
            stmt.setString(2, player.getName());
            stmt.setString(3, player.getCountry());
            stmt.setInt(4, player.getMatches());
            stmt.setInt(5, player.getRuns());
            stmt.setInt(6, player.getNotOuts());
            stmt.setInt(7, player.getHighestScore());
            stmt.setInt(8, player.getHighestScoreFlag());
            stmt.setDouble(9, player.getBatAvg());
            stmt.setDouble(10, player.getBatStrikeRate());
            stmt.setInt(11, player.getHundreds());
            stmt.setInt(12, player.getFifties());
            stmt.setDouble(13, player.getOvers());
            stmt.setInt(14, player.getWickets());
            stmt.setDouble(15, player.getBowlAvg());
            stmt.setDouble(16, player.getBowlEconRate());
            stmt.setInt(17, player.getBbiWickets());
            stmt.setDouble(18, player.getBbiRuns());
            stmt.setInt(19, player.getCatches());
            stmt.setString(20, player.getImageUrl());
            stmt.setString(21, String.valueOf(LocalDate.now()));
            stmt.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void updateRankTable(PlayerRankObj pObj) {

        String sql = """
                INSERT INTO ranks
                (id, playerRank, updatedAt)
                VALUES (?,?,?)
                ON CONFLICT (id)
                DO UPDATE SET
                playerRank = excluded.playerRank,
                updatedAt = excluded.updatedAt
                """;
        Connection conn = Preconditions.checkNotNull(connect(), NULL_CONN_MESSAGE);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pObj.getId());
            stmt.setInt(2, pObj.getPlayerRank());
            stmt.setString(3, String.valueOf(LocalDate.now()));
            stmt.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
