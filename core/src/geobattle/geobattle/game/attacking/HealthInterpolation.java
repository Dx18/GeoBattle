package geobattle.geobattle.game.attacking;

// Interpolation of health
public final class HealthInterpolation {
    // Health at first time point
    public final double health1;

    // Time of first time point
    public final double time1;

    // Health at second time point
    public final double health2;

    // Time of second time point
    public final double time2;

    public HealthInterpolation(double health1, double time1, double health2, double time2) {
        this.health1 = health1;
        this.time1 = time1;
        this.health2 = health2;
        this.time2 = time2;
    }

    // Returns health at moment of time
    public double getHealth(double time) {
        if (time < time1)
            return health1;
        if (time > time2)
            return health2;

        double factor = (time - time1) / (time2 - time1);

        return health1 + factor * (health2 - health1);
    }
}
