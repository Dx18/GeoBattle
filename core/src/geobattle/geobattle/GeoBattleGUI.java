package geobattle.geobattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.adapter.ArrayListAdapter;
import com.kotcrab.vis.ui.widget.ListView;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

import java.util.ArrayList;

public final class GeoBattleGUI {
    public static class GeoBattleMessage {
        public final String message;

        private float timeLeft;

        public GeoBattleMessage(String message, float timeLeft) {
            this.message = message;
            this.timeLeft = timeLeft;
        }

        public void update(float delta) {
            timeLeft -= delta;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    private final Skin skin;

    public final VisTable root;

    public final ListView<GeoBattleMessage> messagesList;

    private final ArrayListAdapter<GeoBattleMessage, VisTable> adapter;

    private final ArrayList<GeoBattleMessage> messages;

    private final int maxCount;

    public GeoBattleGUI(AssetManager assetManager, GeoBattle game, Stage guiStage, int maxCount) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);
        messages = new ArrayList<GeoBattleMessage>(maxCount);
        this.maxCount = maxCount;

        adapter = new ArrayListAdapter<GeoBattleMessage, VisTable>(messages) {
            @Override
            protected VisTable createView(GeoBattleMessage item) {
                VisTable result = new VisTable();

                if (item == null)
                    return result;


                VisTable background = new VisTable();
                background.setBackground("message");

                VisLabel message = new VisLabel(item.message);
                message.setAlignment(Align.center, Align.center);
                background.add(message)
                        .expandX()
                        .height(50)
                        .padLeft(30)
                        .padRight(30)
                        .align(Align.center);

                result.add(background)
                        .growX()
                        .pad(10);

                return result;
            }
        };

        root = new VisTable();
        messagesList = new ListView<GeoBattleMessage>(adapter);
        guiStage.addActor(root);

        reset(game);
    }

    public void reset(GeoBattle game) {
        initMessages();
    }

    private void initMessages() {
        root.clear();
        root.setFillParent(true);

        root.add(messagesList.getMainTable())
                .width(Gdx.graphics.getWidth() - 120);

        root.center().pad(20).bottom().pad(Gdx.graphics.getHeight() / 3);
    }

    public void update(float delta) {
        for (GeoBattleMessage message : messages)
            message.update(delta);

        int toRemove = 0;
        while (toRemove < messages.size() && messages.get(toRemove).timeLeft <= 0)
            toRemove++;

        for (int i = 0; i < toRemove; i++)
            messages.remove(0);

        setItems();
    }

    public void addMessage(GeoBattleMessage message) {
        if (messages.size() > maxCount)
            messages.remove(0);
        messages.add(message);

        setItems();
    }

    private void setItems() {
        adapter.itemsDataChanged();
    }
}
