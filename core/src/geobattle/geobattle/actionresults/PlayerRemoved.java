package geobattle.geobattle.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.GameStateUpdate;
import geobattle.geobattle.game.PlayerState;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;

// Player removed from game
public final class PlayerRemoved implements GameStateUpdate {
    // ID of player
    public final int playerId;

    public PlayerRemoved(int playerId) {
        this.playerId = playerId;
    }

    // Creates PlayerRemoved from JSON
    public static PlayerRemoved fromJson(JsonObject object) {
        int playerId = object.getAsJsonPrimitive("playerId").getAsInt();
        return new PlayerRemoved(playerId);
    }

    @Override
    public void apply(GeoBattle game, GameState gameState) {
        PlayerState toRemove = gameState.getPlayer(playerId);
        if (toRemove == null)
            return;

        try {
            gameState.removePlayer(toRemove);
        } catch (IllegalArgumentException ignored) {
            // Player already removed
        }
    }

    @Override
    public GameScreenMode screenModeAfterApply() {
        return null;
    }
}
