package geobattle.geobattle.game.research;

import com.badlogic.gdx.math.MathUtils;
import com.google.gson.JsonObject;

// Info about player's research
public final class ResearchInfo {
    // Level of turret damage
    private int turretDamageLevel;

    // Level of unit damage
    private int unitDamageLevel;

    // Level of generator efficiency
    private int generatorEfficiencyLevel;

    public ResearchInfo(int turretDamageLevel, int unitDamageLevel, int generatorEfficiencyLevel) {
        setLevel(ResearchType.TURRET_DAMAGE, turretDamageLevel);
        setLevel(ResearchType.UNIT_DAMAGE, unitDamageLevel);
        setLevel(ResearchType.GENERATOR_EFFICIENCY, generatorEfficiencyLevel);
    }

    // Returns level of research
    public int getLevel(ResearchType researchType) {
        switch (researchType) {
            case TURRET_DAMAGE: return turretDamageLevel;
            case UNIT_DAMAGE: return unitDamageLevel;
            case GENERATOR_EFFICIENCY: return generatorEfficiencyLevel;
        }
        return 0;
    }

    // Sets level of research
    public void setLevel(ResearchType researchType, int level) {
        int clampedLevel = MathUtils.clamp(level, 0, researchType.getLevelCount());

        switch (researchType) {
            case TURRET_DAMAGE: turretDamageLevel = clampedLevel; break;
            case UNIT_DAMAGE: unitDamageLevel = clampedLevel; break;
            case GENERATOR_EFFICIENCY: generatorEfficiencyLevel = clampedLevel; break;
        }
    }

    // Increments level of research
    public void incrementLevel(ResearchType researchType) {
        setLevel(researchType, getLevel(researchType) + 1);
    }

    // Creates ResearchInfo from JSON
    public static ResearchInfo fromJson(JsonObject object) {
        return new ResearchInfo(
                object.getAsJsonPrimitive("turretDamageLevel").getAsInt(),
                object.getAsJsonPrimitive("unitDamageLevel").getAsInt(),
                object.getAsJsonPrimitive("generatorEfficiencyLevel").getAsInt()
        );
    }
}
