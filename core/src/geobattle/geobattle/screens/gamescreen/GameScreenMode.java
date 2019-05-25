package geobattle.geobattle.screens.gamescreen;

// Current mode of game screen
public enum GameScreenMode {
    // Normal mode with main tool bar
    NORMAL("Normal"),
    // Mode where player builds buildings
    BUILD("Build"),
    // Mode where player destroys buildings
    DESTROY("Destroy"),
    // Mode where player creates his first sector
    BUILD_FIRST_SECTOR("BuildFirstSector"),
    // Mode where player wants to expand base
    BUILD_SECTOR("BuildSector"),
    // Mode where player selects hangars before attacking other player
    SELECT_HANGARS("SelectHangars"),
    // Mode where player selects sector to attack
    SELECT_SECTOR("SelectSector");

    // Name of mode used in toString()
    public final String name;

    GameScreenMode(String name) {
        this.name = name;
    }

    // Returns name of mode
    @Override
    public String toString() {
        return name;
    }
}
