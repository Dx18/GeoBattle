package geobattle.geobattle.game;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.units.Unit;

// Object indicating specific unit in game
public final class UnitTransactionInfo {
    // Index of player unit belongs to
    public final int playerIndex;

    // Unit
    public final Unit.ServerSide unit;

    public UnitTransactionInfo(int playerIndex, Unit.ServerSide unit) {
        this.playerIndex = playerIndex;
        this.unit = unit;
    }

    // Creates unit transaction from JSON
    public static UnitTransactionInfo fromJson(JsonObject object) {
        int playerIndex = object.getAsJsonPrimitive("playerIndex").getAsInt();
        Unit.ServerSide unit = Unit.ServerSide.fromJson(object.getAsJsonObject("unit"));

        return new UnitTransactionInfo(playerIndex, unit);
    }
}
