package geobattle.geobattle.util;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonObject;

public final class JsonObjects {
    // Converts Color to JSON
    public static JsonObject toJson(Color color) {
        JsonObject object = new JsonObject();
        object.addProperty("r", color.r);
        object.addProperty("g", color.g);
        object.addProperty("b", color.b);
        return object;
    }

    // Creates Color from JSON
    public static Color fromJson(JsonObject object) {
        float r = object.getAsJsonPrimitive("r").getAsFloat();
        float g = object.getAsJsonPrimitive("g").getAsFloat();
        float b = object.getAsJsonPrimitive("b").getAsFloat();
        return new Color(r, g, b, 1);
    }
}
