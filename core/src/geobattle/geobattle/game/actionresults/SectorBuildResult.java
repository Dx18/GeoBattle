package geobattle.geobattle.game.actionresults;

import geobattle.geobattle.game.SectorTransactionInfo;

// Result of sector building
public abstract class SectorBuildResult {
    // Player successfully taken sector
    public static final class SectorTaken extends SectorBuildResult {
        // Info about sector
        public final SectorTransactionInfo info;

        public SectorTaken(SectorTransactionInfo info) {
            this.info = info;
        }
    }

    // New sector intersects with enemy's sector
    public static final class IntersectsWithEnemySector extends SectorBuildResult {
        public IntersectsWithEnemySector() {}
    }

    // Not enough resources for sector building
    public static final class NotEnoughResources extends SectorBuildResult {
        // Required amount of resources
        public final int required;

        public NotEnoughResources(int required) {
            this.required = required;
        }
    }

    // Wrong position of sector (not neighbour / not aligned)
    public static final class WrongPosition extends SectorBuildResult {
        public WrongPosition() {}
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends SectorBuildResult {
        public WrongAuthInfo() {}
    }

    // Matches SectorBuildResult
    public final void match(
            MatchBranch<SectorTaken> sectorTaken,
            MatchBranch<IntersectsWithEnemySector> intersectsWithEnemySector,
            MatchBranch<NotEnoughResources> notEnoughResources,
            MatchBranch<WrongPosition> wrongPosition,
            MatchBranch<WrongAuthInfo> wrongAuthInfo
    ) {
        if (sectorTaken != null && this instanceof SectorTaken)
            sectorTaken.onMatch((SectorTaken) this);
        else if (notEnoughResources != null && this instanceof NotEnoughResources)
            notEnoughResources.onMatch((NotEnoughResources) this);
        else if (intersectsWithEnemySector != null && this instanceof IntersectsWithEnemySector)
            intersectsWithEnemySector.onMatch((IntersectsWithEnemySector) this);
        else if (wrongPosition != null && this instanceof WrongPosition)
            wrongPosition.onMatch((WrongPosition) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
    }
}
