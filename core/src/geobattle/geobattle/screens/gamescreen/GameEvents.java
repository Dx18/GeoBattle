package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.GeoBattleConst;
import geobattle.geobattle.actionresults.AttackResult;
import geobattle.geobattle.actionresults.BuildResult;
import geobattle.geobattle.actionresults.DestroyResult;
import geobattle.geobattle.actionresults.MatchBranch;
import geobattle.geobattle.actionresults.RatingRequestResult;
import geobattle.geobattle.actionresults.ResearchResult;
import geobattle.geobattle.actionresults.SectorBuildResult;
import geobattle.geobattle.actionresults.UnitBuildResult;
import geobattle.geobattle.actionresults.UpdateRequestResult;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.GameStateUpdate;
import geobattle.geobattle.game.PlayerState;
import geobattle.geobattle.game.attacking.AttackScript;
import geobattle.geobattle.game.attacking.HealthInterpolation;
import geobattle.geobattle.game.attacking.TimePoint;
import geobattle.geobattle.game.attacking.UnitGroupMovingInfo;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.game.research.ResearchInfo;
import geobattle.geobattle.game.research.ResearchType;
import geobattle.geobattle.game.tasks.TimedObject;
import geobattle.geobattle.game.units.UnitGroup;
import geobattle.geobattle.game.units.UnitGroupState;
import geobattle.geobattle.game.units.UnitType;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.rating.RatingEntry;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.SelectHangarsMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.SelectSectorMode;
import geobattle.geobattle.server.AuthInfo;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.util.CoordinateConverter;
import geobattle.geobattle.util.GeoBattleMath;
import geobattle.geobattle.util.IntPoint;
import geobattle.geobattle.util.ReadOnlyArrayList;

// Game events
public class GameEvents {
    // Game server
    private final GeoBattle game;

    // Game state
    public final GameState gameState;

    // Auth info
    public final AuthInfo authInfo;

    // Screen
    public final GameScreen screen;

    // Map
    public final GeoBattleMap map;

    // Time of last update
    private double lastUpdateTime;

    // Attack scripts
    // private TimedObjectQueue<AttackScript> attackScripts;

    // Started attack scripts
    private HashSet<AttackScript> startedAttackScripts;

    public GameEvents(GameState gameState, AuthInfo authInfo, GameScreen screen, GeoBattleMap map, GeoBattle game) {
        this.gameState = gameState;
        this.authInfo = authInfo;
        this.screen = screen;
        this.map = map;
        this.game = game;
        this.lastUpdateTime = gameState.getTime();
        this.startedAttackScripts = new HashSet<AttackScript>();
    }

