package geobattle.geobattle.screens.settingsscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.GeoBattleConst;
import geobattle.geobattle.server.OSAPI;

public final class SettingsScreenGUI {
    private final Skin skin;

    public final VisTable settings;

    public final VisSlider soundVolume;

    public final VisSlider musicVolume;

    // Quality of map
    public final VisTextButton mapQuality;

    // Table with button which hides keyboard
    public final VisTable hideKeyboard;

    // OS API
    private OSAPI oSAPI;

    // Current value of map quality
    private int mapQualityValue;

    // Possible values of map quality
    private static final String[] MAP_QUALITY_VALUES = {
            "mapQualityLow", "mapQualityHigh"
    };

    // Default map quality
    private static final int DEFAULT_MAP_QUALITY = 1;

    public SettingsScreenGUI(AssetManager assetManager, final SettingsScreen screen, OSAPI oSAPI, Stage guiStage) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN, Skin.class);
        this.oSAPI = oSAPI;

        settings = new VisTable();
        soundVolume = new VisSlider(0, 1, 0.01f, false);
        soundVolume.setValue(Float.parseFloat(oSAPI.loadValue("soundVolume", String.valueOf(GeoBattleConst.DEFAULT_SOUND_VOLUME))));
        musicVolume = new VisSlider(0, 1, 0.01f, false);
        musicVolume.setValue(Float.parseFloat(oSAPI.loadValue("musicVolume", String.valueOf(GeoBattleConst.DEFAULT_MUSIC_VOLUME))));

        String currentMapQuality = oSAPI.loadValue("mapQuality", MAP_QUALITY_VALUES[DEFAULT_MAP_QUALITY]);
        mapQualityValue = DEFAULT_MAP_QUALITY;
        for (int index = 0; index < MAP_QUALITY_VALUES.length; index++)
            if (MAP_QUALITY_VALUES[index].equals(currentMapQuality)) {
                mapQualityValue = index;
                break;
            }

        mapQuality = new VisTextButton(screen.getI18NBundle().get(MAP_QUALITY_VALUES[mapQualityValue]), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                mapQualityValue = (mapQualityValue + 1) % MAP_QUALITY_VALUES.length;
                mapQuality.setText(screen.getI18NBundle().get(MAP_QUALITY_VALUES[mapQualityValue]));
            }
        });
        guiStage.addActor(settings);

        hideKeyboard = new VisTable();
        guiStage.addActor(hideKeyboard);

        reset(screen);
    }

    public void reset(SettingsScreen screen) {
        initSettings(screen);
        initHideKeyboard();
    }

    private void initSettings(final SettingsScreen screen) {
        settings.reset();
        settings.setFillParent(true);

        int contentWidth = Gdx.graphics.getWidth() - 120;

        VisTable root = new VisTable();
        root.setBackground("windowCentered");

        root.add(new VisLabel(screen.getI18NBundle().get("settings"), "large"))
                .expandX()
                .pad(5)
                .align(Align.center);
        root.row();

        VisTable volumeRoot = new VisTable();

        volumeRoot.add(new VisLabel(screen.getI18NBundle().get("soundVolume")))
                .width(contentWidth * 0.45f)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        volumeRoot.add(soundVolume)
                .growX()
                .pad(5);
        volumeRoot.row();
        volumeRoot.add(new VisLabel(screen.getI18NBundle().get("musicVolume")))
                .width(contentWidth * 0.45f)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        volumeRoot.add(musicVolume)
                .growX()
                .pad(5);

        root.add(volumeRoot)
                .fillX();
        root.row();

        VisTable mapQualityRoot = new VisTable();

        mapQualityRoot.add(new VisLabel(screen.getI18NBundle().get("mapQuality")))
                .width(contentWidth * 0.45f)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        mapQualityRoot.add(mapQuality)
                .growX()
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);

        root.add(mapQualityRoot)
                .fillX();
        root.row();

        TextButton saveSettings = new TextButton(screen.getI18NBundle().get("save"), skin);
        saveSettings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                saveSettings();
                screen.onSaveSettings();
            }
        });
        root.add(saveSettings)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);

        settings.add(root);
        settings.center().pad(20).top().padTop(40 + Gdx.graphics.getPpcY() * 0.9f);
    }

    // Initializes hide keyboard button
    private void initHideKeyboard() {
        hideKeyboard.clear();
        hideKeyboard.setFillParent(true);

        VisImageButton hide = new VisImageButton("buttonHideKeyboard");
        hide.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.input.setOnscreenKeyboardVisible(false);
            }
        });
        hideKeyboard.add(hide)
                .width(Gdx.graphics.getPpcY() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 0.9f);

        hideKeyboard.top().padTop(20).right().padRight(20);
    }

    private void saveSettings() {
        oSAPI.saveValue("soundVolume", String.valueOf(soundVolume.getValue()));
        oSAPI.saveValue("musicVolume", String.valueOf(musicVolume.getValue()));
        oSAPI.saveValue("mapQuality", MAP_QUALITY_VALUES[mapQualityValue]);
    }
}
