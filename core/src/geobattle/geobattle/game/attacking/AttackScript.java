package geobattle.geobattle.game.attacking;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// Attack event
public final class AttackScript {
    // ID of attack script
    public final int id;

    // ID of attacker
    public final int attackerId;

    // ID of victim
    public final int victimId;

    // ID of sector where attack is taking place
    public final int sectorId;

    // Info about unit group moving
    public final UnitGroupMovingInfo[] unitGroupMoving;

    // Time points
    public final TimePoint[] timePoints;

    // Expiration time
    public final double expirationTime;

    // Time when all unit groups start to arrive
    public final double startArriveTime;

    // Time when all unit groups start to return
    public final double startReturnTime;

    public AttackScript(int id, int attackerId, int victimId, int sectorId, UnitGroupMovingInfo[] unitGroupMoving, TimePoint[] timePoints) {
        this.id = id;
        this.attackerId = attackerId;
        this.victimId = victimId;
        this.sectorId = sectorId;
        this.unitGroupMoving = unitGroupMoving;
        this.timePoints = timePoints;

        if (this.timePoints[this.timePoints.length - 1].sectorHealth == 0) {
            double expirationTime = Double.MIN_VALUE;
            for (UnitGroupMovingInfo unitGroupMovingInfo : this.unitGroupMoving) {
                if (unitGroupMovingInfo.returnTime > expirationTime)
                    expirationTime = unitGroupMovingInfo.returnTime;
            }
            this.expirationTime = expirationTime;
        } else {
            this.expirationTime = this.timePoints[this.timePoints.length - 1].time;
        }

        startArriveTime = timePoints[0].time;
        startReturnTime = timePoints[timePoints.length - 2].time;
    }

    public TimePoint getTimePointBefore(double time) {
        int left = 0;
        int right = timePoints.length - 1;
        while (left <= right) {
            int mid = (left + right) >> 1;

            if (timePoints[mid].time < time)
                left = mid + 1;
            else if (timePoints[mid].time > time)
                right = mid - 1;
            else
                return timePoints[mid];
        }
        return right >= 0 ? timePoints[right] : null;
    }

    public TimePoint getTimePointAfter(double time) {
        int left = 0;
        int right = timePoints.length - 1;
        while (left <= right) {
            int mid = (left + right) >> 1;

            if (timePoints[mid].time < time)
                left = mid + 1;
            else if (timePoints[mid].time > time)
                right = mid - 1;
            else
                return timePoints[mid + 1];
        }
        return left < timePoints.length ? timePoints[left] : null;
    }

    // Returns true if this attack event should be removed
    public boolean isExpired(double currentTime) {
        return currentTime > expirationTime;
    }

    // Creates AttackScript from JSON
    public static AttackScript fromJson(JsonObject object) {
        int id = object.getAsJsonPrimitive("id").getAsInt();
        int attackerId = object.getAsJsonPrimitive("attackerId").getAsInt();
        int victimId = object.getAsJsonPrimitive("victimId").getAsInt();
        int sectorId = object.getAsJsonPrimitive("sectorId").getAsInt();

        JsonArray unitGroupMovingJson = object.getAsJsonArray("unitGroupMoving");
        UnitGroupMovingInfo[] unitGroupMoving = new UnitGroupMovingInfo[unitGroupMovingJson.size()];
        for (int i = 0; i < unitGroupMoving.length; i++)
            unitGroupMoving[i] = UnitGroupMovingInfo.fromJson(unitGroupMovingJson.get(i).getAsJsonObject());

        JsonArray timePointsJson = object.getAsJsonArray("timePoints");
        TimePoint[] timePoints = new TimePoint[timePointsJson.size()];
        for (int i = 0; i < timePoints.length; i++)
            timePoints[i] = TimePoint.fromJson(timePointsJson.get(i).getAsJsonObject());

        return new AttackScript(id, attackerId, victimId, sectorId, unitGroupMoving, timePoints);
    }

    // Clones AttackScript
    public AttackScript clone() {
        UnitGroupMovingInfo[] unitGroupMoving = new UnitGroupMovingInfo[this.unitGroupMoving.length];
        for (int index = 0; index < this.unitGroupMoving.length; index++)
            unitGroupMoving[index] = this.unitGroupMoving[index].clone();

        TimePoint[] timePoints = new TimePoint[this.timePoints.length];
        for (int index = 0; index < this.timePoints.length; index++)
            timePoints[index] = this.timePoints[index].clone();

        return new AttackScript(id, attackerId, victimId, sectorId, unitGroupMoving, timePoints);
    }

    // Return ID of AttackScript used as hash code
    @Override
    public int hashCode() {
        return id;
    }

    // Checks ID equality
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttackScript script = (AttackScript) o;

        return id == script.id;
    }
}
