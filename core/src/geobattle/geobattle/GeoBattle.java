package geobattle.geobattle;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.I18NBundle;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import geobattle.geobattle.actionresults.MatchBranch;
import geobattle.geobattle.actionresults.StateRequestResult;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.screens.LoadingScreen;
import geobattle.geobattle.screens.emailconfirmationscreen.EmailConfirmationScreen;
import geobattle.geobattle.screens.gamescreen.GameScreen;
import geobattle.geobattle.screens.helpscreen.HelpScreen;
import geobattle.geobattle.screens.loginscreen.LoginScreen;
import geobattle.geobattle.screens.mainmenuscreen.MainMenuScreen;
import geobattle.geobattle.screens.selectserverscreen.SelectServerScreen;
import geobattle.geobattle.screens.settingsscreen.SettingsScreen;
import geobattle.geobattle.server.AuthInfo;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.server.ExternalAPI;

// Main game class
public final class GeoBattle extends Game {
    // External API
    private ExternalAPI externalAPI;

    // Asset manager
	private AssetManager assetManager;

	// Internationalization bundle
	private I18NBundle i18NBundle;

	// Music controller
	private GeoBattleMusicController musicController;

	// Volume of sound
	private float soundVolume;

	// Stage with GUI
	private Stage guiStage;

	// Overlaying GUI of game
	private GeoBattleGUI gui;

	// Version of client
	public final String clientVersion;

	// Constructor
    public GeoBattle(ExternalAPI externalAPI, String clientVersion) {
        this.externalAPI = externalAPI;
        this.clientVersion = clientVersion;
    }

