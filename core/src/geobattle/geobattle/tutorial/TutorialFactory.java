package geobattle.geobattle.tutorial;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.SnapshotArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import geobattle.geobattle.GeoBattleAssets;
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

    public Tutorial createMainTutorial(AssetManager assetManager, I18NBundle i18NBundle) {
        JsonObject tutorial = new JsonParser().parse(new JsonReader(Gdx.files.internal(
                String.format("tutorial/%s", i18NBundle.get("tutorialFile"))
        ).reader())).getAsJsonObject();

        class BuildTutorialStep extends TutorialStep {
            private BuildingType buildingType;

            public BuildTutorialStep(BuildingType buildingType, String message, TextureRegion image) {
                super(message, image);
                this.buildingType = buildingType;
            }

            @Override
            public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                if (gui.getMode() == GameScreenMode.NORMAL) {
                    setButtonsEnabled(gui.toolBar, "buildMode");
                } else if (gui.getMode() == GameScreenMode.BUILD) {
                    if (gui.selectBuildingTypeDialog.getBuildingType() == buildingType)
                        setButtonsEnabled(gui.buildToolBar, "build", "buildingType");
                    else
                        setButtonsEnabled(gui.buildToolBar, "buildingType");
                }

                return gameState.getCurrentPlayer().getCount(buildingType) >= 1;
            }

            @Override
            public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                setButtonsDisabled(gui.toolBar);
                setButtonsDisabled(gui.buildToolBar);
            }
        }

        TextureAtlas buildings = assetManager.get(GeoBattleAssets.BUILDINGS_ATLAS);
        TextureAtlas buttons = assetManager.get("skins/geoBattleSkin/skin.atlas");

        return new Tutorial(new TutorialStep[] {
                new TutorialStep(tutorial.getAsJsonPrimitive("welcome").getAsString()) {},
                new TutorialStep(tutorial.getAsJsonPrimitive("buildFirstSector").getAsString(), buttons.findRegion("ok")) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        return gui.getMode() == GameScreenMode.NORMAL;
                    }
                },
                new TutorialStep(tutorial.getAsJsonPrimitive("enterBuildMode").getAsString(), buttons.findRegion("build")) {
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
                new TutorialStep(tutorial.getAsJsonPrimitive("buildMine").getAsString(), buttons.findRegion("buildingType")) {
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
                new TutorialStep(tutorial.getAsJsonPrimitive("buildMoreMines").getAsString(), buildings.findRegion(GeoBattleAssets.MINE)) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        if (gui.getMode() == GameScreenMode.NORMAL) {
                            setButtonsEnabled(gui.toolBar, "buildMode");
                        } else if (gui.getMode() == GameScreenMode.BUILD) {
                            if (gui.selectBuildingTypeDialog.getBuildingType() == BuildingType.MINE)
                                setButtonsEnabled(gui.buildToolBar, "build", "buildingType");
                            else
                                setButtonsEnabled(gui.buildToolBar, "buildingType");
                        }

                        return gameState.getCurrentPlayer().getCount(BuildingType.MINE) >= 3;
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsDisabled(gui.toolBar);
                        setButtonsDisabled(gui.buildToolBar);
                    }
                },
                new TutorialStep(tutorial.getAsJsonPrimitive("enterDestroyMode").getAsString(), buttons.findRegion("destroy")) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsEnabled(gui.toolBar, "destroyMode");
                        return gui.getMode() == GameScreenMode.DESTROY;
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsDisabled(gui.toolBar);
                    }
                },
                new TutorialStep(tutorial.getAsJsonPrimitive("destroyMine").getAsString(), buildings.findRegion(GeoBattleAssets.MINE)) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        if (gui.getMode() == GameScreenMode.NORMAL)
                            setButtonsEnabled(gui.toolBar, "destroyMode");

                        return gameState.getCurrentPlayer().getCount(BuildingType.MINE) <= 2;
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsDisabled(gui.toolBar);
                    }
                },
                new TutorialStep(tutorial.getAsJsonPrimitive("buildGenerator").getAsString(), buildings.findRegion(GeoBattleAssets.GENERATOR)) {
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
                new TutorialStep(tutorial.getAsJsonPrimitive("buildTurret").getAsString(), buildings.findRegion(GeoBattleAssets.TURRET)) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        if (gui.getMode() == GameScreenMode.NORMAL) {
                            setButtonsEnabled(gui.toolBar, "buildMode");
                        } else if (gui.getMode() == GameScreenMode.BUILD) {
                            if (gui.selectBuildingTypeDialog.getBuildingType() == BuildingType.TURRET)
                                setButtonsEnabled(gui.buildToolBar, "build", "buildingType");
                            else
                                setButtonsEnabled(gui.buildToolBar, "buildingType");
                        }

                        return gameState.getCurrentPlayer().getCount(BuildingType.TURRET) >= 1;
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsDisabled(gui.toolBar);
                        setButtonsDisabled(gui.buildToolBar);
                    }
                },
                new TutorialStep(tutorial.getAsJsonPrimitive("buildHangar").getAsString(), buildings.findRegion(GeoBattleAssets.HANGAR)) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        if (gui.getMode() == GameScreenMode.NORMAL) {
                            setButtonsEnabled(gui.toolBar, "buildMode");
                        } else if (gui.getMode() == GameScreenMode.BUILD) {
                            if (gui.selectBuildingTypeDialog.getBuildingType() == BuildingType.HANGAR)
                                setButtonsEnabled(gui.buildToolBar, "build", "buildingType");
                            else
                                setButtonsEnabled(gui.buildToolBar, "buildingType");
                        }

                        return gameState.getCurrentPlayer().getCount(BuildingType.HANGAR) >= 1;
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsDisabled(gui.toolBar);
                        setButtonsDisabled(gui.buildToolBar);
                    }
                },
                new TutorialStep(tutorial.getAsJsonPrimitive("openHangarDialog").getAsString(), buttons.findRegion("hangar")) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsEnabled(gui.toolBar);
                        return gui.hangarDialog.root.getStage() != null;
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsDisabled(gui.toolBar);
                    }
                },
                new TutorialStep(tutorial.getAsJsonPrimitive("closeHangarDialog").getAsString()) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        return gui.hangarDialog.root.getStage() == null;
                    }
                },
                new BuildTutorialStep(BuildingType.RESEARCH_CENTER, tutorial.getAsJsonPrimitive("buildResearchCenter").getAsString(), buildings.findRegion(GeoBattleAssets.RESEARCH_CENTER)),
                new TutorialStep(tutorial.getAsJsonPrimitive("openResearchCenterDialog").getAsString(), buttons.findRegion("research")) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsEnabled(gui.toolBar, "researchMode");
                        return gui.researchDialog.root.getStage() != null;
                    }

                    @Override
                    public void onEnd(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        setButtonsDisabled(gui.toolBar);
                    }
                },
                new TutorialStep(tutorial.getAsJsonPrimitive("closeResearchCenterDialog").getAsString()) {
                    @Override
                    public boolean update(GameScreen screen, GameScreenGUI gui, GameState gameState) {
                        return gui.researchDialog.root.getStage() == null;
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
