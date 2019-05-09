package geobattle.geobattle.game.research;

import geobattle.geobattle.game.buildings.ResearchCenter;

public enum ResearchType {
    TURRET_DAMAGE(
            "TurretDamage",
            new int[]   {    1000, 2000, 4000, 8000, 16000 },
            new float[] { 4, 5,    6,    7,    8,    9     }
    ),
    UNIT_DAMAGE(
            "UnitDamage",
            new int[]   {     1000, 2000, 4000, 8000, 16000 },
            new float[] { 12, 13,   14,   15,   16,   17    }
    ),
    GENERATOR_EFFICIENCY(
            "GeneratorEfficiency",
            new int[]   {     1000, 2000, 4000, 8000, 16000 },
            new float[] { 30, 31,   32,   33,   34,   35    }
    );

    // Name of research type used in toString()
    public final String name;

    // Costs of research
    private final int[] costs;

    // Values of research
    private final float[] values;

    // Creates ResearchType
    // Throws IllegalArgumentException if costs.length + 1 != values.length
    ResearchType(String name, int[] costs, float[] values) {
        if (costs.length + 1 != values.length)
            throw new IllegalArgumentException("Invalid length of costs and values in ResearchType");
        this.name = name;
        this.costs = costs;
        this.values = values;
    }

    // Returns number of levels in research type
    public int getLevelCount() {
        return costs.length;
    }

    // Returns cost of research level
    public int getCost(int level) {
        if (level <= 0)
            return 0;
        if (level > costs.length)
            return Integer.MAX_VALUE;
        return costs[level - 1];
    }

    // Returns value of research level
    public float getValue(int level) {
        return values[level];
    }

    // Returns name of research type
    @Override
    public String toString() {
        return name;
    }
}
