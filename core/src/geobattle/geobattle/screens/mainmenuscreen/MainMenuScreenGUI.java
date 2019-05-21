package geobattle.geobattle.screens.mainmenuscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

import geobattle.geobattle.GeoBattleAssets;

public final class MainMenuScreenGUI {
    private final Skin skin;

    private final VisTable menu;

    public MainMenuScreenGUI(AssetManager assetManager, MainMenuScreen screen, Stage guiStage) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);

        menu = new VisTable();
        guiStage.addActor(menu);

        reset(screen);
    }

    public void reset(MainMenuScreen screen) {
        resetMenu(screen);
    }

    private void resetMenu(final MainMenuScreen screen) {
        menu.clear();
        menu.setFillParent(true);

        VisTextButton play = new VisTextButton(screen.getI18NBundle().get("play"), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onPlay();
            }
        });
        menu.add(play)
                .growX()
                .height(Gdx.graphics.getPpcY());
        menu.row();

        VisTextButton settings = new VisTextButton(screen.getI18NBundle().get("settings"), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onSettings();
            }
        });
        menu.add(settings)
                .growX()
                .height(Gdx.graphics.getPpcY());
        menu.row();

        VisTextButton exit = new VisTextButton(screen.getI18NBundle().get("exit"), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onExit();
            }
        });
        menu.add(exit)
                .growX()
                .height(Gdx.graphics.getPpcY());

        menu.bottom().right().pad(20).padLeft(Gdx.graphics.getWidth() / 3);
    }
}
