package geobattle.geobattle.game;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.buildings.Building;

// Object indicating specific building in game
public final class BuildTransactionInfo {
    // Index of player building belongs to
    public final int playerIndex;

    // Building
    public final Building building;

    public BuildTransactionInfo(int playerIndex, Building building) {
        this.playerIndex = playerIndex;
        this.building = building;
    }

    // Creates build transaction from JSON
    public static BuildTransactionInfo fromJson(JsonObject object) {
        int playerId = object.getAsJsonPrimitive("playerIndex").getAsInt();
        Building building = Building.fromJson(object.getAsJsonObject("building"));

        return new BuildTransactionInfo(playerId, building);
    }
}
