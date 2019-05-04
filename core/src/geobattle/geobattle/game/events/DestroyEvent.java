package geobattle.geobattle.game.events;

import com.google.gson.JsonObject;

import geobattle.geobattle.server.AuthInfo;

// Destroy building event
public final class DestroyEvent {
    // Auth info
    public final AuthInfo authInfo;

    // ID of building to destroy
    public final int id;

    public DestroyEvent(AuthInfo authInfo, int id) {
        this.authInfo = authInfo;
        this.id = id;
    }

    // Converts DestroyEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "DestroyEvent");
        object.add("authInfo", authInfo.toJson());
        object.addProperty("id", id);
        return object;
    }
}