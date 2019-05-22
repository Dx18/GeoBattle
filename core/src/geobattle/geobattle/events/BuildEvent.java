package geobattle.geobattle.events;

import com.google.gson.JsonObject;

import geobattle.geobattle.server.AuthInfo;

// Build event
public final class BuildEvent {
    // Auth info
    public final AuthInfo authInfo;

    // Type of building
    public final String buildingType;

    // X coordinate of building
    public final int x;

    // Y coordinate of building
    public final int y;

    public BuildEvent(AuthInfo authInfo, String buildingType, int x, int y) {
        this.authInfo = authInfo;
        this.buildingType = buildingType;
        this.x = x;
        this.y = y;
    }

    // Converts BuildEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "BuildEvent");
        object.add("authInfo", authInfo.toJson());
        object.addProperty("buildingType", buildingType);
        object.addProperty("x", x);
        object.addProperty("y", y);
        return object;
    }
}
