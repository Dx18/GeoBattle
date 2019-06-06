package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.game.research.ResearchInfo;
import geobattle.geobattle.game.research.ResearchType;

public final class ResearchGUI {
    public class ResearchTypeItem {
        // Root of item
        public final VisTable root;

        // Type of research
        public final ResearchType researchType;

        // Value change
        private final VisLabel valueChange;

        // Level indicators
        private final VisImage[] indicators;

        // Button performing research
        private final VisTextButton researchButton;

        // Cost of next research
        private int researchCost;

        // True if max level of research reached
        private boolean researched;

        public ResearchTypeItem(final GameScreen screen, final ResearchInfo researchInfo, final ResearchType researchType, Table itemsTable) {
            root = new VisTable();

            this.researchType = researchType;

            valueChange = new VisLabel("");
            indicators = new VisImage[researchType.getLevelCount()];
            for (int level = 0; level < researchType.getLevelCount(); level++)
                indicators[level] = new VisImage("researchIndicatorOff");
            researchButton = new VisTextButton("", new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // researchInfo.incrementLevel(researchType);
                    // setResearchLevel(researchInfo.getLevel(researchType));
                    screen.getGameEvents().onResearchEvent(researchType);
                    Gdx.app.log("GeoBattle", "Research");
                }
            });

            init(researchInfo, itemsTable, screen);
        }

        public void init(final ResearchInfo researchInfo, Table itemsTable, GameScreen screen) {
            root.clear();

            VisLabel researchName = new VisLabel(screen.getI18NBundle().get(String.format("research%s", researchType.toString())));
            root.add(researchName)
                    .growX()
                    .padBottom(10)
                    .colspan(researchType.getLevelCount());
            root.add(valueChange);
            root.row();

            for (int level = 0; level < researchType.getLevelCount(); level++)
                root.add(indicators[level])
                        .width(indicators[level].getDrawable().getMinWidth())
                        .padLeft(5)
                        .padRight(5)
                        .expand(level == researchType.getLevelCount() - 1, false)
                        .align(Align.left);

            root.add(researchButton)
                    .width(130)
                    .height(Math.max(50, Gdx.graphics.getPpcY() * 0.9f))
                    .align(Align.right);

            itemsTable.add(root)
                    .width(Gdx.graphics.getWidth() - 80)
                    .pad(5);
            itemsTable.row();

            setResearchLevel(researchInfo.getLevel(researchType));
        }

        // Sets research level
        public void setResearchLevel(int level) {
            for (int indicator = 0; indicator < indicators.length; indicator++) {
                indicators[indicator].setDrawable(skin, level > indicator
                        ? "researchIndicatorOn"
                        : "researchIndicatorOff"
                );
            }
            researchCost = researchType.getCost(level + 1);
            if (researchCost == Integer.MAX_VALUE) {
                researchButton.setDisabled(true);
                researchButton.setText("");
                valueChange.setText(researchType.getValue(level) + "");
                researched = true;
            } else {
                researchButton.setDisabled(false);
                researchButton.setText(researchCost + "");
                valueChange.setText(
                        researchType.getValue(level) +
                        " > " +
                        researchType.getValue(level + 1)
                );
                researched = false;
            }
        }
    }

    // GUI skin
    private final Skin skin;

    // Root of GUI
    public final VisDialog root;

    // Types of research
    private final ResearchTypeItem[] researchTypes;

    public ResearchGUI(AssetManager assetManager, final GameScreen screen) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);

        root = new VisDialog(screen.getI18NBundle().get("research"));

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

        root.getContentTable().padTop(20);

        init(screen);
    }

    public void init(GameScreen screen) {
        root.getContentTable().clear();
        root.getButtonsTable().clear();

        final ResearchInfo researchInfo = screen.getGameEvents().gameState.getCurrentPlayer().getResearchInfo();
        int index = 0;
        for (final ResearchType ignored : ResearchType.values()) {
            researchTypes[index].init(researchInfo, root.getContentTable(), screen);
            index++;
        }

        VisTextButton close = new VisTextButton(screen.getI18NBundle().get("close"));
        close.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                root.hide();
            }
        });
        root.getButtonsTable().add(close)
                .width(Math.max(Gdx.graphics.getPpcY() * 2, close.getMinWidth()))
                .height(Math.max(50, Gdx.graphics.getPpcY() * 0.9f));

        root.center();
    }

    // Sets research info
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
    public void unlockButtons(int resources) {
        for (ResearchTypeItem researchTypeItem : researchTypes) {
            researchTypeItem.researchButton.setDisabled(
                    researchTypeItem.researched || resources < researchTypeItem.researchCost
            );
        }
    }
}
