package geobattle.geobattle.game.gamestatediff;

import java.util.ArrayList;
import java.util.Iterator;

import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;

// Difference between sectors
public final class SectorDiff {
    // ID of sectors
    public final int sectorId;

    // Added buildings
    public final ArrayList<Building> addedBuildings;

    // Removed buildings
    public final ArrayList<Building> removedBuildings;

    // Changed hangars
    public final ArrayList<HangarDiff> changedHangars;

    // Creates and calculates SectorDiff
    public SectorDiff(int sectorId, Sector sector1, Sector sector2) {
        this.sectorId = sectorId;

        addedBuildings = new ArrayList<Building>();
        removedBuildings = new ArrayList<Building>();
        changedHangars = new ArrayList<HangarDiff>();

        Iterator<Building> buildings1 = sector1.getAllBuildings();
        while (buildings1.hasNext()) {
            Building next = buildings1.next();

            if (sector2.getBuilding(next.id) == null)
                removedBuildings.add(next);
        }

        Iterator<Building> buildings2 = sector2.getAllBuildings();
        while (buildings2.hasNext()) {
            Building next = buildings2.next();

            if (sector1.getBuilding(next.id) == null)
                addedBuildings.add(next);
            else if (next.getBuildingType() == BuildingType.HANGAR)
                changedHangars.add(new HangarDiff(next.id, ((Hangar) next).units));
        }
    }
}
