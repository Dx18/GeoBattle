package geobattle.geobattle.game.attacking;

import com.badlogic.gdx.utils.IntFloatMap;

// Time point
public final class TimePoint {
    // Time
    public final double time;

    // Health of sector in moment of time
    public final double sectorHealth;

    // Info about units' health
    public final IntFloatMap unitGroupHealth;

    public TimePoint(double time, double sectorHealth, IntFloatMap unitGroupHealth) {
        this.time = time;
        this.sectorHealth = sectorHealth;
        this.unitGroupHealth = unitGroupHealth;
    }

    // Clones TimePoint
    public TimePoint clone() {
        return new TimePoint(time, sectorHealth, new IntFloatMap(unitGroupHealth));
    }
}
