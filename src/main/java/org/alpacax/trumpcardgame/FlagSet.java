package org.alpacax.trumpcardgame;

import lombok.Data;

@Data
public class FlagSet {

    private int turnOwner = 0;
    private int clashUser = 1;
    private int clashAi = 1;
}