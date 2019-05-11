package geobattle.geobattle.game.buildings;

// Type of building
public enum BuildingType {
    RESEARCH_CENTER ("ResearchCenter", 6, 5, 50, 100, -4, 1),
    TURRET          ("Turret",         2, 2, 15, 150, -6),
    GENERATOR       ("Generator",      5, 5, 25, 200, 30) {
        @Override
        public int getEnergyDelta(ResearchInfo researchInfo) {
            return (int) ResearchType.GENERATOR_EFFICIENCY.getValue(researchInfo);
        }
    },
    MINE            ("Mine",           5, 5, 25, 200, -5),
    HANGAR          ("Hangar",         7, 7, 50, 300, -10);

    // Name used in `from` and `toString`
    public final String name;

    // Width of building
    public final int sizeX;

    // Height of building
    public final int sizeY;

    // Cost of building
    public final int cost;

    // Health bonus of building
    public final int healthBonus;

    // Energy consumption (if negative) or production (if positive)
    private final int energyDelta;

    // Max count of building
    public final int maxCount;

    BuildingType(String name, int sizeX, int sizeY, int cost, int healthBonus, int energyDelta) {
        this(name, sizeX, sizeY, cost, healthBonus, energyDelta, Integer.MAX_VALUE);
    }

    BuildingType(String name, int sizeX, int sizeY, int cost, int healthBonus, int energyDelta, int maxCount) {
        this.name = name;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.cost = cost;
        this.healthBonus = healthBonus;
        this.energyDelta = energyDelta;
        this.maxCount = maxCount;
    }

    // Parses BuildingType by name
    public static BuildingType from(String string) {
        for (BuildingType type : BuildingType.values())
            if (string.equals(type.name))
                return type;
        return null;
    }

    // Returns name of building type
    @Override
    public String toString() {
        return name;
    }

    public int getEnergyDelta(ResearchInfo researchInfo) {
        return energyDelta;
    }
}
