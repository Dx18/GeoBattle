package geobattle.geobattle.game.units;

import com.google.gson.JsonObject;

// Spotter
public final class Spotter extends Unit {
    public Spotter(double x, double y, double direction, int id, int hangarId, int hangarSlot) {
        super(x, y, direction, id, hangarId, hangarSlot, UnitType.SPOTTER);
    }

    // Creates spotter from JSON
    public static Spotter fromJson(JsonObject object, double x, double y, double direction, int id, int hangarId, int hangarSlot) {
        return new Spotter(x, y, direction, id, hangarId, hangarSlot);
    }
}
