package geobattle.geobattle.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.GameStateUpdate;
import geobattle.geobattle.game.SectorTransactionInfo;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;

// Result of sector building
public abstract class SectorBuildResult implements ActionResult {
    // Sector successfully built
    public static final class SectorBuilt extends SectorBuildResult implements GameStateUpdate {
        // Info about sector
        public final SectorTransactionInfo info;

        public SectorBuilt(SectorTransactionInfo info) {
            this.info = info;
        }

        public static SectorBuilt fromJson(JsonObject object) {
            SectorTransactionInfo info = SectorTransactionInfo.fromJson(object.getAsJsonObject("info"));
            return new SectorBuilt(info);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            try {
                gameState.getPlayer(info.playerIndex).addSector(new Sector(
                        info.x, info.y, info.id,
                        info.playerIndex,
                        gameState.getCurrentPlayer().getResearchInfo()
                ));
            } catch (IllegalArgumentException ignored) {
                // Sector already added
            }
        }

        @Override
        public GameScreenMode screenModeAfterApply() {
            return GameScreenMode.NORMAL;
        }
    }

    // Cannot build because amount of resources is too low
    public static final class NotEnoughResources extends SectorBuildResult {
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
            game.showMessage(game.getI18NBundle().format("sectorBuildResultNotEnoughResources", required));
        }
    }

    // Cannot build because sector player wants to build intersects with other player's sector
    public static final class IntersectsWithEnemy extends SectorBuildResult {
        // Index of enemy which owns nearby sector
        public final int enemyIndex;

        public IntersectsWithEnemy(int enemyIndex) {
            this.enemyIndex = enemyIndex;
        }

        public static IntersectsWithEnemy fromJson(JsonObject object) {
            int enemyIndex = object.getAsJsonPrimitive("enemyIndex").getAsInt();
            return new IntersectsWithEnemy(enemyIndex);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.showMessage(game.getI18NBundle().get("sectorBuildResultIntersectsWithEnemy"));
        }
    }

    // Sector is not aligned or it's not attached to other sector
    public static final class WrongPosition extends SectorBuildResult {
        public WrongPosition() {}

        public static WrongPosition fromJson(JsonObject object) {
            return new WrongPosition();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.showMessage(game.getI18NBundle().get("sectorBuildResultWrongPosition"));
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends SectorBuildResult {
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
    public static final class MalformedJson extends SectorBuildResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot build sector: JSON request is not well-formed. Probable bug. Tell the developers");
        }
    }

    // Value of field is not valid
    public static final class IncorrectData extends SectorBuildResult {
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
            game.getExternalAPI().oSAPI.showMessage("Cannot build sector: value of field in request is not valid. Probable bug. Tell the developers");
        }
    }

    @Override
    public GameScreenMode screenModeAfterApply() {
        return null;
    }

    // Creates SectorBuildResult from JSON
    public static SectorBuildResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("SectorBuilt"))
            return SectorBuilt.fromJson(object);
        else if (type.equals("NotEnoughResources"))
            return NotEnoughResources.fromJson(object);
        else if (type.equals("IntersectsWithEnemy"))
            return IntersectsWithEnemy.fromJson(object);
        else if (type.equals("WrongPosition"))
            return WrongPosition.fromJson(object);
        else if (type.equals("WrongAuthInfo"))
            return WrongAuthInfo.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        else if (type.equals("IncorrectData"))
            return IncorrectData.fromJson(object);
        return null;
    }

    // Matches SectorBuildResult
    public void match(
            MatchBranch<SectorBuilt> sectorBuilt,
            MatchBranch<NotEnoughResources> notEnoughResources,
            MatchBranch<IntersectsWithEnemy> intersectsWithEnemy,
            MatchBranch<WrongPosition> wrongPosition,
            MatchBranch<WrongAuthInfo> wrongAuthInfo,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (sectorBuilt != null && this instanceof SectorBuilt)
            sectorBuilt.onMatch((SectorBuilt) this);
        else if (notEnoughResources != null && this instanceof NotEnoughResources)
            notEnoughResources.onMatch((NotEnoughResources) this);
        else if (intersectsWithEnemy != null && this instanceof IntersectsWithEnemy)
            intersectsWithEnemy.onMatch((IntersectsWithEnemy) this);
        else if (wrongPosition != null && this instanceof WrongPosition)
            wrongPosition.onMatch((WrongPosition) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}
