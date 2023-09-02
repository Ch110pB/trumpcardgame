package org.alpacax.trumpcardgame;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GamePostRequest {

    private String gameToken;
    private String statSelected;
}
