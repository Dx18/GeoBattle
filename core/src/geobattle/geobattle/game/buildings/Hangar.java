package geobattle.geobattle.game.buildings;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.units.UnitGroup;

// Hangar
public final class Hangar extends Building {
    // Units in hangar
    public final UnitGroup units;

    public Hangar(BuildingParams params) {
        super(params, BuildingType.HANGAR);
        units = new UnitGroup();
    }

    public Hangar(BuildingParams params, UnitGroup units) {
        super(params, BuildingType.HANGAR);
        this.units = units;
    }

    @Override
    public void update(float delta, GameState gameState) {}

    // Clones hangar
    @Override
    public Building clone() {
        return new Hangar(getParams(), units.clone());
    }

    // Creates hangar from JSON
    public static Hangar fromJson(JsonObject object, BuildingParams params) {
        UnitGroup units = UnitGroup.fromJson(object.getAsJsonObject("units"), params.x, params.y);

        return new Hangar(params, units);
    }
}
