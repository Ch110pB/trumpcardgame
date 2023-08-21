package org.alpacax.trumpcarddb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    private int id;
    @NonNull private String name;
    @NonNull private String country;
    private int matches;
    private int runs;
    private int notOuts;
    private int highestScore;
    private int highestScoreFlag;
    private double batAvg;
    private double batStrikeRate;
    private int hundreds;
    private int fifties;
    private double overs;
    private int wickets;
    private double bowlAvg;
    private double bowlEconRate;
    private int bbiWickets;
    private int bbiRuns;
    private int catches;
    private String imageUrl;
}
