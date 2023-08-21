package org.alpacax.trumpcarddb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UtilityClass
public class PlayerStatWebScraper {

    static final ConcurrentHashMap<Player, String> map = new ConcurrentHashMap<>();
    static final Set<Player> playerStats = map.keySet("PLAYER-STAT");
    private static final String WEB_HEADER = "https://www.espncricinfo.com/cricketers/";
    private static final CharMatcher ID_MATCHER = CharMatcher.inRange('0', '9');
    private static final String[] batStats = {"Mat", "Inns", "Runs", "NO", "HS", "Ave",
            "SR", "100s", "50s", "Ct"};
    private static final String[] bowlStats = {"Inns", "Balls", "Wkts", "Ave", "Econ", "BBI"};
    private static final String ODI_STRING_STRUCT = "tbody tr:contains(ODI) td";
    static Player player = new Player();

    private static @NonNull List<String> getBaseStats(@NonNull Document doc)
            throws JsonProcessingException {

        Elements elementsFlex = doc.getElementsByClass("ds-bg-raw-black/85");
        final String name = elementsFlex.select("h1").text();
        final String country = Objects.requireNonNull(
                elementsFlex.select("span").first()).text();
        Element script = doc.select("script#__NEXT_DATA__").first();
        String imageUrl = "";
        if (script != null) {
            String jsonContent = script.html();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonObject = objectMapper.readValue(
                            jsonContent, JsonNode.class).get("props")
                    .get("appPageProps").get("data").get("player")
                    .get("headshotImage").get("url");
            if (jsonObject != null) {
                imageUrl = "https://img1.hscicdn.com/image/upload/lsci" + jsonObject.asText();
            }
        }

        return new ArrayList<>(List.of(name, country, imageUrl));
    }

    private static @NonNull List<Integer> getIntegerBattingStats(
            @NonNull Elements elementBat,
            @NonNull List<Integer> batIndices) {

        int matches;
        int runs = 0;
        int notOuts = 0;
        String highestScoreWithStar;
        int highestScore = 0;
        int highestScoreFlag = 0;
        int hundreds = 0;
        int fifties = 0;
        int catches;

        matches = Integer.parseInt(elementBat.get(batIndices.get(0)).text());
        catches = Integer.parseInt(elementBat.get(batIndices.get(9)).text());
        if (!elementBat.get(batIndices.get(1)).text().contains("-")) {
            runs = Integer.parseInt(elementBat.get(batIndices.get(2)).text());
            notOuts = Integer.parseInt(elementBat.get(batIndices.get(3)).text());
            highestScoreWithStar = elementBat.get(batIndices.get(4)).text();
            if (highestScoreWithStar.endsWith("*")) {
                highestScore = Integer.parseInt(
                        highestScoreWithStar.replace("*", ""));
                highestScoreFlag = 1;
            } else {
                highestScore = Integer.parseInt(highestScoreWithStar);
            }
            hundreds = Integer.parseInt(elementBat.get(batIndices.get(7)).text());
            fifties = Integer.parseInt(elementBat.get(batIndices.get(8)).text());
        }

        return new ArrayList<>(List.of(matches, runs, notOuts, highestScore,
                highestScoreFlag, hundreds, fifties, catches));
    }

    private static @NonNull List<Double> getDoubleBattingStats(
            @NonNull Elements elementBat,
            @NonNull List<Integer> batIndices) {

        double batAvg;
        double batStrikeRate;

        if (elementBat.get(batIndices.get(1)).text().contains("-")) {
            batAvg = Double.MIN_VALUE;
            batStrikeRate = Double.MIN_VALUE;
        } else {
            if (elementBat.get(batIndices.get(5)).text().contains("-")) {
                batAvg = Double.MIN_VALUE;
            } else {
                batAvg = Double.parseDouble(elementBat.get(batIndices.get(5)).text());
            }
            if (elementBat.get(batIndices.get(6)).text().contains("-")) {
                batStrikeRate = Double.MIN_VALUE;
            } else {
                batStrikeRate = Double.parseDouble(elementBat.get(batIndices.get(6)).text());
            }
        }

        return new ArrayList<>(List.of(batAvg, batStrikeRate));
    }

    private static @NonNull List<Integer> getIntegerBowlingStats(
            @NonNull Elements elementBowl,
            @NonNull List<Integer> bowlIndices) {

        int wickets = 0;
        String bbiString;
        int bbiWickets;
        int bbiRuns;

        if (!elementBowl.get(bowlIndices.get(0)).text().contains("-")) {
            wickets = Integer.parseInt(elementBowl.get(bowlIndices.get(2)).text());
        }
        bbiString = elementBowl.get(bowlIndices.get(5)).text();
        if (bbiString.contains("-")) {
            bbiWickets = 0;
            bbiRuns = Integer.MAX_VALUE;
        } else {
            bbiWickets = Integer.parseInt(bbiString.split("/")[0]);
            bbiRuns = Integer.parseInt(bbiString.split("/")[1]);
        }

        return new ArrayList<>(List.of(wickets, bbiWickets, bbiRuns));
    }

