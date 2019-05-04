package geobattle.geobattle.game.buildings;

// Parameters of building
public final class BuildingParams {
    // X coordinate of building (bottom-left corner)
    public final int x;

    // Y coordinate of building (bottom-left corner)
    public final int y;

    // ID of building
    public final int id;

    // ID of player this building belongs to
    public final int playerId;

    // ID of sector this building belongs to
    public final int sectorId;

    public BuildingParams(int x, int y, int id, int playerId, int sectorId) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.playerId = playerId;
        this.sectorId = sectorId;
    }
}
