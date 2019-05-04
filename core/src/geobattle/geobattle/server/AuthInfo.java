package geobattle.geobattle.server;

import com.google.gson.JsonObject;

// Authorization info
public final class AuthInfo {
    // ID of player
    public final int id;

    // Token of player
    public final String token;

    public AuthInfo(int id, String token) {
        this.id = id;
        this.token = token;
    }

    // Creates AuthInfo from JSON
    public static AuthInfo fromJson(JsonObject object) {
        int playerId = object.getAsJsonPrimitive("id").getAsInt();
        String playerToken = object.getAsJsonPrimitive("token").getAsString();
        return new AuthInfo(playerId, playerToken);
    }

    // Converts AuthInfo to JSON
    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.addProperty("id", id);
        result.addProperty("token", token);
        return result;
    }
}
