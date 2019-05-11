package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.game.research.ResearchInfo;
import geobattle.geobattle.game.research.ResearchType;

public final class ResearchGUI {
    public class ResearchTypeItem {
        public final Table root;

        public final ResearchType researchType;

        private final Label valueChange;

        private final Image[] indicators;

        private final TextButton researchButton;

        private boolean researched;

        public ResearchTypeItem(final GameScreen screen, final ResearchInfo researchInfo, final ResearchType researchType, Table itemsTable) {
            root = new Table();

            this.researchType = researchType;

            valueChange = new Label("", skin);
            indicators = new Image[researchType.getLevelCount()];
            for (int level = 0; level < researchType.getLevelCount(); level++)
                indicators[level] = new Image(skin, "rect");
            researchButton = new TextButton("", skin);
            researchButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // researchInfo.incrementLevel(researchType);
                    // setResearchLevel(researchInfo.getLevel(researchType));
                    screen.getGameEvents().onResearch(researchType);
                }
            });

            init(researchInfo, itemsTable);
        }

        public void init(final ResearchInfo researchInfo, Table itemsTable) {
            root.clear();

            Label researchName = new Label(researchType.name, skin);
            root.add(researchName)
                    .expandX()
                    .fillX()
                    .colspan(researchType.getLevelCount());
            root.add(valueChange);
            root.row();

            for (int level = 0; level < researchType.getLevelCount(); level++)
                root.add(indicators[level])
                        .width(Gdx.graphics.getPpcX() / 3)
                        .padLeft(5)
                        .padRight(5)
                        .expand(level == researchType.getLevelCount() - 1, false)
                        .align(Align.left);

            GlyphLayout glyphLayout = new GlyphLayout();
            glyphLayout.setText(skin.getFont("default"), "16000");
            root.add(researchButton)
                    .width(glyphLayout.width + 40)
                    .align(Align.right);

            itemsTable.add(root)
                    .expandX()
                    .fillX()
                    .width(Gdx.graphics.getWidth() - 40);
            itemsTable.row();

            setResearchLevel(researchInfo.getLevel(researchType));
        }

        public void setResearchLevel(int level) {
            for (int indicator = 0; indicator < indicators.length; indicator++)
                indicators[indicator].setColor(level > indicator
                        ? new Color(0.4f, 0.4f, 0.4f, 1)
                        : new Color(1, 1, 1, 1)
                );
            int newCost = researchType.getCost(level + 1);
            if (newCost == Integer.MAX_VALUE) {
                researchButton.setDisabled(true);
                researchButton.setText("");
                valueChange.setText(researchType.getValue(level) + "");
                researched = true;
            } else {
                researchButton.setText(newCost + "");
                valueChange.setText(
                        researchType.getValue(level) +
                        " > " +
                        researchType.getValue(level + 1)
                );
                researched = false;
            }
        }
    }

    private final Skin skin;

    public final Dialog root;

    private final ResearchTypeItem[] researchTypes;

    public ResearchGUI(AssetManager assetManager, final GameScreen screen) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);

        root = new Dialog("", skin);

        researchTypes = new ResearchTypeItem[ResearchType.values().length];
        int index = 0;
        for (ResearchType researchType : ResearchType.values()) {
            researchTypes[index] = new ResearchTypeItem(
                    screen,
                    screen.getGameEvents().gameState.getCurrentPlayer().getResearchInfo(),
                    researchType,
                    root.getContentTable()
            );
            index++;
        }

        init(screen);
    }

    public void init(GameScreen screen) {
        root.getContentTable().clear();
        root.getButtonTable().clear();
        root.getContentTable().setFillParent(true);

        Label title = new Label("Research", skin);
        root.getContentTable().add(title)
                .expandX()
                .width(Gdx.graphics.getWidth() - 40)
                .height(Gdx.graphics.getPpcY());
        root.getContentTable().row();

        final ResearchInfo researchInfo = screen.getGameEvents().gameState.getCurrentPlayer().getResearchInfo();
        int index = 0;
        for (final ResearchType ignored : ResearchType.values()) {
            researchTypes[index].init(researchInfo, root.getContentTable());
            index++;
        }

        TextButton close = new TextButton("Close", skin);
        close.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                root.hide();
            }
        });
        root.getContentTable().add(close);

        root.getContentTable().pad(20);
        root.center();
    }

    public void setResearchInfo(ResearchInfo researchInfo) {
        for (ResearchTypeItem researchTypeItem : researchTypes)
            researchTypeItem.setResearchLevel(researchInfo.getLevel(researchTypeItem.researchType));
    }

    // Locks research buttons
    public void lockButtons() {
        for (ResearchTypeItem researchTypeItem : researchTypes)
            researchTypeItem.researchButton.setDisabled(true);
    }

    // Unlocks research buttons
    public void unlockButtons() {
        for (ResearchTypeItem researchTypeItem : researchTypes)
            if (!researchTypeItem.researched)
                researchTypeItem.researchButton.setDisabled(false);
    }
}
