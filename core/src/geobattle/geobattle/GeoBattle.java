package geobattle.geobattle;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
import com.kotcrab.vis.ui.VisUI;

import geobattle.geobattle.actionresults.MatchBranch;
import geobattle.geobattle.actionresults.StateRequestResult;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.screens.LoadingScreen;
import geobattle.geobattle.screens.emailconfirmationscreen.EmailConfirmationScreen;
import geobattle.geobattle.screens.gamescreen.GameScreen;
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

	private float soundVolume;

	// Constructor
    public GeoBattle(ExternalAPI externalAPI) {
        this.externalAPI = externalAPI;
    }

    @Override
	public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        externalAPI.server.setOnFailListener(new Runnable() {
            @Override
            public void run() {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        externalAPI.oSAPI.showMessage("You have problems with connection");
                        switchToLoginScreen();
                    }
                });
            }
        });

        assetManager = new AssetManager();

        assetManager.load(GeoBattleAssets.GUI_SKIN, Skin.class);

        TextureLoader.TextureParameter param = new TextureLoader.TextureParameter();
        param.genMipMaps = true;

        assetManager.load(GeoBattleAssets.GUI_SKIN, Skin.class);

        assetManager.load(GeoBattleAssets.BUILDINGS_ATLAS, TextureAtlas.class);
        assetManager.load(GeoBattleAssets.BUILDING_ICONS_ATLAS, TextureAtlas.class);

        assetManager.load(GeoBattleAssets.UNITS_ATLAS, TextureAtlas.class);

        assetManager.load(GeoBattleAssets.ANIMATION_EXPLOSION, Texture.class);
        assetManager.load(GeoBattleAssets.ANIMATION_TURRET_FLASH, Texture.class);

//        assetManager.load(GeoBattleAssets.BOMBER, Texture.class, param);
//        assetManager.load(GeoBattleAssets.SPOTTER, Texture.class, param);

//        assetManager.load(GeoBattleAssets.BOMBER_TEAM_COLOR, Texture.class, param);
//        assetManager.load(GeoBattleAssets.SPOTTER_TEAM_COLOR, Texture.class, param);

        assetManager.load(GeoBattleAssets.MAIN_MENU_BACKGROUND, Texture.class);
        assetManager.load(GeoBattleAssets.MAIN_MENU_TITLE, Texture.class);
        assetManager.load(GeoBattleAssets.SETTINGS_BACKGROUND, Texture.class);

        assetManager.load(GeoBattleAssets.SOUND_SHOTS, Sound.class);
        assetManager.load(GeoBattleAssets.SOUND_EXPLOSION, Sound.class);

        assetManager.load(GeoBattleAssets.COLOR, Texture.class);

        // I18NBundleLoader.I18NBundleParameter i18NParam = new I18NBundleLoader.I18NBundleParameter(Locale.ROOT);

        assetManager.load(GeoBattleAssets.I18N, I18NBundle.class);

        assetManager.finishLoading();

        i18NBundle = assetManager.get(GeoBattleAssets.I18N);

        if (!VisUI.isLoaded())
            VisUI.load(assetManager.get(GeoBattleAssets.GUI_SKIN, Skin.class));
//        VisUI.load();

        soundVolume = Float.parseFloat(externalAPI.oSAPI.loadValue("soundVolume", "0.5"));

        musicController = new GeoBattleMusicController(new String[] {
                GeoBattleAssets.MUSIC_BACKGROUND_1,
                GeoBattleAssets.MUSIC_BACKGROUND_2,
                GeoBattleAssets.MUSIC_BACKGROUND_3
        }, Float.parseFloat(externalAPI.oSAPI.loadValue("musicVolume", "0.5")));
        musicController.nextTrack();

        // setScreen(new LoginScreen(externalAPI, assetManager, this));
        setScreen(new MainMenuScreen(assetManager, this));
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
        setScreen(new SettingsScreen(externalAPI, assetManager, this));
    }

	public void switchToLoginScreen() {
        setScreen(new LoginScreen(externalAPI, assetManager, this));
    }

    public void switchToEmailConfirmationScreen(String name) {
        setScreen(new EmailConfirmationScreen(externalAPI, assetManager, this, name));
    }

    public void switchToSelectServerScreen() {
        setScreen(new SelectServerScreen(assetManager, this));
    }

	public void onAuthInfoObtained(final AuthInfo authInfo) {
        externalAPI.server.requestState(authInfo, new Callback<StateRequestResult>() {
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
                externalAPI.oSAPI.showMessage("Failed to get game state");
                switchToLoginScreen();
            }
        });

        setScreen(new LoadingScreen());
    }

    public void onGameStateObtained(GameState gameState, AuthInfo authInfo) {
        setScreen(new GameScreen(externalAPI, gameState, assetManager, authInfo, this));
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
        setScreen(new LoginScreen(externalAPI, assetManager, this));
    }

	@Override
	public void render() {
        super.render();
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
