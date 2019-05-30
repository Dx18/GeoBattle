package geobattle.geobattle.tutorial;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.screens.gamescreen.GameScreen;
import geobattle.geobattle.screens.gamescreen.GameScreenGUI;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;

public final class TutorialFactory {
    public TutorialFactory() {

    }

    public Tutorial createDebugTutorial() {
        return new Tutorial(new TutorialStep[] {
                new TutorialStep("Hello") {
                    @Override
                    public void onBegin(GameScreen screen, GameScreenGUI gui, GameState gameState) {

                    }

                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        return true;
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {

                    }
                },
                new TutorialStep("World") {
                    @Override
                    public void onBegin(GameScreen screen, GameScreenGUI gui, GameState gameState) {

                    }

                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        return gui.getMode() == GameScreenMode.BUILD;
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {

                    }
                },
                new TutorialStep("End") {
                    @Override
                    public void onBegin(GameScreen screen, GameScreenGUI gui, GameState gameState) {

                    }

                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        return false;
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {

                    }
                }
        });
    }
}
