package geobattle.geobattle.screens.selectserverscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.util.adapter.ArrayListAdapter;
import com.kotcrab.vis.ui.widget.ListView;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;

import java.util.ArrayList;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.server.ServerAddress;

public final class SelectServerScreenGUI {
    private final Skin skin;

    private final VisScrollPane serverListPanel;

    private final ListView<ServerAddress> officialServerList;

    private final ListView<ServerAddress> customServerList;

    private static class ServerListAdapter extends ArrayListAdapter<ServerAddress, VisTable> {
        public ServerListAdapter(ArrayList<ServerAddress> array) {
            super(array);
        }

        @Override
        protected VisTable createView(ServerAddress item) {
            VisTable result = new VisTable();

            VisLabel name = new VisLabel(item.name);
            result.add(name)
                    .width(Gdx.graphics.getWidth() * 0.7f)
                    .height(Gdx.graphics.getPpcY());

            return result;
        }
    }

    public SelectServerScreenGUI(AssetManager assetManager, SelectServerScreen screen, Stage guiStage) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);

        serverListPanel = new VisScrollPane(null, "default");
        officialServerList = new ListView<ServerAddress>(new ServerListAdapter(screen.getOfficialServers()));
        customServerList = new ListView<ServerAddress>(new ServerListAdapter(new ArrayList<ServerAddress>()));
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

        serverListPanel.setActor(root);
        serverListPanel.setFillParent(true);
        serverListPanel.setScrollingDisabled(true, false);
        serverListPanel.layout();
    }

//    private Table createServerItem(ServerAddress address) {
//        Table result = new Table(skin);
//        result.setBackground("panel");
//
//        result.add(new Label(address.name, skin, "black"))
//                .width(Gdx.graphics.getWidth() * 0.9f)
//                .height(Gdx.graphics.getPpcY());
//        result.row();
//        result.add(new Label(address.ip + ":" + address.port, skin, "black"))
//                .width(Gdx.graphics.getWidth() * 0.7f)
//                .height(Gdx.graphics.getPpcY());
//
//        return result;
//    }
}
