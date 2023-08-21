package org.alpacax.trumpcardgame;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSession {

    private String token;
    private int player2;
    private Deck gameDeck;
    private FlagSet intFlags;
    private int isLive;
}