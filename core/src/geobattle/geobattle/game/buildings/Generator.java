package geobattle.geobattle.game.buildings;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.GameState;

// Generator
public final class Generator extends Building {
    public Generator(BuildingParams params) {
        super(params, BuildingType.GENERATOR);
    }

    // Clones generator
    @Override
    public Building clone() {
        return new Generator(getParams());
    }

    // Creates generator from JSON
    public static Generator fromJson(JsonObject object, BuildingParams params) {
        return new Generator(params);
    }
}
