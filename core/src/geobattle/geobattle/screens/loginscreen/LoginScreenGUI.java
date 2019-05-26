package geobattle.geobattle.screens.loginscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

import geobattle.geobattle.GeoBattleAssets;

// GUI for login screen
final class LoginScreenGUI {
    // GUI skin
    private final Skin skin;

    // Switch between "Login" and "Register" modes
    public final VisTable loginRegisterSwitch;

    // Login form
    public final VisTable loginScreen;

    // User name in login form
    public final VisTextField loginUserName;

    // Password in login form
    public final VisTextField loginPassword;

    // Register form
    public final VisTable registerScreen;

    // User name in register form
    public final VisTextField registerUserName;

    // Email in register form
    public final VisTextField registerEmail;

    // Password in register form
    public final VisTextField registerPassword;

    // Repeat password in register form
    public final VisTextField registerRepeatPassword;

    // Pick color form
    public final VisTable pickColorScreen;

    // Picked color
    public final VisImage pickColorResult;

    // Red component of picked color
    public final VisSlider pickColorR;

    // Green component of picked color
    public final VisSlider pickColorG;

    // Blue component of picked color
    public final VisSlider pickColorB;

    public LoginScreenGUI(AssetManager assetManager, final LoginScreen screen, final Stage guiStage) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);

        loginRegisterSwitch = new VisTable();
        guiStage.addActor(loginRegisterSwitch);

        loginScreen = new VisTable();
        loginUserName = new VisTextField("");
        loginUserName.setMessageText(screen.getI18NBundle().get("userName"));
        loginUserName.setAlignment(Align.center);
        loginPassword = new VisTextField("");
        loginPassword.setMessageText(screen.getI18NBundle().get("password"));
        loginPassword.setAlignment(Align.center);
        loginPassword.setPasswordMode(true);
        loginPassword.setPasswordCharacter('*');
        guiStage.addActor(loginScreen);

        registerScreen = new VisTable();
        registerUserName = new VisTextField("");
        registerUserName.setMessageText(screen.getI18NBundle().get("userName"));
        registerUserName.setAlignment(Align.center);
        registerEmail = new VisTextField("");
        registerEmail.setMessageText(screen.getI18NBundle().get("email"));
        registerEmail.setAlignment(Align.center);
        registerPassword = new VisTextField("");
        registerPassword.setMessageText(screen.getI18NBundle().get("password"));
        registerPassword.setAlignment(Align.center);
        registerPassword.setPasswordMode(true);
        registerPassword.setPasswordCharacter('*');
        registerRepeatPassword = new VisTextField("");
        registerRepeatPassword.setMessageText(screen.getI18NBundle().get("repeatPassword"));
        registerRepeatPassword.setAlignment(Align.center);
        registerRepeatPassword.setPasswordMode(true);
        registerRepeatPassword.setPasswordCharacter('*');
        guiStage.addActor(registerScreen);

        EventListener updateColor = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                pickColorResult.setColor(pickColorR.getValue(), pickColorG.getValue(), pickColorB.getValue(), 1);
            }
        };

        pickColorScreen = new VisTable();
        pickColorResult = new VisImage(new TextureRegionDrawable(assetManager.get(GeoBattleAssets.COLOR, Texture.class)));
        pickColorR = new VisSlider(0, 1, 1 / 255f, false);
        pickColorR.setValue((float) Math.random());
        pickColorR.addListener(updateColor);
        pickColorG = new VisSlider(0, 1, 1 / 255f, false);
        pickColorG.setValue((float) Math.random());
        pickColorG.addListener(updateColor);
        pickColorB = new VisSlider(0, 1, 1 / 255f, false);
        pickColorB.setValue((float) Math.random());
        pickColorB.addListener(updateColor);
        ((ChangeListener) updateColor).changed(null, null);
        guiStage.addActor(pickColorScreen);

        reset(screen);
    }

    // Resets all forms
    public void reset(LoginScreen screen) {
        initLoginRegisterSwitch(screen);
        initLoginScreen(screen);
        initRegisterScreen(screen);
        initPickColorScreen(screen);
        setMode(LoginScreenMode.LOGIN);
    }

    // Initializes "Login <-> Register" switch
    private void initLoginRegisterSwitch(final LoginScreen screen) {
        loginRegisterSwitch.reset();
        loginRegisterSwitch.setFillParent(true);

        VisTextButton loginMode = new VisTextButton(screen.getI18NBundle().get("login"), "menu", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setMode(LoginScreenMode.LOGIN);
            }
        });
        loginRegisterSwitch.add(loginMode)
                .growX()
                // .width(Gdx.graphics.getWidth() / 2)
                .height(Gdx.graphics.getPpcY());

        VisTextButton registerMode = new VisTextButton(screen.getI18NBundle().get("register"), "menu", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setMode(LoginScreenMode.REGISTER);
            }
        });
        loginRegisterSwitch.add(registerMode)
                .growX()
                // .width(Gdx.graphics.getWidth() / 2)
                .height(Gdx.graphics.getPpcY());

        loginRegisterSwitch.row();

        VisTextButton emailConfirmation = new VisTextButton(screen.getI18NBundle().get("confirmEmail"), "menu", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onEmailConfirmation();
            }
        });
        loginRegisterSwitch.add(emailConfirmation)
                .growX()
                .height(Gdx.graphics.getPpcY())
                .padTop(10)
                .colspan(2);

        loginRegisterSwitch.center().bottom().padBottom(20);
    }

    // Initializes login form
    private void initLoginScreen(final LoginScreen screen) {
        loginScreen.reset();
        loginScreen.setFillParent(true);

        int contentWidth = Gdx.graphics.getWidth() - 120;

        VisTable root = new VisTable();
        root.setBackground("windowCentered");

        root.add(new VisLabel(screen.getI18NBundle().get("login"), "large"))
                .expandX()
                .pad(5)
                .align(Align.center);
        root.row();

        root.add(loginUserName)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        root.add(loginPassword)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        VisTextButton login = new VisTextButton(screen.getI18NBundle().get("login"), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onLogin();
            }
        });
        root.add(login)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);

        loginScreen.add(root);
        loginScreen.center().pad(20).top().padTop(20);
    }

    // Initializes register form
    private void initRegisterScreen(final LoginScreen screen) {
        registerScreen.reset();
        registerScreen.setFillParent(true);

        int contentWidth = Gdx.graphics.getWidth() - 120;

        VisTable root = new VisTable();
        root.setBackground("windowCentered");

        root.add(new VisLabel(screen.getI18NBundle().get("register"), "large"))
                .expandX()
                .pad(5)
                .align(Align.center);
        root.row();

        root.add(registerUserName)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        root.add(registerEmail)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        root.add(registerPassword)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        root.add(registerRepeatPassword)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        VisTextButton registerPickColor = new VisTextButton(screen.getI18NBundle().get("pickColor"), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setMode(LoginScreenMode.PICK_COLOR);
            }
        });
        root.add(registerPickColor)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        VisTextButton register = new VisTextButton(screen.getI18NBundle().get("register"), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onRegister();
            }
        });
        root.add(register)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);

        registerScreen.add(root);
        registerScreen.center().pad(20).top().padTop(20);
    }

    private void initPickColorScreen(LoginScreen screen) {
        pickColorScreen.reset();
        pickColorScreen.setFillParent(true);

        int contentWidth = Gdx.graphics.getWidth() - 120;

        VisTable root = new VisTable();
        root.setBackground("windowCentered");

        root.add(new VisLabel(screen.getI18NBundle().get("pickColor"), "large"))
                .expandX()
                .pad(5)
                .align(Align.center);
        root.row();

        root.add(pickColorResult)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        root.add(pickColorR)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        root.add(pickColorG)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        root.add(pickColorB)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.row();

        VisTextButton pickColor = new VisTextButton(screen.getI18NBundle().get("pickColor"), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setMode(LoginScreenMode.REGISTER);
            }
        });
        root.add(pickColor)
                .width(contentWidth)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);

        pickColorScreen.add(root);
        pickColorScreen.center().pad(20).top().padTop(20);
    }

    // Sets mode of GUI
    public void setMode(LoginScreenMode mode) {
        loginScreen.setVisible(false);
        registerScreen.setVisible(false);
        pickColorScreen.setVisible(false);
        switch (mode) {
            case LOGIN:
                loginScreen.setVisible(true);
                break;
            case REGISTER:
                registerScreen.setVisible(true);
                break;
            case PICK_COLOR:
                pickColorScreen.setVisible(true);
                break;
        }
    }
}
