package geobattle.geobattle.game.attacking;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// Info about unit group moving
public final class UnitGroupMovingInfo {
    // Hangar ID (home for unit group)
    public final int hangarId;

    // Time of arrive
    public final double arriveTime;

    // X coordinate of arrive point
    public final int arriveX;

    // Y coordinate of arrive point
    public final int arriveY;

    // Time when unit should be in hangar
    public final double returnTime;

    public UnitGroupMovingInfo(int hangarId, double arriveTime, int arriveX, int arriveY, double returnTime) {
        this.hangarId = hangarId;
        this.arriveTime = arriveTime;
        this.arriveX = arriveX;
        this.arriveY = arriveY;
        this.returnTime = returnTime;
    }

    // Clones UnitGroupMovingInfo
    public UnitGroupMovingInfo clone() {
        return new UnitGroupMovingInfo(hangarId, arriveTime, arriveX, arriveY, returnTime);
    }

    // Creates UnitGroupMovingInfo from JSON
    public static UnitGroupMovingInfo fromJson(JsonObject object) {
        int hangarId = object.getAsJsonPrimitive("hangarId").getAsInt();
        double arriveTime = object.getAsJsonPrimitive("arriveTime").getAsDouble();
        int arriveX = object.getAsJsonPrimitive("arriveX").getAsInt();
        int arriveY = object.getAsJsonPrimitive("arriveY").getAsInt();
        double returnTime = object.getAsJsonPrimitive("returnTime").getAsDouble();

        return new UnitGroupMovingInfo(hangarId, arriveTime, arriveX, arriveY, returnTime);
    }
}
