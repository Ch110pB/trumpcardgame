package org.alpacax.trumpcardgame;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GamePostResponse {

    private CardFaceView aiPlayer;
    private String aiCountryShortName;
}
