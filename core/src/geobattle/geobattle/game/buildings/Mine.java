package geobattle.geobattle.game.buildings;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.GameState;

// Mine
public final class Mine extends Building {
    public Mine(BuildingParams params) {
        super(params, BuildingType.MINE);
    }

    @Override
    public void update(float delta, GameState gameState) {}

    // Clones mine
    @Override
    public Building clone() {
        return new Mine(getParams());
    }

    // Creates mine from JSON
    public static Mine fromJson(JsonObject object, BuildingParams params) {
        return new Mine(params);
    }
}
