package geobattle.geobattle.game.gamestatediff;

import java.util.ArrayList;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.PlayerState;

// Difference between game states
public final class GameStateDiff {
    // Added players
    public final ArrayList<PlayerState> addedPlayers;

    // Players which could be changed
    public final ArrayList<PlayerStateDiff> changedPlayers;

    // Creates and calculates GameStateDiff
    public GameStateDiff(GameState gameState1, GameState gameState2) {
        addedPlayers = new ArrayList<PlayerState>();
        changedPlayers = new ArrayList<PlayerStateDiff>();

        for (int index = 0; index < gameState1.getPlayers().size(); index++)
            changedPlayers.add(new PlayerStateDiff(
                    index,
                    gameState1.getPlayers().get(index),
                    gameState2.getPlayers().get(index)
            ));

        for (int index = gameState1.getPlayers().size(); index < gameState2.getPlayers().size(); index++)
            addedPlayers.add(gameState2.getPlayers().get(index));
    }
}
