package geobattle.geobattle.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Predicate;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.Generator;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Mine;
import geobattle.geobattle.game.buildings.ResearchCenter;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.game.buildings.Turret;
import geobattle.geobattle.game.gamestatediff.PlayerStateDiff;
import geobattle.geobattle.game.gamestatediff.SectorDiff;
import geobattle.geobattle.game.research.ResearchInfo;
import geobattle.geobattle.game.units.Bomber;
import geobattle.geobattle.game.units.Spotter;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.game.units.UnitType;
import geobattle.geobattle.util.CastIterator;
import geobattle.geobattle.util.GeoBattleMath;
import geobattle.geobattle.util.IntPoint;
import geobattle.geobattle.util.IntRect;
import geobattle.geobattle.util.JoinIterator;
import geobattle.geobattle.util.JsonObjects;

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

    // Point of base center
    private IntPoint centerPoint;

    public PlayerState(String name, int playerId, Color color, ResearchInfo researchInfo) {
        this.name = name;
        this.playerId = playerId;
        this.color = color;
        this.researchInfo = researchInfo;
        this.sectors = new ArrayList<Sector>();
        this.units = new ArrayList<Unit>();
    }

    // Clones player state
    public PlayerState clone() {
        String name = this.name;
        Color color = this.color.cpy();
        ResearchInfo researchInfo = this.researchInfo.clone();

        PlayerState cloned = new PlayerState(name, this.playerId, color, researchInfo);
        for (Sector sector : sectors)
            cloned.addSector(sector.clone());
        for (Unit unit : units)
            cloned.addUnit(unit.clone());

        return cloned;
    }

    // Adds sector. Keeps sectors sorted by ID
    public void addSector(Sector sector) {
        int index = Collections.binarySearch(sectors, sector, sectorComparator);
        if (index >= 0)
            throw new IllegalArgumentException("Cannot add sector with existing ID");

        if (
                sectors.size() > 0 &&
                ((sector.x - sectors.get(0).x) % Sector.SECTOR_SIZE != 0 ||
                (sector.y - sectors.get(0).y) % Sector.SECTOR_SIZE != 0)
        )
            throw new IllegalArgumentException("Sector is not aligned with other sectors");

        if (centerPoint == null)
            centerPoint = new IntPoint(0, 0);

        centerPoint.x = (int) ((centerPoint.x * sectors.size() + sector.x + Sector.SECTOR_SIZE / 2) / (sectors.size() + 1.0));
        centerPoint.y = (int) ((centerPoint.y * sectors.size() + sector.y + Sector.SECTOR_SIZE / 2) / (sectors.size() + 1.0));

        sectors.add(-index - 1, sector);
    }

    // Removes sector
    public void removeSector(Sector sector) {
        int removeIndex = Collections.binarySearch(sectors, sector, sectorComparator);
        if (removeIndex < 0)
            throw new IllegalArgumentException("Cannot remove sector with specified ID");

        sectors.remove(removeIndex);

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

    // Returns iterator over sectors
    public Iterator<Sector> getAllSectors() {
        return sectors.iterator();
    }

    // Returns all sectors in rect
    public Iterator<Sector> getSectorsInRect(final int x, final int y, final int width, final int height) {
        return new Predicate.PredicateIterator<Sector>(getAllSectors(), new Predicate<Sector>() {
            @Override
            public boolean evaluate(Sector sector) {
                return GeoBattleMath.tileRectanglesIntersect(
                        x, y, width, height,
                        sector.x, sector.y, Sector.SECTOR_SIZE, Sector.SECTOR_SIZE
                );
            }
        });
    }

    // Returns all sectors in rect
    public Iterator<Sector> getSectorsInRect(IntRect rect) {
        return getSectorsInRect(rect.x, rect.y, rect.width, rect.height);
    }

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

    // Returns all buildings in rect
    public Iterator<Building> getBuildingsInRect(final int x, final int y, final int width, final int height) {
        return new Predicate.PredicateIterator<Building>(getAllBuildings(), new Predicate<Building>() {
            @Override
            public boolean evaluate(Building building) {
                return GeoBattleMath.tileRectanglesIntersect(
                        x, y, width, height,
                        building.x, building.y, building.getSizeX(), building.getSizeY()
                );
            }
        });
    }

    // Returns all buildings in rect
    public Iterator<Building> getBuildingsInRect(IntRect rect) {
        return getBuildingsInRect(rect.x, rect.y, rect.width, rect.height);
    }

    // Returns iterator over all buildings
    public Iterator<Building> getAllBuildings() {
        ArrayList<Iterator<Building>> buildingIterators = new ArrayList<Iterator<Building>>(sectors.size());
        for (Sector sector : sectors) {
            buildingIterators.add(sector.getAllBuildings());
        }
        return new JoinIterator<Building>(buildingIterators);
    }

    // Returns iterator over specified type of buildings
    public <T extends Building> Iterator<T> getBuildings(final Class<T> type) {
        final Predicate.PredicateIterator<Building> filtered = new Predicate.PredicateIterator<Building>(getAllBuildings(), new Predicate<Building>() {
            @Override
            public boolean evaluate(Building building) {
                return type.isInstance(building);
            }
        });

        return new CastIterator<Building, T>(filtered);
    }

    // Returns iterator over research centers
    public Iterator<ResearchCenter> getResearchCenters() {
        return getBuildings(ResearchCenter.class);
    }

    // Returns iterator over turrets
    public Iterator<Turret> getTurrets() {
        return getBuildings(Turret.class);
    }

    // Returns iterator over generators
    public Iterator<Generator> getGenerators() {
        return getBuildings(Generator.class);
    }

    // Returns iterator over mines
    public Iterator<Mine> getMines() {
        return getBuildings(Mine.class);
    }

    // Returns iterator over hangars
    public Iterator<Hangar> getHangars() {
        return getBuildings(Hangar.class);
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

    // Removes unit and unbinds it from hangar
    public void removeUnit(Unit unit) {
        // TODO Check if null and not hangar
        Hangar hangar = (Hangar) getBuilding(unit.hangarId);
        hangar.units.removeUnit(unit);

        int removeIndex = Collections.binarySearch(units, unit, unitComparator);
        if (removeIndex < 0)
            throw new IllegalArgumentException("Cannot remove unit with specified ID");
        units.remove(removeIndex);
    }

    // Returns unit with specified ID
    // Returns null if there is no such unit
    public Unit getUnit(int id) {
        int index = Collections.binarySearch(units, new Unit(0, 0, 0, id, 0, 0, UnitType.BOMBER) {
            @Override
            public Unit clone() { return null; }
        }, unitComparator);

        if (index >= 0)
            return units.get(index);

        return null;
    }

    // Returns iterator over all units
    public Iterator<Unit> getAllUnits() {
        return units.iterator();
    }

    // Returns iterator over specified type of buildings
    public <T extends Unit> Iterator<T> getUnits(final UnitType type) {
        final Predicate.PredicateIterator<Unit> filtered = new Predicate.PredicateIterator<Unit>(units, new Predicate<Unit>() {
            @Override
            public boolean evaluate(Unit unit) {
                return unit.getUnitType() == type;
            }
        });

        return new CastIterator<Unit, T>(filtered);
    }

    // Returns iterator over specified type of buildings
    public <T extends Unit> Iterator<T> getUnits(final Class<T> type) {
        final Predicate.PredicateIterator<Unit> filtered = new Predicate.PredicateIterator<Unit>(units, new Predicate<Unit>() {
            @Override
            public boolean evaluate(Unit unit) {
                return type.isInstance(unit);
            }
        });

        return new CastIterator<Unit, T>(filtered);
    }

    // Returns iterator over bombers
    public Iterator<Bomber> getBombers() {
        return getUnits(Bomber.class);
    }

    // Returns iterator over spotters
    public Iterator<Spotter> getSpotters() {
        return getUnits(Spotter.class);
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
    public static PlayerState fromJson(JsonObject object) {
        String name = object.getAsJsonPrimitive("name").getAsString();
        int playerId = object.getAsJsonPrimitive("playerId").getAsInt();
        Color color = JsonObjects.fromJson(object.getAsJsonObject("color"));
        ResearchInfo researchInfo = ResearchInfo.fromJson(object.getAsJsonObject("researchInfo"));

        PlayerState player = new PlayerState(name, playerId, color, researchInfo);

        JsonArray jsonSectors = object.getAsJsonArray("sectors");
        for (JsonElement jsonSector : jsonSectors)
            player.addSector(Sector.fromJson(jsonSector.getAsJsonObject(), playerId, researchInfo));

        return player;
    }

    // Applies PlayerStateDiff to player state
    public void applyDiff(PlayerStateDiff diff) {
        for (Sector removed : diff.removedSectors)
            removeSector(removed);
        for (SectorDiff sectorDiff : diff.changedSectors)
            getSector(sectorDiff.sectorId).applyDiff(sectorDiff);
        for (Sector added : diff.addedSectors)
            addSector(added);
    }

    // Returns point of base center
    public IntPoint getCenterPoint() {
        return centerPoint == null ? null : centerPoint.clone();
    }
}
