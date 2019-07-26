package geobattle.geobattle.game.units;

import com.google.gson.JsonObject;

// Bomber
public final class Bomber extends Unit {
    public Bomber(double x, double y, double direction, int id, int hangarId, int hangarSlot) {
        super(x, y, direction, id, hangarId, hangarSlot, UnitType.BOMBER);
    }

    // Creates bomber from JSON
    public static Bomber fromJson(JsonObject object, double x, double y, double direction, int id, int hangarId, int hangarSlot) {
        return new Bomber(x, y, direction, id, hangarId, hangarSlot);
    }
}
