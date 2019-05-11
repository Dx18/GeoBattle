package geobattle.geobattle.server.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.actionresults.MatchBranch;
import geobattle.geobattle.server.AuthInfo;

// Result of authorization
public abstract class AuthorizationResult {
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
    }

    // Pair name-password not found
    public static final class PairNotFound extends AuthorizationResult {
        public PairNotFound() {}

        public static PairNotFound fromJson(JsonObject object) {
            return new PairNotFound();
        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends AuthorizationResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
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
    }

    // Creates AuthorizationResult from JSON
    public static AuthorizationResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("Success"))
            return Success.fromJson(object);
        else if (type.equals("PairNotFound"))
            return PairNotFound.fromJson(object);
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
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (success != null && this instanceof Success)
            success.onMatch((Success) this);
        else if (pairNotFound != null && this instanceof PairNotFound)
            pairNotFound.onMatch((PairNotFound) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}
