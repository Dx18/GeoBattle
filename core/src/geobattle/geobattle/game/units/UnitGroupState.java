package geobattle.geobattle.game.units;

import java.util.ArrayList;
import java.util.Iterator;

import geobattle.geobattle.actionresults.MatchBranch;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;

public abstract class UnitGroupState {
    // State when units stay in hangar
    public static final class Idle extends UnitGroupState {
        // Hangar where units stay
        public final Hangar hangar;

        public Idle(Hangar hangar) {
            this.hangar = hangar;
        }
    }

    // State when units are moving to specific point by specific time
    public static final class Moving extends UnitGroupState {
        // X coordinate of start
        public final double x1;

        // Y coordinate of start
        public final double y1;

        // Time when unit group must start arriving
        public final double time1;

        // X coordinate of end
        public final double x2;

        // Y coordinate of end
        public final double y2;

        // Time when unit group must arrive
        public final double time2;

        public Moving(double x1, double y1, double time1, double x2, double y2, double time2) {
            this.x1 = x1;
            this.y1 = y1;
            this.time1 = time1;
            this.x2 = x2;
            this.y2 = y2;
            this.time2 = time2;
        }
    }

    // State when units are attacking sector
    public static final class Attacking extends UnitGroupState {
        // Sector units attacking
        public final Sector sector;

        // All buildings
        public final Building[] allBuildings;

        // Buildings attacked by units
        public final Building[] attackedBuildings;

        public Attacking(Sector sector) {
            this.sector = sector;
            this.attackedBuildings = new Building[4];

            ArrayList<Building> allBuildings = new ArrayList<Building>();

            if (sector != null) {
                Iterator<Building> buildings = sector.getAllBuildings();
                while (buildings.hasNext()) {
                    Building next = buildings.next();
                    allBuildings.add(next);
                }
            }

            this.allBuildings = allBuildings.toArray(new Building[0]);
        }
    }

    // Matches UnitGroupState
    public void match(
            MatchBranch<Idle> idle,
            MatchBranch<Moving> moving,
            MatchBranch<Attacking> attacking
    ) {
        if (idle != null && this instanceof Idle)
            idle.onMatch((Idle) this);
        else if (moving != null && this instanceof Moving)
            moving.onMatch((Moving) this);
        else if (attacking != null && this instanceof Attacking)
            attacking.onMatch((Attacking) this);
    }
}
