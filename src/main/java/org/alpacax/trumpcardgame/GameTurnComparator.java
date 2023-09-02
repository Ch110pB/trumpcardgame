package org.alpacax.trumpcardgame;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GameTurnComparator {

    public static Integer getInteger(boolean b1, boolean b2) {

        if (b1) {
            return 0;
        } else if (b2) {
            return 1;
        } else {
            return -1;
        }
    }

    public static Integer getBigger(@NonNull Number stat1,
                                    @NonNull Number stat2) {

        return getInteger(stat1.doubleValue() > stat2.doubleValue(),
                stat1.doubleValue() < stat2.doubleValue());
    }

    public static Integer getSmaller(Double stat1, Double stat2) {

        return getInteger(stat1 < stat2, stat1 > stat2);
    }

    public static Integer getHigherHighest(int stat1, int stat1Star,
                                           int stat2, int stat2Star) {

        if (stat1 > stat2) {
            return 0;
        } else if (stat1 < stat2) {
            return 1;
        } else return getInteger(stat1Star > stat2Star, stat1Star < stat2Star);
    }

    public static Integer getBetterBbi(int stat1W, int stat1R,
                                       int stat2W, int stat2R) {

        if (stat1W > stat2W) {
            return 0;
        } else if (stat1W < stat2W) {
            return 1;
        } else return getInteger(stat1R < stat2R, stat1R > stat2R);
    }

    public static int getTurnWinner(Deck gameDecks, @NonNull Stat selectedStat)
            throws IllegalArgumentException {
        switch (selectedStat) {
            case MATCHES -> {
                return getBigger(gameDecks.getSideStack().get(0).getMatches(),
                        gameDecks.getSideStack().get(1).getMatches());
            }
            case RUNS -> {
                return getBigger(gameDecks.getSideStack().get(0).getRuns(),
                        gameDecks.getSideStack().get(1).getRuns());
            }
            case NOT_OUTS -> {
                return getBigger(gameDecks.getSideStack().get(0).getNotOuts(),
                        gameDecks.getSideStack().get(1).getNotOuts());
            }
            case HIGHEST_SCORE -> {
                return getHigherHighest(gameDecks.getSideStack().get(0).getHighestScore(),
                        gameDecks.getSideStack().get(0).getHighestScoreFlag(),
                        gameDecks.getSideStack().get(1).getHighestScore(),
                        gameDecks.getSideStack().get(1).getHighestScoreFlag());
            }
            case BAT_AVG -> {
                return getBigger(gameDecks.getSideStack().get(0).getBatAvg(),
                        gameDecks.getSideStack().get(1).getBatAvg());
            }
            case BAT_STRIKE_RATE -> {
                return getBigger(gameDecks.getSideStack().get(0).getBatStrikeRate(),
                        gameDecks.getSideStack().get(1).getBatStrikeRate());
            }
            case HUNDREDS -> {
                return getBigger(gameDecks.getSideStack().get(0).getHundreds(),
                        gameDecks.getSideStack().get(1).getHundreds());
            }
            case FIFTIES -> {
                return getBigger(gameDecks.getSideStack().get(0).getFifties(),
                        gameDecks.getSideStack().get(1).getFifties());
            }
            case OVERS -> {
                return getBigger(gameDecks.getSideStack().get(0).getOvers(),
                        gameDecks.getSideStack().get(1).getOvers());
            }
            case WICKETS -> {
                return getBigger(gameDecks.getSideStack().get(0).getWickets(),
                        gameDecks.getSideStack().get(1).getWickets());
            }
            case BOWL_AVG -> {
                return getSmaller(gameDecks.getSideStack().get(0).getBowlAvg(),
                        gameDecks.getSideStack().get(1).getBowlAvg());
            }
            case BOWL_ECON_RATE -> {
                return getSmaller(gameDecks.getSideStack().get(0).getBowlEconRate(),
                        gameDecks.getSideStack().get(1).getBowlEconRate());
            }
            case BOWL_BBI -> {
                return getBetterBbi(gameDecks.getSideStack().get(0).getBbiWickets(),
                        gameDecks.getSideStack().get(0).getBbiRuns(),
                        gameDecks.getSideStack().get(1).getBbiWickets(),
                        gameDecks.getSideStack().get(1).getBbiRuns());
            }
            case CATCHES -> {
                return getBigger(gameDecks.getSideStack().get(0).getCatches(),
                        gameDecks.getSideStack().get(1).getCatches());
            }
            default -> throw new IllegalArgumentException("Invalid stat selected");
        }
    }

    public enum Stat {

        MATCHES,
        RUNS,
        NOT_OUTS,
        HIGHEST_SCORE,
        BAT_AVG,
        BAT_STRIKE_RATE,
        HUNDREDS,
        FIFTIES,
        OVERS,
        WICKETS,
        BOWL_AVG,
        BOWL_ECON_RATE,
        BOWL_BBI,
        CATCHES
    }
}