    // Invokes when player requests first sector building
    public void onFirstSectorBuildEvent() {
        IntPoint coordinates = map.getPointedTile();

        if (!GeoBattleMath.tileRectangleContains(map.getVisibleRect(), coordinates.x, coordinates.y))
            return;

        coordinates.x -= Sector.SECTOR_SIZE / 2;
        coordinates.y -= Sector.SECTOR_SIZE / 2;

        Vector2 playerPosition = GeoBattleMath.latLongToMercator(
                game.getExternalAPI().geolocationAPI.getCurrentCoordinates()
        );

        if (!gameState.canBuildSector(coordinates.x, coordinates.y, game, new IntPoint(
                CoordinateConverter.realWorldToSubTiles(playerPosition.x, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.realWorldToSubTiles(playerPosition.y, GeoBattleConst.SUBDIVISION)
        )))
            return;

        game.getExternalAPI().server.onSectorBuildEvent(authInfo, coordinates.x, coordinates.y, new Callback<SectorBuildResult>() {
            @Override
            public void onResult(final SectorBuildResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onSectorBuildResult(result);
                    }
                });
            }
        }, null);
    }

    // Invokes when player requests sector building
    public void onSectorBuildEvent() {
        IntPoint coordinates = map.getPointedTile();

        if (!GeoBattleMath.tileRectangleContains(map.getVisibleRect(), coordinates.x, coordinates.y))
            return;

        Sector sector = gameState.getCurrentPlayer().getAllSectors().get(0);
        coordinates.x -= ((coordinates.x - sector.x) % Sector.SECTOR_SIZE + Sector.SECTOR_SIZE) % Sector.SECTOR_SIZE;
        coordinates.y -= ((coordinates.y - sector.y) % Sector.SECTOR_SIZE + Sector.SECTOR_SIZE) % Sector.SECTOR_SIZE;

        Vector2 playerPosition = GeoBattleMath.latLongToMercator(
                game.getExternalAPI().geolocationAPI.getCurrentCoordinates()
        );

        if (!gameState.canBuildSector(coordinates.x, coordinates.y, game, new IntPoint(
                CoordinateConverter.realWorldToSubTiles(playerPosition.x, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.realWorldToSubTiles(playerPosition.y, GeoBattleConst.SUBDIVISION)
        )))
            return;

        game.getExternalAPI().server.onSectorBuildEvent(authInfo, coordinates.x, coordinates.y, new Callback<SectorBuildResult>() {
            @Override
            public void onResult(final SectorBuildResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onSectorBuildResult(result);
                    }
                });
            }
        }, null);
    }

    // Invokes when player receives sector building result
    private void onSectorBuildResult(SectorBuildResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    // Invokes when player requests building
    public void onBuildEvent() {
        BuildingType buildingType = map.getSelectedBuildingType();

        if (buildingType == null)
            return;

        IntPoint coordinates = map.getPointedTile();

        if (!GeoBattleMath.tileRectangleContains(map.getVisibleRect(), coordinates.x, coordinates.y))
            return;

        coordinates.x -= buildingType.sizeX / 2;
        coordinates.y -= buildingType.sizeY / 2;

        if (!gameState.canBuildBuilding(map.getSelectedBuildingType(), coordinates.x, coordinates.y, game))
            return;

        game.getExternalAPI().server.onBuildEvent(authInfo, buildingType, coordinates.x, coordinates.y, new Callback<BuildResult>() {
            @Override
            public void onResult(final BuildResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onBuildResult(result);
                    }
                });
            }
        }, null);
    }

    public void setSelectedBuildingType(BuildingType type) {
        map.setSelectedBuildingType(type);
    }

    // Invokes when player receives building result
    private void onBuildResult(BuildResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    // Invokes when player requests building destroying
    public void onDestroyEvent() {
        IntPoint coordinates = map.getPointedTile();

        if (!GeoBattleMath.tileRectangleContains(map.getVisibleRect(), coordinates.x, coordinates.y))
            return;

        Building building = map.getPointedBuilding();

        if (building != null)
            game.getExternalAPI().server.onDestroyEvent(authInfo, building.id, new Callback<DestroyResult>() {
                @Override
                public void onResult(final DestroyResult result) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            onDestroyResult(result);
                        }
                    });
                }
            }, null);
    }

    // Invokes when player receives destroy result
    private void onDestroyResult(DestroyResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    // Invokes when player requests unit building
    public void onUnitBuild(UnitType unitType) {
        Building building = map.getPointedBuilding();

        game.getExternalAPI().server.onUnitBuildEvent(authInfo, unitType, building, new Callback<UnitBuildResult>() {
            @Override
            public void onResult(final UnitBuildResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onUnitBuildResult(result);
                    }
                });
            }
        }, null);
    }

    // Invokes when player receives unit building result
    private void onUnitBuildResult(UnitBuildResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    // Invokes when client requests update
    public void onUpdateRequestEvent() {
        game.getExternalAPI().server.onUpdateRequestEvent(authInfo, gameState.getLastUpdateTime(), new Callback<UpdateRequestResult>() {
            @Override
            public void onResult(final UpdateRequestResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onUpdateRequestResult(result);
                    }
                });
            }
        }, null);
    }

    // Invokes when client receives update result
    private void onUpdateRequestResult(UpdateRequestResult result) {
        result.match(
                new MatchBranch<UpdateRequestResult.UpdateRequestSuccess>() {
                    @Override
                    public void onMatch(UpdateRequestResult.UpdateRequestSuccess updateRequestSuccess) {
                        if (gameState.getLastUpdateTime() < updateRequestSuccess.time) {
                            gameState.setLastUpdateTime(updateRequestSuccess.time);
                            lastUpdateTime = updateRequestSuccess.time;

                            ResearchInfo researchInfoOld = gameState.getCurrentPlayer().getResearchInfo();

                            ResearchInfo researchInfoNew = updateRequestSuccess.researchInfo;

                            researchInfoOld.setLevel(ResearchType.TURRET_DAMAGE, researchInfoNew.getLevel(ResearchType.TURRET_DAMAGE));
                            researchInfoOld.setLevel(ResearchType.UNIT_DAMAGE, researchInfoNew.getLevel(ResearchType.UNIT_DAMAGE));
                            researchInfoOld.setLevel(ResearchType.GENERATOR_EFFICIENCY, researchInfoNew.getLevel(ResearchType.GENERATOR_EFFICIENCY));

                            gameState.setResources(updateRequestSuccess.resources);

                            for (GameStateUpdate update : updateRequestSuccess.updates)
                                update.apply(game, gameState);
                        }
                    }
                },
                new MatchBranch<UpdateRequestResult.StateRequestSuccess>() {
                    @Override
                    public void onMatch(UpdateRequestResult.StateRequestSuccess stateRequestSuccess) {
                        if (gameState.getLastUpdateTime() < stateRequestSuccess.gameState.getLastUpdateTime()) {
                            gameState.setData(stateRequestSuccess.gameState);
                            lastUpdateTime = gameState.getTime();
                        }
                    }
                },
                new MatchBranch<UpdateRequestResult.WrongAuthInfo>() {
                    @Override
                    public void onMatch(UpdateRequestResult.WrongAuthInfo wrongAuthInfo) {
                        game.getExternalAPI().oSAPI.showMessage("Not authorized!");
                        game.switchToLoginScreen();
                        // Gdx.app.error("GeoBattle", "Not authorized!");
                        // Gdx.app.exit();
                    }
                },
                new MatchBranch<UpdateRequestResult.MalformedJson>() {
                    @Override
                    public void onMatch(UpdateRequestResult.MalformedJson malformedJson) {
                        game.getExternalAPI().oSAPI.showMessage("Cannot update: JSON request is not well-formed. Probable bug. Tell the developers");
                    }
                },
                new MatchBranch<UpdateRequestResult.IncorrectData>() {
                    @Override
                    public void onMatch(UpdateRequestResult.IncorrectData incorrectData) {
                        game.getExternalAPI().oSAPI.showMessage("Cannot update: incorrect data");
                    }
                }
        );
    }

    // Invokes when player requests research
    public void onResearchEvent(ResearchType researchType) {
        game.getExternalAPI().server.onResearchEvent(authInfo, researchType, new Callback<ResearchResult>() {
            @Override
            public void onResult(final ResearchResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onResearchResult(result);
                    }
                });
            }
        }, null);
    }

    // Invokes when player receives research result
    private void onResearchResult(ResearchResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    // Invokes when player wants to attack another player
    public void onRequestAttack() {
        SelectHangarsMode selectHangarsMode = (SelectHangarsMode) map.getScreenModeData(GameScreenMode.SELECT_HANGARS);
        if (selectHangarsMode.getSelectedHangarsCount() == 0)
            return;

        SelectSectorMode selectSectorMode = (SelectSectorMode) map.getScreenModeData(GameScreenMode.SELECT_SECTOR);
        if (selectSectorMode.getPointedSector() == null)
            return;

        Vector2 playerPosition = GeoBattleMath.latLongToMercator(
                game.getExternalAPI().geolocationAPI.getCurrentCoordinates()
        );

        Sector victimSector = selectSectorMode.getPointedSector();
        if (!GeoBattleMath.tileRectangleContains(
                victimSector.x, victimSector.y, Sector.SECTOR_SIZE, Sector.SECTOR_SIZE,
                CoordinateConverter.realWorldToSubTiles(playerPosition.x, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.realWorldToSubTiles(playerPosition.y, GeoBattleConst.SUBDIVISION)
        )) {
            game.showMessage(game.getI18NBundle().get("attackResultWrongGeolocation"));
            return;
        }

        int attackerId = gameState.getPlayerId();
        int victimId = selectSectorMode.getOwningPlayerId();
        int sectorId = selectSectorMode.getPointedSector().sectorId;

        int[] hangarIds = new int[selectHangarsMode.getSelectedHangarsCount()];
        Iterator<Hangar> hangars = selectHangarsMode.getSelectedHangars();
        int hangar = 0;
        while (hangars.hasNext()) {
            hangarIds[hangar] = hangars.next().id;
            hangar++;
        }

        game.getExternalAPI().server.onAttackEvent(authInfo, attackerId, victimId, hangarIds, sectorId, new Callback<AttackResult>() {
            @Override
            public void onResult(final AttackResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onAttackResult(result);
                    }
                });
            }
        }, null);
    }

    // Invokes when player receives attack result
    private void onAttackResult(AttackResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    public void onRatingRequestEvent() {
        game.getExternalAPI().server.onRatingRequestEvent(new Callback<RatingRequestResult>() {
            @Override
            public void onResult(final RatingRequestResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onRatingRequestResult(result);
                    }
                });
            }
        }, null);
    }

    private void onRatingRequestResult(RatingRequestResult result) {
        if (result != null) {
            result.match(
                    new MatchBranch<RatingRequestResult.RatingRequestSuccess>() {
                        @Override
                        public void onMatch(RatingRequestResult.RatingRequestSuccess ratingRequestSuccess) {
                            for (RatingEntry entry : ratingRequestSuccess.rating)
                                entry.setPlayerData(gameState.getPlayer(entry.playerId), gameState.getPlayerId());
                            screen.getGUI().ratingDialog.setRating(ratingRequestSuccess.rating, screen);
                        }
                    },
                    new MatchBranch<RatingRequestResult.MalformedJson>() {
                        @Override
                        public void onMatch(RatingRequestResult.MalformedJson malformedJson) {
                            game.getExternalAPI().oSAPI.showMessage("Cannot get rating: JSON request is not well-formed. Probable bug. Tell the developers");
                        }
                    }
            );
        }
    }

    // Invokes every frame
    public void onTick(float delta) {
        gameState.addTime(delta);

        if (gameState.getTime() - lastUpdateTime >= 1)
            onUpdateRequestEvent();

        HashSet<AttackScript> toRemove = new HashSet<AttackScript>();

        // Removing expired attack scripts
        Iterator<AttackScript> scripts = gameState.getAttackScripts();
        while (scripts.hasNext()) {
            AttackScript script = scripts.next();
            if (script.isExpired(gameState.getTime())) {
                startedAttackScripts.remove(script);
                toRemove.add(script);
            }
        }
        for (AttackScript script : toRemove)
            gameState.removeAttackScript(script);

        // Resetting sector blocks
        Iterator<PlayerState> players = gameState.getPlayers();
        while (players.hasNext()) {
            ReadOnlyArrayList<Sector> sectors = players.next().getAllSectors();
            for (int sectorIndex = 0; sectorIndex < sectors.size(); sectorIndex++)
                sectors.get(sectorIndex).setBlocked(false);
        }
        scripts = gameState.getAttackScripts();
        while (scripts.hasNext()) {
            AttackScript script = scripts.next();
            PlayerState attacker = gameState.getPlayer(script.attackerId);
            for (UnitGroupMovingInfo movingInfo : script.unitGroupMoving) {
                Sector attackerSector = attacker.getSector(attacker.getBuilding(movingInfo.hangarId).sectorId);
                if (attackerSector != null)
                    attackerSector.setBlocked(true);
            }

            PlayerState victim = gameState.getPlayer(script.victimId);
            if (victim != null) {
                Sector victimSector = victim.getSector(script.sectorId);
                if (victimSector != null)
                    victimSector.setBlocked(true);
            }
        }

        // Processing new attack scripts
        scripts = gameState.getAttackScripts();
        while (scripts.hasNext()) {
            AttackScript script = scripts.next();
            if (script.startArriveTime > gameState.getTime())
                continue;
            if (startedAttackScripts.contains(script))
                continue;
            startedAttackScripts.add(script);

            PlayerState attacker = gameState.getPlayer(script.attackerId);
            PlayerState victim = gameState.getPlayer(script.victimId);
            Sector victimSector = victim.getSector(script.sectorId);

            for (int index = 0; index < script.unitGroupMoving.length; index++) {
                UnitGroupMovingInfo movingInfo = script.unitGroupMoving[index];
                Building building = attacker.getBuilding(movingInfo.hangarId);
                if (building instanceof Hangar) {
                    Hangar hangar = (Hangar) building;

                    double hangarCenterX = hangar.x + hangar.getSizeX() / 2.0;
                    double hangarCenterY = hangar.y + hangar.getSizeY() / 2.0;

                    hangar.units.states.addTimedObject(new TimedObject<UnitGroupState>(
                            script.startArriveTime, new UnitGroupState.Moving(
                                    hangarCenterX, hangarCenterY, script.startArriveTime,
                                    movingInfo.arriveX, movingInfo.arriveY, movingInfo.arriveTime
                            )
                    ));
                    hangar.units.states.addTimedObject(new TimedObject<UnitGroupState>(
                            movingInfo.arriveTime, new UnitGroupState.Attacking(victimSector)
                    ));
                    hangar.units.states.addTimedObject(new TimedObject<UnitGroupState>(
                            script.startReturnTime, new UnitGroupState.Moving(
                                    movingInfo.arriveX, movingInfo.arriveY, script.startReturnTime,
                                    hangarCenterX, hangarCenterY, movingInfo.returnTime
                            )
                    ));
                    hangar.units.states.addTimedObject(new TimedObject<UnitGroupState>(
                            movingInfo.returnTime, new UnitGroupState.Idle(hangar)
                    ));

                    for (int timePoint = 0; timePoint < script.timePoints.length - 1; timePoint++) {
                        TimePoint curr = script.timePoints[timePoint];
                        TimePoint next = script.timePoints[timePoint + 1];
                        hangar.units.healthInterpolations.addTimedObject(new TimedObject<HealthInterpolation>(
                                curr.time, new HealthInterpolation(
                                        curr.unitGroupHealth.get(hangar.id, Float.NaN), curr.time,
                                        next.unitGroupHealth.get(hangar.id, Float.NaN), next.time
                                )
                        ));
                    }
                    hangar.units.healthInterpolations.addTimedObject(new TimedObject<HealthInterpolation>(
                            script.timePoints[script.timePoints.length - 1].time, null
                    ));

                    if (victimSector != null) {
                        victimSector.incomingUnits.addTimedObject(new TimedObject<UnitGroup>(
                                movingInfo.arriveTime, hangar.units
                        ));
                    }
                }

                if (victimSector != null) {
                    for (int timePoint = 0; timePoint < script.timePoints.length - 1; timePoint++) {
                        TimePoint curr = script.timePoints[timePoint];
                        TimePoint next = script.timePoints[timePoint + 1];
                        victimSector.healthInterpolations.addTimedObject(new TimedObject<HealthInterpolation>(
                                curr.time, new HealthInterpolation(
                                        curr.sectorHealth, curr.time,
                                        next.sectorHealth, next.time
                                )
                        ));
                    }
                    victimSector.healthInterpolations.addTimedObject(new TimedObject<HealthInterpolation>(
                            script.timePoints[script.timePoints.length - 1].time, null
                    ));
                }
            }
        }

        ArrayList<Sector> sectorsToRemove = new ArrayList<Sector>();

        // Updating game state
        players = gameState.getPlayers();
        while (players.hasNext()) {
            PlayerState player = players.next();
            ReadOnlyArrayList<Sector> sectors = player.getAllSectors();
            for (int sectorIndex = 0; sectorIndex < sectors.size(); sectorIndex++) {
                Sector next = sectors.get(sectorIndex);
                ReadOnlyArrayList<Building> buildings = next.getAllBuildings();
                for (int building = 0; building < buildings.size(); building++) {
                    Building nextBuilding = buildings.get(building);
                    if (nextBuilding instanceof Hangar)
                        ((Hangar) nextBuilding).units.update(delta, gameState.getTime(), map);
                }
                next.update(delta, gameState.getTime(), map);

                if (next.getHealth() == 0)
                    sectorsToRemove.add(next);
            }
        }

        for (Sector sectorToRemove : sectorsToRemove)
            gameState.getPlayer(sectorToRemove.playerId).removeSector(sectorToRemove);
    }
}
