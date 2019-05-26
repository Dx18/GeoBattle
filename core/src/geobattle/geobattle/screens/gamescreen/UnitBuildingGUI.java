package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.adapter.AbstractListAdapter;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.widget.ListView;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.game.units.UnitType;

public final class UnitBuildingGUI {
    private final Skin skin;

    public final VisDialog root;

    public final VisLabel unitName;

    public final VisLabel description;

    public final VisLabel strength;

    public final VisTextButton buildButton;

    private UnitType unitType;

    public UnitBuildingGUI(AssetManager assetManager, final GameScreen screen) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);

        unitType = UnitType.BOMBER;

        root = new VisDialog(screen.getI18NBundle().get("unitBuilding"));

        unitName = new VisLabel("");
        description = new VisLabel("");
        strength = new VisLabel("");
        buildButton = new VisTextButton("");
        buildButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().onUnitBuild(unitType);
                // root.hide();
            }
        });

        init(screen);
    }

    public void init(final GameScreen screen) {
        root.getContentTable().clear();
        root.getButtonsTable().clear();
        root.getContentTable().pad(20);

        Table unitInfo = new Table(skin);
        unitName.setAlignment(Align.left, Align.left);
        unitInfo.add(unitName).growX();
        unitInfo.row();
        description.setAlignment(Align.left, Align.left);
        unitInfo.add(description).growX();
        unitInfo.row();
        strength.setAlignment(Align.left, Align.left);
        unitInfo.add(strength).growX();
        unitInfo.row();

        ArrayAdapter<UnitType, VisTable> adapter = new ArrayAdapter<UnitType, VisTable>(Array.with(new UnitType[] { UnitType.BOMBER })) {
            @Override
            protected VisTable createView(UnitType item) {
                VisTable result = new VisTable();

                VisLabel label = new VisLabel(screen.getI18NBundle().get(String.format("unit%s", item.toString())));
                result.add(label)
                        .height(Math.max(50, (int) (Gdx.graphics.getPpcY() * 0.9f)));

                if (item == unitType)
                    selectView(result);
                else
                    deselectView(result);

                return result;
            }

            @Override
            protected void selectView(VisTable view) {
                view.setBackground("listItemSelected");
            }

            @Override
            protected void deselectView(VisTable view) {
                view.setBackground("listItemDeselected");
            }
        };

        adapter.setSelectionMode(AbstractListAdapter.SelectionMode.SINGLE);

        final ListView<UnitType> unitTypes = new ListView<UnitType>(adapter);

        unitTypes.setItemClickListener(new ListView.ItemClickListener<UnitType>() {
            @Override
            public void clicked(UnitType item) {
                setUnitType(screen, item);
            }
        });

        adapter.getSelection().setSize(1);
        adapter.getSelection().set(0, unitType);
        unitTypes.getClickListener().clicked(unitType);

        root.getContentTable().add(unitTypes.getMainTable())
                .growX()
                .width(Gdx.graphics.getWidth() - 120)
                .pad(5);
        root.getContentTable().row();
        root.getContentTable().add(unitInfo)
                .growX()
                .width(Gdx.graphics.getWidth() - 120)
                .pad(5);

        root.getButtonsTable().add(buildButton)
                .width(Math.max(Gdx.graphics.getPpcY() * 2, (int) (buildButton.getMinWidth() * 1.2f)))
                .height(Math.max(50, Gdx.graphics.getPpcY() * 0.9f));

        VisTextButton close = new VisTextButton(screen.getI18NBundle().get("close"), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                root.hide();
            }
        });

        root.getButtonsTable().add(close)
                .width(Math.max(Gdx.graphics.getPpcY() * 2, (int) (buildButton.getMinWidth() * 1.2f)))
                .height(Math.max(50, Gdx.graphics.getPpcY() * 0.9f));

        root.center();
    }

    public void setUnitType(GameScreen screen, UnitType unitType) {
        unitName.setText(screen.getI18NBundle().get(String.format("unit%s", unitType.toString())));
        description.setText(screen.getI18NBundle().get(String.format("unit%sDescription", unitType.toString())));
        strength.setText(screen.getI18NBundle().format("unitStrength", unitType.maxHealth));
        buildButton.setText(screen.getI18NBundle().format("unitBuild", unitType.cost));
    }
}
