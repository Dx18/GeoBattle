package geobattle.geobattle.server;

import com.badlogic.gdx.graphics.Color;

import geobattle.geobattle.GeoBattle;
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

    // Sets game instance
    void setGame(GeoBattle game);

    // Sets IP address and port of server
    void setAddress(String ipAddress, int port);

    // Returns IP address of server
    String getIPAddress();

    // Returns port of server
    int getPort();

    // Cancels RegistrationEvent
    void cancelRegistrationEvent();

    // Requests player register
    CancelHandle register(String playerName, String email, String password, Color color, Callback<RegistrationResult> callback, Runnable failCallback);

    // Cancels AuthorizationEvent
    void cancelAuthorizationEvent();

    // Requests player login
    CancelHandle login(String playerName, String password, Callback<AuthorizationResult> callback, Runnable failCallback);

    // Invalidates player token on server
    CancelHandle invalidatePlayerToken(int playerId, String playerToken, Runnable failCallback);

    // Cancels StateRequestEvent
    void cancelStateRequestEvent();

    // Requests state of game
    CancelHandle requestState(AuthInfo authInfo, Callback<StateRequestResult> callback, Runnable failCallback);

    // Cancels UpdateRequestEvent
    void cancelUpdateRequestEvent();

    // Requests update of game
    CancelHandle requestUpdate(AuthInfo authInfo, double lastUpdateTime, Callback<UpdateRequestResult> callback, Runnable failCallback);

    // Cancels BuildEvent
    void cancelBuildEvent();

    // Requests build action
    CancelHandle requestBuild(AuthInfo authInfo, BuildingType type, int x, int y, Callback<BuildResult> callback, Runnable failCallback);

    // Cancels SectorBuildEvent
    void cancelSectorBuildEvent();

    // Request sector build action
    CancelHandle requestSectorBuild(AuthInfo authInfo, int x, int y, Callback<SectorBuildResult> callback, Runnable failCallback);

    // Cancels DestroyEvent
    void cancelDestroyEvent();

    // Requests destroy action
    CancelHandle requestDestroy(AuthInfo authInfo, int id, Callback<DestroyResult> callback, Runnable failCallback);

    // Cancels UnitBuildEvent
    void cancelUnitBuildEvent();

    // Requests unit build action
    CancelHandle requestUnitBuild(AuthInfo authInfo, UnitType type, Building building, Callback<UnitBuildResult> callback, Runnable failCallback);

    // Cancels ResearchEvent
    void cancelResearchEvent();

    // Requests research
    CancelHandle requestResearch(AuthInfo authInfo, ResearchType researchType, Callback<ResearchResult> callback, Runnable failCallback);

    // Cancels AttackEvent
    void cancelAttackEvent();

    // Requests attack
    CancelHandle requestAttack(AuthInfo authInfo, int attackerId, int victimId, int[] hangarIds, int sectorId, Callback<AttackResult> callback, Runnable failCallback);

    // Cancels EmailConfirmationEvent
    void cancelEmailConfirmationEvent();

    // Requests email confirmation
    CancelHandle requestEmailConfirmation(String name, int code, Callback<EmailConfirmationResult> callback, Runnable failCallback);

    // Cancels EmailResendEvent
    void cancelEmailResendEvent();

    // Requests email resend
    CancelHandle requestEmailResend(String name, Callback<ResendEmailResult> callback, Runnable failCallback);
}
