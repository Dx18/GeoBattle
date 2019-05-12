package geobattle.geobattle.game.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.UnitTransactionInfo;

// Result of unit building
public abstract class UnitBuildResult {
    // Unit successfully built
    public static final class Built extends UnitBuildResult {
        // Info about built unit
        public final UnitTransactionInfo info;

        // Cost of unit
        public final int cost;

        public Built(UnitTransactionInfo info, int cost) {
            this.info = info;
            this.cost = cost;
        }

        public static Built fromJson(JsonObject object) {
            UnitTransactionInfo info = UnitTransactionInfo.fromJson(object.getAsJsonObject("info"));
            int cost = object.getAsJsonPrimitive("cost").getAsInt();
            return new Built(info, cost);
        }
    }

    // Not enough resources to build unit
    public static final class NotEnoughResources extends UnitBuildResult {
        // Required amount of resources
        public final int required;

        public NotEnoughResources(int required) {
            this.required = required;
        }

        public static NotEnoughResources fromJson(JsonObject object) {
            int required = object.getAsJsonPrimitive("required").getAsInt();
            return new NotEnoughResources(required);
        }
    }

    // Cannot build unit because there is no place in hangar
    public static final class NoPlaceInHangar extends UnitBuildResult {
        public NoPlaceInHangar() {}

        public static NoPlaceInHangar fromJson(JsonObject object) {
            return new NoPlaceInHangar();
        }
    }

    // Cannot build unit because sector is blocked
    public static final class SectorBlocked extends UnitBuildResult {
        public SectorBlocked() {}

        public static SectorBlocked fromJson(JsonObject object) {
            return new SectorBlocked();
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends UnitBuildResult {
        public WrongAuthInfo() {}

        public static WrongAuthInfo fromJson(JsonObject object) {
            return new WrongAuthInfo();
        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends UnitBuildResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }
    }

    // Value of field is not valid
    public static final class IncorrectData extends UnitBuildResult {
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

    // Creates UnitBuildResult from JSON
    public static UnitBuildResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("Built"))
            return Built.fromJson(object);
        else if (type.equals("NotEnoughResources"))
            return NotEnoughResources.fromJson(object);
        else if (type.equals("NoPlaceInHangar"))
            return NoPlaceInHangar.fromJson(object);
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

    // Matches UnitBuildResult
    public void match(
            MatchBranch<Built> built,
            MatchBranch<NotEnoughResources> notEnoughResources,
            MatchBranch<NoPlaceInHangar> noPlaceInHangar,
            MatchBranch<SectorBlocked> sectorBlocked,
            MatchBranch<WrongAuthInfo> wrongAuthInfo,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (built != null && this instanceof Built)
            built.onMatch((Built) this);
        else if (notEnoughResources != null && this instanceof NotEnoughResources)
            notEnoughResources.onMatch((NotEnoughResources) this);
        else if (noPlaceInHangar != null && this instanceof NoPlaceInHangar)
            noPlaceInHangar.onMatch((NoPlaceInHangar) this);
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
