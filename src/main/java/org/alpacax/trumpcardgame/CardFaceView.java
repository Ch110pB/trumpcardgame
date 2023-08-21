package org.alpacax.trumpcardgame;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardFaceView {

    private String name;
    private String country;
    private String matches;
    private String runs;
    private String notOuts;
    private String highestScoreStar;
    private String batAvg;
    private String batStrikeRate;
    private String hundreds;
    private String fifties;
    private String overs;
    private String wickets;
    private String bowlAvg;
    private String bowlEconRate;
    private String bowlBbi;
    private String catches;
    private String imageUrl;
}