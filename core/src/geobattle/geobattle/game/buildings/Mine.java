package geobattle.geobattle.game.buildings;

import com.google.gson.JsonObject;

// Mine
public final class Mine extends Building {
    public Mine(BuildingParams params) {
        super(params, BuildingType.MINE);
    }

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
