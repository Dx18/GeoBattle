package geobattle.geobattle.game.gamestatediff;

import java.util.ArrayList;

import geobattle.geobattle.game.PlayerState;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.util.ReadOnlyArrayList;

// Difference between player states
public final class PlayerStateDiff {
    // ID of players
    public final int playerId;

    // Added sectors
    public final ArrayList<Sector> addedSectors;

    // Removed sectors
    public final ArrayList<Sector> removedSectors;

    // Sectors which could be changed
    public final ArrayList<SectorDiff> changedSectors;

    // Creates and calculates PlayerStateDiff
    public PlayerStateDiff(int playerId, PlayerState player1, PlayerState player2) {
        this.playerId = playerId;

        addedSectors = new ArrayList<Sector>();
        removedSectors = new ArrayList<Sector>();
        changedSectors = new ArrayList<SectorDiff>();

        ReadOnlyArrayList<Sector> sectors1 = player1.getAllSectors();
        for (int sectorIndex = 0; sectorIndex < sectors1.size(); sectorIndex++) {
            Sector next = sectors1.get(sectorIndex);

            Sector sector = player2.getSector(next.sectorId);
            if (sector == null)
                removedSectors.add(next);
            else
                changedSectors.add(new SectorDiff(next.sectorId, next, sector));
        }

        ReadOnlyArrayList<Sector> sectors2 = player2.getAllSectors();
        for (int sectorIndex = 0; sectorIndex < sectors2.size(); sectorIndex++) {
            Sector next = sectors2.get(sectorIndex);

            if (player1.getSector(next.sectorId) == null)
                addedSectors.add(next);
        }
    }
}
