package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntIntMap;

import java.util.Iterator;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.GameStateUpdate;
import geobattle.geobattle.game.PlayerState;
import geobattle.geobattle.game.actionresults.BuildResult;
import geobattle.geobattle.game.actionresults.DestroyResult;
import geobattle.geobattle.game.actionresults.MatchBranch;
import geobattle.geobattle.game.actionresults.ResearchResult;
import geobattle.geobattle.game.actionresults.SectorBuildResult;
import geobattle.geobattle.game.actionresults.StateRequestResult;
import geobattle.geobattle.game.actionresults.UnitBuildResult;
import geobattle.geobattle.game.actionresults.UpdateRequestResult;
import geobattle.geobattle.game.attacking.AttackEvent;
import geobattle.geobattle.game.attacking.TimePoint;
import geobattle.geobattle.game.attacking.UnitGroupMovingInfo;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.game.research.ResearchType;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.game.units.UnitType;
import geobattle.geobattle.map.GeoBattleMap;
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
        Sector sector = gameState.getPlayers().get(gameState.getPlayerId()).getAllSectors().next();


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
                        gameState.getPlayers().get(sectorBuilt.info.playerIndex).addSector(new Sector(
                                sectorBuilt.info.x,
                                sectorBuilt.info.y,
                                sectorBuilt.info.id
                        ));
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
        result.match(
                new MatchBranch<BuildResult.Built>() {
                    @Override
                    public void onMatch(BuildResult.Built built) {
                        gameState.getPlayers().get(built.info.playerIndex).addBuilding(built.info.building);
                        gameState.setResources(gameState.getResources() - built.cost);

                        screen.switchToNormalMode();
                    }
                },
                new MatchBranch<BuildResult.CollisionFound>() {
                    @Override
                    public void onMatch(BuildResult.CollisionFound collisionFound) {
                        oSAPI.showMessage("Cannot build: collision found");
                    }
                },
                new MatchBranch<BuildResult.NotEnoughResources>() {
                    @Override
                    public void onMatch(BuildResult.NotEnoughResources notEnoughResources) {
                        oSAPI.showMessage("Cannot build: not enough resources");
                    }
                },
                new MatchBranch<BuildResult.BuildingLimitExceeded>() {
                    @Override
                    public void onMatch(BuildResult.BuildingLimitExceeded buildingLimitExceeded) {
                        oSAPI.showMessage("Cannot build: building limit exceeded");
                    }
                },
                new MatchBranch<BuildResult.NotInTerritory>() {
                    @Override
                    public void onMatch(BuildResult.NotInTerritory notInTerritory) {
                        oSAPI.showMessage("Cannot build: not in territory");
                    }
                },
                new MatchBranch<BuildResult.SectorBlocked>() {
                    @Override
                    public void onMatch(BuildResult.SectorBlocked sectorBlocked) {
                        oSAPI.showMessage("Cannot build: sector is blocked");
                    }
                },
                new MatchBranch<BuildResult.WrongAuthInfo>() {
                    @Override
                    public void onMatch(BuildResult.WrongAuthInfo wrongAuthInfo) {
                        oSAPI.showMessage("Not authorized!");
                        game.switchToLoginScreen();
                        // Gdx.app.error("GeoBattle", "Not authorized!");
                        // Gdx.app.exit();
                    }
                },
                new MatchBranch<BuildResult.MalformedJson>() {
                    @Override
                    public void onMatch(BuildResult.MalformedJson malformedJson) {
                        oSAPI.showMessage("Cannot build: JSON request is not well-formed. Probable bug. Tell the developers");
                    }
                },
                new MatchBranch<BuildResult.IncorrectData>() {
                    @Override
                    public void onMatch(BuildResult.IncorrectData incorrectData) {
                        oSAPI.showMessage("Cannot build: value of field in request is not valid. Probable bug. Tell the developers");
                    }
                }
        );
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
        result.match(
                new MatchBranch<DestroyResult.Destroyed>() {
                    @Override
                    public void onMatch(DestroyResult.Destroyed destroyed) {
                        gameState.getPlayers().get(destroyed.info.playerIndex).removeBuilding(destroyed.info.building);
                    }
                },
                new MatchBranch<DestroyResult.NotOwningBuilding>() {
                    @Override
                    public void onMatch(DestroyResult.NotOwningBuilding notOwningBuilding) {
                        oSAPI.showMessage("Cannot destroy: you are not owning this building");
                    }
                },
                new MatchBranch<DestroyResult.SectorBlocked>() {
                    @Override
                    public void onMatch(DestroyResult.SectorBlocked sectorBlocked) {
                        oSAPI.showMessage("Cannot destroy: sector is blocked");
                    }
                },
                new MatchBranch<DestroyResult.WrongAuthInfo>() {
                    @Override
                    public void onMatch(DestroyResult.WrongAuthInfo wrongAuthInfo) {
                        oSAPI.showMessage("Not authorized!");
                        game.switchToLoginScreen();
                        // Gdx.app.error("GeoBattle", "Not authorized!");
                        // Gdx.app.exit();
                    }
                },
                new MatchBranch<DestroyResult.MalformedJson>() {
                    @Override
                    public void onMatch(DestroyResult.MalformedJson malformedJson) {
                        oSAPI.showMessage("Cannot build: JSON request is not well-formed. Probable bug. Tell the developers");
                    }
                },
                new MatchBranch<DestroyResult.IncorrectData>() {
                    @Override
                    public void onMatch(DestroyResult.IncorrectData incorrectData) {
                        oSAPI.showMessage("Cannot build: value of field in request is not valid. Probable bug. Tell the developers");
                    }
                }
        );
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
        result.match(
                new MatchBranch<UnitBuildResult.Built>() {
                    @Override
                    public void onMatch(UnitBuildResult.Built built) {
                        gameState.getPlayers().get(built.info.playerIndex).addUnit(built.info.unit);
                        gameState.setResources(gameState.getResources() - built.cost);
                    }
                },
                new MatchBranch<UnitBuildResult.NotEnoughResources>() {
                    @Override
                    public void onMatch(UnitBuildResult.NotEnoughResources notEnoughResources) {
                        oSAPI.showMessage("Cannot build: not in territory");
                    }
                },
                new MatchBranch<UnitBuildResult.NotHangar>() {
                    @Override
                    public void onMatch(UnitBuildResult.NotHangar notHangar) {
                        oSAPI.showMessage("Cannot build: not a hangar");
                    }
                },
                new MatchBranch<UnitBuildResult.NoPlaceInHangar>() {
                    @Override
                    public void onMatch(UnitBuildResult.NoPlaceInHangar noPlaceInHangar) {
                        oSAPI.showMessage("Cannot build: no place in hangar");
                    }
                },
                new MatchBranch<UnitBuildResult.DoesNotExist>() {
                    @Override
                    public void onMatch(UnitBuildResult.DoesNotExist doesNotExist) {
                        oSAPI.showMessage("Cannot build: does not exist");
                    }
                },
                new MatchBranch<UnitBuildResult.WrongAuthInfo>() {
                    @Override
                    public void onMatch(UnitBuildResult.WrongAuthInfo match) {
                        oSAPI.showMessage("Not authorized!");
                        game.switchToLoginScreen();
                        // Gdx.app.error("GeoBattle", "Not authorized!");
                        // Gdx.app.exit();
                    }
                }
        );
    }

    public void onRequestUpdate() {
        server.requestState(authInfo, new Callback<StateRequestResult>() {
            @Override
            public void onResult(final StateRequestResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onRequestUpdateResult(result);
                    }
                });
            }
        });
