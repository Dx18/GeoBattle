package geobattle.geobattle.game.gamestatediff;

import java.util.ArrayList;

import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.util.ReadOnlyArrayList;

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

        ReadOnlyArrayList<Building> buildings1 = sector1.getAllBuildings();
        for (int buildingIndex = 0; buildingIndex < buildings1.size(); buildingIndex++) {
            Building next = buildings1.get(buildingIndex);

            if (sector2.getBuilding(next.id) == null)
                removedBuildings.add(next);
        }

        ReadOnlyArrayList<Building> buildings2 = sector2.getAllBuildings();
        for (int buildingIndex = 0; buildingIndex < buildings2.size(); buildingIndex++) {
            Building next = buildings2.get(buildingIndex);

            if (sector1.getBuilding(next.id) == null)
                addedBuildings.add(next);
            else if (next.getBuildingType() == BuildingType.HANGAR)
                changedHangars.add(new HangarDiff(next.id, ((Hangar) next).units));
        }
    }
}
