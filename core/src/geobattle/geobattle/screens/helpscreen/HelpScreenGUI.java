package geobattle.geobattle.screens.helpscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;

import geobattle.geobattle.GeoBattleAssets;

public final class HelpScreenGUI {
    private final Skin skin;

    public final Table help;

    public HelpScreenGUI(AssetManager assetManager, HelpScreen screen, Stage guiStage) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);

        help = new Table();
        guiStage.addActor(help);

        reset(screen);
    }

    public void reset(HelpScreen screen) {
        resetHelp(screen);
    }

    private void resetHelp(HelpScreen screen) {
        help.reset();
        help.setFillParent(true);

        int contentWidth = Gdx.graphics.getWidth() - 120;

        VisTable root = new VisTable();
        root.setBackground("windowCentered");

        root.add(new VisLabel(screen.getI18NBundle().get("help"), "large"))
                .expandX()
                .pad(5)
                .align(Align.center);
        root.row();

        String text = Gdx.files.internal(GeoBattleAssets.HELP).readString();
        VisLabel contents = new VisLabel(text);
        contents.setWrap(true);
        contents.setAlignment(Align.topLeft, Align.left);

        VisScrollPane scrollPane = new VisScrollPane(contents);

        root.add(scrollPane)
                .width(contentWidth)
                .growY()
                .pad(5);

        help.add(root)
                .grow();

        help.center().pad(20);
    }
}
