package org.alpacax.trumpcardgame;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.alpacax.trumpcarddb.Player;

import java.util.LinkedList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deck {

    private LinkedList<Player> userDeck;
    private LinkedList<Player> aiDeck;
    private LinkedList<Player> sideStack;
}