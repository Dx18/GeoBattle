package geobattle.geobattle.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.GameStateUpdate;
import geobattle.geobattle.game.attacking.AttackScript;
import geobattle.geobattle.screens.gamescreen.GameScreen;
import geobattle.geobattle.server.ExternalAPI;

public abstract class AttackResult implements ActionResult {
    // Attack successfully started
    public static final class AttackStarted extends AttackResult implements GameStateUpdate {
        // Attack script
        public final AttackScript attackScript;

        public AttackStarted(AttackScript attackScript) {
            this.attackScript = attackScript;
        }

        public static AttackStarted fromJson(JsonObject object) {
            AttackScript attackScript = AttackScript.fromJson(object.getAsJsonObject("attackScript"));
            return new AttackStarted(attackScript);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            gameState.getAttackScripts().add(attackScript);

            if (game.getScreen() instanceof GameScreen)
                ((GameScreen) game.getScreen()).switchToNormalMode();
        }
    }

    // Cannot attack: sector blocked or one
    public static final class NotAttackable extends AttackResult {
        public NotAttackable() {}

        public static NotAttackable fromJson(JsonObject object) {
            return new NotAttackable();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot attack: sector is not attackable");
        }
    }

    // Cannot attack: some hangars are already used
    public static final class HangarsAlreadyUsed extends AttackResult {
        public HangarsAlreadyUsed() {}

        public static HangarsAlreadyUsed fromJson(JsonObject object) {
            return new HangarsAlreadyUsed();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot attack: some hangars are already used");
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends AttackResult {
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
    public static final class MalformedJson extends AttackResult {
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
    public static final class IncorrectData extends AttackResult {
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

    // Creates AttackResult from JSON
    public static AttackResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("AttackStarted"))
            return AttackStarted.fromJson(object);
        else if (type.equals("NotAttackable"))
            return NotAttackable.fromJson(object);
        else if (type.equals("HangarsAlreadyUsed"))
            return HangarsAlreadyUsed.fromJson(object);
        else if (type.equals("WrongAuthInfo"))
            return WrongAuthInfo.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        else if (type.equals("IncorrectData"))
            return IncorrectData.fromJson(object);
        return null;
    }

    // Matches AttackResult
    public void match(
            MatchBranch<AttackStarted> attackStarted,
            MatchBranch<NotAttackable> notAttackable,
            MatchBranch<HangarsAlreadyUsed> hangarsAlreadyUsed,
            MatchBranch<WrongAuthInfo> wrongAuthInfo,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (attackStarted != null && this instanceof AttackStarted)
            attackStarted.onMatch((AttackStarted) this);
        else if (notAttackable != null && this instanceof NotAttackable)
            notAttackable.onMatch((NotAttackable) this);
        else if (hangarsAlreadyUsed != null && this instanceof HangarsAlreadyUsed)
            hangarsAlreadyUsed.onMatch((HangarsAlreadyUsed) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}
