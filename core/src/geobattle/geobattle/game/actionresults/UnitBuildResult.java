package geobattle.geobattle.game.actionresults;

import geobattle.geobattle.game.UnitTransactionInfo;

// Result of unit building
public abstract class UnitBuildResult {
    // Unit successfully built
    public static final class Built extends UnitBuildResult {
        // Unit built by player
        public final UnitTransactionInfo info;

        // Cost of unit
        public final int cost;

        public Built(UnitTransactionInfo info, int cost) {
            this.info = info;
            this.cost = cost;
        }
    }

    // Not enough resources
    public static final class NotEnoughResources extends UnitBuildResult {
        // Required amount of resources
        public final int required;

        public NotEnoughResources(int required) {
            this.required = required;
        }
    }

    // Building is not a hangar
    public static final class NotHangar extends UnitBuildResult {
        public NotHangar() {}
    }

    // No place in hangar left (max: 4 units)
    public static final class NoPlaceInHangar extends UnitBuildResult {
        public NoPlaceInHangar() {}
    }

    // Hangar does not exist by the moment player started building unit
    public static final class DoesNotExist extends UnitBuildResult {
        public DoesNotExist() {}
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends UnitBuildResult {
        public WrongAuthInfo() {}
    }

    // Matches UserBuildResult
    public final void match(
            MatchBranch<Built> built,
            MatchBranch<NotEnoughResources> notEnoughResources,
            MatchBranch<NotHangar> notHangar,
            MatchBranch<NoPlaceInHangar> noPlaceInHangar,
            MatchBranch<DoesNotExist> doesNotExist,
            MatchBranch<WrongAuthInfo> wrongAuthInfo
    ) {
        if (built != null && this instanceof Built)
            built.onMatch((Built) this);
        else if (notEnoughResources != null && this instanceof NotEnoughResources)
            notEnoughResources.onMatch((NotEnoughResources) this);
        else if (notHangar != null && this instanceof NotHangar)
            notHangar.onMatch((NotHangar) this);
        else if (noPlaceInHangar != null && this instanceof NoPlaceInHangar)
            noPlaceInHangar.onMatch((NoPlaceInHangar) this);
        else if (doesNotExist != null && this instanceof DoesNotExist)
            doesNotExist.onMatch((DoesNotExist) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
    }
}
