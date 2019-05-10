package geobattle.geobattle.game.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.SectorTransactionInfo;

// Result of sector building
public abstract class SectorBuildResult {
    // Sector successfully built
    public static final class SectorBuilt extends SectorBuildResult {
        // Info about sector
        public final SectorTransactionInfo info;

        public SectorBuilt(SectorTransactionInfo info) {
            this.info = info;
        }

        public static SectorBuilt fromJson(JsonObject object) {
            SectorTransactionInfo info = SectorTransactionInfo.fromJson(object.getAsJsonObject("info"));
            return new SectorBuilt(info);
        }
    }

    // Cannot build because amount of resources is too low
    public static final class NotEnoughResources extends SectorBuildResult {
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

    // Cannot build because sector player wants to build intersects with other player's sector
    public static final class IntersectsWithEnemy extends SectorBuildResult {
        // Index of enemy which owns nearby sector
        public final int enemyIndex;

        public IntersectsWithEnemy(int enemyIndex) {
            this.enemyIndex = enemyIndex;
        }

        public static IntersectsWithEnemy fromJson(JsonObject object) {
            int enemyIndex = object.getAsJsonPrimitive("enemyIndex").getAsInt();
            return new IntersectsWithEnemy(enemyIndex);
        }
    }

    // Sector is not aligned or it's not attached to other sector
    public static final class WrongPosition extends SectorBuildResult {
        public WrongPosition() {}

        public static WrongPosition fromJson(JsonObject object) {
            return new WrongPosition();
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends SectorBuildResult {
        public WrongAuthInfo() {}

        public static WrongAuthInfo fromJson(JsonObject object) {
            return new WrongAuthInfo();
        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends SectorBuildResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }
    }

    // Value of field is not valid
    public static final class IncorrectData extends SectorBuildResult {
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

    // Creates SectorBuildResult from JSON
    public static SectorBuildResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("SectorBuilt"))
            return SectorBuilt.fromJson(object);
        else if (type.equals("NotEnoughResources"))
            return NotEnoughResources.fromJson(object);
        else if (type.equals("IntersectsWithEnemy"))
            return IntersectsWithEnemy.fromJson(object);
        else if (type.equals("WrongPosition"))
            return WrongPosition.fromJson(object);
        else if (type.equals("WrongAuthInfo"))
            return WrongAuthInfo.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        else if (type.equals("IncorrectData"))
            return IncorrectData.fromJson(object);
        return null;
    }

    // Matches SectorBuildResult
    public void match(
            MatchBranch<SectorBuilt> sectorBuilt,
            MatchBranch<NotEnoughResources> notEnoughResources,
            MatchBranch<IntersectsWithEnemy> intersectsWithEnemy,
            MatchBranch<WrongPosition> wrongPosition,
            MatchBranch<WrongAuthInfo> wrongAuthInfo,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (sectorBuilt != null && this instanceof SectorBuilt)
            sectorBuilt.onMatch((SectorBuilt) this);
        else if (notEnoughResources != null && this instanceof NotEnoughResources)
            notEnoughResources.onMatch((NotEnoughResources) this);
        else if (intersectsWithEnemy != null && this instanceof IntersectsWithEnemy)
            intersectsWithEnemy.onMatch((IntersectsWithEnemy) this);
        else if (wrongPosition != null && this instanceof WrongPosition)
            wrongPosition.onMatch((WrongPosition) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}
