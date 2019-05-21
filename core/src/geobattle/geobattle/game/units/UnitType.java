package geobattle.geobattle.game.units;

// Type of unit
public enum UnitType {
    BOMBER("Bomber", 3, 3, 20, 50),
    SPOTTER("Spotter", 2, 2, 40, 50);

    // Name used in `from` and `toString`
    public final String name;

    // Width of unit
    public final int sizeX;

    // Height of unit
    public final int sizeY;

    // Cost of unit
    public final int cost;

    // Max healthBonus of unit
    public final int maxHealth;

    UnitType(String name, int sizeX, int sizeY, int cost, int maxHealth) {
        this.name = name;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.cost = cost;
        this.maxHealth = maxHealth;
    }

    // Parses UnitType by name
    public static UnitType from(String string) {
        for (UnitType type : UnitType.values())
            if (string.equals(type.name))
                return type;
        return null;
    }

    // Returns name of unit type
    @Override
    public String toString() {
        return name;
    }
}
