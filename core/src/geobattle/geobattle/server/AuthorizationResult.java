package geobattle.geobattle.server;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.actionresults.MatchBranch;

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

    // Creates AuthorizationResult from JSON
    public static AuthorizationResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("Success"))
            return Success.fromJson(object);
        else if (type.equals("PairNotFound"))
            return PairNotFound.fromJson(object);
        return null;
    }

    // Matches AuthorizationResult
    public void match(
            MatchBranch<Success> success,
            MatchBranch<PairNotFound> pairNotFound
    ) {
        if (success != null && this instanceof Success)
            success.onMatch((Success) this);
        else if (pairNotFound != null && this instanceof PairNotFound)
            pairNotFound.onMatch((PairNotFound) this);
    }
}
