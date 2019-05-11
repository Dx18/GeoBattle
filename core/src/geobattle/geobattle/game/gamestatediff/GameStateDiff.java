package geobattle.geobattle.game.gamestatediff;

import java.util.ArrayList;
import java.util.Iterator;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.PlayerState;

// Difference between game states
public final class GameStateDiff {
    // Added players
    public final ArrayList<PlayerState> addedPlayers;

    // Removed players
    public final ArrayList<PlayerState> removedPlayers;

    // Players which could be changed
    public final ArrayList<PlayerStateDiff> changedPlayers;

    // Creates and calculates GameStateDiff
    public GameStateDiff(GameState gameState1, GameState gameState2) {
        addedPlayers = new ArrayList<PlayerState>();
        removedPlayers = new ArrayList<PlayerState>();
        changedPlayers = new ArrayList<PlayerStateDiff>();

        Iterator<PlayerState> players1 = gameState1.getPlayers();
        while (players1.hasNext()) {
            PlayerState next = players1.next();

            PlayerState player = gameState2.getPlayer(next.getPlayerId());
            if (player == null)
                removedPlayers.add(next);
            else
                changedPlayers.add(new PlayerStateDiff(next.getPlayerId(), next, player));
        }

        Iterator<PlayerState> players2 = gameState2.getPlayers();
        while (players2.hasNext()) {
            PlayerState next = players2.next();

            if (gameState1.getPlayer(next.getPlayerId()) == null)
                addedPlayers.add(next);
        }
    }
}
