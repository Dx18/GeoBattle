package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.game.buildings.BuildingType;

public final class BuildingGUI {
    private final Skin skin;

    public final Dialog root;

    public final Label buildingName;

    public final Label description;

    public final Label size;

    public final Label strength;

    public final Label energy;

    public final Label maxCount;

    public final TextButton buildButton;

    private BuildingType buildingType;

    public BuildingGUI(AssetManager assetManager, final GameScreen screen, BuildingType initialBuildingType) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);
        buildingType = initialBuildingType;

        root = new Dialog("", skin);

        buildingName = new Label("", skin);
        description = new Label("", skin);
        size = new Label("", skin);
        strength = new Label("", skin);
        energy = new Label("", skin);
        maxCount = new Label("", skin);
        buildButton = new TextButton("", skin);

        init(screen);
    }

    public void init(final GameScreen screen) {
        root.getContentTable().clear();
        root.getContentTable().pad(20);

        Label title = new Label("Building", skin);
        root.getContentTable().add(title)
                .expandX()
                .width(Gdx.graphics.getWidth() - 40)
                .colspan(2)
                .height(Gdx.graphics.getPpcY());
        root.getContentTable().row();

        Table buildingInfo = new Table(skin);
        buildingName.setAlignment(Align.left, Align.left);
        buildingInfo.add(buildingName)
                .expandX()
                .fillX();
        buildingInfo.row();
        description.setAlignment(Align.left, Align.left);
        buildingInfo.add(description)
                .expandX()
                .fillX();
        buildingInfo.row();
        size.setAlignment(Align.left, Align.left);
        buildingInfo.add(size)
                .expandX()
                .fillX();
        buildingInfo.row();
        strength.setAlignment(Align.left, Align.left);
        buildingInfo.add(strength)
                .expandX()
                .fillX();
        buildingInfo.row();
        energy.setAlignment(Align.left, Align.left);
        buildingInfo.add(energy)
                .expandX()
                .fillX();
        buildingInfo.row();
        maxCount.setAlignment(Align.left, Align.left);
        buildingInfo.add(maxCount)
                .expandX()
                .fillX();
        buildingInfo.row();
        buildingInfo.add(buildButton)
                .expandX()
                .height(Gdx.graphics.getPpcY())
                .bottom()
                .right();
        buildingInfo.top();

        final List<BuildingType> buildingTypes = new List<BuildingType>(skin);
        buildingTypes.setItems(BuildingType.values());

        buildingTypes.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setBuildingType(buildingTypes.getSelected());
            }
        });

        buildingTypes.setSelected(buildingType);

        buildButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().setSelectedBuildingType(buildingTypes.getSelected());
                root.hide();
            }
        });

        root.getContentTable().add(buildingTypes)
                .width((Gdx.graphics.getWidth() - 60) * 0.4f)
                .fill()
                .expand()
                .pad(5);
        root.getContentTable().add(buildingInfo)
                .width((Gdx.graphics.getWidth() - 60) * 0.6f)
                .fill()
                .expand()
                .pad(5);

        root.getContentTable().setFillParent(true);

        root.center();
    }

    public void setBuildingType(BuildingType buildingType) {
        buildingName.setText(buildingType.toString());
        description.setText("Just " + buildingType.toString());
        size.setText("Size: " + buildingType.sizeX + " x " + buildingType.sizeY);
        strength.setText("Strength: " + buildingType.healthBonus);
        energy.setText("Energy: " + buildingType.energyDelta);
        if (buildingType.maxCount != Integer.MAX_VALUE)
            maxCount.setText("Max count: " + buildingType.maxCount);
        else
            maxCount.setText("");
        buildButton.setText("Build: " + buildingType.cost);
    }
}
