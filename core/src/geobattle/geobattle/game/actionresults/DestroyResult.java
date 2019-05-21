package geobattle.geobattle.game.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.BuildTransactionInfo;

// Result of building destroying
public abstract class DestroyResult {
    // Building is successfully destroyed
    public static final class BuildingDestroyed extends DestroyResult {
        // Info about destroyed building
        public final BuildTransactionInfo info;

        public BuildingDestroyed(BuildTransactionInfo info) {
            this.info = info;
        }

        public static BuildingDestroyed fromJson(JsonObject object) {
            BuildTransactionInfo info = BuildTransactionInfo.fromJson(object.getAsJsonObject("info"));
            return new BuildingDestroyed(info);
        }
    }

    // Player does not own building he wants to destroy
    public static final class NotOwningBuilding extends DestroyResult {
        public NotOwningBuilding() {}

        public static NotOwningBuilding fromJson(JsonObject object) {
            return new NotOwningBuilding();
        }
    }

    // Cannot destroy building because sector is blocked
    public static final class SectorBlocked extends DestroyResult {
        public SectorBlocked() {}

        public static SectorBlocked fromJson(JsonObject object) {
            return new SectorBlocked();
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends DestroyResult {
        public WrongAuthInfo() {}

        public static WrongAuthInfo fromJson(JsonObject object) {
            return new WrongAuthInfo();
        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends DestroyResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }
    }

    // Value of field is not valid
    public static final class IncorrectData extends DestroyResult {
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

    // Creates DestroyResult from JSON
    public static DestroyResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("BuildingDestroyed"))
            return BuildingDestroyed.fromJson(object);
        else if (type.equals("NotOwningBuilding"))
            return NotOwningBuilding.fromJson(object);
        else if (type.equals("SectorBlocked"))
            return SectorBlocked.fromJson(object);
        else if (type.equals("WrongAuthInfo"))
            return WrongAuthInfo.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        else if (type.equals("IncorrectData"))
            return IncorrectData.fromJson(object);
        return null;
    }

    // Matches DestroyResult
    public void match(
            MatchBranch<BuildingDestroyed> destroyed,
            MatchBranch<NotOwningBuilding> notOwningBuilding,
            MatchBranch<SectorBlocked> sectorBlocked,
            MatchBranch<WrongAuthInfo> wrongAuthInfo,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (destroyed != null && this instanceof BuildingDestroyed)
            destroyed.onMatch((BuildingDestroyed) this);
        else if (notOwningBuilding != null && this instanceof NotOwningBuilding)
            notOwningBuilding.onMatch((NotOwningBuilding) this);
        else if (sectorBlocked != null && this instanceof SectorBlocked)
            sectorBlocked.onMatch((SectorBlocked) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}