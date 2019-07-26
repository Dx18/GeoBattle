package geobattle.geobattle.game.buildings;

import com.google.gson.JsonObject;

// Mine
public final class Mine extends Building {
    public Mine(BuildingParams params) {
        super(params, BuildingType.MINE);
    }

    // Creates mine from JSON
    public static Mine fromJson(JsonObject object, BuildingParams params) {
        return new Mine(params);
    }
}
