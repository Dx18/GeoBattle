package geobattle.geobattle.screens.settingsscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.server.OSAPI;

public final class SettingsScreenGUI {
    private final Skin skin;

    public final VisTable settings;

    public final VisSlider soundVolume;

    public final VisSlider musicVolume;

    public final VisTextField ip;

    public final VisTextField port;

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
        ip = new VisTextField(oSAPI.loadValue("ip", "78.47.182.60"));
        ip.setMessageText(screen.getI18NBundle().get("ipAddress"));
        port = new VisTextField(oSAPI.loadValue("port", "12000"));
        port.setMessageText(screen.getI18NBundle().get("port"));
        guiStage.addActor(settings);

        reset(screen);
    }

    public void reset(SettingsScreen screen) {
        initSettings(screen);
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
                .height(Gdx.graphics.getPpcY() * 0.9f);
        volumeRoot.add(soundVolume)
                .growX();
        volumeRoot.row();
        volumeRoot.add(new VisLabel(screen.getI18NBundle().get("musicVolume")))
                .width(contentWidth * 0.45f)
                .height(Gdx.graphics.getPpcY() * 0.9f);
        volumeRoot.add(musicVolume)
                .growX();

        root.add(volumeRoot)
                .fillX();
        root.row();

        root.add(ip)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        root.add(port)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        TextButton saveSettings = new TextButton(screen.getI18NBundle().get("save"), skin);
        saveSettings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (saveSettings())
                    screen.onSaveSettings();
            }
        });
        root.add(saveSettings)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);

        settings.add(root);
        settings.center().pad(20).top().padTop(20);
    }

    private boolean saveSettings() {
        try {
            int parsedPort = Integer.parseInt(port.getText());

            oSAPI.saveValue("soundVolume", String.valueOf(soundVolume.getValue()));
            oSAPI.saveValue("musicVolume", String.valueOf(musicVolume.getValue()));
            oSAPI.saveValue("ip", ip.getText());
            oSAPI.saveValue("port", String.valueOf(parsedPort));
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
