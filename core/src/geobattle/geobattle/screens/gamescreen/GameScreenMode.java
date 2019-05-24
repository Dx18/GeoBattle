package geobattle.geobattle.screens.gamescreen;

// Current mode of game screen
public enum GameScreenMode {
    // Normal mode with main tool bar
    NORMAL,
    // Mode where player builds buildings
    BUILD,
    // Mode where player destroys buildings
    DESTROY,
    // Mode where player creates his first sector
    BUILD_FIRST_SECTOR,
    // Mode where player wants to expand base
    BUILD_SECTOR,
    // Mode where player selects hangars before attacking other player
    SELECT_HANGARS,
    // Mode where player selects sector to attack
    SELECT_SECTOR,
}
