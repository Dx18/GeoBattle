package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.research.ResearchInfo;
import geobattle.geobattle.game.research.ResearchType;
import geobattle.geobattle.game.units.UnitType;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.BuildFirstSectorMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.BuildMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.BuildSectorMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.DestroyMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.GameScreenModeData;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.NormalMode;

// GUI for game screen
final class GameScreenGUI {
    // GUI skin
    private final Skin skin;

    // Stage where all GUI is
    private final Stage guiStage;

    // Tool bar which contains "Zoom in/out" and "To current geolocation" buttons
    public final Table navigationToolBar;

    // Table with information about resources and energy
    public final Table info;

    // Label which contains info about resources
    public final Label resourcesLabel;

    // Label which contains info about max count of selected building type
    public final Label maxBuildingCountLabel;

    // Label which shows FPS
    public final Label debugInfo;

    // Main tool bar
    public final Table toolBar;

    // Tool bar for build mode
    public final Table buildToolBar;

    // Tool bar for destroy mode
    public final Table destroyToolBar;

    // Tool bar for build command center mode
    public final Table buildFirstSectorToolBar;

    public final Table buildSectorToolBar;

    // "Select building type" dialog
    public final Dialog selectBuildingTypeDialog;

    // Tool bar for hangar
    public final Table hangarToolBar;

    // "Research" dialog
    public final Dialog researchDialog;

    // Initializes GUI
    public GameScreenGUI(AssetManager assetManager, final GameScreen screen, final Stage guiStage) {
        Gdx.app.log("GeoBattle", "Initializing GUI...");

        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);
        this.guiStage = guiStage;

        navigationToolBar = new Table();
        guiStage.addActor(navigationToolBar);

        info = new Table();
        resourcesLabel = new Label("<resources>", skin, "black");
        maxBuildingCountLabel = new Label("<maxBuildingCountLabel>", skin, "black");
        debugInfo = new Label("<debugInfo>", skin, "black");
        guiStage.addActor(info);

        toolBar = new Table();
        guiStage.addActor(toolBar);

        buildToolBar = new Table();
        guiStage.addActor(buildToolBar);

        destroyToolBar = new Table();
        guiStage.addActor(destroyToolBar);

        buildFirstSectorToolBar = new Table();
        guiStage.addActor(buildFirstSectorToolBar);

        buildSectorToolBar = new Table();
        guiStage.addActor(buildSectorToolBar);

        selectBuildingTypeDialog = new Dialog("", skin);

        hangarToolBar = new Table();
        guiStage.addActor(hangarToolBar);

        researchDialog = new Dialog("", skin);

