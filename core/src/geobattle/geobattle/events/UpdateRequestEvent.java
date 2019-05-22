package geobattle.geobattle.events;

import com.google.gson.JsonObject;

import geobattle.geobattle.server.AuthInfo;

// Update request event
public final class UpdateRequestEvent {
    // Auth info
    public final AuthInfo authInfo;

    // Time of last update
    public final double lastUpdateTime;

    public UpdateRequestEvent(AuthInfo authInfo, double lastUpdateTime) {
        this.authInfo = authInfo;
        this.lastUpdateTime = lastUpdateTime;
    }

    // Converts UpdateRequestEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "UpdateRequestEvent");
        object.add("authInfo", authInfo.toJson());
        object.addProperty("lastUpdateTime", lastUpdateTime);
        return object;
    }
}
