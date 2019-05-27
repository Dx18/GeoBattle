package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntIntMap;

import java.util.ArrayList;
import java.util.Iterator;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.actionresults.AttackResult;
import geobattle.geobattle.actionresults.BuildResult;
import geobattle.geobattle.actionresults.DestroyResult;
import geobattle.geobattle.actionresults.MatchBranch;
import geobattle.geobattle.actionresults.ResearchResult;
import geobattle.geobattle.actionresults.SectorBuildResult;
import geobattle.geobattle.actionresults.UnitBuildResult;
import geobattle.geobattle.actionresults.UpdateRequestResult;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.GameStateUpdate;
import geobattle.geobattle.game.PlayerState;
import geobattle.geobattle.game.attacking.AttackScript;
import geobattle.geobattle.game.attacking.TimePoint;
import geobattle.geobattle.game.attacking.UnitGroupMovingInfo;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.game.buildings.SectorState;
import geobattle.geobattle.game.buildings.Turret;
import geobattle.geobattle.game.research.ResearchInfo;
import geobattle.geobattle.game.research.ResearchType;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.game.units.UnitGroup;
import geobattle.geobattle.game.units.UnitGroupState;
import geobattle.geobattle.game.units.UnitType;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.SelectHangarsMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.SelectSectorMode;
import geobattle.geobattle.server.AuthInfo;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.util.IntPoint;

// Game events
public class GameEvents {
    // Game server
    private final GeoBattle game;

    // Game state
    public final GameState gameState;

    public final AuthInfo authInfo;

    public final GameScreen screen;

    public final GeoBattleMap map;

    private double lastUpdateTime;

    public GameEvents(GameState gameState, AuthInfo authInfo, GameScreen screen, GeoBattleMap map, GeoBattle game) {
        this.gameState = gameState;
        this.authInfo = authInfo;
        this.screen = screen;
        this.map = map;
        this.game = game;
        this.lastUpdateTime = gameState.getTime();
    }