        reset(screen);
    }

    // Shows message with specified text
    public void showMessage(String message) {
        Dialog dialog = new Dialog("", skin);
        dialog.pad(20);
        dialog.text(message);
        dialog.button("OK");
        dialog.show(guiStage);
    }

    // Resets GUI
    public void reset(GameScreen screen) {
        initNavigationToolBar(screen);
        initInfo();
        initToolBar(screen);
        initBuildToolBar(screen);
        initDestroyToolBar(screen);
        initBuildFirstSectorToolBar(screen);
        initBuildSectorToolBar(screen);
        initSelectBuildingTypeDialog(screen);
        initHangarToolBar(screen);
        initResearchDialog(screen);
    }

    // Initializes navigation tool bar
    private void initNavigationToolBar(final GameScreen screen) {
        navigationToolBar.reset();
        navigationToolBar.setFillParent(true);
        TextButton zoomIn = new TextButton("+", skin);
        zoomIn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getCamera().zoomIn(1);
            }
        });
        navigationToolBar.add(zoomIn)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        navigationToolBar.row();
        TextButton zoomOut = new TextButton("-", skin);
        zoomOut.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getCamera().zoomOut(1);
            }
        });
        navigationToolBar.add(zoomOut)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        navigationToolBar.row();
        TextButton toPlayer = new TextButton("*", skin);
        toPlayer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onMoveToPlayer();
            }
        });
        navigationToolBar.add(toPlayer)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        navigationToolBar.left().padLeft(20).bottom().padBottom(20);
    }

    // Initializes information table
    private void initInfo() {
        info.reset();
        info.setFillParent(true);
        info.add(resourcesLabel)
                .fillX()
                .height(Gdx.graphics.getPpcY());
        info.row();
        info.add(maxBuildingCountLabel)
                .fillX()
                .height(Gdx.graphics.getPpcY());
        info.row();
        info.add(debugInfo)
                .fillX()
                .height(Gdx.graphics.getPpcY() * 5);
        info.left().padLeft(20).top().padTop(20);
    }

    // Initializes main tool bar
    private void initToolBar(final GameScreen screen) {
        toolBar.reset();
        toolBar.setFillParent(true);
        TextButton buildMode = new TextButton("B", skin);
        buildMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onBuildMode();
            }
        });
        toolBar.add(buildMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        TextButton destroyMode = new TextButton("D", skin);
        destroyMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onDestroyMode();
            }
        });
        toolBar.add(destroyMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        TextButton buildSectorMode = new TextButton("S", skin);
        buildSectorMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onBuildSectorMode();
            }
        });
        toolBar.add(buildSectorMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        TextButton research = new TextButton("R", skin);
        research.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showResearchDialog();
            }
        });
        toolBar.add(research)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        toolBar.right().padRight(20).bottom().padBottom(20);
    }

    // Initializes build mode tool bar
    private void initBuildToolBar(final GameScreen screen) {
        buildToolBar.reset();
        buildToolBar.setFillParent(true);
        ImageTextButton exitBuildMode = new ImageTextButton("B", skin);
        exitBuildMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onBuildMode();
            }
        });
        buildToolBar.add(exitBuildMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        ImageTextButton build = new ImageTextButton("V", skin);
        build.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().onRequestBuild();
            }
        });
        buildToolBar.add(build)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        final ImageTextButton buildingType = new ImageTextButton("T", skin);
        buildingType.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSelectBuildingTypeDialog();
            }
        });
        buildToolBar.add(buildingType)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        buildToolBar.right().padRight(20).bottom().padBottom(20);
    }

    // Initializes "Select building type" dialog
    private void initSelectBuildingTypeDialog(final GameScreen screen) {
        selectBuildingTypeDialog.getContentTable().clear();
        selectBuildingTypeDialog.getButtonTable().clear();

        Label title = new Label("Building", skin);
        selectBuildingTypeDialog.getContentTable().add(title)
                .expandX()
                .width(Gdx.graphics.getWidth() - 40)
                .colspan(2)
                .height(Gdx.graphics.getPpcY());
        selectBuildingTypeDialog.getContentTable().row();

        selectBuildingTypeDialog.getContentTable().pad(20);

        Table buildingInfo = new Table(skin);
        final Label buildingName = new Label("", skin);
        buildingName.setAlignment(Align.left, Align.left);
        buildingInfo.add(buildingName)
                .expandX()
                .fillX();
        buildingInfo.row();
        final Label description = new Label("", skin);
        description.setAlignment(Align.left, Align.left);
        buildingInfo.add(description)
                .expandX()
                .fillX();
        buildingInfo.row();
        final Label size = new Label("", skin);
        size.setAlignment(Align.left, Align.left);
        buildingInfo.add(size)
                .expandX()
                .fillX();
        buildingInfo.row();
        final Label strength = new Label("", skin);
        strength.setAlignment(Align.left, Align.left);
        buildingInfo.add(strength)
                .expandX()
                .fillX();
        buildingInfo.row();
        final Label energy = new Label("", skin);
        energy.setAlignment(Align.left, Align.left);
        buildingInfo.add(energy)
                .expandX()
                .fillX();
        buildingInfo.row();
        final Label maxCount = new Label("", skin);
        maxCount.setAlignment(Align.left, Align.left);
        buildingInfo.add(maxCount)
                .expandX()
                .fillX();
        buildingInfo.row();
        final TextButton buildButton = new TextButton("", skin);
        buildingInfo.add(buildButton)
                .expandX()
                .height(Gdx.graphics.getPpcY())
                .bottom()
                .right();
        buildingInfo.top();

        final List<BuildingType> buildingTypes = new List<BuildingType>(skin);
        buildingTypes.setItems(BuildingType.values());

        buildingTypes.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                BuildingType buildingType = buildingTypes.getSelected();
                buildingName.setText(buildingType.toString());
                description.setText("Just " + buildingType.toString());
                size.setText("Size: " + buildingType.sizeX + " x " + buildingType.sizeY);
                strength.setText("Strength: " + buildingType.healthBonus);
                energy.setText("Energy: " + buildingType.energyDelta);
                if (buildingType.maxCount != Integer.MAX_VALUE)
                    maxCount.setText("Max count: " + buildingType.maxCount);
                else
                    maxCount.setText("");
                buildButton.setText("Build: " + buildingType.cost);
            }
        });

        buildingTypes.setSelected(BuildingType.GENERATOR);

        buildButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().setSelectedBuildingType(buildingTypes.getSelected());
                selectBuildingTypeDialog.hide();
            }
        });

        selectBuildingTypeDialog.getContentTable().add(buildingTypes)
                .width((Gdx.graphics.getWidth() - 60) * 0.4f)
                .fill()
                .expand()
                .pad(5);
        selectBuildingTypeDialog.getContentTable().add(buildingInfo)
                .width((Gdx.graphics.getWidth() - 60) * 0.6f)
                .fill()
                .expand()
                .pad(5);

        selectBuildingTypeDialog.getContentTable().setFillParent(true);

        selectBuildingTypeDialog.center();
    }

    // Shows dialog where player must select type of building he wants to build
    private void showSelectBuildingTypeDialog() {
        selectBuildingTypeDialog.show(guiStage);
    }

    // Initialize destroy mode tool bar
    private void initDestroyToolBar(final GameScreen screen) {
        destroyToolBar.reset();
        destroyToolBar.setFillParent(true);
        ImageTextButton exitDestroyMode = new ImageTextButton("D", skin);
        exitDestroyMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onDestroyMode();
            }
        });
        destroyToolBar.add(exitDestroyMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        ImageTextButton destroy = new ImageTextButton("V", skin);
        destroy.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().onRequestDestroy();
            }
        });
        destroyToolBar.add(destroy)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        destroyToolBar.right().padRight(20).bottom().padBottom(20);
    }

    // Initializes build command center tool bar
    private void initBuildFirstSectorToolBar(final GameScreen screen) {
        buildFirstSectorToolBar.reset();
        buildFirstSectorToolBar.setFillParent(true);

        ImageTextButton buildCommandCenter = new ImageTextButton("V", skin);
        buildCommandCenter.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().onRequestBuildFirstSector();
            }
        });
        buildFirstSectorToolBar.add(buildCommandCenter)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());

        buildFirstSectorToolBar.right().padRight(20).bottom().padBottom(20);
    }

    private void initBuildSectorToolBar(final GameScreen screen) {
        buildSectorToolBar.reset();
        buildSectorToolBar.setFillParent(true);

        TextButton exitMode = new TextButton("S", skin);
        exitMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onBuildSectorMode();
            }
        });
        buildSectorToolBar.add(exitMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());

        ImageTextButton buildCommandCenter = new ImageTextButton("V", skin);
        buildCommandCenter.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().onRequestBuildSector();
            }
        });
        buildSectorToolBar.add(buildCommandCenter)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());

        buildSectorToolBar.right().padRight(20).bottom().padBottom(20);
    }

    private void initHangarToolBar(final GameScreen screen) {
        hangarToolBar.clear();
        hangarToolBar.setFillParent(true);

        for (final UnitType unitType : UnitType.values()) {
            TextButton buildUnit = new TextButton(unitType.name.substring(0, 1), skin);
            buildUnit.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    screen.getGameEvents().onUnitBuild(unitType);
                }
            });
            hangarToolBar.add(buildUnit)
                    .width(Gdx.graphics.getPpcX())
                    .height(Gdx.graphics.getPpcY());
        }

        hangarToolBar.right().padRight(20).top().padTop(20);
    }

    private void initResearchDialog(final GameScreen screen) {
        researchDialog.getContentTable().clear();
        researchDialog.getButtonTable().clear();
        researchDialog.getContentTable().setFillParent(true);

        Label title = new Label("Research", skin);
        researchDialog.getContentTable().add(title)
                .expandX()
                .width(Gdx.graphics.getWidth() - 40)
                .height(Gdx.graphics.getPpcY());
        researchDialog.getContentTable().row();

        final ResearchInfo researchInfo = screen.getGameEvents().gameState.getResearchInfo();
        for (final ResearchType researchType : ResearchType.values()) {
            final int currentLevel = researchInfo.getLevel(researchType);

            Table researchTypeTable = new Table();

            Label researchName = new Label(researchType.name, skin);
            researchTypeTable.add(researchName)
                    .expandX()
                    .fillX()
                    .colspan(researchType.getLevelCount());

            final Label researchValue = new Label("", skin);
            researchTypeTable.add(researchValue);

            researchTypeTable.row();

            final Image[] indicators = new Image[researchType.getLevelCount()];
            for (int level = 0; level < researchType.getLevelCount(); level++) {
                indicators[level] = new Image(skin, "rect");
                indicators[level].setColor(researchInfo.getLevel(researchType) > level
                        ? new Color(0.4f, 0.4f, 0.4f, 1)
                        : new Color(1, 1, 1, 1)
                );
                researchTypeTable.add(indicators[level])
                        .width(Gdx.graphics.getPpcX() / 3)
                        .padLeft(5)
                        .padRight(5)
                        .expand(level == researchType.getLevelCount() - 1, false)
                        .align(Align.left);
            }

            int cost = researchType.getCost(currentLevel + 1);
            final TextButton research = new TextButton("", skin);
            if (cost == Integer.MAX_VALUE) {
                research.setDisabled(true);
                research.setText("");
                researchValue.setText(researchType.getValue(currentLevel) + "");
            } else {
                research.setText(cost + "");
                researchValue.setText(
                        researchType.getValue(currentLevel) + " > " + researchType.getValue(currentLevel + 1)
                );
            }
            research.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    researchInfo.incrementLevel(researchType);

                    int newLevel = researchInfo.getLevel(researchType);
                    indicators[newLevel - 1].setColor(0.4f, 0.4f, 0.4f, 1);
                    int newCost = researchType.getCost(newLevel + 1);
                    if (newCost == Integer.MAX_VALUE) {
                        research.setDisabled(true);
                        research.setText("");
                        researchValue.setText(researchType.getValue(newLevel) + "");
                    } else {
                        research.setText(newCost + "");
                        researchValue.setText(
                                researchType.getValue(newLevel) +
                                " > " +
                                researchType.getValue(newLevel + 1)
                        );
                    }
                }
            });
            GlyphLayout glyphLayout = new GlyphLayout();
            glyphLayout.setText(skin.getFont("default"), "16000");
            researchTypeTable.add(research)
                .width(glyphLayout.width + 40)
                .align(Align.right);

            researchDialog.getContentTable().add(researchTypeTable)
                    .expandX()
                    .fillX()
                    .width(Gdx.graphics.getWidth() - 40);
            researchDialog.getContentTable().row();
        }

        TextButton close = new TextButton("Close", skin);
        close.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                researchDialog.hide();
            }
        });
        researchDialog.getContentTable().add(close);

        researchDialog.getContentTable().pad(20);
        researchDialog.center();
    }

    // Shows research dialog
    private void showResearchDialog() {
        researchDialog.show(guiStage);
    }

    // Sets mode of game screen
    public void setMode(GameScreenModeData modeData) {
        toolBar.setVisible(false);
        buildToolBar.setVisible(false);
        destroyToolBar.setVisible(false);
        buildFirstSectorToolBar.setVisible(false);
        buildSectorToolBar.setVisible(false);
        hangarToolBar.setVisible(false);
        if (modeData instanceof NormalMode)
            toolBar.setVisible(true);
        else if (modeData instanceof BuildMode)
            buildToolBar.setVisible(true);
        else if (modeData instanceof DestroyMode)
            destroyToolBar.setVisible(true);
        else if (modeData instanceof BuildFirstSectorMode)
            buildFirstSectorToolBar.setVisible(true);
        else if (modeData instanceof BuildSectorMode)
            buildSectorToolBar.setVisible(true);
    }

    public void onBuildingSelected(Building building) {
        hangarToolBar.setVisible(false);

        if (building == null)
            return;

        switch (building.getBuildingType()) {
            case HANGAR:
                hangarToolBar.setVisible(true);
                break;
        }
    }
}
