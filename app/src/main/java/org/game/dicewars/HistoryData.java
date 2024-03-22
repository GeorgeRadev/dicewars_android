package org.game.dicewars;

public class HistoryData {
    public int from = 0; // Attack source area, supply area
    public int to = 0; // Attack destination area, 0 is supply flag
    public boolean res = false; // Result: 0 attack failures, 1 occupations
}