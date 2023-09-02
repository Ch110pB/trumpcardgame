package org.alpacax.trumpcardgame;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.alpacax.trumpcarddb.Player;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.alpacax.trumpcarddb.CrudManager.connect;

@UtilityClass
public class GameHelper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String JDBC = "jdbc:sqlite:";
    private static final String DB_LOC = "src/main/resources/players.db";

    public static @NonNull @Unmodifiable Deck getDecks()
            throws NoSuchAlgorithmException, SQLException {

        List<LinkedList<Player>> gameCardsTemp = CardManager.getAllDecks();
        LinkedList<Player> player1Cards = gameCardsTemp.get(0);
        LinkedList<Player> player2Cards = gameCardsTemp.get(1);
        LinkedList<Player> sideStack = new LinkedList<>();

        return new Deck(player1Cards, player2Cards, sideStack);
    }

    public static @Unmodifiable @NonNull Deck getHands(@NonNull Deck gameDecks) {

        LinkedList<Player> userDeck = gameDecks.getUserDeck();
        LinkedList<Player> aiDeck = gameDecks.getAiDeck();
        LinkedList<Player> sideStack = gameDecks.getSideStack();
        Player player1CurrentCard = userDeck.poll();
        Player player2CurrentCard = aiDeck.poll();
        assert player1CurrentCard != null;
        assert player2CurrentCard != null;
        if (sideStack.isEmpty()) {
            sideStack.addAll(List.of(player1CurrentCard, player2CurrentCard));
        } else {
            sideStack.addAll(0, List.of(player1CurrentCard, player2CurrentCard));
        }

        return new Deck(userDeck, aiDeck, sideStack);
    }

    private static String getHyphenHelper(@NonNull Number value, Number boundaryNum) {

        if (value.equals(boundaryNum)) {
            return "-";
        } else if (value instanceof Integer) {
            return String.valueOf(value);
        } else {
            return String.valueOf(
                    BigDecimal.valueOf((double) value).setScale(
                            2, RoundingMode.HALF_UP));
        }
    }

    public static @NonNull CardFaceView getCardFaceView(@NonNull Player player) {

        CardFaceView cardView = new CardFaceView();
        cardView.setName(player.getName());
        cardView.setCountry(player.getCountry());
        cardView.setMatches(String.valueOf(player.getMatches()));
        cardView.setRuns(getHyphenHelper(player.getRuns(), 0));
        cardView.setNotOuts(getHyphenHelper(player.getNotOuts(), 0));
        cardView.setHighestScoreStar(player.getHighestScoreFlag() == 0 ?
                String.valueOf(player.getHighestScore()) :
                player.getHighestScore() + "*");
        cardView.setBatAvg(getHyphenHelper(player.getBatAvg(), Double.MIN_VALUE));
        cardView.setBatStrikeRate(getHyphenHelper(player.getBatStrikeRate(), Double.MIN_VALUE));
        cardView.setHundreds(getHyphenHelper(player.getHundreds(), 0));
        cardView.setFifties(getHyphenHelper(player.getFifties(), 0));
        String overTemp = getHyphenHelper(player.getOvers(), 0.0);
        if (!Objects.equals(overTemp, "-")) {
            cardView.setOvers(StringUtils.chop(overTemp));
        } else {
            cardView.setOvers(overTemp);
        }
        cardView.setWickets(getHyphenHelper(player.getWickets(), 0));
        cardView.setBowlAvg(getHyphenHelper(player.getBowlAvg(), Double.MAX_VALUE));
        cardView.setBowlEconRate(getHyphenHelper(player.getBowlEconRate(), Double.MAX_VALUE));
        cardView.setBowlBbi(player.getBbiWickets() == 0 ?
                getHyphenHelper(player.getBbiRuns(), Integer.MAX_VALUE) :
                player.getBbiWickets() + "/" + player.getBbiRuns());
        cardView.setCatches(getHyphenHelper(player.getCatches(), 0));
        cardView.setImageUrl(player.getImageUrl());

        return cardView;
    }

    public static void createSessionTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS sessions (
                token TEXT PRIMARY KEY,
                player2 INTEGER,
                gameDeck TEXT,
                intFlags TEXT,
                isLive INTEGER
                );
                """;
        try (Connection conn = DriverManager.getConnection(JDBC + DB_LOC);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Sessions table has been created.");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void dropSessionTable() {

        String sql = """
                DROP TABLE IF EXISTS sessions;
                """;
        try (Connection conn = DriverManager.getConnection(JDBC + DB_LOC);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Sessions table has been dropped.");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void updateSessionTable(GameSession sess) {

        String sql = """
                INSERT INTO sessions
                (token, player2, gameDeck, intFlags, isLive)
                VALUES (?,?,?,?,?)
                ON CONFLICT (token)
                DO UPDATE SET
                token = excluded.token,
                player2 = excluded.player2,
                gameDeck = excluded.gameDeck,
                intFlags = excluded.intFlags,
                isLive = excluded.isLive;
                """;
        Connection conn = Preconditions.checkNotNull(connect(),
                "The connection is null.");
        ObjectMapper mapper = new ObjectMapper();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sess.getToken());
            stmt.setInt(2, sess.getPlayer2());
            stmt.setString(3, mapper.writeValueAsString(sess.getGameDeck()));
            stmt.setString(4, mapper.writeValueAsString(sess.getIntFlags()));
            stmt.setInt(5, sess.getIsLive());
            stmt.executeUpdate();
        } catch (SQLException | NullPointerException | JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static @NonNull String createToken() {

        final char[] symbols = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789".toCharArray();
        final char[] buf = new char[6];
        SecureRandom random = new SecureRandom();
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];

        return new String(buf);
    }

    public static boolean isTokenUnique(String token) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM sessions
                WHERE token = ?
                AND isLive <> 1;
                """;
        try (Connection conn = DriverManager.getConnection(JDBC + DB_LOC);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();

            return resultSet.getInt(1) == 0;
        }
    }

    public static boolean isSessionFree(String token) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM sessions
                WHERE token = ?
                AND player2 = 0
                AND isLive = 1;
                """;
        try (Connection conn = DriverManager.getConnection(JDBC + DB_LOC);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();

            return resultSet.getInt(1) == 1;
        }
    }

    public static String getSession(String token) throws SQLException {

        String sql = """
                SELECT gameDeck
                FROM sessions
                WHERE token = ?
                """;
        try (Connection conn = DriverManager.getConnection(JDBC + DB_LOC);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();

            return resultSet.getString(1);
        }
    }

    public static void updateConnectedSession(String token) throws SQLException {

        String sql = """
                UPDATE sessions
                SET player2 = 1
                WHERE token = ?
                """;
        try (Connection conn = DriverManager.getConnection(JDBC + DB_LOC);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        }
    }

    @Contract("_ -> new")
    public static @NonNull Deck flipDeck(@NonNull Deck deck) {

        Deck flippedDeck = new Deck();
        flippedDeck.setUserDeck(deck.getAiDeck());
        flippedDeck.setAiDeck(deck.getUserDeck());
        flippedDeck.setSideStack(deck.getSideStack());

        return flippedDeck;
    }
}
