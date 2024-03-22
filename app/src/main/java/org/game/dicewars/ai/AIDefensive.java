package org.game.dicewars.ai;

import org.game.dicewars.Game;

/**
 * Defense AI
 */
public class AIDefensive implements AI {
    public static class AreaInfo {
        int friendly_neighbors;
        int unfriendly_neighbors;
        int highest_friendly_neighbor_dice;
        int highest_unfriendly_neighbor_dice;
        int second_highest_unfriendly_neighbor_dice;
        int num_neighbors;
    }

    @Override
    public int ai_default(Game game) {
        // compute the area info once per move
        AreaInfo[] area_info = new AreaInfo[game.AREA_MAX];
        for (int i = 0; i < area_info.length; i++) {
            area_info[i] = area_get_info(game, i);
        }

        int pn = game.getCurrentPlayer();

        game.attackAreaFrom = -1;
        game.attackAreaTo = -1;

        // for all potential defenders
        for (int i = 0; i < game.AREA_MAX; i++) {
            if (game.allAreaData[i].playerIx == pn)
                continue;

            // for all potential attackers of game defender
            for (int j = 0; j < game.AREA_MAX; j++) {
                if (game.allAreaData[j].playerIx != pn)
                    continue;
                if (!game.allAreaData[i].neighbors[j])
                    continue;

                // is the attacker actually in a position to attack?
                if (game.allAreaData[i].dices >= game.allAreaData[j].dices && game.allAreaData[j].dices != 8)
                    continue;
                // does winning invite a strong counter attack?
                if (area_info[i].highest_friendly_neighbor_dice > game.allAreaData[j].dices)
                    continue;
                // does the attacker have something to defend from (and I have a meaningful
                // connected area)?
                if (game.playersData[pn].areaTotalCount > 4 && area_info[j].second_highest_unfriendly_neighbor_dice > 2
                        && game.playersData[pn].stock == 0)
                    continue;

                // check against previous attacker
                if (game.attackAreaFrom == -1) {
                    // no previous attacker, assign them
                    game.attackAreaFrom = j;
                    game.attackAreaTo = i;
                } else {
                    if (area_info[game.attackAreaFrom].unfriendly_neighbors == 1) { // if it's the only way out
                        if (area_info[j].unfriendly_neighbors == 1) { // ...for both of them
                            if (game.allAreaData[j].dices < game.allAreaData[game.attackAreaFrom].dices)
                                continue; // prefer larger dice
                            else if (game.allAreaData[j].dices == game.allAreaData[game.attackAreaFrom].dices)
                                // then prefer the less connected region
                                if (area_info[j].num_neighbors < area_info[game.attackAreaFrom].num_neighbors) {
                                    continue;
                                }

                        } else
                            continue; // let the other one out first
                    }
                    game.attackAreaFrom = j;
                    game.attackAreaTo = i;
                }

            }
        }

        // only return 0 if I don't want to make a move
        if (game.attackAreaFrom == -1) {
            return 0;
        }
        return 1;
    }

    // function to analyze each area
    AreaInfo area_get_info(Game game, int area_id) {
        int friendly_neighbors = 0;
        int unfriendly_neighbors = 0;
        int highest_friendly_neighbor_dice = 0;
        int highest_unfriendly_neighbor_dice = 0;
        int second_highest_unfriendly_neighbor_dice = 0;
        int num_neighbors = 0;

        for (int i = 0; i < game.AREA_MAX; i++) {
            if (i == area_id) {
                continue;
            }

            // find adjacent regions
            if (!game.allAreaData[area_id].neighbors[i]) {
                continue;
            }

            int num_dice = game.allAreaData[i].dices;

            if (game.allAreaData[area_id].playerIx == game.allAreaData[i].playerIx) {
                friendly_neighbors += 1;

                if (highest_friendly_neighbor_dice < num_dice) {
                    highest_friendly_neighbor_dice = num_dice;
                }
            } else {
                unfriendly_neighbors += 1;

                if (highest_unfriendly_neighbor_dice < num_dice) {
                    second_highest_unfriendly_neighbor_dice = highest_unfriendly_neighbor_dice;
                    highest_unfriendly_neighbor_dice = num_dice;
                } else if (second_highest_unfriendly_neighbor_dice < num_dice) {
                    second_highest_unfriendly_neighbor_dice = num_dice;
                }
            }
        }

        num_neighbors = friendly_neighbors + unfriendly_neighbors;

        AreaInfo result = new AreaInfo();
        result.friendly_neighbors = friendly_neighbors;
        result.unfriendly_neighbors = unfriendly_neighbors;
        result.highest_friendly_neighbor_dice = highest_friendly_neighbor_dice;
        result.highest_unfriendly_neighbor_dice = highest_unfriendly_neighbor_dice;
        result.second_highest_unfriendly_neighbor_dice = second_highest_unfriendly_neighbor_dice;
        result.num_neighbors = num_neighbors;
        return result;
    }
}
