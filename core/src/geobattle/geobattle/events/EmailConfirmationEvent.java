package geobattle.geobattle.events;

import com.google.gson.JsonObject;

// Email confirmation event
public final class EmailConfirmationEvent {
    // Name of player trying to register
    public final String name;

    // Code player sending to server
    public final int code;

    public EmailConfirmationEvent(String name, int code) {
        this.name = name;
        this.code = code;
    }

    // Converts EmailConfirmationEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "EmailConfirmationEvent");
        object.addProperty("name", name);
        object.addProperty("code", code);
        return object;
    }
}