package geobattle.geobattle.tutorial;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.screens.gamescreen.GameScreen;
import geobattle.geobattle.screens.gamescreen.GameScreenGUI;

// Step of tutorial
public abstract class TutorialStep {
    // Message of current step
    public final String message;

    public TutorialStep(String message) {
        this.message = message;
    }

    // Invokes when step of tutorial begins
    public void onBegin(GameScreen screen, GameScreenGUI gui, GameState gameState) {

    }

    // Performs update and returns true if tutorial step should end
    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
        return true;
    }

    // Invokes when step of tutorial ends
    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {

    }
}
