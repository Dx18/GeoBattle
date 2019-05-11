package geobattle.geobattle.game;

import com.badlogic.gdx.Gdx;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// Update of game state
public abstract class GameStateUpdate {
    public final double time;

    // New building is built
    public static class BuildingBuilt extends GameStateUpdate {
        // Information about a new building
        public final BuildTransactionInfo info;

        public BuildingBuilt(BuildTransactionInfo info, double time) {
            super(time);
            this.info = info;
        }

        // Creates BuildingBuilt from JSON
        public static BuildingBuilt fromJson(JsonObject object) {
            double time = object.getAsJsonPrimitive("time").getAsDouble();
            BuildTransactionInfo info = BuildTransactionInfo.fromJson(object);
            return new BuildingBuilt(info, time);
        }

        // Performs update
        @Override
        public void update(GameState gameState) {
            gameState.getPlayer(info.playerIndex).addBuilding(info.building);
        }
    }

    // Building is destroyed
    public static class BuildingDestroyed extends GameStateUpdate {
        // Information about destroyed building
        public final BuildTransactionInfo info;

        public BuildingDestroyed(BuildTransactionInfo info, double time) {
            super(time);
            this.info = info;
        }

        // Creates BuildingDestroyed from JSON
        public static BuildingDestroyed fromJson(JsonObject object) {
            double time = object.getAsJsonPrimitive("time").getAsDouble();
            BuildTransactionInfo info = BuildTransactionInfo.fromJson(object);
            return new BuildingDestroyed(info, time);
        }

        // Performs update
        @Override
        public void update(GameState gameState) {
            gameState.getPlayer(info.playerIndex).removeBuilding(info.building);
        }
    }

    public GameStateUpdate(double time) {
        this.time = time;
    }

    // Creates update of game state from JSON
    public static GameStateUpdate fromJson(JsonObject object) {
        JsonElement type = object.get("type");

        if (type == null) {
            Gdx.app.error("GeoBattle", "Trying to create GameStateUpdate for null type");
            return null;
        }

        try {
            String typeString = type.getAsString();
            if (typeString.equals("BuildingBuilt"))
                return BuildingBuilt.fromJson(object);
            else if (typeString.equals("BuildingDestroyed"))
                return BuildingDestroyed.fromJson(object);
        } catch (Exception e) {
            Gdx.app.error("GeoBattle","Cannot create GameStateUpdate from JSON " + object.toString());
            e.printStackTrace();
        }

        return null;
    }

    // Performs update
    public abstract void update(GameState gameState);
}
