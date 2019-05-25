package geobattle.geobattle.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.GameStateUpdate;
import geobattle.geobattle.game.UnitTransactionInfo;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;

// Result of unit building
public abstract class UnitBuildResult implements ActionResult {
    // Unit successfully built
    public static final class UnitBuilt extends UnitBuildResult implements GameStateUpdate {
        // Info about built unit
        public final UnitTransactionInfo info;

        // Cost of unit
        public final int cost;

        public UnitBuilt(UnitTransactionInfo info, int cost) {
            this.info = info;
            this.cost = cost;
        }

        public static UnitBuilt fromJson(JsonObject object) {
            UnitTransactionInfo info = UnitTransactionInfo.fromJson(object.getAsJsonObject("info"));
            int cost = object.getAsJsonPrimitive("cost").getAsInt();
            return new UnitBuilt(info, cost);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            try {
                gameState.getPlayer(info.playerIndex).addUnit(info.unit);
            } catch (IllegalArgumentException e) {
                // Unit already added
            }
            gameState.setResources(gameState.getResources() - cost);
        }
    }

    // Not enough resources to build unit
    public static final class NotEnoughResources extends UnitBuildResult {
        // Required amount of resources
        public final int required;

        public NotEnoughResources(int required) {
            this.required = required;
        }

        public static NotEnoughResources fromJson(JsonObject object) {
            int required = object.getAsJsonPrimitive("required").getAsInt();
            return new NotEnoughResources(required);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot build: not enough resources");
        }
    }

    // Cannot build unit because there is no place in hangar
    public static final class NoPlaceInHangar extends UnitBuildResult {
        public NoPlaceInHangar() {}

        public static NoPlaceInHangar fromJson(JsonObject object) {
            return new NoPlaceInHangar();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot build: no place in hangar");
        }
    }

    // Cannot build unit because sector is blocked
    public static final class SectorBlocked extends UnitBuildResult {
        public SectorBlocked() {}

        public static SectorBlocked fromJson(JsonObject object) {
            return new SectorBlocked();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot build: sector is blocked");
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends UnitBuildResult {
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
    public static final class MalformedJson extends UnitBuildResult {
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
    public static final class IncorrectData extends UnitBuildResult {
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

    // Creates UnitBuildResult from JSON
    public static UnitBuildResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("UnitBuilt"))
            return UnitBuilt.fromJson(object);
        else if (type.equals("NotEnoughResources"))
            return NotEnoughResources.fromJson(object);
        else if (type.equals("NoPlaceInHangar"))
            return NoPlaceInHangar.fromJson(object);
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

    // Matches UnitBuildResult
    public void match(
            MatchBranch<UnitBuilt> built,
            MatchBranch<NotEnoughResources> notEnoughResources,
            MatchBranch<NoPlaceInHangar> noPlaceInHangar,
            MatchBranch<SectorBlocked> sectorBlocked,
            MatchBranch<WrongAuthInfo> wrongAuthInfo,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (built != null && this instanceof UnitBuilt)
            built.onMatch((UnitBuilt) this);
        else if (notEnoughResources != null && this instanceof NotEnoughResources)
            notEnoughResources.onMatch((NotEnoughResources) this);
        else if (noPlaceInHangar != null && this instanceof NoPlaceInHangar)
            noPlaceInHangar.onMatch((NoPlaceInHangar) this);
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
