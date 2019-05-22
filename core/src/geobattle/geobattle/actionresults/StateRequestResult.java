package geobattle.geobattle.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;

// Result of game state request
public abstract class StateRequestResult implements ActionResult {
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

        @Override
        public void apply(GeoBattle game, GameState gameState) {

        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends StateRequestResult {
        public WrongAuthInfo() {}

        public static WrongAuthInfo fromJson(JsonObject object) {
            return new WrongAuthInfo();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {

        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends StateRequestResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {

        }
    }

    // Creates StateRequestResult from JSON
    public static StateRequestResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("StateRequestSuccess"))
            return StateRequestSuccess.fromJson(object);
        else if (type.equals("WrongAuthInfo"))
            return WrongAuthInfo.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        return null;
    }

    @Override
    public GameScreenMode screenModeAfterApply() {
        return null;
    }

    // Matches StateRequestResult
    public void match(
            MatchBranch<StateRequestSuccess> stateRequestSuccess,
            MatchBranch<WrongAuthInfo> wrongAuthInfo,
            MatchBranch<MalformedJson> malformedJson
    ) {
        if (stateRequestSuccess != null && this instanceof StateRequestSuccess)
            stateRequestSuccess.onMatch((StateRequestSuccess) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
    }
}