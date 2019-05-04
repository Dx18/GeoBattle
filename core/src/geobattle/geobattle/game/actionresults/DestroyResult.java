package geobattle.geobattle.game.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.BuildTransactionInfo;

// Result of building destroying
public abstract class DestroyResult {
    // Building is successfully destroyed
    public static final class Destroyed extends DestroyResult {
        // Info about destroyed building
        public final BuildTransactionInfo info;

        public Destroyed(BuildTransactionInfo info) {
            this.info = info;
        }

        public static Destroyed fromJson(JsonObject object) {
            BuildTransactionInfo info = BuildTransactionInfo.fromJson(object.getAsJsonObject("info"));
            return new Destroyed(info);
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends DestroyResult {
        public WrongAuthInfo() {}

        public static WrongAuthInfo fromJson(JsonObject object) {
            return new WrongAuthInfo();
        }
    }

    // Creates DestroyResult from JSON
    public static DestroyResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("Destroyed"))
            return Destroyed.fromJson(object);
        else if (type.equals("WrongAuthInfo"))
            return WrongAuthInfo.fromJson(object);
        return null;
    }

    // Matches DestroyResult
    public void match(
            MatchBranch<Destroyed> destroyed,
            MatchBranch<WrongAuthInfo> wrongAuthInfo
    ) {
        if (destroyed != null && this instanceof Destroyed)
            destroyed.onMatch((Destroyed) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
    }
}