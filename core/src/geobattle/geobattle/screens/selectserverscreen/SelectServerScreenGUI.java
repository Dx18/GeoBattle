package geobattle.geobattle.screens.selectserverscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.server.ServerAddress;

public final class SelectServerScreenGUI {
    private final Skin skin;

    private final ScrollPane serverListPanel;

    private final List<ServerAddress> officialServerList;

    private final List<ServerAddress> customServerList;

    public SelectServerScreenGUI(AssetManager assetManager, SelectServerScreen screen, Stage guiStage) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);

        serverListPanel = new ScrollPane(null);
        officialServerList = new List<ServerAddress>(skin);
        customServerList = new List<ServerAddress>(skin);
        guiStage.addActor(serverListPanel);

        reset(screen);
    }

    public void reset(SelectServerScreen screen) {
        resetServerListPanel(screen);
    }

    private void resetServerListPanel(SelectServerScreen screen) {
        serverListPanel.clear();
        serverListPanel.setFillParent(true);

        Table root = new Table();
        root.setFillParent(true);

        Label title = new Label("Servers", skin, "black");
        root.add(title)
                .width(Gdx.graphics.getWidth() * 0.9f)
                .height(Gdx.graphics.getPpcY());
        root.row();

        Label official = new Label("Official", skin, "black");
        official.setFontScale(0.8f);
        root.add(official)
                .width(Gdx.graphics.getWidth() * 0.9f)
                .height(Gdx.graphics.getPpcY());
        root.row();

        root.add(createServerItem(new ServerAddress("Alpha", "78.47.182.60", 12000)))
                .width(Gdx.graphics.getWidth() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 2);
        root.row();
        root.add(createServerItem(new ServerAddress("Alpha", "78.47.182.60", 12000)))
                .width(Gdx.graphics.getWidth() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 2);
        root.row();
        root.add(createServerItem(new ServerAddress("Alpha", "78.47.182.60", 12000)))
                .width(Gdx.graphics.getWidth() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 2);
        root.row();
        root.add(createServerItem(new ServerAddress("Alpha", "78.47.182.60", 12000)))
                .width(Gdx.graphics.getWidth() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 2);
        root.row();
        root.add(createServerItem(new ServerAddress("Alpha", "78.47.182.60", 12000)))
                .width(Gdx.graphics.getWidth() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 2);
        root.row();
        root.add(createServerItem(new ServerAddress("Alpha", "78.47.182.60", 12000)))
                .width(Gdx.graphics.getWidth() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 2);
        root.row();
        root.add(createServerItem(new ServerAddress("Alpha", "78.47.182.60", 12000)))
                .width(Gdx.graphics.getWidth() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 2);
        root.row();

        serverListPanel.setActor(root);
        serverListPanel.setFillParent(true);
        serverListPanel.setScrollingDisabled(true, false);
        serverListPanel.layout();
    }

    private Table createServerItem(ServerAddress address) {
        Table result = new Table(skin);
        result.setBackground("panel");

        result.add(new Label(address.name, skin, "black"))
                .width(Gdx.graphics.getWidth() * 0.9f)
                .height(Gdx.graphics.getPpcY());
        result.row();
        result.add(new Label(address.ip + ":" + address.port, skin, "black"))
                .width(Gdx.graphics.getWidth() * 0.7f)
                .height(Gdx.graphics.getPpcY());

        return result;
    }
}
