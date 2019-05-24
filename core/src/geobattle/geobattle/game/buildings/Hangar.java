package geobattle.geobattle.game.buildings;

import com.google.gson.JsonObject;

import java.util.Iterator;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.game.units.UnitGroup;

// Hangar
public final class Hangar extends Building {
    // Units in hangar
    public final UnitGroup units;

    public Hangar(BuildingParams params) {
        super(params, BuildingType.HANGAR);
        units = new UnitGroup(this);
    }

    public void setUnits(UnitGroup units) {
        Unit[] newUnits = new Unit[4];
        Iterator<Unit> unitsIterator = units.getAllUnits();
        while (unitsIterator.hasNext()) {
            Unit next = unitsIterator.next();
            newUnits[next.hangarSlot] = next;
        }

        Unit[] oldUnits = new Unit[4];
        unitsIterator = this.units.getAllUnits();
        while (unitsIterator.hasNext()) {
            Unit next = unitsIterator.next();
            oldUnits[next.hangarSlot] = next;
        }

        for (int slot = 0; slot < 4; slot++) {
            if (oldUnits[slot] == null && newUnits[slot] != null)
                this.units.addUnit(newUnits[slot]);
            else if (oldUnits[slot] != null && newUnits[slot] == null)
                this.units.removeUnit(oldUnits[slot]);
            else if (oldUnits[slot] != null && oldUnits[slot].id != newUnits[slot].id) {
                this.units.removeUnit(oldUnits[slot]);
                this.units.addUnit(newUnits[slot]);
            }
        }
    }

    // Clones hangar
    @Override
    public Building clone() {
        return new Hangar(getParams());
    }

    // Creates hangar from JSON
    public static Hangar fromJson(JsonObject object, BuildingParams params) {
        Hangar result = new Hangar(params);

        UnitGroup units = UnitGroup.fromJson(object.getAsJsonObject("units"), result);

        result.setUnits(units);

        return result;
    }
}
