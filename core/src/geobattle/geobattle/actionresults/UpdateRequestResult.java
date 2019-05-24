package geobattle.geobattle.actionresults;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import geobattle.geobattle.game.GameStateUpdate;
import geobattle.geobattle.game.research.ResearchInfo;

// Result of update request
public abstract class UpdateRequestResult {
    // Successfully got an update
    public static final class UpdateRequestSuccess extends UpdateRequestResult {
        // New amount of resources
        public final int resources;

        // Time on server
        public final double time;

        // Info about research
        public final ResearchInfo researchInfo;

        // Updates
        public final GameStateUpdate[] updates;

        public UpdateRequestSuccess(int resources, double time, ResearchInfo researchInfo, GameStateUpdate[] updates) {
            this.resources = resources;
            this.time = time;
            this.researchInfo = researchInfo;
            this.updates = updates;
        }

        private static GameStateUpdate gameStateUpdateFromJson(JsonObject object) {
            BuildResult buildResult = BuildResult.fromJson(object);
            if (buildResult instanceof GameStateUpdate)
                return (GameStateUpdate) buildResult;

            DestroyResult destroyResult = DestroyResult.fromJson(object);
            if (destroyResult instanceof GameStateUpdate)
                return (GameStateUpdate) destroyResult;

            SectorBuildResult sectorBuildResult = SectorBuildResult.fromJson(object);
            if (sectorBuildResult instanceof GameStateUpdate)
                return (GameStateUpdate) sectorBuildResult;

            UnitBuildResult unitBuildResult = UnitBuildResult.fromJson(object);
            if (unitBuildResult instanceof GameStateUpdate)
                return (GameStateUpdate) unitBuildResult;

            AttackResult attackResult = AttackResult.fromJson(object);
            if (attackResult instanceof GameStateUpdate)
                return (GameStateUpdate) attackResult;

            String type = object.getAsJsonPrimitive("type").getAsString();
            if (type.equals("PlayerAdded"))
                return PlayerAdded.fromJson(object);
            if (type.equals("PlayerRemoved"))
                return PlayerRemoved.fromJson(object);

            return null;
        }

        public static UpdateRequestSuccess fromJson(JsonObject object) {
            int resources = object.getAsJsonPrimitive("resources").getAsInt();
            double time = object.getAsJsonPrimitive("time").getAsDouble();
            ResearchInfo researchInfo = ResearchInfo.fromJson(object.getAsJsonObject("researchInfo"));

            JsonArray updatesJson = object.getAsJsonArray("updates");
            GameStateUpdate[] updates = new GameStateUpdate[updatesJson.size()];
            for (int i = 0; i < updates.length; i++)
                updates[i] = gameStateUpdateFromJson(updatesJson.get(i).getAsJsonObject());

            return new UpdateRequestSuccess(resources, time, researchInfo, updates);
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends UpdateRequestResult {
        public WrongAuthInfo() {}

        public static WrongAuthInfo fromJson(JsonObject object) {
            return new WrongAuthInfo();
        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends UpdateRequestResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }
    }

    // Value of field is not valid
    public static final class IncorrectData extends UpdateRequestResult {
        // Name of invalid field
        public final String field;

        public IncorrectData(String field) {
            this.field = field;
        }

        public static IncorrectData fromJson(JsonObject object) {
            String field = object.getAsJsonPrimitive("field").getAsString();
            return new IncorrectData(field);
        }
    }

    // Creates UpdateRequestResult from JSON
    public static UpdateRequestResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("UpdateRequestSuccess"))
            return UpdateRequestSuccess.fromJson(object);
        else if (type.equals("WrongAuthInfo"))
            return WrongAuthInfo.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        else if (type.equals("IncorrectData"))
            return IncorrectData.fromJson(object);
        return null;
    }

    // Matches UpdateRequestResult
    public void match(
            MatchBranch<UpdateRequestSuccess> updateRequestSuccess,
            MatchBranch<WrongAuthInfo> wrongAuthInfo,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (updateRequestSuccess != null && this instanceof UpdateRequestSuccess)
            updateRequestSuccess.onMatch((UpdateRequestSuccess) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}
