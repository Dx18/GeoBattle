package geobattle.geobattle.game.buildings;

import com.google.gson.JsonObject;

// Research center
public final class ResearchCenter extends Building {
    public ResearchCenter(BuildingParams params) {
        super(params, BuildingType.RESEARCH_CENTER);
    }

    // Creates research center from JSON
    public static ResearchCenter fromJson(JsonObject object, BuildingParams params) {
        return new ResearchCenter(params);
    }
}
