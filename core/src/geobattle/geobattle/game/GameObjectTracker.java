package geobattle.geobattle.game;

import java.util.HashSet;

import geobattle.geobattle.GeoBattleConst;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.map.TileTree;
import geobattle.geobattle.util.IntPoint;
import geobattle.geobattle.util.IntRect;
import geobattle.geobattle.util.QuadTree;

// Tracker of game objects
public final class GameObjectTracker {
    // Tracked sectors
    private final QuadTree<Sector> sectors;

    public GameObjectTracker() {
        sectors = new QuadTree<Sector>(16, new IntRect(
                0, 0,
                1 << (TileTree.MAX_ZOOM_LEVEL + GeoBattleConst.SUBDIVISION),
                1 << (TileTree.MAX_ZOOM_LEVEL + GeoBattleConst.SUBDIVISION)
        ));
    }

    // Adds sector
    public void addSector(Sector sector) {
        sectors.insertAsPoint(sector, new IntPoint(sector.x, sector.y), null);
    }

    // Removes sector
    public void removeSector(Sector sector) {
        sectors.removeAsPoint(sector, new IntPoint(sector.x, sector.y));
    }

    // Returns sectors in rect
    public HashSet<Sector> getSectors(IntRect rect) {
        return sectors.queryByRectIntersection(new IntRect(
                rect.x - Sector.SECTOR_SIZE, rect.y - Sector.SECTOR_SIZE,
                rect.width + Sector.SECTOR_SIZE, rect.height + Sector.SECTOR_SIZE
        ));
    }
}
