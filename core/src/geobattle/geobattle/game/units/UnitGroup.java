package geobattle.geobattle.game.units;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Predicate;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Iterator;

public final class UnitGroup {
    private final Unit[] units;

    private float health;

    private UnitGroupState state;

    public UnitGroup(Unit[] units, UnitGroupState state) {
        this(units);
        this.state = state;
    }

    public UnitGroup(Unit[] units) {
        if (units.length != 4)
            throw new IllegalArgumentException("Length of units must be 4");

        this.units = units;
        setHealth(getMaxHealth());
    }

    public UnitGroup() {
        this.units = new Unit[4];
        this.health = 0;
    }

    public float getMaxHealth() {
        float result = 0;
        for (Unit unit : units) {
            if (unit != null)
                result += unit.getUnitType().maxHealth;
        }
        return result;
    }

    public void setHealth(float health) {
        this.health = MathUtils.clamp(health, 0, getMaxHealth());
    }

    public float getHealth() {
        return health;
    }

    public void removeUnit(Unit unit) {
        this.units[unit.hangarSlot] = null;
    }

    public Unit addUnit(Unit unit) {
        Unit existing = this.units[unit.hangarSlot];
        this.units[unit.hangarSlot] = unit;

        // TODO Update health

        return existing;
    }

    public Iterator<Unit> getAllUnits() {
        return new Predicate.PredicateIterator<Unit>(new Iterator<Unit>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < units.length;
            }

            @Override
            public Unit next() {
                Unit next = units[index];
                index++;
                return next;
            }
        }, new Predicate<Unit>() {
            @Override
            public boolean evaluate(Unit unit) {
                return unit != null;
            }
        });
    }

    public void setState(UnitGroupState state) {
        this.state = state;
    }

    public UnitGroupState getState() {
        return this.state;
    }

    public int getFreeSlot() {
        for (int index = 0; index < 4; index++)
            if (units[index] == null)
                return index;
        return -1;
    }

    public int getCount() {
        int count = 0;
        for (Unit unit : units)
            if (unit != null)
                count++;
        return count;
    }

    public UnitGroup clone() {
        Unit[] units = new Unit[4];
        for (int index = 0; index < 4; index++)
            units[index] = this.units[index].clone();

        return new UnitGroup(units);
    }

    public static UnitGroup fromJson(JsonObject object, int hangarX, int hangarY) {
        // float health = object.getAsJsonPrimitive("health").getAsFloat();
        JsonArray jsonUnits = object.getAsJsonArray("units");

        Unit[] units = new Unit[4];
        int index = 0;
        for (JsonElement jsonUnit : jsonUnits) {
            if (index >= 4)
                break;
            Unit.ServerSide serverSideUnit = Unit.ServerSide.fromJson(jsonUnit.getAsJsonObject());
            units[index] = Unit.from(serverSideUnit, hangarX, hangarY);
            index++;
        }

        return new UnitGroup(units);
    }
}
