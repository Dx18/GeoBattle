package geobattle.geobattle.server;

import com.badlogic.gdx.graphics.Color;

import geobattle.geobattle.actionresults.AttackResult;
import geobattle.geobattle.actionresults.AuthorizationResult;
import geobattle.geobattle.actionresults.BuildResult;
import geobattle.geobattle.actionresults.DestroyResult;
import geobattle.geobattle.actionresults.EmailConfirmationResult;
import geobattle.geobattle.actionresults.RegistrationResult;
import geobattle.geobattle.actionresults.ResearchResult;
import geobattle.geobattle.actionresults.ResendEmailResult;
import geobattle.geobattle.actionresults.SectorBuildResult;
import geobattle.geobattle.actionresults.StateRequestResult;
import geobattle.geobattle.actionresults.UnitBuildResult;
import geobattle.geobattle.actionresults.UpdateRequestResult;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.research.ResearchType;
import geobattle.geobattle.game.units.UnitType;

// Game server
public interface Server {
    // Sets network fail listener
    void setOnFailListener(Runnable onFail);

    // Sets IP address and port of server
    void setAddress(String ipAddress, int port);

    // Returns IP address of server
    String getIPAddress();

    // Returns port of server
    int getPort();

    // Requests player register
    CancelHandle register(String playerName, String email, String password, Color color, Callback<RegistrationResult> callback, Runnable failCallback);

    // Requests player login
    CancelHandle login(String playerName, String password, Callback<AuthorizationResult> callback, Runnable failCallback);

    // Invalidates player token on server
    CancelHandle invalidatePlayerToken(int playerId, String playerToken, Runnable failCallback);

    // Requests state of game
    CancelHandle requestState(AuthInfo authInfo, Callback<StateRequestResult> callback, Runnable failCallback);

    // Requests update of game
    CancelHandle requestUpdate(AuthInfo authInfo, double lastUpdateTime, Callback<UpdateRequestResult> callback, Runnable failCallback);

    // Requests build action
    CancelHandle requestBuild(AuthInfo authInfo, BuildingType type, int x, int y, Callback<BuildResult> callback, Runnable failCallback);

    // Request sector build action
    CancelHandle requestSectorBuild(AuthInfo authInfo, int x, int y, Callback<SectorBuildResult> callback, Runnable failCallback);

    // Requests destroy action
    CancelHandle requestDestroy(AuthInfo authInfo, int id, Callback<DestroyResult> callback, Runnable failCallback);

    // Requests unit build action
    CancelHandle requestUnitBuild(AuthInfo authInfo, UnitType type, Building building, Callback<UnitBuildResult> callback, Runnable failCallback);

    // Requests research
    CancelHandle requestResearch(AuthInfo authInfo, ResearchType researchType, Callback<ResearchResult> callback, Runnable failCallback);

    // Requests attack
    CancelHandle requestAttack(AuthInfo authInfo, int attackerId, int victimId, int[] hangarIds, int sectorId, Callback<AttackResult> callback, Runnable failCallback);

    // Requests email confirmation
    CancelHandle requestEmailConfirmation(String name, int code, Callback<EmailConfirmationResult> callback, Runnable failCallback);

    // Requests email resend
    CancelHandle requestEmailResend(String name, Callback<ResendEmailResult> callback, Runnable failCallback);
}
