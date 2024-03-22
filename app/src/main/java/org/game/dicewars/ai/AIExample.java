package org.game.dicewars.ai;

import org.game.dicewars.AreaData;
import org.game.dicewars.Game;

/**
 * Random
 */
public class AIExample implements AI {
    @Override
    public int ai_default(Game game) {
        // the index of the current player (us),
        // so we can tell which regions are friendly
        int current_player = game.getCurrentPlayer();

        // Make a list of valid moves (attacker / defender pairs)
        // Once we have found all the valid moves, we pick one at random
        int[][] list_moves = new int[game.AREA_MAX * game.AREA_MAX][];
        int number_of_moves = 0;

        // for every area (region with dice) in the game
        for (int i = 1; i < game.AREA_MAX; i++) {
            AreaData attacking_area = game.allAreaData[i];

            if (attacking_area.size == 0) {
                continue; // if the area has a size of zero, ignore it
            }
            if (attacking_area.playerIx != current_player) {
                continue; // if the area isn't ours, we can't attack with it
            }
            if (attacking_area.dices <= 1) {
                continue; // if our area has one die, we can't attack with it
            }

            // for every area on the map (this time, looking for a region to attack)
            for (int j = 1; j < game.AREA_MAX; j++) {
                AreaData defending_area = game.allAreaData[j];

                if (defending_area.size == 0) {
                    continue; // if the area has a size of zero, ignore it
                }
                if (defending_area.playerIx == current_player) {
                    continue; // make sure the area isn't ours (because we want to attack it)
                }
                if (!attacking_area.neighbors[j]) {
                    continue; // make sure the area to attack is next to the area we're attacking with
                }

                // if the defending area has more dice than us, it's (probably) a bad move so
                // we'll ignore it
                if (defending_area.dices >= game.allAreaData[i].dices) {
                    continue;
                }

                // if we made it this far, this pair of attacker and defender are valid so we
                // add them to the list
                list_moves[number_of_moves] = new int[]{
                        // i is the index of the attacking area
                        i,
                        // j is the index of the defending area
                        j};
                number_of_moves++;
            }
        }

        // if there are no moves to pick from, return 0 to end our turn
        if (number_of_moves == 0) {
            return 0;
        }

        // randomly pick a move
        int n = (int) (Math.floor(Math.random() * number_of_moves));
        int[] move = list_moves[n];

        // get the attacking and defending area's indices from the list
        int attacking_area_index = move[0];
        int defending_area_index = move[1];

        game.attackAreaFrom = attacking_area_index; // tell the game the index of the area we're attacking with
        game.attackAreaTo = defending_area_index; // tell the game the index of the area that we are attacking
        // DO NOT RETURN ANYTHING to finalize the attack
        // This function will get called repeatedly until we return 0
        return 1;
    }

}
