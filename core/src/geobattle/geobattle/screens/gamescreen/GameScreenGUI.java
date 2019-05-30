package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;

// GUI for game screen
public final class GameScreenGUI {
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

    // Info about sector
    public final VisTable sectorInfo;

    // Label which contains info about sector's energy
    public final VisLabel energyLabel;

    // Label which contains info about sector's health
    public final VisLabel healthLabel;

    // Label which contains name of player who owns selected sector
    public final VisLabel nameLabel;

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

    // "Unit building" dialog
    public final UnitBuildingGUI hangarDialog;

    // "Research" dialog
    public final ResearchGUI researchDialog;

    // Some labels
    public final VisTable labels;

    // Label which contains info about max count of selected building type
    public final VisLabel maxBuildingCountLabel;

    // Current mode label
    public final VisLabel currentModeLabel;

    // Size of usual tool bar button
    private int buttonSize;

    // Selected building
    private Building selectedBuilding;

    // True if exit dialog is shown to player
    private boolean exitDialogShown;

    // Mode of game screen
    private GameScreenMode mode;

    // True if tutorial message is shown to player
    private boolean tutorialMessageShown;

    // Initializes GUI
    public GameScreenGUI(AssetManager assetManager, final GameScreen screen, final Stage guiStage) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);
        this.guiStage = guiStage;

        navigationToolBar = new VisTable();
        guiStage.addActor(navigationToolBar);

        info = new VisTable();
        resourcesLabel = new VisLabel("<resources>");
        sectorInfo = new VisTable();
        energyLabel = new VisLabel("<energy>");
        healthLabel = new VisLabel("<health>");
        nameLabel = new VisLabel("<name>");
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

        hangarDialog = new UnitBuildingGUI(assetManager, screen);

        researchDialog = new ResearchGUI(assetManager, screen);

        labels = new VisTable();
        currentModeLabel = new VisLabel("<currentMode>", "black");
        currentModeLabel.setAlignment(Align.center);
        maxBuildingCountLabel = new VisLabel("<maxBuildingCount>", "black");
        maxBuildingCountLabel.setAlignment(Align.center);
        guiStage.addActor(labels);

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
        buttonSize = Math.min(
                Gdx.graphics.getWidth() / 7,
                Gdx.graphics.getHeight() / 7
        );

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
        initHangarDialog(screen);
        initResearchDialog(screen);
        initLabels();
    }

    // Initializes navigation tool bar
    private void initNavigationToolBar(final GameScreen screen) {
        navigationToolBar.reset();
        navigationToolBar.setFillParent(true);
        VisImageButton zoomIn = new VisImageButton("buttonZoomIn");
        zoomIn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getCamera().zoomIn(1);
            }
        });
        navigationToolBar.add(zoomIn)
                .width(buttonSize)
                .height(buttonSize);
        navigationToolBar.row();
        VisImageButton zoomOut = new VisImageButton("buttonZoomOut");
        zoomOut.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getCamera().zoomOut(1);
            }
        });
        navigationToolBar.add(zoomOut)
                .width(buttonSize)
                .height(buttonSize);
        navigationToolBar.row();
        VisImageButton toPlayer = new VisImageButton("buttonToPlayer");
        toPlayer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onMoveToPlayer();
            }
        });
        navigationToolBar.add(toPlayer)
                .width(buttonSize)
                .height(buttonSize);
        navigationToolBar.left().padLeft(20).bottom().padBottom(20);
    }

    // Initializes information table
    private void initInfo() {
        info.clear();
        info.setFillParent(true);

        final int PARAM_COUNT = 3;
        final int ICON_SIZE = 40;
        final int ICON_PADDING = 5;
        final int LABEL_PADDING = 10;
        final int SCREEN_PADDING = 20;
        final int BACKGROUND_PADDING = 10;

        final int totalLabelWidth = Gdx.graphics.getWidth() -
                PARAM_COUNT * (ICON_SIZE + ICON_PADDING * 2 + LABEL_PADDING) -
                SCREEN_PADDING * 2 - BACKGROUND_PADDING * 2;

        VisTable globalInfo = new VisTable();
        globalInfo.setBackground("infoBackgroundLeft");

        globalInfo.add(new VisImage("resources"))
                .width(ICON_SIZE)
                .height(ICON_SIZE)
                .pad(ICON_PADDING);
        globalInfo.add(resourcesLabel)
                .width(totalLabelWidth * 0.35f)
                .height(ICON_SIZE)
                .padLeft(LABEL_PADDING);

        info.add(globalInfo)
            .top();

        sectorInfo.clear();
        sectorInfo.setBackground("infoBackgroundRight");

        sectorInfo.add(new VisImage("energy"))
                .width(ICON_SIZE)
                .height(ICON_SIZE)
                .pad(ICON_PADDING);
        sectorInfo.add(energyLabel)
                .width(totalLabelWidth * 0.3f)
                .height(ICON_SIZE)
                .padLeft(LABEL_PADDING);

        sectorInfo.add(new VisImage("health"))
                .width(ICON_SIZE)
                .height(ICON_SIZE)
                .pad(ICON_PADDING);
        sectorInfo.add(healthLabel)
                .width(totalLabelWidth * 0.35f)
                .height(ICON_SIZE)
                .padLeft(LABEL_PADDING);

        sectorInfo.row();

        sectorInfo.add(new VisImage("person"))
                .width(ICON_SIZE)
                .height(ICON_SIZE)
                .pad(ICON_PADDING);
        sectorInfo.add(nameLabel)
                .fillX()
                .height(ICON_SIZE)
                .padLeft(LABEL_PADDING)
                .colspan(3);

        info.add(sectorInfo);
        info.row();

        info.left().padLeft(SCREEN_PADDING).top().padTop(SCREEN_PADDING);
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
                .width(buttonSize)
                .height(buttonSize);
        VisImageButton destroyMode = new VisImageButton("buttonDestroy");
        destroyMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onDestroyMode();
            }
        });
        toolBar.add(destroyMode)
                .width(buttonSize)
                .height(buttonSize);
        VisImageButton buildSectorMode = new VisImageButton("buttonBuildSector");
        buildSectorMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onBuildSectorMode();
            }
        });
        toolBar.add(buildSectorMode)
                .width(buttonSize)
                .height(buttonSize);
        VisImageButton research = new VisImageButton("buttonResearch");
        research.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showResearchDialog();
            }
        });
        toolBar.add(research)
                .width(buttonSize)
                .height(buttonSize);
        VisImageButton attack = new VisImageButton("buttonAttack");
        attack.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onSelectHangarsMode();
            }
        });
        toolBar.add(attack)
                .width(buttonSize)
                .height(buttonSize);
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
                .width(buttonSize)
                .height(buttonSize);
        VisImageButton build = new VisImageButton("buttonOk");
        build.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().onBuildEvent();
            }
        });
        buildToolBar.add(build)
                .width(buttonSize)
                .height(buttonSize);
        final VisImageButton buildingType = new VisImageButton("buttonBuildingType");
        buildingType.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSelectBuildingTypeDialog();
            }
        });
        buildToolBar.add(buildingType)
                .width(buttonSize)
                .height(buttonSize);
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
                .width(buttonSize)
                .height(buttonSize);
        VisImageButton destroy = new VisImageButton("buttonDestroy");
        destroy.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().onDestroyEvent();
            }
        });
        destroyToolBar.add(destroy)
                .width(buttonSize)
                .height(buttonSize);
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
                screen.getGameEvents().onFirstSectorBuildEvent();
            }
        });
        buildFirstSectorToolBar.add(buildCommandCenter)
                .width(buttonSize)
                .height(buttonSize);

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
                .width(buttonSize)
                .height(buttonSize);

        VisImageButton buildCommandCenter = new VisImageButton("buttonOk");
        buildCommandCenter.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().onSectorBuildEvent();
            }
        });
        buildSectorToolBar.add(buildCommandCenter)
                .width(buttonSize)
                .height(buttonSize);

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
                .width(buttonSize)
                .height(buttonSize);

        VisImageButton selectHangars = new VisImageButton("buttonNext");
        selectHangars.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onSelectSectorMode();
            }
        });
        selectHangarsToolBar.add(selectHangars)
                .width(buttonSize)
                .height(buttonSize);

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
                .width(buttonSize)
                .height(buttonSize);

        VisImageButton selectSector = new VisImageButton("buttonOk");
        selectSector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().onRequestAttack();
            }
        });
        selectSectorToolBar.add(selectSector)
                .width(buttonSize)
                .height(buttonSize);

        selectSectorToolBar.right().padRight(20).bottom().padBottom(20);
    }

    private void initHangarToolBar(final GameScreen screen) {
        hangarToolBar.clear();
        hangarToolBar.setFillParent(true);

        VisImageButton hangar = new VisImageButton("buttonHangar");
        hangar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showHangarDialog();
            }
        });
        hangarToolBar.add(hangar)
                .width(buttonSize)
                .height(buttonSize);

        hangarToolBar.right().padRight(20).bottom().padBottom(20 + buttonSize);
    }

    // Initializes hangar dialog
    private void initHangarDialog(GameScreen screen) {
        hangarDialog.init(screen);
    }

    // Shows hangar dialog
    private void showHangarDialog() {
        hangarDialog.root.show(guiStage);
    }

    private void initResearchDialog(final GameScreen screen) {
        researchDialog.init(screen);
    }

    // Shows research dialog
    private void showResearchDialog() {
        researchDialog.root.show(guiStage);
    }

    private void initLabels() {
        labels.clear();
        labels.setFillParent(true);

        labels.add(currentModeLabel)
                .growX()
                .height(40);
        labels.row();
        labels.add(maxBuildingCountLabel)
                .growX()
                .height(40);

        labels.top().padTop(150);
    }

    // Sets mode of game screen
    public void setMode(GameScreenMode mode) {
        this.mode = mode;

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

    // Returns mode of game screen
    public GameScreenMode getMode() {
        return mode;
    }

    public void onBuildingSelected(Building building) {
        if (building == selectedBuilding)
            return;

        selectedBuilding = building;

        hangarToolBar.setVisible(false);

        if (building == null)
            return;

        switch (building.getBuildingType()) {
            case HANGAR:
                hangarToolBar.setVisible(true);
                break;
        }
    }

    public void showExitDialog(final GameScreen screen) {
        if (exitDialogShown)
            return;

        final VisDialog dialog = new VisDialog(screen.getI18NBundle().get("exit"));

        dialog.getContentTable().add(new VisLabel(screen.getI18NBundle().get("exitQuestion")));

        VisTextButton yes = new VisTextButton(screen.getI18NBundle().get("yes"), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onExit();
                dialog.hide();
                exitDialogShown = true;
            }
        });
        dialog.getButtonsTable().add(yes)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY() * 0.9f);

        VisTextButton no = new VisTextButton(screen.getI18NBundle().get("no"), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
                exitDialogShown = false;
            }
        });
        dialog.getButtonsTable().add(no)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY() * 0.9f);

        dialog.show(guiStage);
        exitDialogShown = true;
    }

    public boolean isTutorialMessageShown() {
        return tutorialMessageShown;
    }

    public void showTutorialMessage(final GameScreen screen, String message) {
        if (tutorialMessageShown)
            return;

        final VisDialog dialog = new VisDialog("", "transparent");

        VisTable root = new VisTable();

        VisLabel messageText = new VisLabel(message);
        root.add(messageText)
                .expand()
                .pad(30)
                .align(Align.topLeft);

        dialog.getContentTable().add(root)
                .width(Gdx.graphics.getWidth() - 150)
                .height(Gdx.graphics.getHeight() / 2);

        VisTextButton close = new VisTextButton(screen.getI18NBundle().get("close"));
        close.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide(Actions.alpha(0));
                tutorialMessageShown = false;
            }
        });
        dialog.getButtonsTable().add(close)
                .width(Gdx.graphics.getPpcX())
                .height(Gdx.graphics.getPpcY() * 0.9f);

        dialog.show(guiStage, Actions.alpha(1));
        dialog.setPosition(
                (Gdx.graphics.getWidth() - dialog.getWidth()) / 2,
                (Gdx.graphics.getHeight() - dialog.getHeight()) / 2
        );
        tutorialMessageShown = true;
    }
}
