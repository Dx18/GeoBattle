package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

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
    public final VisTable navigationToolBar;

    // Table with information about resources and energy
    public final VisTable info;

    // Label which contains info about resources
    public final VisLabel resourcesLabel;

    // Label which contains info about max count of selected building type
    public final VisLabel maxBuildingCountLabel;

    // Label which shows FPS
    public final VisLabel debugInfo;

    // Main tool bar
    public final VisTable toolBar;

    // Tool bar for build mode
    public final VisTable buildToolBar;

    // Tool bar for destroy mode
    public final VisTable destroyToolBar;

    // Tool bar for build first sector mode
    public final VisTable buildFirstSectorToolBar;

    // Tool bar for build sector mode
    public final VisTable buildSectorToolBar;

    // Tool bar for selecting hangars to attack
    public final VisTable selectHangarsToolBar;

    // Tool bar for selecting sector to attack
    public final VisTable selectSectorToolBar;

    // "Select building type" dialog
    public final BuildingGUI selectBuildingTypeDialog;

    // Tool bar for hangar
    public final VisTable hangarToolBar;

    // "Research" dialog
    public final ResearchGUI researchDialog;

    // Initializes GUI
    public GameScreenGUI(AssetManager assetManager, final GameScreen screen, final Stage guiStage) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);
        this.guiStage = guiStage;

        navigationToolBar = new VisTable();
        guiStage.addActor(navigationToolBar);

        info = new VisTable();
        resourcesLabel = new VisLabel("<resources>", "black");
        maxBuildingCountLabel = new VisLabel("<maxBuildingCountLabel>", "black");
        debugInfo = new VisLabel("<debugInfo>", "black");
        guiStage.addActor(info);

        toolBar = new VisTable();
        guiStage.addActor(toolBar);

        buildToolBar = new VisTable();
        guiStage.addActor(buildToolBar);

        destroyToolBar = new VisTable();
        guiStage.addActor(destroyToolBar);

        buildFirstSectorToolBar = new VisTable();
        guiStage.addActor(buildFirstSectorToolBar);

        buildSectorToolBar = new VisTable();
        guiStage.addActor(buildSectorToolBar);

        selectHangarsToolBar = new VisTable();
        guiStage.addActor(selectHangarsToolBar);

        selectSectorToolBar = new VisTable();
        guiStage.addActor(selectSectorToolBar);

        selectBuildingTypeDialog = new BuildingGUI(assetManager, screen, BuildingType.GENERATOR);

        hangarToolBar = new VisTable();
        guiStage.addActor(hangarToolBar);

        researchDialog = new ResearchGUI(assetManager, screen);

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
        initSelectHangarsToolBar(screen);
        initSelectSectorToolBar(screen);
        initSelectBuildingTypeDialog(screen);
        initHangarToolBar(screen);
        initResearchDialog(screen);
    }

    // Initializes navigation tool bar
    private void initNavigationToolBar(final GameScreen screen) {
        navigationToolBar.reset();
        navigationToolBar.setFillParent(true);
        VisTextButton zoomIn = new VisTextButton("+", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getCamera().zoomIn(1);
            }
        });
        navigationToolBar.add(zoomIn)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        navigationToolBar.row();
        VisTextButton zoomOut = new VisTextButton("-", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getCamera().zoomOut(1);
            }
        });
        navigationToolBar.add(zoomOut)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        navigationToolBar.row();
        VisTextButton toPlayer = new VisTextButton("*", new ChangeListener() {
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
        VisImageButton buildMode = new VisImageButton("buttonBuild");
        buildMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onBuildMode();
            }
        });
        toolBar.add(buildMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        VisImageButton destroyMode = new VisImageButton("buttonDestroy");
        destroyMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onDestroyMode();
            }
        });
        toolBar.add(destroyMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        VisImageButton buildSectorMode = new VisImageButton("buttonBuildSector");
        buildSectorMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onBuildSectorMode();
            }
        });
        toolBar.add(buildSectorMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        VisImageButton research = new VisImageButton("buttonResearch");
        research.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showResearchDialog();
            }
        });
        toolBar.add(research)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        VisImageButton attack = new VisImageButton("buttonAttack");
        attack.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onSelectHangarsMode();
            }
        });
        toolBar.add(attack)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        toolBar.right().padRight(20).bottom().padBottom(20);
    }

    // Initializes build mode tool bar
    private void initBuildToolBar(final GameScreen screen) {
        buildToolBar.reset();
        buildToolBar.setFillParent(true);
        VisImageButton exitBuildMode = new VisImageButton("buttonBack");
        exitBuildMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onBuildMode();
            }
        });
        buildToolBar.add(exitBuildMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        VisImageButton build = new VisImageButton("buttonOk");
        build.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().onRequestBuild();
            }
        });
        buildToolBar.add(build)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        final VisImageButton buildingType = new VisImageButton("buttonEmpty");
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
        selectBuildingTypeDialog.init(screen);
    }

    // Shows dialog where player must select type of building he wants to build
    private void showSelectBuildingTypeDialog() {
        selectBuildingTypeDialog.root.show(guiStage);
    }

    // Initialize destroy mode tool bar
    private void initDestroyToolBar(final GameScreen screen) {
        destroyToolBar.reset();
        destroyToolBar.setFillParent(true);
        VisImageButton exitDestroyMode = new VisImageButton("buttonBack");
        exitDestroyMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onDestroyMode();
            }
        });
        destroyToolBar.add(exitDestroyMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());
        VisImageButton destroy = new VisImageButton("buttonDestroy");
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

        VisImageButton buildCommandCenter = new VisImageButton("buttonOk");
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

        VisImageButton exitMode = new VisImageButton("buttonBack");
        exitMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onBuildSectorMode();
            }
        });
        buildSectorToolBar.add(exitMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());

        VisImageButton buildCommandCenter = new VisImageButton("buttonOk");
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

    private void initSelectHangarsToolBar(final GameScreen screen) {
        selectHangarsToolBar.clear();
        selectHangarsToolBar.setFillParent(true);

        VisImageButton exitMode = new VisImageButton("buttonBack");
        exitMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onSelectHangarsMode();
            }
        });
        selectHangarsToolBar.add(exitMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());

        VisImageButton selectHangars = new VisImageButton("buttonNext");
        selectHangars.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onSelectSectorMode();
            }
        });
        selectHangarsToolBar.add(selectHangars)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());

        selectHangarsToolBar.right().padRight(20).bottom().padBottom(20);
    }

    private void initSelectSectorToolBar(final GameScreen screen) {
        selectSectorToolBar.clear();
        selectSectorToolBar.setFillParent(true);

        VisImageButton exitMode = new VisImageButton("buttonBack");
        exitMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onSelectSectorMode();
            }
        });
        selectSectorToolBar.add(exitMode)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());

        VisImageButton selectSector = new VisImageButton("buttonOk");
        selectSector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().onRequestAttack();
            }
        });
        selectSectorToolBar.add(selectSector)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY());

        selectSectorToolBar.right().padRight(20).bottom().padBottom(20);
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
        researchDialog.init(screen);
    }

    // Shows research dialog
    private void showResearchDialog() {
        researchDialog.root.show(guiStage);
    }

    // Sets mode of game screen
    public void setMode(GameScreenMode mode) {
        toolBar.setVisible(false);
        buildToolBar.setVisible(false);
        destroyToolBar.setVisible(false);
        buildFirstSectorToolBar.setVisible(false);
        buildSectorToolBar.setVisible(false);
        selectHangarsToolBar.setVisible(false);
        selectSectorToolBar.setVisible(false);
        hangarToolBar.setVisible(false);
        switch (mode) {
            case NORMAL: toolBar.setVisible(true); break;
            case BUILD: buildToolBar.setVisible(true); break;
            case DESTROY: destroyToolBar.setVisible(true); break;
            case BUILD_FIRST_SECTOR: buildFirstSectorToolBar.setVisible(true); break;
            case BUILD_SECTOR: buildSectorToolBar.setVisible(true); break;
            case SELECT_HANGARS: selectHangarsToolBar.setVisible(true); break;
            case SELECT_SECTOR: selectSectorToolBar.setVisible(true); break;
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
