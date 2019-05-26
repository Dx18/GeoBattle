package geobattle.geobattle.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.BuildTransactionInfo;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.GameStateUpdate;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;

// Result of building
public abstract class BuildResult implements ActionResult {
    // Building is successfully built
    public static final class BuildingBuilt extends BuildResult implements GameStateUpdate {
        // Building instance
        public final BuildTransactionInfo info;

        // Cost of building
        public final int cost;

        public BuildingBuilt(BuildTransactionInfo info, int cost) {
            this.info = info;
            this.cost = cost;
        }

        public static BuildingBuilt fromJson(JsonObject object) {
            BuildTransactionInfo info = BuildTransactionInfo.fromJson(object.getAsJsonObject("info"));
            int cost = object.getAsJsonPrimitive("cost").getAsInt();
            return new BuildingBuilt(info, cost);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            try {
                gameState.getPlayer(info.playerIndex).addBuilding(info.building);
            } catch (IllegalArgumentException ignored) {
                // Building already added
            }
        }

        @Override
        public GameScreenMode screenModeAfterApply() {
            return GameScreenMode.NORMAL;
        }
    }

    // Cannot build because building collides with other buildings
    public static final class CollisionFound extends BuildResult {
        public CollisionFound() {}

        public static CollisionFound fromJson(JsonObject object) {
            return new CollisionFound();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.showMessage(game.getI18NBundle().get("buildResultCollisionFound"));
        }
    }

    // Cannot build because limit of buildings is exceeded
    public static final class NotEnoughResources extends BuildResult {
        // Max number of buildings
        public final int max;

        public NotEnoughResources(int max) {
            this.max = max;
        }

        public static NotEnoughResources fromJson(JsonObject object) {
            int max = object.getAsJsonPrimitive("max").getAsInt();
            return new NotEnoughResources(max);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.showMessage(game.getI18NBundle().get("buildResultNotEnoughResources"));
        }
    }

    // Reached max count of buildings
    public static final class BuildingLimitExceeded extends BuildResult {
        // Limit of buildings
        public final int max;

        public BuildingLimitExceeded(int max) {
            this.max = max;
        }

        public static BuildingLimitExceeded fromJson(JsonObject object) {
            int max = object.getAsJsonPrimitive("max").getAsInt();
            return new BuildingLimitExceeded(max);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.showMessage(game.getI18NBundle().format("buildResultBuildingLimitExceeded", max));
        }
    }

    // Cannot build because building is not in player's territory
    public static final class NotInTerritory extends BuildResult {
        public NotInTerritory() {}

        public static NotInTerritory fromJson(JsonObject object) {
            return new NotInTerritory();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.showMessage(game.getI18NBundle().get("buildResultNotInTerritory"));
        }
    }

    // Cannot build building on sector because it's blocked
    public static final class SectorBlocked extends BuildResult {
        public SectorBlocked() {}

        public static SectorBlocked fromJson(JsonObject object) {
            return new SectorBlocked();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.showMessage(game.getI18NBundle().get("buildResultSectorBlocked"));
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends BuildResult {
        public WrongAuthInfo() {}

        public static WrongAuthInfo fromJson(JsonObject object) {
            return new WrongAuthInfo();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Not authorized!");
            game.switchToLoginScreen();
        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends BuildResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot build: JSON request is not well-formed. Probable bug. Tell the developers");
        }
    }

    // Value of field is not valid
    public static final class IncorrectData extends BuildResult {
        // Field with error
        public final String field;

        public IncorrectData(String field) {
            this.field = field;
        }

        public static IncorrectData fromJson(JsonObject object) {
            String field = object.getAsJsonPrimitive("field").getAsString();
            return new IncorrectData(field);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot build: value of field in request is not valid. Probable bug. Tell the developers");
        }
    }

    @Override
    public GameScreenMode screenModeAfterApply() {
        return null;
    }

    // Creates BuildResult from JSON
    public static BuildResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("BuildingBuilt"))
            return BuildingBuilt.fromJson(object);
        else if (type.equals("CollisionFound"))
            return CollisionFound.fromJson(object);
        else if (type.equals("NotEnoughResources"))
            return NotEnoughResources.fromJson(object);
        else if (type.equals("BuildingLimitExceeded"))
            return BuildingLimitExceeded.fromJson(object);
        else if (type.equals("NotInTerritory"))
            return NotInTerritory.fromJson(object);
        else if (type.equals("SectorBlocked"))
            return SectorBlocked.fromJson(object);
        else if (type.equals("WrongAuthInfo"))
            return WrongAuthInfo.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        else if (type.equals("IncorrectData"))
            return IncorrectData.fromJson(object);
        return null;
    }

    // Matches BuildResult
    public void match(
            MatchBranch<BuildingBuilt> built,
            MatchBranch<CollisionFound> collisionFound,
            MatchBranch<NotEnoughResources> notEnoughResources,
            MatchBranch<BuildingLimitExceeded> buildingLimitExceeded,
            MatchBranch<NotInTerritory> notInTerritory,
            MatchBranch<SectorBlocked> sectorBlocked,
            MatchBranch<WrongAuthInfo> wrongAuthInfo,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (built != null && this instanceof BuildingBuilt)
            built.onMatch((BuildingBuilt) this);
        else if (collisionFound != null && this instanceof CollisionFound)
            collisionFound.onMatch((CollisionFound) this);
        else if (notEnoughResources != null && this instanceof NotEnoughResources)
            notEnoughResources.onMatch((NotEnoughResources) this);
        else if (buildingLimitExceeded != null && this instanceof BuildingLimitExceeded)
            buildingLimitExceeded.onMatch((BuildingLimitExceeded) this);
        else if (notInTerritory != null && this instanceof NotInTerritory)
            notInTerritory.onMatch((NotInTerritory) this);
        else if (sectorBlocked != null && this instanceof SectorBlocked)
            sectorBlocked.onMatch((SectorBlocked) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}