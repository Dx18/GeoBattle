package geobattle.geobattle.screens.loginscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import geobattle.geobattle.GeoBattleAssets;

// GUI for login screen
final class LoginScreenGUI {
    // GUI skin
    private final Skin skin;

    // Switch between "Login" and "Register" modes
    public final Table loginRegisterSwitch;

    // Login form
    public final Table loginScreen;

    // User name in login form
    public final TextField loginUserName;

    // Password in login form
    public final TextField loginPassword;

    // Register form
    public final Table registerScreen;

    // User name in register form
    public final TextField registerUserName;

    // Email in register form
    public final TextField registerEmail;

    // Password in register form
    public final TextField registerPassword;

    // Repeat password in register form
    public final TextField registerRepeatPassword;

    // Pick color form
    public final Table pickColorScreen;

    // Picked color
    public final Image pickColorResult;

    // Red component of picked color
    public final Slider pickColorR;

    // Green component of picked color
    public final Slider pickColorG;

    // Blue component of picked color
    public final Slider pickColorB;

    public LoginScreenGUI(AssetManager assetManager, final LoginScreen screen, final Stage guiStage) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);

        loginRegisterSwitch = new Table();
        guiStage.addActor(loginRegisterSwitch);

        loginScreen = new Table();
        loginUserName = new TextField("", skin);
        loginUserName.setMessageText("User name...");
        loginUserName.setAlignment(Align.center);
        loginPassword = new TextField("", skin);
        loginPassword.setMessageText("Password...");
        loginPassword.setAlignment(Align.center);
        loginPassword.setPasswordMode(true);
        loginPassword.setPasswordCharacter('*');
        guiStage.addActor(loginScreen);

        registerScreen = new Table();
        registerUserName = new TextField("", skin);
        registerUserName.setMessageText("User name...");
        registerUserName.setAlignment(Align.center);
        registerEmail = new TextField("", skin);
        registerEmail.setMessageText("Email...");
        registerEmail.setAlignment(Align.center);
        registerPassword = new TextField("", skin);
        registerPassword.setMessageText("Password...");
        registerPassword.setAlignment(Align.center);
        registerPassword.setPasswordMode(true);
        registerPassword.setPasswordCharacter('*');
        registerRepeatPassword = new TextField("", skin);
        registerRepeatPassword.setMessageText("Repeat password...");
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

        pickColorScreen = new Table();
        pickColorResult = new Image(new TextureRegionDrawable(assetManager.get(GeoBattleAssets.COLOR, Texture.class)));
        pickColorR = new Slider(0, 1, 1 / 255f, false, skin);
        pickColorR.addListener(updateColor);
        pickColorG = new Slider(0, 1, 1 / 255f, false, skin);
        pickColorG.addListener(updateColor);
        pickColorB = new Slider(0, 1, 1 / 255f, false, skin);
        pickColorB.addListener(updateColor);
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

        TextButton loginMode = new TextButton("LOGIN", skin);
        loginMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setMode(LoginScreenMode.LOGIN);
            }
        });
        loginRegisterSwitch.add(loginMode)
                .width(Gdx.graphics.getWidth() / 3)
                .height(Gdx.graphics.getPpcY());

        TextButton registerMode = new TextButton("REGISTER", skin);
        registerMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setMode(LoginScreenMode.REGISTER);
            }
        });
        loginRegisterSwitch.add(registerMode)
                .width(Gdx.graphics.getWidth() / 3)
                .height(Gdx.graphics.getPpcY());

        TextButton settings = new TextButton("SETTINGS", skin);
        settings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onSettings();
            }
        });
        loginRegisterSwitch.add(settings)
                .width(Gdx.graphics.getWidth() / 3)
                .height(Gdx.graphics.getPpcY());

        loginRegisterSwitch.center().bottom().padBottom(20);
    }

    // Initializes login form
    private void initLoginScreen(final LoginScreen screen) {
        loginScreen.reset();
        loginScreen.setFillParent(true);

        Label loginLabel = new Label("LOGIN", skin, "black");
        loginScreen.add(loginLabel)
                .expandX()
                .height(Gdx.graphics.getPpcY());
        loginScreen.row();

        loginScreen.add(loginUserName)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        loginScreen.row();

        loginScreen.add(loginPassword)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        loginScreen.row();

        TextButton login = new TextButton("Login", skin);
        login.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onLogin();
            }
        });
        loginScreen.add(login)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(15);

        loginScreen.center().pad(20).top().padTop(20);
    }

    // Initializes register form
    private void initRegisterScreen(final LoginScreen screen) {
        registerScreen.reset();
        registerScreen.setFillParent(true);

        Label registerLabel = new Label("REGISTER", skin, "black");
        registerScreen.add(registerLabel)
                .expandX()
                .height(Gdx.graphics.getPpcY());
        registerScreen.row();

        registerScreen.add(registerUserName)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        registerScreen.row();

        registerScreen.add(registerEmail)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        registerScreen.row();

        registerScreen.add(registerPassword)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        registerScreen.row();

        registerScreen.add(registerRepeatPassword)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        registerScreen.row();

        TextButton registerPickColor = new TextButton("Pick color...", skin);
        registerPickColor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setMode(LoginScreenMode.PICK_COLOR);
            }
        });
        registerScreen.add(registerPickColor)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        registerScreen.row();

        TextButton register = new TextButton("Register", skin);
        register.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onRegister();
            }
        });
        registerScreen.add(register)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(15);

        registerScreen.center().pad(20).top().padTop(20);
    }

    private void initPickColorScreen(LoginScreen screen) {
        pickColorScreen.reset();
        pickColorScreen.setFillParent(true);

        Label pickColorLabel = new Label("PICK COLOR", skin, "black");
        pickColorScreen.add(pickColorLabel)
                .expandX()
                .height(Gdx.graphics.getPpcY());
        pickColorScreen.row();

        pickColorResult.setColor(Color.BLACK);
        pickColorScreen.add(pickColorResult)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        pickColorScreen.row();

        pickColorScreen.add(pickColorR)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        pickColorScreen.row();

        pickColorScreen.add(pickColorG)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        pickColorScreen.row();

        pickColorScreen.add(pickColorB)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(5);
        pickColorScreen.row();

        TextButton pickColor = new TextButton("Pick", skin);
        pickColor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setMode(LoginScreenMode.REGISTER);
            }
        });
        pickColorScreen.add(pickColor)
                .fillX()
                .height(Gdx.graphics.getPpcY())
                .padTop(15);

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