    public void onRequestBuildFirstSector() {
        IntPoint coords = map.getPointedTile();
        coords.x -= Sector.SECTOR_SIZE / 2;
        coords.y -= Sector.SECTOR_SIZE / 2;

        if (!gameState.canBuildSector(coords.x, coords.y))
            return;

        game.getExternalAPI().server.requestSectorBuild(authInfo, coords.x, coords.y, new Callback<SectorBuildResult>() {
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

    public void onRequestBuildSector() {
        Sector sector = gameState.getCurrentPlayer().getAllSectors().next();

        IntPoint coords = map.getPointedTile();
        coords.x -= ((coords.x - sector.x) % Sector.SECTOR_SIZE + Sector.SECTOR_SIZE) % Sector.SECTOR_SIZE;
        coords.y -= ((coords.y - sector.y) % Sector.SECTOR_SIZE + Sector.SECTOR_SIZE) % Sector.SECTOR_SIZE;

        if (!gameState.canBuildSector(coords.x, coords.y))
            return;

        game.getExternalAPI().server.requestSectorBuild(authInfo, coords.x, coords.y, new Callback<SectorBuildResult>() {
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

    private void onSectorBuildResult(SectorBuildResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    // Invokes when user requests building
    public void onRequestBuild() {
        BuildingType buildingType = map.getSelectedBuildingType();
        IntPoint coords = map.getPointedTile();
        coords.x -= buildingType.sizeX / 2;
        coords.y -= buildingType.sizeY / 2;

        if (!gameState.canBuildBuilding(map.getSelectedBuildingType(), coords.x, coords.y))
            return;

        game.getExternalAPI().server.requestBuild(authInfo, buildingType, coords.x, coords.y, new Callback<BuildResult>() {
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

    // Invokes when user receives build result
    private void onBuildResult(BuildResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    // Invokes when user requests building destroying
    public void onRequestDestroy() {
        Building building = map.getPointedBuilding();

        if (building != null)
            game.getExternalAPI().server.requestDestroy(authInfo, building.id, new Callback<DestroyResult>() {
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

        screen.switchToNormalMode();
    }

    // Invokes when user receives destroy result
    private void onDestroyResult(DestroyResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    public void onUnitBuild(UnitType unitType) {
        Building building = map.getPointedBuilding();

        game.getExternalAPI().server.requestUnitBuild(authInfo, unitType, building, new Callback<UnitBuildResult>() {
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

    private void onUnitBuildResult(UnitBuildResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    public void onRequestUpdate() {
        game.getExternalAPI().server.requestUpdate(authInfo, gameState.getLastUpdateTime(), new Callback<UpdateRequestResult>() {
            @Override
            public void onResult(final UpdateRequestResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onRequestUpdateResult(result);
                    }
                });
            }
        }, null);
    }

    private void onRequestUpdateResult(UpdateRequestResult result) {
        result.match(
                new MatchBranch<UpdateRequestResult.UpdateRequestSuccess>() {
                    @Override
                    public void onMatch(UpdateRequestResult.UpdateRequestSuccess updateRequestSuccess) {
                        if (gameState.getLastUpdateTime() < updateRequestSuccess.time) {
                            gameState.setLastUpdateTime(updateRequestSuccess.time);

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
                        game.getExternalAPI().oSAPI.showMessage("Cannot build: JSON request is not well-formed. Probable bug. Tell the developers");
                    }
                },
                new MatchBranch<UpdateRequestResult.IncorrectData>() {
                    @Override
                    public void onMatch(UpdateRequestResult.IncorrectData incorrectData) {
                        game.getExternalAPI().oSAPI.showMessage("Cannot build: incorrect data");
                    }
                }
        );
    }

    public void onResearch(ResearchType researchType) {
        game.getExternalAPI().server.requestResearch(authInfo, researchType, new Callback<ResearchResult>() {
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

    private void onResearchResult(ResearchResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    public void onRequestAttack() {
        SelectHangarsMode selectHangarsMode = (SelectHangarsMode) map.getScreenModeData(GameScreenMode.SELECT_HANGARS);
        Gdx.app.log("GeoBattle", "onRequestAttack: count is " + selectHangarsMode.getSelectedHangarsCount());
        if (selectHangarsMode.getSelectedHangarsCount() == 0)
            return;

        SelectSectorMode selectSectorMode = (SelectSectorMode) map.getScreenModeData(GameScreenMode.SELECT_SECTOR);
        Gdx.app.log("GeoBattle", "onRequestAttack: pointed sector is " + selectSectorMode.getPointedSector());
        if (selectSectorMode.getPointedSector() == null)
            return;

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

        game.getExternalAPI().server.requestAttack(authInfo, attackerId, victimId, hangarIds, sectorId, new Callback<AttackResult>() {
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

    private void onAttackResult(AttackResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    public void onTick(float delta) {
        gameState.addTime(delta);

        if (gameState.getTime() - lastUpdateTime >= 1) {
            onRequestUpdate();
            lastUpdateTime = gameState.getTime();
        }

        {
            Iterator<PlayerState> players = gameState.getPlayers();
            while (players.hasNext()) {
                Iterator<Sector> sectors = players.next().getAllSectors();
                while (sectors.hasNext())
                    sectors.next().setBlocked(false);
            }
        }

        for (int eventIndex = 0; eventIndex < gameState.getAttackScripts().size();) {
            if (gameState.getAttackScripts().get(eventIndex).isExpired(gameState.getTime())) {
                AttackScript attackScript = gameState.getAttackScripts().get(eventIndex);
                PlayerState attacker = gameState.getPlayer(attackScript.attackerId);
                IntIntMap hangarIds = new IntIntMap(attackScript.unitGroupMoving.length);
                for (int index = 0; index < attackScript.unitGroupMoving.length; index++)
                    hangarIds.put(attackScript.unitGroupMoving[index].hangarId, index);
                Iterator<Hangar> hangars = attacker.getHangars();
                while (hangars.hasNext()) {
                    Hangar next = hangars.next();

                    if (!hangarIds.containsKey(next.id))
                        continue;

                    next.units.setState(new UnitGroupState.Idle(next));
                }

                gameState.getAttackScripts().remove(eventIndex);
            } else
                eventIndex++;
        }

        // Gdx.app.log("GeoBattle", "Count of events: " + gameState.getAttackScripts().size());
        for (AttackScript attackScript : gameState.getAttackScripts()) {
            if (attackScript.startArriveTime > gameState.getTime())
                continue;

            PlayerState attacker = gameState.getPlayer(attackScript.attackerId);
            PlayerState victim = gameState.getPlayer(attackScript.victimId);
            Sector victimSector = victim.getSector(attackScript.sectorId);

            IntIntMap hangarIds = new IntIntMap(attackScript.unitGroupMoving.length);
            for (int index = 0; index < attackScript.unitGroupMoving.length; index++)
                hangarIds.put(attackScript.unitGroupMoving[index].hangarId, index);

            TimePoint prevTimePoint = attackScript.getTimePointBefore(gameState.getTime());
            TimePoint nextTimePoint = attackScript.getTimePointAfter(gameState.getTime());

            if (victimSector != null)
                victimSector.setBlocked(true);

            if (victimSector != null && !(victimSector.getState() instanceof SectorState.Attacked)) {
                Iterator<Turret> turrets = victimSector.getTurrets();
                int turretCount = 0;
                while (turrets.hasNext()) {
                    turretCount++;
                    turrets.next();
                }

                victimSector.setState(new SectorState.Attacked(new ArrayList<UnitGroup>(), new Unit[turretCount]));
            }

            Iterator<Hangar> hangars = attacker.getHangars();
            while (hangars.hasNext()) {
                Hangar next = hangars.next();

                if (!hangarIds.containsKey(next.id))
                    continue;

                attacker.getSector(next.sectorId).setBlocked(true);

                float factor = (float) ((gameState.getTime() - prevTimePoint.time) / (nextTimePoint.time - prevTimePoint.time));

                float prevHealth = prevTimePoint.unitGroupHealth.get(next.id, Float.NaN);
                float nextHealth = nextTimePoint.unitGroupHealth.get(next.id, Float.NaN);

                float currentHealth = prevHealth + factor * (nextHealth - prevHealth);

                next.units.setHealth(currentHealth);

                if (victimSector != null) {
                    double prevSectorHealth = prevTimePoint.sectorHealth;
                    double nextSectorHealth = nextTimePoint.sectorHealth;

                    double currentSectorHealth = prevSectorHealth + factor * (nextSectorHealth - prevSectorHealth);

                    victimSector.setHealth((float) currentSectorHealth);
                    if (victimSector.getHealth() == 0) {
                        victim.removeSector(victimSector);
                        victimSector = null;
                    }
                }

                UnitGroupMovingInfo unitGroupMovingInfo = attackScript.unitGroupMoving[hangarIds.get(next.id, -1)];

                if (gameState.getTime() > unitGroupMovingInfo.returnTime && !(next.units.getState() instanceof UnitGroupState.Idle)) {
                    next.units.setState(new UnitGroupState.Idle(next));
                    continue;
                }

                double hangarCenterX = next.x + next.getSizeX() / 2.0;
                double hangarCenterY = next.y + next.getSizeY() / 2.0;

                if (gameState.getTime() < unitGroupMovingInfo.arriveTime) {
                    if (next.units.getLastUpdateTime() < attackScript.startArriveTime)
                        next.units.setState(new UnitGroupState.Moving(hangarCenterX, hangarCenterY, attackScript.startArriveTime, unitGroupMovingInfo.arriveX, unitGroupMovingInfo.arriveY, unitGroupMovingInfo.arriveTime));
                } else if (gameState.getTime() > attackScript.startReturnTime) {
                    if (next.units.getLastUpdateTime() < attackScript.startReturnTime) {
                        next.units.setState(new UnitGroupState.Moving(unitGroupMovingInfo.arriveX, unitGroupMovingInfo.arriveY, attackScript.startReturnTime, hangarCenterX, hangarCenterY, unitGroupMovingInfo.returnTime));
                    }
                    if (victimSector != null && victimSector.getLastUpdateTime() < attackScript.startReturnTime) {
                        victimSector.setState(new SectorState.Normal());
                    }
                } else {
                    if (victimSector != null && next.units.getLastUpdateTime() < unitGroupMovingInfo.arriveTime) {
                        next.units.setState(new UnitGroupState.Attacking(victimSector));
                        if (victimSector.getState() instanceof SectorState.Attacked) {
                            ((SectorState.Attacked) victimSector.getState()).units.add(next.units);
                        }
                    }
                }
            }
        }

        Iterator<PlayerState> players = gameState.getPlayers();
        while (players.hasNext()) {
            Iterator<Sector> sectors = players.next().getAllSectors();
            while (sectors.hasNext()) {
                Sector next = sectors.next();
                Iterator<Hangar> hangars = next.getHangars();
                while (hangars.hasNext()) {
                    hangars.next().units.update(delta, gameState.getTime(), map);
                }
                next.update(delta, gameState.getTime(), map);
            }
        }
    }
}
