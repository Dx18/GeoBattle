package geobattle.geobattle.events;

import com.google.gson.JsonObject;

import geobattle.geobattle.server.AuthInfo;

public final class UnitBuildEvent {
    // Auth info
    public final AuthInfo authInfo;

    // Type of unit to build
    public final String unitType;

    // ID of hangar
    public final int hangarId;

    public UnitBuildEvent(AuthInfo authInfo, String unitType, int hangarId) {
        this.authInfo = authInfo;
        this.unitType = unitType;
        this.hangarId = hangarId;
    }

    // Converts UnitBuildEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "UnitBuildEvent");
        object.add("authInfo", authInfo.toJson());
        object.addProperty("unitType", unitType);
        object.addProperty("hangarId", hangarId);
        return object;
    }
}
