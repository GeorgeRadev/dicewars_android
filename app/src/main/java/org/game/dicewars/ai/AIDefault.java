package org.game.dicewars.ai;

import org.game.dicewars.Game;

/**
 * (COM thinking - the original AI from gamedesign.jp)
 */
public class AIDefault implements AI {
    @Override
    public int ai_default(Game game) {
        int i, j;
        // Number of areas, total dice check
        for (i = 0; i < 8; i++) {
            game.playersData[i].areaCount = 0;
            game.playersData[i].diceCount = 0;
        }
        int sum = 0;
        for (i = 1; i < game.AREA_MAX; i++) {
            if (game.allAreaData[i].size == 0) {
                continue;
            }
            int arm = game.allAreaData[i].playerIx;
            game.playersData[arm].areaCount++;
            game.playersData[arm].diceCount += game.allAreaData[i].dices;
            sum += game.allAreaData[i].dices;
        }
        // Dice ranking
        for (i = 0; i < 8; i++)
            game.playersData[i].dice_jun = i;
        for (i = 0; i < 8 - 1; i++) {
            for (j = i + 1; j < 8; j++) {
                if (game.playersData[i].diceCount < game.playersData[j].diceCount) {
                    int tmp = game.playersData[i].dice_jun;
                    game.playersData[i].dice_jun = game.playersData[j].dice_jun;
                    game.playersData[j].dice_jun = tmp;
                }
            }
        }
        // Dantotsu top eyes...find the most advantaged player
        int top = -1;
        for (i = 0; i < 8; i++) {
            if (game.playersData[i].diceCount > sum * 2 / 5)
                top = i;
        }
        // Make a list of attack sources and destinations, decide at random
        int[] list_from = new int[game.AREA_MAX * game.AREA_MAX];
        int[] list_to = new int[game.AREA_MAX * game.AREA_MAX];
        int lc = 0;
        int pn = game.playersOrder[game.currentPlayer];
        for (i = 1; i < game.AREA_MAX; i++) {
            if (game.allAreaData[i].size == 0) {
                continue;
            }
            if (game.allAreaData[i].playerIx != pn) {
                continue;
            }
            if (game.allAreaData[i].dices <= 1) {
                continue;
            }
            for (j = 1; j < game.AREA_MAX; j++) {
                if (game.allAreaData[j].size == 0) {
                    continue;
                }
                if (game.allAreaData[j].playerIx == pn) {
                    continue;
                }
                if (!game.allAreaData[i].neighbors[j]) {
                    continue;
                }
                if (top >= 0) { // if there is an advantaged player, make sure to attack it?
                    if (game.allAreaData[i].playerIx != top && game.allAreaData[j].playerIx != top)
                        continue;
                }
                if (game.allAreaData[j].dices > game.allAreaData[i].dices)
                    continue; // massive enemies
                // if we have the same number of dice as enemies
                if (game.allAreaData[j].dices == game.allAreaData[i].dices) {
                    int en = game.allAreaData[j].playerIx;
                    int f = 0;
                    if (game.playersData[pn].dice_jun == 0) {
                        f = 1; // If we're top ranked, attack
                    }
                    if (game.playersData[en].dice_jun == 0) {
                        f = 1; // If the opponent is top ranked, attack
                    }
                    if (Math.random() * 10 > 1) {
                        f = 1;
                    }
                    if (f == 0) {
                        continue;
                    }
                }
                list_from[lc] = i;
                list_to[lc] = j;
                lc++;
            }
        }
        if (lc == 0) {
            return 0;
        }

        int n = (int) (Math.floor(Math.random() * lc));
        game.attackAreaFrom = list_from[n];
        game.attackAreaTo = list_to[n];
        return 1;
    }

}