    private static @NonNull List<Double> getDoubleBowlingStats(
            @NonNull Elements elementBowl,
            @NonNull List<Integer> bowlIndices) {

        int balls;
        double overs;
        double bowlAvg;
        double bowlEconRate;

        if (elementBowl.get(bowlIndices.get(0)).text().contains("-")) {
            overs = 0.0;
            bowlAvg = Double.MAX_VALUE;
            bowlEconRate = Double.MAX_VALUE;
        } else {
            balls = Integer.parseInt(elementBowl.get(bowlIndices.get(1)).text());
            overs = Math.floor((double) balls / 6) + 0.1 * (balls % 6);
            if (elementBowl.get(bowlIndices.get(3)).text().contains("-")) {
                bowlAvg = Double.MAX_VALUE;
            } else {
                bowlAvg = Double.parseDouble(elementBowl.get(bowlIndices.get(3)).text());
            }
            if (elementBowl.get(bowlIndices.get(4)).text().contains("-")) {
                bowlEconRate = Double.MAX_VALUE;
            } else {
                bowlEconRate = Double.parseDouble(elementBowl.get(bowlIndices.get(4)).text());
            }
        }

        return new ArrayList<>(List.of(overs, bowlAvg, bowlEconRate));
    }

    private static @NonNull List<Integer> getIndices(
            @NonNull Elements tempElements,
            String[] stats) {

        Elements headerObjs = Objects.requireNonNull(tempElements.first()).select("th");
        List<String> headers = new ArrayList<>();
        for (Element el : headerObjs) {
            headers.add(el.text());
        }
        List<Integer> indices = new ArrayList<>();
        for (String stat : stats) {
            indices.add(headers.indexOf(stat));
        }

        return indices;
    }

    private static @NonNull Elements getActualElements(@NonNull Elements tempElements) {

        Element element = Objects.requireNonNull(tempElements.first()).parent();
        assert element != null;

        return element.select(ODI_STRING_STRUCT);
    }

    public static void getPlayerStats(String pl) {

        final int id = Integer.parseInt(ID_MATCHER.retainFrom(pl));
        HttpClient httpClient = HttpClient.newHttpClient();
        ExecutorService executor = Executors.newFixedThreadPool(1);
        CompletableFuture<String> pageContentFuture =
                PlayerIdWebScraper.readWebpage(WEB_HEADER + pl, httpClient, executor);
        String pageContent = pageContentFuture.join();
        executor.shutdown();
        Document doc = Jsoup.parse(pageContent);
        List<String> baseStats;
        try {
            baseStats = getBaseStats(doc);
        } catch (JsonProcessingException e) {
            throw new ScriptReadingException(e);
        }

        Elements elementsTempBat = doc.select("table thead:contains(HS)");
        List<Integer> batIndices = getIndices(elementsTempBat, batStats);
        Elements elementBat = getActualElements(elementsTempBat);
        List<Integer> integerBattingStats = getIntegerBattingStats(elementBat, batIndices);
        List<Double> doubleBattingStats = getDoubleBattingStats(elementBat, batIndices);

        Elements elementsTempBowl = doc.select("table thead:contains(BBI)");
        List<Integer> bowlIndices = getIndices(elementsTempBowl, bowlStats);
        Elements elementBowl = getActualElements(elementsTempBowl);
        List<Integer> integerBowlingStats = getIntegerBowlingStats(elementBowl, bowlIndices);
        List<Double> doubleBowlingStats = getDoubleBowlingStats(elementBowl, bowlIndices);

        player = new Player(id, baseStats.get(0), baseStats.get(1),
                integerBattingStats.get(0), integerBattingStats.get(1),
                integerBattingStats.get(2), integerBattingStats.get(3),
                integerBattingStats.get(4), doubleBattingStats.get(0),
                doubleBattingStats.get(1), integerBattingStats.get(5),
                integerBattingStats.get(6), doubleBowlingStats.get(0),
                integerBowlingStats.get(0), doubleBowlingStats.get(1),
                doubleBowlingStats.get(2), integerBowlingStats.get(1),
                integerBowlingStats.get(2), integerBattingStats.get(7),
                baseStats.get(2));
        playerStats.add(player);
    }

    public static class ScriptReadingException extends RuntimeException {
        public ScriptReadingException(Throwable cause) {
            super(cause);
        }
    }
}
