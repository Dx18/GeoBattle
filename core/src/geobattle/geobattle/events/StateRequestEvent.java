package geobattle.geobattle.events;

import com.google.gson.JsonObject;

import geobattle.geobattle.server.AuthInfo;

// State request event
public final class StateRequestEvent {
    // Auth info
    public final AuthInfo authInfo;

    public StateRequestEvent(AuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    // Converts StateRequestEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "StateRequestEvent");
        object.add("authInfo", authInfo.toJson());
        return object;
    }
}
