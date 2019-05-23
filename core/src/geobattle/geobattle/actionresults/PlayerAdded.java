package geobattle.geobattle.actionresults;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonObject;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.GameStateUpdate;
import geobattle.geobattle.game.PlayerState;
import geobattle.geobattle.game.research.ResearchInfo;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;
import geobattle.geobattle.util.JsonObjects;

// Player added to game
public final class PlayerAdded implements GameStateUpdate {
    // Name of player
    public final String name;

    // ID of player
    public final int playerId;

    // Color of player
    public final Color color;

    public PlayerAdded(String name, int playerId, Color color) {
        this.name = name;
        this.playerId = playerId;
        this.color = color;
    }

    // Creates PlayerAdded from JSON
    public static PlayerAdded fromJson(JsonObject object) {
        String name = object.getAsJsonPrimitive("name").getAsString();
        int playerId = object.getAsJsonPrimitive("playerId").getAsInt();
        Color color = JsonObjects.fromJson(object.getAsJsonObject("color"));
        return new PlayerAdded(name, playerId, color);
    }

    @Override
    public void apply(GeoBattle game, GameState gameState) {
        try {
            gameState.addPlayer(new PlayerState(
                    name, playerId, color,
                    new ResearchInfo(0, 0, 0)
            ));
        } catch (IllegalArgumentException ignored) {
            // Player already added
        }
    }

    @Override
    public GameScreenMode screenModeAfterApply() {
        return null;
    }
}
