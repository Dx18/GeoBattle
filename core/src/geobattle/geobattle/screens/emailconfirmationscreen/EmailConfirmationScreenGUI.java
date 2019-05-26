package geobattle.geobattle.screens.emailconfirmationscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

import geobattle.geobattle.GeoBattleAssets;

public final class EmailConfirmationScreenGUI {
    private final Skin skin;

    private final Stage guiStage;

    public final VisTable emailConfirmation;

    public final VisTextField playerNameField;

    public final VisTextField codeField;

    public EmailConfirmationScreenGUI(AssetManager assetManager, final EmailConfirmationScreen screen, final Stage guiStage, String name) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);
        this.guiStage = guiStage;

        emailConfirmation = new VisTable();
        playerNameField = new VisTextField(name == null ? "" : name);
        playerNameField.setMessageText(screen.getI18NBundle().get("userName"));
        playerNameField.setAlignment(Align.center);
        codeField = new VisTextField("");
        codeField.setMessageText(screen.getI18NBundle().get("codeFromEmail"));
        codeField.setAlignment(Align.center);
        codeField.setMaxLength(4);
        codeField.setTextFieldFilter(new VisTextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(VisTextField textField, char c) {
                return c >= '0' && c <= '9';
            }
        });
        guiStage.addActor(emailConfirmation);

        reset(screen);
    }

    public void reset(EmailConfirmationScreen screen) {
        initEmailConfirmation(screen);
    }

    private void initEmailConfirmation(final EmailConfirmationScreen screen) {
        emailConfirmation.clear();
        emailConfirmation.setFillParent(true);

        int contentWidth = Gdx.graphics.getWidth() - 120;

        VisTable root = new VisTable();
        root.setBackground("windowCentered");

        root.add(new VisLabel(screen.getI18NBundle().get("confirmEmail"), "large"))
                .expandX()
                .pad(5)
                .align(Align.center);
        root.row();

        root.add(playerNameField)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        root.add(codeField)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        VisTextButton confirm = new VisTextButton(screen.getI18NBundle().get("confirm"), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onConfirm(playerNameField.getText(), Integer.parseInt(codeField.getText()));
            }
        });
        root.add(confirm)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        VisTextButton resend = new VisTextButton(screen.getI18NBundle().get("resend"), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onResend(playerNameField.getText());
            }
        });
        root.add(resend)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        emailConfirmation.add(root);
        emailConfirmation.center().pad(20).top().padTop(20);
    }
}