//        server.requestUpdate(authInfo, new Callback<UpdateRequestResult>() {
//            @Override
//            public void onResult(UpdateRequestResult result) {
//                onRequestUpdateResult(result);
//            }
//        });
    }

    private void onRequestUpdateResult(StateRequestResult result) {
        result.match(
                new MatchBranch<StateRequestResult.StateRequestSuccess>() {
                    @Override
                    public void onMatch(StateRequestResult.StateRequestSuccess stateRequestSuccess) {
                        gameState.setData(stateRequestSuccess.gameState);
                    }
                },
                new MatchBranch<StateRequestResult.WrongAuthInfo>() {
                    @Override
                    public void onMatch(StateRequestResult.WrongAuthInfo match) {
                        oSAPI.showMessage("Not authorized!");
                        game.switchToLoginScreen();
                        // Gdx.app.error("GeoBattle", "Not authorized!");
                        // Gdx.app.exit();
                    }
                },
                new MatchBranch<StateRequestResult.MalformedJson>() {
                    @Override
                    public void onMatch(StateRequestResult.MalformedJson malformedJson) {
                        oSAPI.showMessage("Cannot build: JSON request is not well-formed. Probable bug. Tell the developers");
                    }
                }
        );
    }

    private void onRequestUpdateResult(UpdateRequestResult result) {
        result.match(
                new MatchBranch<UpdateRequestResult.UpdateRequestSuccess>() {
                    @Override
                    public void onMatch(UpdateRequestResult.UpdateRequestSuccess updateRequestSuccess) {
                        for (GameStateUpdate update : updateRequestSuccess.updates)
                            update.update(gameState);
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
        result.match(
                new MatchBranch<ResearchResult.Researched>() {
                    @Override
                    public void onMatch(ResearchResult.Researched researched) {
                        gameState.getResearchInfo().incrementLevel(ResearchType.from(researched.researchType));
                    }
                },
                new MatchBranch<ResearchResult.NotEnoughResources>() {
                    @Override
                    public void onMatch(ResearchResult.NotEnoughResources notEnoughResources) {
                        oSAPI.showMessage("Cannot research: not enough resources");
                    }
                },
                new MatchBranch<ResearchResult.MaxLevel>() {
                    @Override
                    public void onMatch(ResearchResult.MaxLevel maxLevel) {
                        oSAPI.showMessage("Cannot research: max level reached");
                    }
                },
                new MatchBranch<ResearchResult.WrongAuthInfo>() {
                    @Override
                    public void onMatch(ResearchResult.WrongAuthInfo wrongAuthInfo) {
                        oSAPI.showMessage("Not authorized!");
                        game.switchToLoginScreen();
                    }
                },
                new MatchBranch<ResearchResult.MalformedJson>() {
                    @Override
                    public void onMatch(ResearchResult.MalformedJson malformedJson) {
                        oSAPI.showMessage("Cannot build: malformed JSON");
                    }
                },
                new MatchBranch<ResearchResult.IncorrectData>() {
                    @Override
                    public void onMatch(ResearchResult.IncorrectData incorrectData) {
                        oSAPI.showMessage("Cannot build: incorrect data");
                    }
                }
        );
    }

    public void onTick(float delta) {
        gameState.addTime(delta);

        if (gameState.getTime() - lastUpdateTime >= 1) {
            onRequestUpdate();
            lastUpdateTime = gameState.getTime();
        }

        for (int eventIndex = 0; eventIndex < gameState.getAttackEvents().size();) {
            if (gameState.getAttackEvents().get(eventIndex).isExpired(gameState.getTime())) {
                gameState.getAttackEvents().remove(eventIndex);
            } else
                eventIndex++;
        }

        // Gdx.app.log("GeoBattle", "Count of events: " + gameState.getAttackEvents().size());
        for (AttackEvent attackEvent : gameState.getAttackEvents()) {
            if (attackEvent.startArriveTime > gameState.getTime())
                continue;

            PlayerState attacker = gameState.getPlayers().get(attackEvent.attackerId);
            PlayerState victim = gameState.getPlayers().get(attackEvent.victimId);

            IntIntMap hangarIds = new IntIntMap(attackEvent.unitGroupMoving.length);
            for (int index = 0; index < attackEvent.unitGroupMoving.length; index++)
                hangarIds.put(attackEvent.unitGroupMoving[index].hangarId, index);

            TimePoint prev = attackEvent.getTimePointBefore(gameState.getTime());
            TimePoint curr = attackEvent.getTimePointAfter(gameState.getTime());

            Iterator<Hangar> hangars = attacker.getHangars();
            while (hangars.hasNext()) {
                Hangar next = hangars.next();

                if (!hangarIds.containsKey(next.id))
                    continue;

                UnitGroupMovingInfo unitGroupMovingInfo = attackEvent.unitGroupMoving[hangarIds.get(next.id, -1)];

                double realPosX;
                double realPosY;
                double direction;
                if (gameState.getTime() < unitGroupMovingInfo.arriveTime) {
                    double factor = (gameState.getTime() - attackEvent.startArriveTime) / (unitGroupMovingInfo.arriveTime - attackEvent.startArriveTime);

                    double deltaX = (unitGroupMovingInfo.arriveX - next.x) * factor;
                    double deltaY = (unitGroupMovingInfo.arriveY - next.y) * factor;

                    realPosX = next.x + deltaX;
                    realPosY = next.y + deltaY;

                    double cosDirection = deltaX / Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    direction = Math.acos(cosDirection);

                    if (deltaY < 0)
                        direction = 2 * Math.PI - direction;
                } else if (gameState.getTime() > attackEvent.startReturnTime) {
                    double factor = (gameState.getTime() - attackEvent.startReturnTime) / (unitGroupMovingInfo.returnTime - attackEvent.startReturnTime);

                    double deltaX = (next.x - unitGroupMovingInfo.arriveX) * factor;
                    double deltaY = (next.y - unitGroupMovingInfo.arriveY) * factor;

                    realPosX = unitGroupMovingInfo.arriveX + deltaX;
                    realPosY = unitGroupMovingInfo.arriveY + deltaY;

                    double cosDirection = deltaX / Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    direction = Math.acos(cosDirection);

                    if (deltaY < 0)
                        direction = 2 * Math.PI - direction;
                } else {
                    realPosX = unitGroupMovingInfo.arriveX;
                    realPosY = unitGroupMovingInfo.arriveY;

                    direction = 0;
                }

                double[] unitOffsetX = { 1, -1, -1, 1 };
                double[] unitOffsetY = { 1, 1, -1, -1 };
                double factor = 2 * Math.sqrt(2);

                Iterator<Unit> units = next.units.getAllUnits();
                while (units.hasNext()) {
                    Unit nextUnit = units.next();
                    nextUnit.direction = Math.toDegrees(direction);
                    nextUnit.x = realPosX + factor * (unitOffsetX[nextUnit.hangarSlot] * Math.cos(direction) - unitOffsetY[nextUnit.hangarSlot] * Math.sin(direction));
                    nextUnit.y = realPosY + factor * (unitOffsetX[nextUnit.hangarSlot] * Math.sin(direction) + unitOffsetY[nextUnit.hangarSlot] * Math.cos(direction));
                }
            }
        }
    }
}
