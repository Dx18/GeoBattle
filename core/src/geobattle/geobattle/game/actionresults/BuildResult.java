package geobattle.geobattle.game.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.BuildTransactionInfo;

// Result of building
public abstract class BuildResult {
    // Building is successfully built
    public static final class Built extends BuildResult {
        // Building instance
        public final BuildTransactionInfo info;

        // Cost of building
        public final int cost;

        public Built(BuildTransactionInfo info, int cost) {
            this.info = info;
            this.cost = cost;
        }

        public static Built fromJson(JsonObject object) {
            BuildTransactionInfo info = BuildTransactionInfo.fromJson(object.getAsJsonObject("info"));
            int cost = object.getAsJsonPrimitive("cost").getAsInt();
            return new Built(info, cost);
        }
    }

    // Cannot build because building collides with other buildings
    public static final class CollisionFound extends BuildResult {
        public CollisionFound() {}

        public static CollisionFound fromJson(JsonObject object) {
            return new CollisionFound();
        }
    }

    // Cannot build because limit of buildings is exceeded
    public static final class NotEnoughResources extends BuildResult {
        // Max number of buildings
        public final int max;

        public NotEnoughResources(int max) {
            this.max = max;
        }

        public static NotEnoughResources fromJson(JsonObject object) {
            int max = object.getAsJsonPrimitive("max").getAsInt();
            return new NotEnoughResources(max);
        }
    }

    // Cannot build because building is not in player's territory
    public static final class NotInTerritory extends BuildResult {
        public NotInTerritory() {}

        public static NotInTerritory fromJson(JsonObject object) {
            return new NotInTerritory();
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends BuildResult {
        public WrongAuthInfo() {}

        public static WrongAuthInfo fromJson(JsonObject object) {
            return new WrongAuthInfo();
        }
    }

    // Creates BuildResult from JSON
    public static BuildResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("Built"))
            return Built.fromJson(object);
        else if (type.equals("CollisionFound"))
            return CollisionFound.fromJson(object);
        else if (type.equals("NotEnoughResources"))
            return NotEnoughResources.fromJson(object);
        else if (type.equals("NotInTerritory"))
            return NotInTerritory.fromJson(object);
        else if (type.equals("WrongAuthInfo"))
            return WrongAuthInfo.fromJson(object);
        return null;
    }

    // Matches BuildResult
    public void match(
            MatchBranch<Built> built,
            MatchBranch<CollisionFound> collisionFound,
            MatchBranch<NotEnoughResources> notEnoughResources,
            MatchBranch<NotInTerritory> notInTerritory,
            MatchBranch<WrongAuthInfo> wrongAuthInfo
    ) {
        if (built != null && this instanceof Built)
            built.onMatch((Built) this);
        else if (collisionFound != null && this instanceof CollisionFound)
            collisionFound.onMatch((CollisionFound) this);
        else if (notEnoughResources != null && this instanceof NotEnoughResources)
            notEnoughResources.onMatch((NotEnoughResources) this);
        else if (notInTerritory != null && this instanceof NotInTerritory)
            notInTerritory.onMatch((NotInTerritory) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
    }
}