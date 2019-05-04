package geobattle.geobattle.screens.settingsscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import geobattle.geobattle.GeoBattleAssets;

public final class SettingsScreenGUI {
    private final Skin skin;

    public final Table settings;

    public final TextField ip;

    public final TextField port;

    public SettingsScreenGUI(AssetManager assetManager, SettingsScreen screen, Stage guiStage) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN, Skin.class);

        settings = new Table();
        ip = new TextField("localhost", skin);
        port = new TextField("12000", skin);
        guiStage.addActor(settings);

        reset(screen);
    }

    public void reset(SettingsScreen screen) {
        initSettings(screen);
    }

    private void initSettings(final SettingsScreen screen) {
        settings.reset();
        settings.setFillParent(true);

        Label settingsLabel = new Label("SETTINGS", skin, "black");
        settings.add(settingsLabel)
                .expandX()
                .height(Gdx.graphics.getPpcY());
        settings.row();

        settings.add(ip)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        settings.row();

        settings.add(port)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        settings.row();

        TextButton saveSettings = new TextButton("Save", skin);
        saveSettings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    screen.setAddress(ip.getText(), Integer.parseInt(port.getText()));
                    screen.exit();
                } catch (NumberFormatException e) {
                    Gdx.app.log("GeoBattle", "Cannot parse port");
                }
            }
        });
        settings.add(saveSettings)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(15);

        settings.center().pad(20).top().padTop(20);
    }
}
