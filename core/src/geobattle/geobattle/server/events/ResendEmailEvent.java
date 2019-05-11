package geobattle.geobattle.server.events;

import com.google.gson.JsonObject;

public final class ResendEmailEvent {
    // Name of player requesting resend
    public final String name;

    public ResendEmailEvent(String name) {
        this.name = name;
    }

    // Converts ResendEmailEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "ResendEmailEvent");
        object.addProperty("name", name);
        return object;
    }
}
