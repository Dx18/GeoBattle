package geobattle.geobattle.screens.emailconfirmationscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisImageButton;
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

    // Table with button which hides keyboard
    public final VisTable hideKeyboard;

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

        hideKeyboard = new VisTable();
        guiStage.addActor(hideKeyboard);

        reset(screen);
    }

    public void reset(EmailConfirmationScreen screen) {
        initEmailConfirmation(screen);
        initHideKeyboard();
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
        emailConfirmation.center().pad(20).top().padTop(40 + Gdx.graphics.getPpcY() * 0.9f);
    }

    // Initializes hide keyboard button
    private void initHideKeyboard() {
        hideKeyboard.clear();
        hideKeyboard.setFillParent(true);

        VisImageButton hide = new VisImageButton("buttonHideKeyboard");
        hide.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.input.setOnscreenKeyboardVisible(false);
            }
        });
        hideKeyboard.add(hide)
                .width(Gdx.graphics.getPpcY() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 0.9f);

        hideKeyboard.top().padTop(20).right().padRight(20);
    }
}
