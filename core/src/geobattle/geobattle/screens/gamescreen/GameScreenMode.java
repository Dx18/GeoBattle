package geobattle.geobattle.screens.gamescreen;

import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.GameScreenModeData;

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
    BUILD_SECTOR
}
