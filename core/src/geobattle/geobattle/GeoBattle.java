package geobattle.geobattle;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import geobattle.geobattle.game.actionresults.MatchBranch;
import geobattle.geobattle.game.actionresults.StateRequestResult;
import geobattle.geobattle.screens.LoadingScreen;
import geobattle.geobattle.screens.gamescreen.GameScreen;
import geobattle.geobattle.screens.loginscreen.LoginScreen;
import geobattle.geobattle.server.AuthInfo;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.server.ExternalAPI;

// Main game class
public final class GeoBattle extends Game {
    // External API
    private ExternalAPI externalAPI;

    // Asset manager
	private AssetManager assetManager;

	// Constructor
    public GeoBattle(ExternalAPI externalAPI) {
        this.externalAPI = externalAPI;
    }
    
    @Override
	public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        assetManager = new AssetManager();

        assetManager.load(GeoBattleAssets.GUI_SKIN, Skin.class);

        TextureLoader.TextureParameter param = new TextureLoader.TextureParameter();
        param.genMipMaps = true;

        assetManager.load(GeoBattleAssets.GUI_SKIN, Skin.class);

        assetManager.load(GeoBattleAssets.BUILDINGS_ATLAS, TextureAtlas.class);
        assetManager.load(GeoBattleAssets.BUILDING_ICONS_ATLAS, TextureAtlas.class);

        assetManager.load(GeoBattleAssets.BOMBER, Texture.class, param);
        assetManager.load(GeoBattleAssets.SPOTTER, Texture.class, param);

        assetManager.load(GeoBattleAssets.BOMBER_TEAM_COLOR, Texture.class, param);
        assetManager.load(GeoBattleAssets.SPOTTER_TEAM_COLOR, Texture.class, param);

        assetManager.load(GeoBattleAssets.COLOR, Texture.class);
        assetManager.finishLoading();

        setScreen(new LoginScreen(externalAPI, assetManager, this));
	}

	public void switchToLoginScreen() {
        setScreen(new LoginScreen(externalAPI, assetManager, this));
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
        });

        setScreen(new LoadingScreen());
    }

    private void onStateRequestResult(StateRequestResult result, final AuthInfo authInfo) {
        result.match(
                new MatchBranch<StateRequestResult.StateRequestSuccess>() {
                    @Override
                    public void onMatch(StateRequestResult.StateRequestSuccess stateRequestSuccess) {
                        setScreen(new GameScreen(externalAPI, stateRequestSuccess.gameState, assetManager, authInfo, GeoBattle.this));
                    }
                },
                new MatchBranch<StateRequestResult.WrongAuthInfo>() {
                    @Override
                    public void onMatch(StateRequestResult.WrongAuthInfo wrongAuthInfo) {
                        Gdx.app.log("GeoBattle", "Not authorized!");
                        Gdx.app.exit();
                    }
                }
        );
    }

    public void onExitGame(final AuthInfo authInfo) {
        externalAPI.server.invalidatePlayerToken(authInfo.id, authInfo.token);
        setScreen(new LoginScreen(externalAPI, assetManager, this));
    }

	@Override
	public void render() {
        super.render();
	}
	
	@Override
	public void dispose() {
        assetManager.dispose();
        getScreen().dispose();
    }
}
