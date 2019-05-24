package geobattle.geobattle.actionresults;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;

public interface ActionResult {
    void apply(GeoBattle game, GameState gameState);

    GameScreenMode screenModeAfterApply();
}
