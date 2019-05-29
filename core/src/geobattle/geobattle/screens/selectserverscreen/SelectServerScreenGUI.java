package geobattle.geobattle.screens.selectserverscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.adapter.ArrayListAdapter;
import com.kotcrab.vis.ui.widget.ListView;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

import java.util.ArrayList;
import java.util.Locale;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.server.ServerAddress;

public final class SelectServerScreenGUI {
    private final Skin skin;

    private final Stage guiStage;

    private final VisTable servers;

    private final ListView<ServerAddress> officialServerList;

    private final ListView<ServerAddress> customServerList;

    private class ServerListAdapter extends ArrayListAdapter<ServerAddress, VisTable> {
        private SelectServerScreen screen;

        private boolean editable;

        public ServerListAdapter(ArrayList<ServerAddress> array, SelectServerScreen screen, boolean editable) {
            super(array);
            this.screen = screen;
            this.editable = editable;
        }

        @Override
        protected VisTable createView(final ServerAddress item) {
            VisTable result = new VisTable();

            Button root = new Button(skin, "buttonListItem");

            root.add(new VisLabel(item.name, "medium"))
                    .padLeft(20)
                    .growX()
                    .height(Gdx.graphics.getPpcY() * 0.7f);
            root.row();
            root.add(new VisLabel(String.format(Locale.US, "%s:%d", item.ip, item.port)))
                    .padLeft(30)
                    .growX()
                    .height(Gdx.graphics.getPpcY() * 0.6f);

            root.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    screen.onServerSelected(item);
                }
            });

            result.add(root)
                    .growX()
                    .padTop(5)
                    .padBottom(5);

            if (editable) {
                VisImageButton edit = new VisImageButton("buttonEditServer");
                edit.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        showEditDialog(screen, item, new Callback<ServerAddress>() {
                            @Override
                            public void onResult(ServerAddress result) {
                                item.set(result);
                                itemsChanged();
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                remove(item);
                            }
                        });
                    }
                });
                result.add(edit)
                        .width(Gdx.graphics.getPpcX() * 0.9f)
                        .height(Gdx.graphics.getPpcY() * 0.9f)
                        .align(Align.center)
                        .padTop(5)
                        .padBottom(5)
                        .padLeft(10);
            }

            return result;
        }
    }

    public SelectServerScreenGUI(AssetManager assetManager, SelectServerScreen screen, Stage guiStage) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);
        this.guiStage = guiStage;

        servers = new VisTable();
        officialServerList = new ListView<ServerAddress>(new ServerListAdapter(screen.getOfficialServers(), screen, false));
        customServerList = new ListView<ServerAddress>(new ServerListAdapter(screen.getCustomServers(), screen, true));
        guiStage.addActor(servers);

        reset(screen);
    }

    public void reset(SelectServerScreen screen) {
        resetServerListPanel(screen);
    }

    private void resetServerListPanel(final SelectServerScreen screen) {
        servers.clear();
        servers.setFillParent(true);

        int contentWidth = Gdx.graphics.getWidth() - 120;

        VisTable root = new VisTable();
        root.setBackground("windowCentered");

        root.add(new VisLabel(screen.getI18NBundle().get("serverList"), "large"))
                .expandX()
                .pad(5)
                .align(Align.center);
        root.row();

        VisTable scrollableContents = new VisTable();

        scrollableContents.add(new VisLabel(screen.getI18NBundle().get("officialServers"), "large"))
                .expandX()
                .align(Align.left);
        scrollableContents.row();

        scrollableContents.add(officialServerList.getMainTable())
                .width(contentWidth);
        scrollableContents.row();

        scrollableContents.add(new VisLabel(screen.getI18NBundle().get("customServers"), "large"))
                .expandX()
                .align(Align.left);
        scrollableContents.row();

        scrollableContents.add(customServerList.getMainTable())
                .width(contentWidth);
        scrollableContents.align(Align.top);

        VisScrollPane scrollPane = new VisScrollPane(scrollableContents);

        root.add(scrollPane)
                .width(contentWidth)
                .growY()
                .pad(5);
        root.row();

        VisImageButton add = new VisImageButton("buttonAddServer");
        add.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showEditDialog(screen, null, new Callback<ServerAddress>() {
                    @Override
                    public void onResult(ServerAddress result) {
                        customServerList.getAdapter().add(result);
                    }
                }, null);
            }
        });
        root.add(add)
                .width(Gdx.graphics.getPpcX() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 0.9f);

        servers.add(root)
                .grow();

        servers.center().pad(20);
    }

    private void showEditDialog(final SelectServerScreen screen, final ServerAddress serverAddress, final Callback<ServerAddress> onSave, final Runnable onRemove) {
        final VisDialog dialog = new VisDialog(screen.getI18NBundle().get(serverAddress == null ? "create" : "edit"));

        int contentWidth = Gdx.graphics.getWidth() - 200;

        final VisTextField name = new VisTextField(serverAddress == null ? "" : serverAddress.name);
        name.setMessageText(screen.getI18NBundle().get("serverName"));
        dialog.getContentTable().add(name)
                .minWidth(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f);
        dialog.getContentTable().row();

        final VisTextField ip = new VisTextField(serverAddress == null ? "" : serverAddress.ip);
        ip.setMessageText(screen.getI18NBundle().get("serverIp"));
        dialog.getContentTable().add(ip)
                .minWidth(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f);
        dialog.getContentTable().row();

        final VisTextField port = new VisTextField(serverAddress == null ? "" : String.valueOf(serverAddress.port));
        port.setMessageText(screen.getI18NBundle().get("serverPort"));
        dialog.getContentTable().add(port)
                .minWidth(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f);

        VisImageButton ok = new VisImageButton("buttonOk");
        ok.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ServerAddress created = screen.onCreateServerAddress(
                        name.getText(), ip.getText(), port.getText()
                );
                if (created != null) {
                    onSave.onResult(created);
                    dialog.hide();
                    Gdx.input.setOnscreenKeyboardVisible(false);
                }
            }
        });
        dialog.getButtonsTable().add(ok)
                .width(Gdx.graphics.getPpcX() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 0.9f);

        VisImageButton cancel = new VisImageButton("buttonBack");
        cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
                Gdx.input.setOnscreenKeyboardVisible(false);
            }
        });
        dialog.getButtonsTable().add(cancel)
                .width(Gdx.graphics.getPpcX() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 0.9f);

        if (serverAddress != null) {
            VisImageButton remove = new VisImageButton("buttonRemoveServer");
            remove.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    onRemove.run();
                    dialog.hide();
                    Gdx.input.setOnscreenKeyboardVisible(false);
                }
            });
            dialog.getButtonsTable().add(remove)
                    .width(Gdx.graphics.getPpcX() * 0.9f)
                    .height(Gdx.graphics.getPpcY() * 0.9f);
        }

        dialog.show(guiStage);
        dialog.setPosition(
                (Gdx.graphics.getWidth() - dialog.getWidth()) / 2,
                Gdx.graphics.getHeight() - dialog.getHeight() - 50
        );
    }
}
