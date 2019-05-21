package geobattle.geobattle.util;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

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

    // Converts int[] to JSON
    public static JsonArray toJson(int[] array) {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < array.length; i++)
            jsonArray.add(array[i]);
        return jsonArray;
    }

    // Creates int[] from JSON
    public static int[] fromJson(JsonArray jsonArray) {
        int[] array = new int[jsonArray.size()];
        for (int i = 0; i < array.length; i++)
            array[i] = jsonArray.get(i).getAsInt();
        return array;
    }
}
