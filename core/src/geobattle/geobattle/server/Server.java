package geobattle.geobattle.server;

import com.badlogic.gdx.graphics.Color;

import geobattle.geobattle.game.actionresults.BuildResult;
import geobattle.geobattle.game.actionresults.DestroyResult;
import geobattle.geobattle.game.actionresults.ResearchResult;
import geobattle.geobattle.game.actionresults.SectorBuildResult;
import geobattle.geobattle.game.actionresults.StateRequestResult;
import geobattle.geobattle.game.actionresults.UnitBuildResult;
import geobattle.geobattle.game.actionresults.UpdateRequestResult;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.research.ResearchType;
import geobattle.geobattle.game.units.UnitType;

// Game server
public interface Server {
    // Sets IP address and port of server
    void setAddress(String ipAddress, int port);

    // Returns IP address of server
    String getIPAddress();

    // Returns port of server
    int getPort();

    // Requests player register
    CancelHandle register(String playerName, String email, String password, Color color, Callback<RegistrationResult> callback);

    // Requests player login
    CancelHandle login(String playerName, String password, Callback<AuthorizationResult> callback);

    // Invalidates player token on server
    CancelHandle invalidatePlayerToken(int playerId, String playerToken);

    // Requests state of game
    CancelHandle requestState(AuthInfo authInfo, Callback<StateRequestResult> callback);

    // Requests update of game
    CancelHandle requestUpdate(AuthInfo authInfo, Callback<UpdateRequestResult> callback);

    // Requests build action
    CancelHandle requestBuild(AuthInfo authInfo, BuildingType type, int x, int y, Callback<BuildResult> callback);

    // Request sector build action
    CancelHandle requestSectorBuild(AuthInfo authInfo, int x, int y, Callback<SectorBuildResult> callback);

    // Requests destroy action
    CancelHandle requestDestroy(AuthInfo authInfo, int id, Callback<DestroyResult> callback);

    // Requests unit build action
    CancelHandle requestUnitBuild(AuthInfo authInfo, UnitType type, Building building, Callback<UnitBuildResult> callback);

    // Requests research
    CancelHandle requestResearch(AuthInfo authInfo, ResearchType researchType, Callback<ResearchResult> callback);

    // Requests email confirmation
    CancelHandle requestEmailConfirmation(String name, int code, Callback<EmailConfirmationResult> callback);

    // Requests email resend
    CancelHandle requestEmailResend(String name, Callback<ResendEmailResult> callback);
}
