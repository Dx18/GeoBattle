package geobattle.geobattle.game.buildings;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.GameState;

// Research center
public final class ResearchCenter extends Building {
    public ResearchCenter(BuildingParams params) {
        super(params, BuildingType.RESEARCH_CENTER);
    }

    @Override
    public void update(float delta, GameState gameState) {}

    // Clones research center
    @Override
    public Building clone() {
        return new ResearchCenter(getParams());
    }

    // Creates research center from JSON
    public static ResearchCenter fromJson(JsonObject object, BuildingParams params) {
        return new ResearchCenter(params);
    }
}
