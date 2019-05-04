package geobattle.geobattle.server.events;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonObject;

import geobattle.geobattle.util.JsonObjects;

// Registration event
public final class RegistrationEvent {
    // Name of user
    public final String name;

    // Email of user
    public final String email;

    // Password of user
    public final String password;

    // Color of user
    public final Color color;

    public RegistrationEvent(String name, String email, String password, Color color) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.color = color;
    }

    // Converts RegistrationEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "RegistrationEvent");
        object.addProperty("name", name);
        object.addProperty("email", email);
        object.addProperty("password", password);
        object.add("color", JsonObjects.toJson(color));
        return object;
    }
}
