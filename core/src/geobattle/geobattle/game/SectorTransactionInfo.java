package geobattle.geobattle.game;

import com.google.gson.JsonObject;

// Info about sector
public final class SectorTransactionInfo {
    // Index of player
    public final int playerIndex;

    // X of sector
    public final int x;

    // Y of sector
    public final int y;

    // ID of sector
    public final int id;

    public SectorTransactionInfo(int playerIndex, int x, int y, int id) {
        this.playerIndex = playerIndex;
        this.x = x;
        this.y = y;
        this.id = id;
    }

    // Creates SectorTransactionInfo from JSON
    public static SectorTransactionInfo fromJson(JsonObject object) {
        int playerIndex = object.getAsJsonPrimitive("playerIndex").getAsInt();
        int x = object.getAsJsonPrimitive("x").getAsInt();
        int y = object.getAsJsonPrimitive("y").getAsInt();
        int id = object.getAsJsonPrimitive("id").getAsInt();

        return new SectorTransactionInfo(playerIndex, x, y, id);
    }
}
