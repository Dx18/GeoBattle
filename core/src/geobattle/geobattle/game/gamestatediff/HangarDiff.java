package geobattle.geobattle.game.gamestatediff;

import java.util.ArrayList;

import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.game.units.UnitGroup;

// Difference between hangars
public final class HangarDiff {
    // ID of hangar
    public final int hangarId;

    // New units
    public final UnitGroup newUnits;

    public HangarDiff(int hangarId, UnitGroup newUnits) {
        this.hangarId = hangarId;
        this.newUnits = newUnits;
    }
}
