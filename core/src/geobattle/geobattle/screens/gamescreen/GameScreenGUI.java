package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.units.UnitType;

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

    // Initializes GUI
    public GameScreenGUI(AssetManager assetManager, final GameScreen screen, final Stage guiStage) {
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
        hangarToolBar.setVisible(true);

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
        ImageTextButton buildMode = new ImageTextButton("B", skin);
        buildMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onBuildMode();
            }
        });
        toolBar.add(buildMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        ImageTextButton destroyMode = new ImageTextButton("D", skin);
        destroyMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onDestroyMode();
            }
        });
        toolBar.add(destroyMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        ImageTextButton buildSectorMode = new ImageTextButton("S", skin);
        buildSectorMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onBuildSectorMode();
            }
        });
        toolBar.add(buildSectorMode)
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

        selectBuildingTypeDialog.pad(20);

        Table buildingTypes = new Table();
        buildingTypes.setFillParent(true);
        for (final BuildingType type : BuildingType.values()) {
            TextButton button = new TextButton(type.toString(), skin);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    screen.getGameEvents().setSelectedBuildingType(type);
                    selectBuildingTypeDialog.hide();
                }
            });
            buildingTypes.add(button)
                    .expandX()
                    .fillX()
                    .height(Gdx.graphics.getPpcY())
                    .padTop(5);
            buildingTypes.row();
        }

        selectBuildingTypeDialog.getContentTable().add("Select building type...")
                .expandX()
                .height(Gdx.graphics.getPpcY());
        selectBuildingTypeDialog.getContentTable().row();
        selectBuildingTypeDialog.getContentTable().add(buildingTypes)
                .fillX();

        Button close = new TextButton("Close", skin);
        close.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectBuildingTypeDialog.hide();
            }
        });
        selectBuildingTypeDialog.button(close);
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

    // Sets screen mode
    public void setMode(GameScreenMode mode) {
        toolBar.setVisible(false);
        buildToolBar.setVisible(false);
        destroyToolBar.setVisible(false);
        buildFirstSectorToolBar.setVisible(false);
        buildSectorToolBar.setVisible(false);
        switch (mode) {
            case NORMAL:
                toolBar.setVisible(true);
                break;
            case BUILD:
                buildToolBar.setVisible(true);
                break;
            case DESTROY:
                destroyToolBar.setVisible(true);
                break;
            case BUILD_FIRST_SECTOR:
                buildFirstSectorToolBar.setVisible(true);
                break;
            case BUILD_SECTOR:
                buildSectorToolBar.setVisible(true);
                break;
        }
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
