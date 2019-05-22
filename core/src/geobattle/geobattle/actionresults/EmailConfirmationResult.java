package geobattle.geobattle.actionresults;

import com.google.gson.JsonObject;

import java.util.Locale;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;
import geobattle.geobattle.server.AuthInfo;

// Result of email confirmation
public abstract class EmailConfirmationResult implements ActionResult {
    // Email successfully confirmed
    public static final class EmailConfirmed extends EmailConfirmationResult {
        // Auth info
        public final AuthInfo authInfo;

        public EmailConfirmed(AuthInfo authInfo) {
            this.authInfo = authInfo;
        }

        public static EmailConfirmed fromJson(JsonObject object) {
            AuthInfo authInfo = AuthInfo.fromJson(object.getAsJsonObject("authInfo"));
            return new EmailConfirmed(authInfo);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.onAuthInfoObtained(authInfo);
        }
    }

    // Wrong code sent to server
    public static final class WrongCode extends EmailConfirmationResult {
        // Number of tries left
        public final int triesLeft;

        public WrongCode(int triesLeft) {
            this.triesLeft = triesLeft;
        }

        public static WrongCode fromJson(JsonObject object) {
            int triesLeft = object.getAsJsonPrimitive("triesLeft").getAsInt();
            return new WrongCode(triesLeft);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage(String.format(
                    Locale.US,
                    "Wrong code. Tries left: %d",
                    triesLeft
            ));
            if (triesLeft <= 0)
                game.switchToLoginScreen();
        }
    }

    // Player with same name already confirmed email or does not exist
    public static final class DoesNotExist extends EmailConfirmationResult {
        public DoesNotExist() {}

        public static DoesNotExist fromJson(JsonObject object) {
            return new DoesNotExist();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Player with same name already confirmed email or does not exist");
        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends EmailConfirmationResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot confirm email: JSON request is not well-formed. Probable bug. Tell the developers");
        }
    }

    // Value of field is incorrect
    public static final class IncorrectData extends EmailConfirmationResult {
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
            game.getExternalAPI().oSAPI.showMessage("Cannot confirm email: value of field in request is not valid. Probable bug. Tell the developers");
        }
    }

    @Override
    public GameScreenMode screenModeAfterApply() {
        return null;
    }

    // Creates EmailConfirmationResult from JSON
    public static EmailConfirmationResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("EmailConfirmed"))
            return EmailConfirmed.fromJson(object);
        else if (type.equals("WrongCode"))
            return WrongCode.fromJson(object);
        else if (type.equals("DoesNotExist"))
            return DoesNotExist.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        else if (type.equals("IncorrectData"))
            return IncorrectData.fromJson(object);
        return null;
    }

    // Matches EmailConfirmationResult
    public void match(
            MatchBranch<EmailConfirmed> emailConfirmed,
            MatchBranch<WrongCode> wrongCode,
            MatchBranch<DoesNotExist> doesNotExist,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (emailConfirmed != null && this instanceof EmailConfirmed)
            emailConfirmed.onMatch((EmailConfirmed) this);
        else if (wrongCode != null && this instanceof WrongCode)
            wrongCode.onMatch((WrongCode) this);
        else if (doesNotExist != null && this instanceof DoesNotExist)
            doesNotExist.onMatch((DoesNotExist) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}
