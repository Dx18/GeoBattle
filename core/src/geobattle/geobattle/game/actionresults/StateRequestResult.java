package geobattle.geobattle.game.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.GameState;

// Result of game state request
public abstract class StateRequestResult {
    // Successfully got game state
    public static final class StateRequestSuccess extends StateRequestResult {
        // Game state
        public final GameState gameState;

        public StateRequestSuccess(GameState gameState) {
            this.gameState = gameState;
        }

        public static StateRequestSuccess fromJson(JsonObject object) {
            GameState gameState = GameState.fromJson(object.getAsJsonObject("gameState"));
            return new StateRequestSuccess(gameState);
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends StateRequestResult {
        public WrongAuthInfo() {}

        public static WrongAuthInfo fromJson(JsonObject object) {
            return new WrongAuthInfo();
        }
    }

    // Creates StateRequestResult from JSON
    public static StateRequestResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("StateRequestSuccess"))
            return StateRequestSuccess.fromJson(object);
        else if (type.equals("WrongAuthInfo"))
            return WrongAuthInfo.fromJson(object);
        return null;
    }

    // Matches StateRequestResult
    public void match(
            MatchBranch<StateRequestSuccess> stateRequestSuccess,
            MatchBranch<WrongAuthInfo> wrongAuthInfo
    ) {
        if (stateRequestSuccess != null && this instanceof StateRequestSuccess)
            stateRequestSuccess.onMatch((StateRequestSuccess) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
    }
}
