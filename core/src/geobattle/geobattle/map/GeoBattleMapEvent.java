package geobattle.geobattle.map;

import com.google.gson.JsonObject;

import geobattle.geobattle.actionresults.MatchBranch;

// Event of GeoBattle map
public abstract class GeoBattleMapEvent {
    // Bomb is dropped by unit
    public static final class BombDropped extends GeoBattleMapEvent {
        // X coordinate of bomb
        public final double x;

        // Y coordinate of bomb
        public final double y;

        public BombDropped(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    // Matches GeoBattleMapEvent
    public void match(
            MatchBranch<BombDropped> bombDropped
    ) {
        if (bombDropped != null && this instanceof BombDropped)
            bombDropped.onMatch((BombDropped) this);
    }
}