    @Override
	public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        externalAPI.server.setGame(this);
        externalAPI.server.setOnFailListener(new Runnable() {
            @Override
            public void run() {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        showMessage(i18NBundle.get("seriousNetworkProblems"));
                        externalAPI.oSAPI.showMessage("You have problems with connection");
                        setNetworkState(0);
                        switchToLoginScreen();
                    }
                });
            }
        });

        assetManager = new AssetManager();

        assetManager.load(GeoBattleAssets.GUI_SKIN, Skin.class);

        assetManager.load(GeoBattleAssets.BUILDINGS_ATLAS, TextureAtlas.class);
        assetManager.load(GeoBattleAssets.BUILDING_ICONS_ATLAS, TextureAtlas.class);

        assetManager.load(GeoBattleAssets.UNITS_ATLAS, TextureAtlas.class);

        assetManager.load(GeoBattleAssets.SECTOR_STATES_ATLAS, TextureAtlas.class);

        assetManager.load(GeoBattleAssets.ANIMATION_EXPLOSION, Texture.class);
        assetManager.load(GeoBattleAssets.ANIMATION_TURRET_FLASH, Texture.class);

        assetManager.load(GeoBattleAssets.MAIN_MENU_BACKGROUND, Texture.class);
        assetManager.load(GeoBattleAssets.MAIN_MENU_TITLE, Texture.class);
        assetManager.load(GeoBattleAssets.SETTINGS_BACKGROUND, Texture.class);

        assetManager.load(GeoBattleAssets.SOUND_SHOTS, Sound.class);
        assetManager.load(GeoBattleAssets.SOUND_EXPLOSION, Sound.class);

        assetManager.load(GeoBattleAssets.COLOR, Texture.class);

        assetManager.load(GeoBattleAssets.I18N, I18NBundle.class);

        assetManager.finishLoading();

        i18NBundle = assetManager.get(GeoBattleAssets.I18N);

        generateAndUpdateFonts(assetManager);

        if (!VisUI.isLoaded())
            VisUI.load(assetManager.get(GeoBattleAssets.GUI_SKIN, Skin.class));

        soundVolume = Float.parseFloat(externalAPI.oSAPI.loadValue("soundVolume", String.valueOf(GeoBattleConst.DEFAULT_SOUND_VOLUME)));

        musicController = new GeoBattleMusicController(new String[] {
                GeoBattleAssets.MUSIC_BACKGROUND_1,
                GeoBattleAssets.MUSIC_BACKGROUND_2,
                GeoBattleAssets.MUSIC_BACKGROUND_3
        }, Float.parseFloat(externalAPI.oSAPI.loadValue("musicVolume", String.valueOf(GeoBattleConst.DEFAULT_MUSIC_VOLUME))));
        musicController.nextTrack();

        externalAPI.tileRequestPool.setMapQuality(externalAPI.oSAPI.loadValue("mapQuality", GeoBattleConst.DEFAULT_MAP_QUALITY));

        guiStage = new Stage();
        gui = new GeoBattleGUI(assetManager, this, guiStage, 3);

        setScreen(new MainMenuScreen(assetManager, this));
	}

	// Generates fonts and updates GUI skin
	private void generateAndUpdateFonts(AssetManager assetManager) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.characters += "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";

        java.util.List<BitmapFont> fonts = new ArrayList<BitmapFont>();

        fontParameter.size = (int) (Gdx.graphics.getPpcY() * 0.24f);
        fonts.add(generator.generateFont(fontParameter));

        fontParameter.size = (int) (fontParameter.size * 1.35f);
        fonts.add(generator.generateFont(fontParameter));

        fontParameter.size = (int) (fontParameter.size * 1.2f);
        fonts.add(generator.generateFont(fontParameter));

        generator.dispose();

        Skin skin = assetManager.get(GeoBattleAssets.GUI_SKIN);

        java.util.List<BitmapFont> fontsOld = Arrays.asList(
                skin.getFont("small"),
                skin.getFont("medium"),
                skin.getFont("large")
        );

        for (Class cls : new Class[] {
                TextButton.TextButtonStyle.class, Label.LabelStyle.class,
                VisTextButton.VisTextButtonStyle.class, TextField.TextFieldStyle.class,
                VisTextField.VisTextFieldStyle.class, List.ListStyle.class
        }) {
                for (Field field : cls.getFields()) {
                    if (field.getType().equals(BitmapFont.class)) {
                        for (Object style : skin.getAll(cls).values()) {
                            try {
                                int index = fontsOld.indexOf(field.get(style));
                                if (index != -1)
                                    field.set(style, fonts.get(index));
                            } catch (IllegalAccessException ignored) {}
                        }
                    }
                }
        }
    }

	public void setMusicVolume(float volume) {
        musicController.setVolume(volume);
    }

    public void setSoundVolume(float volume) {
        this.soundVolume = volume;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

	public ExternalAPI getExternalAPI() {
        return externalAPI;
    }

    public I18NBundle getI18NBundle() {
        return i18NBundle;
    }

    public void switchToMainMenuScreen() {
        setScreen(new MainMenuScreen(assetManager, this));
    }

    public void switchToSettingsScreen() {
        setScreen(new SettingsScreen(assetManager, this));
    }

	public void switchToLoginScreen() {
        setScreen(new LoginScreen(assetManager, this));
    }

    public void switchToEmailConfirmationScreen(String name) {
        setScreen(new EmailConfirmationScreen(assetManager, this, name));
    }

    public void switchToSelectServerScreen() {
        setScreen(new SelectServerScreen(assetManager, this));
    }

    public void switchToHelpScreen() {
        setScreen(new HelpScreen(assetManager, this));
    }

    @Override
    public void setScreen(Screen screen) {
        if (this.screen != null)
            this.screen.dispose();
        super.setScreen(screen);
        Gdx.input.setOnscreenKeyboardVisible(false);
    }

    // Shows message
    public void showMessage(String message) {
        gui.addMessage(new GeoBattleGUI.GeoBattleMessage(message, 3));
    }

    // Sets position of message
    public void setMessagePad(float pad, boolean fromTop) {
        if (fromTop)
            gui.messagesRoot.top().padTop(pad);
        else
            gui.messagesRoot.bottom().padBottom(pad);
    }

    public void setNetworkState(float state) {
        gui.setNetworkState(state);
    }

    public void onAuthInfoObtained(final AuthInfo authInfo) {
        externalAPI.server.onStateRequestEvent(authInfo, new Callback<StateRequestResult>() {
            @Override
            public void onResult(final StateRequestResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onStateRequestResult(result, authInfo);
                    }
                });
            }
        }, new Runnable() {
            @Override
            public void run() {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        externalAPI.oSAPI.showMessage("Failed to get game state");
                        showMessage(i18NBundle.get("networkProblems"));
                        switchToLoginScreen();
                    }
                });
            }
        });

        setScreen(new LoadingScreen());
    }

    public void onGameStateObtained(GameState gameState, AuthInfo authInfo) {
        setScreen(new GameScreen(gameState, assetManager, authInfo, this));
    }

    private void onStateRequestResult(StateRequestResult result, final AuthInfo authInfo) {
        result.match(
                new MatchBranch<StateRequestResult.StateRequestSuccess>() {
                    @Override
                    public void onMatch(StateRequestResult.StateRequestSuccess stateRequestSuccess) {
                        onGameStateObtained(stateRequestSuccess.gameState, authInfo);
                        // setScreen(new GameScreen(externalAPI, stateRequestSuccess.gameState, assetManager, authInfo, GeoBattle.this));
                    }
                },
                new MatchBranch<StateRequestResult.WrongAuthInfo>() {
                    @Override
                    public void onMatch(StateRequestResult.WrongAuthInfo wrongAuthInfo) {
                        Gdx.app.log("GeoBattle", "Not authorized!");
                        Gdx.app.exit();
                    }
                },
                new MatchBranch<StateRequestResult.MalformedJson>() {
                    @Override
                    public void onMatch(StateRequestResult.MalformedJson malformedJson) {
                        externalAPI.oSAPI.showMessage("Cannot build: JSON request is not well-formed. Probable bug. Tell the developers");
                    }
                }
        );
    }

    public void onExitGame(final AuthInfo authInfo) {
        externalAPI.server.invalidatePlayerToken(authInfo.id, authInfo.token, null);
        setScreen(new LoginScreen(assetManager, this));
    }

	@Override
	public void render() {
        super.render();

        gui.update(Gdx.graphics.getDeltaTime());

        guiStage.act();
        guiStage.draw();
	}
	
	@Override
	public void dispose() {
        super.dispose();
        VisUI.dispose();
        assetManager.dispose();
        getScreen().dispose();
        musicController.dispose();
    }
}
