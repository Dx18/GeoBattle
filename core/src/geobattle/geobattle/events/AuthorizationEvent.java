package geobattle.geobattle.events;

import com.google.gson.JsonObject;

// Authorization event
public final class AuthorizationEvent {
    // Name of user
    public final String name;

    // Password of user
    public final String password;

    public AuthorizationEvent(String name, String password) {
        this.name = name;
        this.password = password;
    }

    // Converts AuthorizationEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "AuthorizationEvent");
        object.addProperty("name", name);
        object.addProperty("password", password);
        return object;
    }
}
