package geobattle.geobattle.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;
import geobattle.geobattle.server.AuthInfo;

// Result of authorization
public abstract class AuthorizationResult implements ActionResult {
    // Successfully authorized
    public static final class Success extends AuthorizationResult {
        // Auth info
        public final AuthInfo authInfo;

        public Success(AuthInfo authInfo) {
            this.authInfo = authInfo;
        }

        public static Success fromJson(JsonObject object) {
            AuthInfo authInfo = AuthInfo.fromJson(object.getAsJsonObject("authInfo"));
            return new Success(authInfo);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.onAuthInfoObtained(authInfo);
        }
    }

    // Pair name-password not found
    public static final class PairNotFound extends AuthorizationResult {
        public PairNotFound() {}

        public static PairNotFound fromJson(JsonObject object) {
            return new PairNotFound();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.showMessage(game.getI18NBundle().get("authorizationResultPairNotFound"));
        }
    }

    // Version of client is invalid
    public static final class InvalidVersion extends AuthorizationResult {
        // Min version
        public final String min;

        // Max version
        public final String max;

        public InvalidVersion(String min, String max) {
            this.min = min;
            this.max = max;
        }

        public static InvalidVersion fromJson(JsonObject object) {
            String min = object.getAsJsonPrimitive("min").getAsString();
            String max = object.getAsJsonPrimitive("max").getAsString();
            return new InvalidVersion(min, max);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.showMessage(game.getI18NBundle().format("invalidVersion", min, max));
        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends AuthorizationResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot login: JSON request is not well-formed. Probable bug. Tell the developers");
        }
    }

    // Value of field is not valid
    public static final class IncorrectData extends AuthorizationResult {
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
            game.getExternalAPI().oSAPI.showMessage("Cannot login: value of field in request is not valid. Probable bug. Tell the developers");
        }
    }

    @Override
    public GameScreenMode screenModeAfterApply() {
        return null;
    }

    // Creates AuthorizationResult from JSON
    public static AuthorizationResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("Success"))
            return Success.fromJson(object);
        else if (type.equals("PairNotFound"))
            return PairNotFound.fromJson(object);
        else if (type.equals("InvalidVersion"))
            return InvalidVersion.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        else if (type.equals("IncorrectData"))
            return IncorrectData.fromJson(object);
        return null;
    }

    // Matches AuthorizationResult
    public void match(
            MatchBranch<Success> success,
            MatchBranch<PairNotFound> pairNotFound,
            MatchBranch<InvalidVersion> invalidVersion,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (success != null && this instanceof Success)
            success.onMatch((Success) this);
        else if (pairNotFound != null && this instanceof PairNotFound)
            pairNotFound.onMatch((PairNotFound) this);
        else if (invalidVersion != null && this instanceof InvalidVersion)
            invalidVersion.onMatch((InvalidVersion) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}
