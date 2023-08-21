package org.alpacax.trumpcarddb;

import com.google.common.base.CharMatcher;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.NonNull;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


public class DatabaseOrchestrator {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final CharMatcher ID_MATCHER = CharMatcher.inRange('0', '9');

    private static @NonNull Observable<String> fetchUpdateIdObservable(String cn) {

        return Observable.create(emitter -> {
            try {
                PlayerIdWebScraper.getPlayerIds(cn);
                emitter.onNext(cn);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    private static @NonNull Observable<String> fetchUpdateStatObservable(String pl) {

        return Observable.create(emitter -> {
            try {
                PlayerStatWebScraper.getPlayerStats(pl);
                emitter.onNext(pl);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    private static void fetchUpdateIdTable() {

        String[] countryIdsArr = PlayerIdWebScraper.countryIds.toArray(new String[0]);
        Observable.fromArray(countryIdsArr).
                flatMap(cn -> fetchUpdateIdObservable(cn)
                        .retryWhen(errors -> errors.zipWith(Observable.range(1, 3),
                                (throwable, retryCount) -> {
                                    if (retryCount < 3) {
                                        String errorMessage = "Error processing item: "
                                                + cn + ". Retry #" + retryCount;
                                        LOGGER.error(errorMessage);
                                        return retryCount;
                                    } else {
                                        return throwable;
                                    }
                                }).flatMap(retryCountOrError -> {
                            if (retryCountOrError instanceof Throwable throwableError) {
                                return Observable.error(throwableError);
                            } else {
                                return Observable
                                        .just(cn).delay(400, TimeUnit.MILLISECONDS);
                            }
                        })).subscribeOn(Schedulers.io())).blockingSubscribe();
        String infoMessage = "Player IDs size: " +
                PlayerIdWebScraper.allPlayerList.size() + ".";
        LOGGER.info(infoMessage);

        Set<PlayerId> playerParsedList = new HashSet<>();
        for (String pId : PlayerIdWebScraper.allPlayerList) {
            int id = Integer.parseInt(ID_MATCHER.retainFrom(pId));
            PlayerId playerId = new PlayerId(id, pId);
            playerParsedList.add(playerId);
        }
        for (PlayerId playerId : playerParsedList) {
            CrudManager.updateIdTable(playerId);
        }
        LOGGER.info("ID table updated.");
    }

    private static void fetchUpdateStatTable() {

        String[] playerIdsArr = PlayerIdWebScraper.allPlayerList.toArray(new String[0]);
        Observable.fromArray(playerIdsArr)
                .flatMap(pl -> fetchUpdateStatObservable(pl)
                        .retryWhen(errors -> errors.zipWith(Observable.range(1, 4),
                                (throwable, retryCount) -> {
                                    if (retryCount < 4) {
                                        String errorMessage = "Error processing item: "
                                                + pl + ". Retry #" + retryCount;
                                        LOGGER.error(errorMessage);
                                        return retryCount;
                                    } else {
                                        return throwable;
                                    }
                                }).flatMap(retryCountOrError -> {
                            if (retryCountOrError instanceof Throwable throwableError) {
                                return Observable.error(throwableError);
                            } else {
                                return Observable.just(pl).delay(600, TimeUnit.MILLISECONDS);
                            }
                        })).subscribeOn(Schedulers.io())).blockingSubscribe();
        String infoMessagePlayer = "Player Stats size: " +
                PlayerStatWebScraper.playerStats.size() + ".";
        LOGGER.info(infoMessagePlayer);

        for (Player player : PlayerStatWebScraper.playerStats) {
            CrudManager.updateStatTable(player);
        }
        LOGGER.info("Stat table updated.");
    }

    @FunctionalInterface
    private interface StatExtractor {
        Number extract(Player player);
    }

    private static double[] rankAndConvert(@NonNull StatExtractor extractor) {
        NaturalRanking ranking = new NaturalRanking(TiesStrategy.MAXIMUM);

        return ranking.rank(PlayerStatWebScraper.playerStats
                .stream()
                .map(extractor::extract)
                .mapToDouble(Number::doubleValue).toArray());
    }

    public static double[] combineScores(@NonNull List<double[]> dataArrays) {
        int length = dataArrays.get(0).length;

        return IntStream.range(0, length).mapToDouble(i -> {
            double sumOfReciprocals = 0.0;
            List<Double> statRanks = new ArrayList<>();
            for (double[] dataArray : dataArrays) {
                statRanks.add(dataArray[i]);
            }
            statRanks.sort(Comparator.reverseOrder());
            List<Double> topHalfRanks = statRanks.subList(0, 5);
            for (Double el : topHalfRanks) {
                sumOfReciprocals += 1.0 / el;
            }
            return 5 / sumOfReciprocals;
        }).toArray();
    }

    private static @NonNull List<Integer> getPlayerRanks() {

        NaturalRanking ranking = new NaturalRanking(TiesStrategy.SEQUENTIAL);

        List<double[]> dataArrays = List.of(rankAndConvert(Player::getMatches),
                rankAndConvert(Player::getRuns), rankAndConvert(Player::getHighestScore),
                rankAndConvert(Player::getNotOuts), rankAndConvert(Player::getBatAvg),
                rankAndConvert(Player::getBatStrikeRate), rankAndConvert(Player::getHundreds),
                rankAndConvert(Player::getFifties), rankAndConvert(Player::getCatches),
                rankAndConvert(Player::getOvers), rankAndConvert(Player::getWickets),
                rankAndConvert(Player::getBowlAvg), rankAndConvert(Player::getBowlEconRate),
                rankAndConvert(Player::getBbiWickets));

        double[] combinedScores = combineScores(dataArrays);
        List<Integer> playerRanks = new ArrayList<>();
        for (double value : ranking.rank(combinedScores)) {
            playerRanks.add(combinedScores.length + 1 - (int) value);
        }

        return playerRanks;
    }

    private static void fetchUpdateRankTable() {

        List<Integer> playerIds = PlayerStatWebScraper.playerStats
                .stream()
                .map(Player::getId).toList();
        List<Integer> playerRanks = getPlayerRanks();

        Set<PlayerRankObj> playerRankList = new HashSet<>();
        for (int i = 0; i < playerIds.size(); i++) {
            int id = playerIds.get(i);
            int pRank = playerRanks.get(i);
            PlayerRankObj playerRankObj = new PlayerRankObj(id, pRank);
            playerRankList.add(playerRankObj);
        }
        for (PlayerRankObj playerId : playerRankList) {
            CrudManager.updateRankTable(playerId);
        }
        LOGGER.info("Rank table updated.");
    }

    public static void main(String[] args) {

        CrudManager.createNewDatabase();
        CrudManager.createIdTable();
        CrudManager.createStatTable();
        CrudManager.createRankTable();
        fetchUpdateIdTable();
        fetchUpdateStatTable();
        fetchUpdateRankTable();
    }
}
