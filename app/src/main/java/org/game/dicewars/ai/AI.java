package org.game.dicewars.ai;

import org.game.dicewars.Game;

public interface AI {
    /**
     * return 0 when doesn't want to make a move
     */
    int ai_default(Game game);
}
