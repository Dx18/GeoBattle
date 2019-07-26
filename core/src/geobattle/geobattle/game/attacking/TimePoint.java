package geobattle.geobattle.game.attacking;

import com.badlogic.gdx.utils.IntFloatMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// Time point
public final class TimePoint {
    // Time
    public final double time;

    // Health of sector in moment of time
    public final double sectorHealth;

    // Info about units' health
    public final IntFloatMap unitGroupHealth;

    public TimePoint(double time, double sectorHealth, IntFloatMap unitGroupHealth) {
        this.time = time;
        this.sectorHealth = sectorHealth;
        this.unitGroupHealth = unitGroupHealth;
    }

    // Creates TimePoint from JSON
    public static TimePoint fromJson(JsonObject object) {
        double time = object.getAsJsonPrimitive("time").getAsDouble();
        double sectorHealth = object.getAsJsonPrimitive("sectorHealth").getAsDouble();

        IntFloatMap planeGroupsHealth = new IntFloatMap();
        JsonArray planeGroupsHealthJson = object.getAsJsonArray("planeGroupsHealth");
        for (JsonElement planeGroupHealthJson : planeGroupsHealthJson) {
            JsonObject planeGroupHealthJsonObject = ((JsonObject) planeGroupHealthJson);
            int hangarId = planeGroupHealthJsonObject.getAsJsonPrimitive("hangarId").getAsInt();
            float health = planeGroupHealthJsonObject.getAsJsonPrimitive("health").getAsFloat();
            planeGroupsHealth.put(hangarId, health);
        }

        return new TimePoint(time, sectorHealth, planeGroupsHealth);
    }
}
