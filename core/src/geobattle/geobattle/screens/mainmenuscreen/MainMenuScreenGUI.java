package geobattle.geobattle.screens.mainmenuscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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
        menu.setRound(true);

        VisTextButton play = new VisTextButton(screen.getI18NBundle().get("play"), "menu", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onPlay();
            }
        });
        menu.add(play)
                .growX()
                .padTop(10)
                .height(Gdx.graphics.getPpcY());
        menu.row();

        VisTextButton settings = new VisTextButton(screen.getI18NBundle().get("settings"), "menu", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onSettings();
            }
        });
        menu.add(settings)
                .growX()
                .padTop(10)
                .height(Gdx.graphics.getPpcY());
        menu.row();

        VisTextButton help = new VisTextButton(screen.getI18NBundle().get("help"), "menu", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onHelp();
            }
        });
        menu.add(help)
                .growX()
                .padTop(10)
                .height(Gdx.graphics.getPpcY());
        menu.row();

        VisTextButton exit = new VisTextButton(screen.getI18NBundle().get("exit"), "menu", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onExit();
            }
        });
        menu.add(exit)
                .growX()
                .padTop(10)
                .height(Gdx.graphics.getPpcY());

        menu.bottom().right().pad(20).padLeft(Gdx.graphics.getWidth() / 3);
    }
}
