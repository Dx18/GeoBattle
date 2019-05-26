package geobattle.geobattle.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.research.ResearchType;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;

// Result of research
public abstract class ResearchResult implements ActionResult {
    // Research successfully researched
    public static final class Researched extends ResearchResult {
        // Type of research
        public final String researchType;

        // Cost of research
        public final int cost;

        public Researched(String researchType, int cost) {
            this.researchType = researchType;
            this.cost = cost;
        }

        public static Researched fromJson(JsonObject object) {
            String researchType = object.getAsJsonPrimitive("researchType").getAsString();
            int cost = object.getAsJsonPrimitive("cost").getAsInt();
            return new Researched(researchType, cost);
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            gameState.getCurrentPlayer().getResearchInfo().incrementLevel(ResearchType.from(researchType));
        }
    }

    // Not enough resources for research
    public static final class NotEnoughResources extends ResearchResult {
        // Amount of resources required for research
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
            game.showMessage(game.getI18NBundle().format("researchResultNotEnoughResources", required));
        }
    }

    // Max level of research is already reached
    public static final class MaxLevel extends ResearchResult {
        public MaxLevel() {}

        public static MaxLevel fromJson(JsonObject object) {
            return new MaxLevel();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.showMessage(game.getI18NBundle().get("researchResultMaxLevel"));
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends ResearchResult {
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

    // Wrong format of JSON sent to server
    public static final class MalformedJson extends ResearchResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }

        @Override
        public void apply(GeoBattle game, GameState gameState) {
            game.getExternalAPI().oSAPI.showMessage("Cannot build: malformed JSON");
        }
    }

    // JSON is valid but request data is incorrect
    public static final class IncorrectData extends ResearchResult {
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
            game.getExternalAPI().oSAPI.showMessage("Cannot build: incorrect data");
        }
    }

    @Override
    public GameScreenMode screenModeAfterApply() {
        return null;
    }

    // Creates ResearchResult from JSON
    public static ResearchResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("Researched"))
            return Researched.fromJson(object);
        else if (type.equals("NotEnoughResources"))
            return NotEnoughResources.fromJson(object);
        else if (type.equals("MaxLevel"))
            return MaxLevel.fromJson(object);
        else if (type.equals("WrongAuthInfo"))
            return WrongAuthInfo.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        else if (type.equals("IncorrectData"))
            return IncorrectData.fromJson(object);
        return null;
    }

    // Matches ResearchResult
    public void match(
            MatchBranch<Researched> researched,
            MatchBranch<NotEnoughResources> notEnoughResources,
            MatchBranch<MaxLevel> maxLevel,
            MatchBranch<WrongAuthInfo> wrongAuthInfo,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (researched != null && this instanceof Researched)
            researched.onMatch((Researched) this);
        else if (notEnoughResources != null && this instanceof NotEnoughResources)
            notEnoughResources.onMatch((NotEnoughResources) this);
        else if (maxLevel != null && this instanceof MaxLevel)
            maxLevel.onMatch((MaxLevel) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}
