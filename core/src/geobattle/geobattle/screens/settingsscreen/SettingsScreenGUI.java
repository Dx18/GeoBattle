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

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.server.OSAPI;

public final class SettingsScreenGUI {
    private final Skin skin;

    public final VisTable settings;

    public final VisSlider soundVolume;

    public final VisSlider musicVolume;

    // Table with button which hides keyboard
    public final VisTable hideKeyboard;

    // OS API
    private OSAPI oSAPI;

    public SettingsScreenGUI(AssetManager assetManager, SettingsScreen screen, OSAPI oSAPI, Stage guiStage) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN, Skin.class);
        this.oSAPI = oSAPI;

        settings = new VisTable();
        soundVolume = new VisSlider(0, 1, 0.01f, false);
        soundVolume.setValue(Float.parseFloat(oSAPI.loadValue("soundVolume", "0.5f")));
        musicVolume = new VisSlider(0, 1, 0.01f, false);
        musicVolume.setValue(Float.parseFloat(oSAPI.loadValue("musicVolume", "0.5")));
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
    }
}
