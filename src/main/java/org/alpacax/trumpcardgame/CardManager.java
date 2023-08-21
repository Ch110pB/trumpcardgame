package org.alpacax.trumpcardgame;

import com.google.common.collect.Iterables;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.alpacax.trumpcarddb.Player;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.alpacax.trumpcarddb.CrudManager.connect;

@UtilityClass
public class CardManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String JDBC = "jdbc:sqlite:";
    private static final String DB_LOC = "src/main/resources/players.db";

    public static void getAllCardWeights(List<Integer> nameList,
                                         List<Integer> ranksList,
                                         List<Double> weightList) {

        String sql = """ 
                SELECT stats.id, ranks.playerRank
                FROM stats
                   INNER JOIN (
                       SELECT id, MAX(updatedAt) updatedAt
                       FROM stats
                       GROUP BY id
                   ) max_ ON stats.id = max_.id
                   INNER JOIN (
                        SELECT id, playerRank
                        FROM ranks
                   ) ranks ON stats.id = ranks.id
                WHERE stats.matches > 10
                AND stats.imageUrl IS NOT NULL AND stats.imageUrl != ''
                """;
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                nameList.add(rs.getInt("id"));
                ranksList.add(rs.getInt("playerRank"));
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        for (Integer rank : ranksList) {
            if (rank <= 100) {
                weightList.add(50.14);
            } else if (rank <= 250) {
                weightList.add(33.43);
            } else if (rank <= 500) {
                weightList.add(10.03);
            } else {
                weightList.add(6.40);
            }
        }
    }

    public static @NonNull List<Integer> getCardStash(@NonNull List<Integer> playerIds,
                                                      int numCards,
                                                      List<Double> weightList) {

        SecureRandom random = new SecureRandom();
        RandomGenerator randomGen = RandomGeneratorFactory.createRandomGenerator(random);
        List<Integer> matchCards = new ArrayList<>();
        List<Pair<Integer, Double>> valueWeightPairs = new ArrayList<>();
        for (int i = 0; i < playerIds.size(); i++) {
            valueWeightPairs.add(new Pair<>(playerIds.get(i), weightList.get(i)));
        }
        EnumeratedDistribution<Integer> distribution =
                new EnumeratedDistribution<>(randomGen, valueWeightPairs);
        for (int i = 0; i < numCards; i++) {
            Integer index = distribution.sample();
            matchCards.add(index);
            distribution = removeSelected(distribution, index);
        }

        return matchCards;
    }

    private static @NonNull EnumeratedDistribution<Integer> removeSelected(
            @NonNull EnumeratedDistribution<Integer> distribution,
            Integer selectedIndex) {

        List<Pair<Integer, Double>> updatedWeights = new ArrayList<>();
        for (Pair<Integer, Double> entry : distribution.getPmf()) {
            if (!Objects.equals(entry.getFirst(), selectedIndex)) {
                updatedWeights.add(entry);
            }
        }

        return new EnumeratedDistribution<>(updatedWeights);
    }

    public static Player getCard(int playerId) throws SQLException {

        QueryRunner run = new QueryRunner();
        ResultSetHandler<Player> playerHandler = new BeanHandler<>(Player.class);
        Connection conn = DriverManager.getConnection(JDBC + DB_LOC);
        Player pl;
        try {
            String sql = """
                    SELECT id, name, country, matches, runs, notOuts,
                    highestScore, highestScoreFlag, batAvg, batStrikeRate,
                    hundreds, fifties, overs, wickets, bowlAvg, bowlEconRate,
                    bbiWickets, bbiRuns, catches, imageUrl
                    FROM stats
                    WHERE id=?
                    """;
            pl = run.query(conn, sql, playerHandler, playerId);
        } finally {
            DbUtils.close(conn);
        }

        return pl;
    }

    public static @NonNull @Unmodifiable List<LinkedList<Player>> getAllDecks()
            throws NoSuchAlgorithmException, SQLException {

        List<Integer> idList = new ArrayList<>();
        List<Integer> ranksList = new ArrayList<>();
        List<Double> weightList = new ArrayList<>();
        getAllCardWeights(idList, ranksList, weightList);
        int individualStackSize = 2;
        List<Integer> matchCards = getCardStash(idList,
                individualStackSize * 2,
                weightList);
        Iterator<List<Integer>> itr = Iterables.partition(matchCards,
                (matchCards.size()) / 2).iterator();
        List<Integer> player1CardIds = itr.next();
        List<Integer> player2CardIds = itr.next();
        LinkedList<Player> player1Cards = new LinkedList<>();
        LinkedList<Player> player2Cards = new LinkedList<>();
        for (Integer player1CardId : player1CardIds) {
            player1Cards.add(getCard(player1CardId));
        }
        for (Integer player2CardId : player2CardIds) {
            player2Cards.add(getCard(player2CardId));
        }
        SecureRandom random = SecureRandom.getInstanceStrong();
        Collections.shuffle(player1Cards, random);
        Collections.shuffle(player2Cards, random);

        return List.of(player1Cards, player2Cards);
    }

    public static CountryShortName getShortName(@NonNull String country) {

        return switch (country) {
            case "Australia" -> CountryShortName.AUS;
            case "India" -> CountryShortName.IND;
            case "Pakistan" -> CountryShortName.PAK;
            case "New Zealand" -> CountryShortName.NZL;
            case "England" -> CountryShortName.ENG;
            case "South Africa" -> CountryShortName.RSA;
            case "Sri Lanka" -> CountryShortName.SRL;
            case "West Indies" -> CountryShortName.WIN;
            case "Bangladesh" -> CountryShortName.BAN;
            case "Zimbabwe" -> CountryShortName.ZIM;
            case "Afghanistan" -> CountryShortName.AFG;
            case "Netherlands" -> CountryShortName.NED;
            case "Kenya" -> CountryShortName.KEN;
            case "Ireland" -> CountryShortName.IRE;
            case "Scotland" -> CountryShortName.SCO;
            case "Oman" -> CountryShortName.OMA;
            case "Namibia" -> CountryShortName.NAM;
            case "U.S.A." -> CountryShortName.USA;
            case "Nepal" -> CountryShortName.NEP;
            case "Canada" -> CountryShortName.CAN;
            case "Hong Kong" -> CountryShortName.HKG;
            case "U.A.E." -> CountryShortName.UAE;
            case "Bermuda" -> CountryShortName.BER;
            case "P.N.G." -> CountryShortName.PNG;
            case "Jersey" -> CountryShortName.JER;
            default -> throw new IllegalArgumentException("Invalid country");
        };
    }

    public enum CountryShortName {

        AUS,
        IND,
        PAK,
        ENG,
        NZL,
        RSA,
        SRL,
        WIN,
        BAN,
        ZIM,
        AFG,
        NED,
        KEN,
        IRE,
        SCO,
        OMA,
        NAM,
        USA,
        NEP,
        CAN,
        HKG,
        UAE,
        BER,
        PNG,
        JER
    }
}
