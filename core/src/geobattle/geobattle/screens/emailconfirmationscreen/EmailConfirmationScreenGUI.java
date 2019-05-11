package geobattle.geobattle.screens.emailconfirmationscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import geobattle.geobattle.GeoBattleAssets;

public final class EmailConfirmationScreenGUI {
    private final Skin skin;

    private final Stage guiStage;

    public final Table emailConfirmation;

    public final TextField playerNameField;

    public final TextField codeField;

    public EmailConfirmationScreenGUI(AssetManager assetManager, final EmailConfirmationScreen screen, final Stage guiStage, String name) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);
        this.guiStage = guiStage;

        emailConfirmation = new Table();
        playerNameField = new TextField(name == null ? "" : name, skin);
        playerNameField.setMessageText("Name...");
        playerNameField.setAlignment(Align.center);
        codeField = new TextField("", skin);
        codeField.setMessageText("Code from email...");
        codeField.setAlignment(Align.center);
        codeField.setMaxLength(4);
        codeField.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
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

        Label title = new Label("CONFIRM EMAIL", skin, "black");
        emailConfirmation.add(title)
                .expandX()
                .height(Gdx.graphics.getPpcY());
        emailConfirmation.row();

        emailConfirmation.add(playerNameField)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        emailConfirmation.row();

        emailConfirmation.add(codeField)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        emailConfirmation.row();

        TextButton confirm = new TextButton("Confirm", skin);
        confirm.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onConfirm(playerNameField.getText(), Integer.parseInt(codeField.getText()));
            }
        });
        emailConfirmation.add(confirm)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(15);
        emailConfirmation.row();

        TextButton resend = new TextButton("Resend email", skin);
        resend.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onResend(playerNameField.getText());
            }
        });
        emailConfirmation.add(resend)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        emailConfirmation.row();

        TextButton back = new TextButton("Back", skin);
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onReturn();
            }
        });
        emailConfirmation.add(back)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);

        emailConfirmation.center().pad(20).top().padTop(20);
    }
}
