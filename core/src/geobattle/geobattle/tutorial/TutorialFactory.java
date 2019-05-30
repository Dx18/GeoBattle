package geobattle.geobattle.tutorial;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.SnapshotArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.screens.gamescreen.GameScreen;
import geobattle.geobattle.screens.gamescreen.GameScreenGUI;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;

public final class TutorialFactory {
    public TutorialFactory() {

    }

    private void setButtonsEnabled(Table buttons, String... enabled) {
        SnapshotArray<Actor> childrenArray = buttons.getChildren();
        Actor[] children = childrenArray.begin();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Button) {
                ((Button) children[i]).setDisabled(true);
                for (String buttonName : enabled) {
                    if (buttonName.equals(children[i].getName())) {
                        ((Button) children[i]).setDisabled(false);
                        break;
                    }
                }
            }
        }
        childrenArray.end();
    }

    private void setButtonsDisabled(Table buttons, String... disabled) {
        SnapshotArray<Actor> childrenArray = buttons.getChildren();
        Actor[] children = childrenArray.begin();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Button) {
                ((Button) children[i]).setDisabled(false);
                for (String buttonName : disabled) {
                    if (buttonName.equals(children[i].getName())) {
                        ((Button) children[i]).setDisabled(true);
                        break;
                    }
                }
            }
        }
        childrenArray.end();
    }

    public Tutorial createMainTutorial(I18NBundle i18NBundle) {
        JsonObject tutorial = new JsonParser().parse(new JsonReader(Gdx.files.internal(
                String.format("tutorial/%s", i18NBundle.get("tutorialFile"))
        ).reader())).getAsJsonObject();

        return new Tutorial(new TutorialStep[] {
                new TutorialStep(tutorial.getAsJsonPrimitive("welcome").getAsString()) {

                },
                new TutorialStep(tutorial.getAsJsonPrimitive("buildFirstSector").getAsString()) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        return gui.getMode() == GameScreenMode.NORMAL;
                    }
                },
                new TutorialStep(tutorial.getAsJsonPrimitive("enterBuildMode").getAsString()) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsEnabled(gui.toolBar, "buildMode");
                        return gui.getMode() == GameScreenMode.BUILD;
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsDisabled(gui.toolBar);
                    }
                },
                new TutorialStep(tutorial.getAsJsonPrimitive("buildMine").getAsString()) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        if (gui.selectBuildingTypeDialog.getBuildingType() == BuildingType.MINE)
                            setButtonsEnabled(gui.buildToolBar, "build", "buildingType");
                        else
                            setButtonsEnabled(gui.buildToolBar, "buildingType");

                        return gameState.getCurrentPlayer().getMines().hasNext();
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsDisabled(gui.buildToolBar);
                    }
                },
                new TutorialStep(tutorial.getAsJsonPrimitive("buildGenerator").getAsString()) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        if (gui.getMode() == GameScreenMode.NORMAL) {
                            setButtonsEnabled(gui.toolBar, "buildMode");
                        } else if (gui.getMode() == GameScreenMode.BUILD) {
                            if (gui.selectBuildingTypeDialog.getBuildingType() == BuildingType.GENERATOR)
                                setButtonsEnabled(gui.buildToolBar, "build", "buildingType");
                            else
                                setButtonsEnabled(gui.buildToolBar, "buildingType");
                        }

                        return gameState.getCurrentPlayer().getGenerators().hasNext();
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsDisabled(gui.toolBar);
                        setButtonsDisabled(gui.buildToolBar);
                    }
                },
                new TutorialStep(tutorial.getAsJsonPrimitive("yourPossibilities").getAsString()) {},
                new TutorialStep(tutorial.getAsJsonPrimitive("goodLuck").getAsString()) {}
        });
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
