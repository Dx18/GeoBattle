package geobattle.geobattle.events;

import com.google.gson.JsonObject;

// Request of rating
public final class RatingRequestEvent {
    public RatingRequestEvent() {}

    // Converts RatingRequestEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "RatingRequestEvent");
        return object;
    }
}
