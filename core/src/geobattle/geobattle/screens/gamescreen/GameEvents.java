package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntIntMap;

import java.util.Iterator;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.GameStateUpdate;
import geobattle.geobattle.game.PlayerState;
import geobattle.geobattle.actionresults.AttackResult;
import geobattle.geobattle.actionresults.BuildResult;
import geobattle.geobattle.actionresults.DestroyResult;
import geobattle.geobattle.actionresults.MatchBranch;
import geobattle.geobattle.actionresults.ResearchResult;
import geobattle.geobattle.actionresults.SectorBuildResult;
import geobattle.geobattle.actionresults.StateRequestResult;
import geobattle.geobattle.actionresults.UnitBuildResult;
import geobattle.geobattle.actionresults.UpdateRequestResult;
import geobattle.geobattle.game.attacking.AttackScript;
import geobattle.geobattle.game.attacking.TimePoint;
import geobattle.geobattle.game.attacking.UnitGroupMovingInfo;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.game.research.ResearchInfo;
import geobattle.geobattle.game.research.ResearchType;
import geobattle.geobattle.game.units.UnitGroupState;
import geobattle.geobattle.game.units.UnitType;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.SelectHangarsMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.SelectSectorMode;
import geobattle.geobattle.server.AuthInfo;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.server.OSAPI;
import geobattle.geobattle.server.Server;
import geobattle.geobattle.util.IntPoint;

// Game events
public class GameEvents {
    // Game server
    public final Server server;

    public final OSAPI oSAPI;

    // Game state
    public final GameState gameState;

    public final AuthInfo authInfo;

    public final GameScreen screen;

    public final GeoBattleMap map;

    private double lastUpdateTime;

    public final GeoBattle game;

