package geobattle.geobattle.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.BuildTransactionInfo;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.GameStateUpdate;
import geobattle.geobattle.screens.gamescreen.GameScreen;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;

// Result of building destroying
public abstract class DestroyResult implements ActionResult {
    // Building is successfully destroyed
    public static final class BuildingDestroyed extends DestroyResult implements GameStateUpdate {
        // Info about destroyed building
        public final BuildTransactionInfo info;

        public BuildingDestroyed(BuildTransactionInfo info) {
            this.info = info;
        }

        public static BuildingDestroyed fromJson(JsonObject object) {
            BuildTransactionInfo info = BuildTransactionInfo.fromJson(object.getAsJsonObject("info"));
            return new BuildingDestroyed(info);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            try {
                gameState.getPlayer(info.playerIndex).removeBuilding(info.building);
            } catch (IllegalArgumentException ignored) {
                // Building already destroyed
            }
        }

        @Override
        public GameScreenMode screenModeAfterApply() {
            return GameScreenMode.NORMAL;
        }
    }

    // Player does not own building he wants to destroy
    public static final class NotOwningBuilding extends DestroyResult {
        public NotOwningBuilding() {}

        public static NotOwningBuilding fromJson(JsonObject object) {
            return new NotOwningBuilding();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot destroy: you are not owning this building");
        }
    }

    // Cannot destroy building because sector is blocked
    public static final class SectorBlocked extends DestroyResult {
        public SectorBlocked() {}

        public static SectorBlocked fromJson(JsonObject object) {
            return new SectorBlocked();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot destroy: sector is blocked");
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends DestroyResult {
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
    public static final class MalformedJson extends DestroyResult {
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
    public static final class IncorrectData extends DestroyResult {
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

    // Creates DestroyResult from JSON
    public static DestroyResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("BuildingDestroyed"))
            return BuildingDestroyed.fromJson(object);
        else if (type.equals("NotOwningBuilding"))
            return NotOwningBuilding.fromJson(object);
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

    // Matches DestroyResult
    public void match(
            MatchBranch<BuildingDestroyed> destroyed,
            MatchBranch<NotOwningBuilding> notOwningBuilding,
            MatchBranch<SectorBlocked> sectorBlocked,
            MatchBranch<WrongAuthInfo> wrongAuthInfo,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (destroyed != null && this instanceof BuildingDestroyed)
            destroyed.onMatch((BuildingDestroyed) this);
        else if (notOwningBuilding != null && this instanceof NotOwningBuilding)
            notOwningBuilding.onMatch((NotOwningBuilding) this);
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