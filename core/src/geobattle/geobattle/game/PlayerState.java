package geobattle.geobattle.game;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.game.gamestatediff.PlayerStateDiff;
import geobattle.geobattle.game.gamestatediff.SectorDiff;
import geobattle.geobattle.game.research.ResearchInfo;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.util.IntPoint;
import geobattle.geobattle.util.JsonObjects;
import geobattle.geobattle.util.ReadOnlyArrayList;

// State of player
public class PlayerState {
    // Name of player
    private final String name;

    // ID of player
    private final int playerId;

    // Color of player
    private final Color color;

    // Player's research info
    private final ResearchInfo researchInfo;

    // Sectors which player has
    private ArrayList<Sector> sectors;

    // Units which player has
    private ArrayList<Unit> units;

    // Comparator for sectors
    private final static Comparator<Sector> sectorComparator = new Comparator<Sector>() {
        @Override
        public int compare(Sector sector1, Sector sector2) {
            return sector1.sectorId - sector2.sectorId;
        }
    };

    // Comparator for units
    private final static Comparator<Unit> unitComparator = new Comparator<Unit>() {
        @Override
        public int compare(Unit unit1, Unit unit2) {
            return unit1.id - unit2.id;
        }
    };

    // X of sector with min X
    private int minSectorX;

    // Y of sector with min Y
    private int minSectorY;

    // X of sector with max X
    private int maxSectorX;

    // Y of sector with max Y
    private int maxSectorY;

    // Point of base center
    private IntPoint centerPoint;

    public PlayerState(String name, int playerId, Color color, ResearchInfo researchInfo) {
        this.name = name;
        this.playerId = playerId;
        this.color = color;
        this.researchInfo = researchInfo;
        this.sectors = new ArrayList<Sector>();
        this.units = new ArrayList<Unit>();

        minSectorX = Integer.MAX_VALUE;
        minSectorY = Integer.MAX_VALUE;
        maxSectorX = Integer.MIN_VALUE;
        maxSectorY = Integer.MIN_VALUE;
    }

    // Adds sector. Keeps sectors sorted by ID
    public void addSector(Sector sector, GameObjectTracker tracker) {
        int index = Collections.binarySearch(sectors, sector, sectorComparator);
        if (index >= 0)
            throw new IllegalArgumentException("Cannot add sector with existing ID");

        if (
                sectors.size() > 0 &&
                ((sector.x - sectors.get(0).x) % Sector.SECTOR_SIZE != 0 ||
                (sector.y - sectors.get(0).y) % Sector.SECTOR_SIZE != 0)
        )
            throw new IllegalArgumentException("Sector is not aligned with other sectors");

        if (sector.x < minSectorX)
            minSectorX = sector.x;
        if (sector.y < minSectorY)
            minSectorY = sector.y;
        if (sector.x > maxSectorX)
            maxSectorX = sector.x;
        if (sector.y > maxSectorY)
            maxSectorY = sector.y;

        if (centerPoint == null)
            centerPoint = new IntPoint(0, 0);

        centerPoint.x = (int) ((centerPoint.x * sectors.size() + sector.x + Sector.SECTOR_SIZE / 2) / (sectors.size() + 1.0));
        centerPoint.y = (int) ((centerPoint.y * sectors.size() + sector.y + Sector.SECTOR_SIZE / 2) / (sectors.size() + 1.0));

        sectors.add(-index - 1, sector);
        tracker.addSector(sector);
    }

    // Removes sector
    public void removeSector(Sector sector, GameObjectTracker tracker) {
        int removeIndex = Collections.binarySearch(sectors, sector, sectorComparator);
        if (removeIndex < 0)
            throw new IllegalArgumentException("Cannot remove sector with specified ID");

        sectors.remove(removeIndex);
        tracker.removeSector(sector);

        if (
                sector.x == minSectorX || sector.y == minSectorY ||
                sector.x == maxSectorX || sector.y == maxSectorY
        ) {
            minSectorX = Integer.MAX_VALUE;
            minSectorY = Integer.MAX_VALUE;
            maxSectorX = Integer.MIN_VALUE;
            maxSectorY = Integer.MIN_VALUE;

            for (Sector currentSector : sectors) {
                if (currentSector.x < minSectorX)
                    minSectorX = currentSector.x;
                if (currentSector.y < minSectorY)
                    minSectorY = currentSector.y;
                if (currentSector.x > maxSectorX)
                    maxSectorX = currentSector.x;
                if (currentSector.y > maxSectorY)
                    maxSectorY = currentSector.y;
            }
        }

        if (sectors.isEmpty()) {
            centerPoint = null;
        } else {
            centerPoint.x = (int) (centerPoint.x + (centerPoint.x - sector.x - Sector.SECTOR_SIZE / 2) / (double) sectors.size());
            centerPoint.y = (int) (centerPoint.y + (centerPoint.y - sector.y - Sector.SECTOR_SIZE / 2) / (double) sectors.size());
        }
    }

    // Returns sector which contains point (`x`; `y`)
    public Sector getSector(int x, int y) {
        for (Sector sector : sectors)
            if (sector.containsPoint(x, y))
                return sector;

        return null;
    }

