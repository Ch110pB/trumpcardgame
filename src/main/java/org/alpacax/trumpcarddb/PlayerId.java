package org.alpacax.trumpcarddb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class PlayerId {

    private int id;
    @NonNull
    private String playerPageId;
}
