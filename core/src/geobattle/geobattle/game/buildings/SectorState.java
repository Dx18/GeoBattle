package geobattle.geobattle.game.buildings;

import java.util.ArrayList;

import geobattle.geobattle.actionresults.MatchBranch;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.game.units.UnitGroup;

// State of sector
public abstract class SectorState {
    // Normal state of sector
    public static final class Normal extends SectorState {
        public Normal() {}
    }

    // Sector is attacked by other player
    public static final class Attacked extends SectorState {
        // Units attacking this sector
        public final ArrayList<UnitGroup> units;

        // Units attacked by turrets
        public final Unit[] attackedUnits;

        // Time left before switching to other unit
        public final double[] timeLeft;

        public Attacked(ArrayList<UnitGroup> units, Unit[] attackedUnits) {
            this.units = units;
            this.attackedUnits = attackedUnits;
            this.timeLeft = new double[attackedUnits.length];
        }
    }

    // Matches SectorState
    public void match(
            MatchBranch<Normal> normal,
            MatchBranch<Attacked> attacked
    ) {
        if (normal != null && this instanceof Normal)
            normal.onMatch((Normal) this);
        else if (attacked != null && this instanceof Attacked)
            attacked.onMatch((Attacked) this);
    }
}