    // Finds sector with specified ID. Uses binary search
    private static int findSector(ArrayList<Sector> sectors, int id) {
        int left = 0;
        int right = sectors.size() - 1;
        while (left <= right) {
            int mid = (left + right) >> 1;
            if (sectors.get(mid).sectorId < id)
                left = mid + 1;
            else if (sectors.get(mid).sectorId > id)
                right = mid - 1;
            else
                return mid;
        }

        return -left - 1;
    }

    // Returns sector with specified ID
    public Sector getSector(int id) {
        int index = findSector(sectors, id);

        if (index >= 0)
            return sectors.get(index);

        return null;
    }

    // Returns read-only list of sectors
    public ReadOnlyArrayList<Sector> getAllSectors() {
        return new ReadOnlyArrayList<Sector>(sectors);
    }

    // Returns count of sectors
    public int getSectorCount() {
        return sectors.size();
    }

    // Adds building (keeps array of buildings sorted by ID).
    // Throws IllegalStateException when trying to add command center
    // when command center exists.
    // Throws IllegalArgumentException if building with same ID exists
    public void addBuilding(Building building) {
        for (Sector sector : sectors)
            if (sector.containsRect(
                    building.x - 1, building.y - 1, building.getSizeX() + 2, building.getSizeY() + 2
            )) {
                sector.addBuilding(building);
                break;
            }
    }

    // Removes building. Uses binary search (because buildings are sorted by ID)
    // Throws IllegalArgumentException if building does not exist
    public void removeBuilding(Building building) {
        for (Sector sector : sectors)
            if (sector.containsRect(
                    building.x - 1, building.y - 1, building.getSizeX() + 2, building.getSizeY() + 2
            )) {
                sector.removeBuilding(building);
                break;
            }
    }

    // Returns building at coordinates
    // If there is no such building
    public Building getBuilding(int x, int y) {
        for (Sector sector : sectors)
            if (sector.containsPoint(x, y))
                return sector.getBuilding(x, y);

        return null;
    }

    // Returns building with specified ID
    // Returns null if there is no such building
    public Building getBuilding(int id) {
        for (Sector sector : sectors) {
            Building building = sector.getBuilding(id);
            if (building != null)
                return building;
        }

        return null;
    }

    public ReadOnlyArrayList<Building>[] getAllBuildings() {
        ReadOnlyArrayList<Building>[] result = new ReadOnlyArrayList[sectors.size()];
        for (int sectorIndex = 0; sectorIndex < sectors.size(); sectorIndex++)
            result[sectorIndex] = sectors.get(sectorIndex).getAllBuildings();
        return result;
    }

    // Returns count of buildings player owns
    public int getCount(BuildingType buildingType) {
        int result = 0;
        for (Sector sector : sectors)
            result += sector.getCount(buildingType);
        return result;
    }

    // Adds unit and binds it to hangar
    public void addUnit(Unit unit) {
        int index = Collections.binarySearch(units, unit, unitComparator);
        if (index >= 0)
            throw new IllegalArgumentException("Cannot add unit with existing ID");

        units.add(-index - 1, unit);

        // TODO Check if null and not hangar
        Hangar hangar = (Hangar) getBuilding(unit.hangarId);
        hangar.units.addUnit(unit);
    }

    // Adds unit and binds it to hangar
    public void addUnit(Unit.ServerSide unit) {
        Building hangar = getBuilding(unit.hangarId);
        addUnit(Unit.from(unit, hangar.x, hangar.y));
    }

    // Returns name of player
    public String getName() {
        return name;
    }

    // Returns player ID
    public int getPlayerId() {
        return playerId;
    }

    // Returns color of player
    public Color getColor() {
        return color;
    }

    // Returns research info
    public ResearchInfo getResearchInfo() {
        return researchInfo;
    }

    // Creates PlayerState from JSON
    public static PlayerState fromJson(JsonObject object, GameObjectTracker tracker) {
        String name = object.getAsJsonPrimitive("name").getAsString();
        int playerId = object.getAsJsonPrimitive("playerId").getAsInt();
        Color color = JsonObjects.fromJson(object.getAsJsonObject("color"));
        ResearchInfo researchInfo = ResearchInfo.fromJson(object.getAsJsonObject("researchInfo"));

        PlayerState player = new PlayerState(name, playerId, color, researchInfo);

        JsonArray jsonSectors = object.getAsJsonArray("sectors");
        for (JsonElement jsonSector : jsonSectors)
            player.addSector(Sector.fromJson(jsonSector.getAsJsonObject(), playerId, researchInfo), tracker);

        return player;
    }

    // Applies PlayerStateDiff to player state
    public void applyDiff(PlayerStateDiff diff, GameObjectTracker tracker) {
        for (Sector removed : diff.removedSectors)
            removeSector(removed, tracker);
        for (SectorDiff sectorDiff : diff.changedSectors)
            getSector(sectorDiff.sectorId).applyDiff(sectorDiff);
        for (Sector added : diff.addedSectors)
            addSector(added, tracker);
    }

    // Returns point of base center
    public IntPoint getCenterPoint() {
        return centerPoint == null ? null : centerPoint.clone();
    }

    // Returns X of sector with min X
    public int getMinSectorX() {
        return minSectorX;
    }

    // Returns Y of sector with min Y
    public int getMinSectorY() {
        return minSectorY;
    }

    // Returns X of sector with max X
    public int getMaxSectorX() {
        return maxSectorX;
    }

    // Returns Y of sector with max Y
    public int getMaxSectorY() {
        return maxSectorY;
    }
}