    public GameEvents(Server server, OSAPI oSAPI, GameState gameState, AuthInfo authInfo, GameScreen screen, GeoBattleMap map, GeoBattle game) {
        this.server = server;
        this.oSAPI = oSAPI;
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

        server.requestSectorBuild(authInfo, coords.x, coords.y, new Callback<SectorBuildResult>() {
            @Override
            public void onResult(final SectorBuildResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onSectorBuildResult(result);
                    }
                });
            }
        });
    }

    public void onRequestBuildSector() {
        Sector sector = gameState.getCurrentPlayer().getAllSectors().next();

        IntPoint coords = map.getPointedTile();
        coords.x -= ((coords.x - sector.x) % Sector.SECTOR_SIZE + Sector.SECTOR_SIZE) % Sector.SECTOR_SIZE;
        coords.y -= ((coords.y - sector.y) % Sector.SECTOR_SIZE + Sector.SECTOR_SIZE) % Sector.SECTOR_SIZE;

        if (!gameState.canBuildSector(coords.x, coords.y))
            return;

        server.requestSectorBuild(authInfo, coords.x, coords.y, new Callback<SectorBuildResult>() {
            @Override
            public void onResult(final SectorBuildResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onSectorBuildResult(result);
                    }
                });
            }
        });
    }

    private void onSectorBuildResult(SectorBuildResult result) {
        result.match(
                new MatchBranch<SectorBuildResult.SectorBuilt>() {
                    @Override
                    public void onMatch(SectorBuildResult.SectorBuilt sectorBuilt) {
                        try {
                            gameState.getPlayer(sectorBuilt.info.playerIndex).addSector(new Sector(
                                    sectorBuilt.info.x,
                                    sectorBuilt.info.y,
                                    sectorBuilt.info.id,
                                    gameState.getCurrentPlayer().getResearchInfo()
                            ));
                        } catch (IllegalArgumentException ignored) {}
                        screen.switchToNormalMode();
                    }
                },
                new MatchBranch<SectorBuildResult.NotEnoughResources>() {
                    @Override
                    public void onMatch(SectorBuildResult.NotEnoughResources notEnoughResources) {
                        oSAPI.showMessage("Cannot build sector: not enough resources");
                    }
                },
                new MatchBranch<SectorBuildResult.IntersectsWithEnemy>() {
                    @Override
                    public void onMatch(SectorBuildResult.IntersectsWithEnemy intersectsWithEnemy) {
                        oSAPI.showMessage("Cannot build sector: not enough resources");
                    }
                },
                new MatchBranch<SectorBuildResult.WrongPosition>() {
                    @Override
                    public void onMatch(SectorBuildResult.WrongPosition wrongPosition) {
                        oSAPI.showMessage("Cannot build sector: wrong position of sector");
                    }
                },
                new MatchBranch<SectorBuildResult.WrongAuthInfo>() {
                    @Override
                    public void onMatch(SectorBuildResult.WrongAuthInfo wrongAuthInfo) {
                        oSAPI.showMessage("Not authorized!");
                        game.switchToLoginScreen();
                        // Gdx.app.error("GeoBattle", "Not authorized!");
                        // Gdx.app.exit();
                    }
                },
                new MatchBranch<SectorBuildResult.MalformedJson>() {
                    @Override
                    public void onMatch(SectorBuildResult.MalformedJson malformedJson) {
                        oSAPI.showMessage("Cannot build sector: JSON request is not well-formed. Probable bug. Tell the developers");
                    }
                },
                new MatchBranch<SectorBuildResult.IncorrectData>() {
                    @Override
                    public void onMatch(SectorBuildResult.IncorrectData incorrectData) {
                        oSAPI.showMessage("Cannot build sector: value of field in request is not valid. Probable bug. Tell the developers");
                    }
                }
        );
    }

    // Invokes when user requests building
    public void onRequestBuild() {
        BuildingType buildingType = map.getSelectedBuildingType();
        IntPoint coords = map.getPointedTile();
        coords.x -= buildingType.sizeX / 2;
        coords.y -= buildingType.sizeY / 2;

        if (!gameState.canBuildBuilding(map.getSelectedBuildingType(), coords.x, coords.y))
            return;

        server.requestBuild(authInfo, buildingType, coords.x, coords.y, new Callback<BuildResult>() {
            @Override
            public void onResult(final BuildResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onBuildResult(result);
                    }
                });
            }
        });
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
            server.requestDestroy(authInfo, building.id, new Callback<DestroyResult>() {
                @Override
                public void onResult(final DestroyResult result) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            onDestroyResult(result);
                        }
                    });
                }
            });

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

        server.requestUnitBuild(authInfo, unitType, building, new Callback<UnitBuildResult>() {
            @Override
            public void onResult(final UnitBuildResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onUnitBuildResult(result);
                    }
                });
            }
        });
    }

    private void onUnitBuildResult(UnitBuildResult result) {
        if (result != null) {
            result.apply(game, gameState);
            screen.switchTo(result.screenModeAfterApply());
        }
    }

    public void onRequestUpdate() {
        server.requestUpdate(authInfo, gameState.getLastUpdateTime(), new Callback<UpdateRequestResult>() {
            @Override
            public void onResult(final UpdateRequestResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onRequestUpdateResult(result);
                    }
                });
            }
        });
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
                new MatchBranch<UpdateRequestResult.WrongAuthInfo>() {
                    @Override
                    public void onMatch(UpdateRequestResult.WrongAuthInfo wrongAuthInfo) {
                        oSAPI.showMessage("Not authorized!");
                        game.switchToLoginScreen();
                        // Gdx.app.error("GeoBattle", "Not authorized!");
                        // Gdx.app.exit();
                    }
                },
                new MatchBranch<UpdateRequestResult.MalformedJson>() {
                    @Override
                    public void onMatch(UpdateRequestResult.MalformedJson malformedJson) {
                        oSAPI.showMessage("Cannot build: JSON request is not well-formed. Probable bug. Tell the developers");
                    }
                },
                new MatchBranch<UpdateRequestResult.IncorrectData>() {
                    @Override
                    public void onMatch(UpdateRequestResult.IncorrectData incorrectData) {
                        oSAPI.showMessage("Cannot build: incorrect data");
                    }
                }
        );
    }

    public void onResearch(ResearchType researchType) {
        server.requestResearch(authInfo, researchType, new Callback<ResearchResult>() {
            @Override
            public void onResult(final ResearchResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onResearchResult(result);
                    }
                });
            }
        });
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

        server.requestAttack(authInfo, attackerId, victimId, hangarIds, sectorId, new Callback<AttackResult>() {
            @Override
            public void onResult(final AttackResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onAttackResult(result);
                    }
                });
            }
        });
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

                    Gdx.app.log("GeoBattle", "Set Idle state");
                    next.units.setState(new UnitGroupState.Idle(next));
                }

                Gdx.app.log("GeoBattle", "Attack script expired");

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

            Iterator<Hangar> hangars = attacker.getHangars();
            while (hangars.hasNext()) {
                Hangar next = hangars.next();

                if (!hangarIds.containsKey(next.id))
                    continue;

                if (prevTimePoint == null && nextTimePoint == null) {
                    Gdx.app.log("GeoBattle", "Both are null");
                } else if (prevTimePoint == null) {
                    Gdx.app.log("GeoBattle", "Next is " + nextTimePoint.time);
                } else if (nextTimePoint == null) {
                    Gdx.app.log("GeoBattle", "Prev is " + prevTimePoint.time);
                }

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

                double hangarCenterX = next.x + next.getSizeX() / 2.0;
                double hangarCenterY = next.y + next.getSizeY() / 2.0;

                if (gameState.getTime() < unitGroupMovingInfo.arriveTime) {
                    if (next.units.getLastUpdateTime() < attackScript.startArriveTime)
                        next.units.setState(new UnitGroupState.Moving(hangarCenterX, hangarCenterY, attackScript.startArriveTime, unitGroupMovingInfo.arriveX, unitGroupMovingInfo.arriveY, unitGroupMovingInfo.arriveTime));
                } else if (gameState.getTime() > attackScript.startReturnTime) {
                    if (next.units.getLastUpdateTime() < attackScript.startReturnTime) {
                        next.units.setState(new UnitGroupState.Moving(unitGroupMovingInfo.arriveX, unitGroupMovingInfo.arriveY, attackScript.startReturnTime, hangarCenterX, hangarCenterY, unitGroupMovingInfo.returnTime));
                    }
                } else {
                    if (next.units.getLastUpdateTime() < unitGroupMovingInfo.arriveTime)
                        next.units.setState(new UnitGroupState.Attacking(victimSector, new Building[4]));
                }
            }
        }

        Iterator<PlayerState> players = gameState.getPlayers();
        while (players.hasNext()) {
            Iterator<Sector> sectors = players.next().getAllSectors();
            while (sectors.hasNext()) {
                Iterator<Hangar> hangars = sectors.next().getHangars();
                while (hangars.hasNext()) {
                    hangars.next().units.update(delta, gameState.getTime());
                }
            }
        }
    }
}
