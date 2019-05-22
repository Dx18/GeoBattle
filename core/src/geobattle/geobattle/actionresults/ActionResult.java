package geobattle.geobattle.actionresults;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;
import geobattle.geobattle.server.ExternalAPI;

public interface ActionResult {
    void apply(GeoBattle game, GameState gameState);

    GameScreenMode screenModeAfterApply();
}
