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
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.research.ResearchInfo;

public final class BuildingGUI {
    private final Skin skin;

    public final VisDialog root;

    public final VisLabel buildingName;

    public final VisLabel description;

    public final VisLabel size;

    public final VisLabel strength;

    public final VisLabel energy;

    public final VisLabel maxCount;

    public final VisTextButton buildButton;

    private BuildingType buildingType;

    public BuildingGUI(AssetManager assetManager, GameScreen screen, BuildingType initialBuildingType) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);
        buildingType = initialBuildingType;

        root = new VisDialog(screen.getI18NBundle().get("building"));

        buildingName = new VisLabel("");
        description = new VisLabel("");
        size = new VisLabel("");
        strength = new VisLabel("");
        energy = new VisLabel("");
        maxCount = new VisLabel("");
        buildButton = new VisTextButton("");
        buildButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                root.hide();
            }
        });

        init(screen);
    }

    public void init(final GameScreen screen) {
        root.getContentTable().clear();
        root.getButtonsTable().clear();
        root.getContentTable().pad(20);

        Table buildingInfo = new Table(skin);
        buildingName.setAlignment(Align.left, Align.left);
        buildingInfo.add(buildingName).growX();
        buildingInfo.row();
        description.setAlignment(Align.left, Align.left);
        buildingInfo.add(description).growX();
        buildingInfo.row();
        size.setAlignment(Align.left, Align.left);
        buildingInfo.add(size).growX();
        buildingInfo.row();
        strength.setAlignment(Align.left, Align.left);
        buildingInfo.add(strength).growX();
        buildingInfo.row();
        energy.setAlignment(Align.left, Align.left);
        buildingInfo.add(energy).growX();
        buildingInfo.row();
        maxCount.setAlignment(Align.left, Align.left);
        buildingInfo.add(maxCount).growX();

        ArrayAdapter<BuildingType, VisTable> adapter = new ArrayAdapter<BuildingType, VisTable>(Array.with(BuildingType.values())) {
            @Override
            protected VisTable createView(BuildingType item) {
                VisTable result = new VisTable();

                VisLabel label = new VisLabel(screen.getI18NBundle().get(String.format("building%s", item.toString())));
                result.add(label)
                        .height(Math.max(50, (int) (Gdx.graphics.getPpcY() * 0.9f)));

                if (item == buildingType)
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

        final ListView<BuildingType> buildingTypes = new ListView<BuildingType>(adapter);

        buildingTypes.setItemClickListener(new ListView.ItemClickListener<BuildingType>() {
            @Override
            public void clicked(BuildingType item) {
                screen.getGameEvents().setSelectedBuildingType(item);
                setBuildingType(screen, item);
            }
        });

        adapter.getSelection().setSize(1);
        adapter.getSelection().set(0, buildingType);
        buildingTypes.getClickListener().clicked(buildingType);

        root.getContentTable().add(buildingTypes.getMainTable())
                .growX()
                .width(Gdx.graphics.getWidth() - 120)
                .pad(5);
        root.getContentTable().row();
        root.getContentTable().add(buildingInfo)
                .growX()
                .width(Gdx.graphics.getWidth() - 120)
                .pad(5);

        root.getButtonsTable().add(buildButton)
                .width(Math.max(Gdx.graphics.getPpcY() * 2, (int) (buildButton.getMinWidth() * 1.2f)))
                .height(Math.max(50, Gdx.graphics.getPpcY() * 0.9f));

        root.center();
    }

    public void setBuildingType(GameScreen screen, BuildingType buildingType) {
        this.buildingType = buildingType;
        buildingName.setText(screen.getI18NBundle().get(String.format("building%s", buildingType.toString())));
        description.setText(screen.getI18NBundle().get(String.format("building%sDescription", buildingType.toString())));
        size.setText(screen.getI18NBundle().format("buildingSize", buildingType.sizeX, buildingType.sizeY));
        strength.setText(screen.getI18NBundle().format("buildingStrength", buildingType.healthBonus));
        energy.setText(screen.getI18NBundle().format("buildingEnergy",buildingType.getEnergyDelta(new ResearchInfo(0, 0, 0))));
        if (buildingType.maxCount != Integer.MAX_VALUE)
            maxCount.setText(screen.getI18NBundle().format("buildingMaxCount", buildingType.maxCount));
        else
            maxCount.setText("");
        buildButton.setText(screen.getI18NBundle().format("buildingBuild", buildingType.cost));
    }

    // Returns type of building
    public BuildingType getBuildingType() {
        return buildingType;
    }
}
