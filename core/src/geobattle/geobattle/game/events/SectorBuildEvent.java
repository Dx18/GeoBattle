package geobattle.geobattle.game.events;

import com.google.gson.JsonObject;

import geobattle.geobattle.server.AuthInfo;

// Sector build event
public final class SectorBuildEvent {
    // Auth info
    public final AuthInfo authInfo;

    // X coordinate of bottom-left corner of sector
    public final int x;

    // Y coordinate of bottom-left corner of sector
    public final int y;

    public SectorBuildEvent(AuthInfo authInfo, int x, int y) {
        this.authInfo = authInfo;
        this.x = x;
        this.y = y;
    }

    // Converts SectorBuildEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "SectorBuildEvent");
        object.add("authInfo", authInfo.toJson());
        object.addProperty("x", x);
        object.addProperty("y", y);
        return object;
    }
}
