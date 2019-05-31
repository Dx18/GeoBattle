package geobattle.geobattle.rating;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.PlayerState;

// Rating of player
public final class RatingEntry {
    // ID of player
    public final int playerId;

    // Wealth of player
    public final int wealth;

    // Name of player
    private String name;

    public RatingEntry(int playerId, int wealth) {
        this.playerId = playerId;
        this.wealth = wealth;
    }

    // Sets additional data
    public void setPlayerData(PlayerState player) {
        name = player.getName();
    }

    // Returns name of player
    public String getName() {
        return name;
    }

    // Creates RatingEntry from JSON
    public static RatingEntry fromJson(JsonObject object) {
        int playerId = object.getAsJsonPrimitive("playerId").getAsInt();
        int wealth = object.getAsJsonPrimitive("wealth").getAsInt();
        return new RatingEntry(playerId, wealth);
    }
}
