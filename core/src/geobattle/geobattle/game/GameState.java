package geobattle.geobattle.game;

import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.IntMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.attacking.AttackScript;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.game.gamestatediff.GameStateDiff;
import geobattle.geobattle.game.gamestatediff.PlayerStateDiff;
import geobattle.geobattle.game.units.UnitType;
import geobattle.geobattle.util.GeoBattleMath;
import geobattle.geobattle.util.IntPoint;
import geobattle.geobattle.util.ReadOnlyArrayList;

// State of game
public class GameState {
    // Player's resources
    private float resources;

    // Player's index in game state
    private int playerId;

    // Time
    private double time;

    // Last update time
    private double lastUpdateTime;

    // Players
    private IntMap<PlayerState> players;

    // Attack events
    private HashSet<AttackScript> attackScripts;

    // Tracker of game objects
    public final GameObjectTracker gameObjectTracker;

    // Comparator for buildings
    private final static Comparator<PlayerState> playerComparator = new Comparator<PlayerState>() {
        @Override
        public int compare(PlayerState player1, PlayerState player2) {
            return player1.getPlayerId() - player2.getPlayerId();
        }
    };

    public GameState(float resources, int playerId, double time) {
        this.resources = resources;
        this.playerId = playerId;
        this.time = time;
        this.players = new IntMap<PlayerState>();
        this.attackScripts = new HashSet<AttackScript>();

        IntFloatMap unitHealth1 = new IntFloatMap();
        unitHealth1.put(-1, UnitType.BOMBER.maxHealth * 4);
        IntFloatMap unitHealth2 = new IntFloatMap();
        unitHealth2.put(-1, UnitType.BOMBER.maxHealth * 2f);

        this.lastUpdateTime = time;

        this.gameObjectTracker = new GameObjectTracker();
    }

    // Returns player's resources
    public float getResources() {
        return resources;
    }

    // Sets player's resources
    public void setResources(float resources) {
        this.resources = Math.max(0, resources);
    }

    // Returns player's index in game state
    public int getPlayerId() {
        return playerId;
    }

    // Sets player's index
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    // Returns current time
    public double getTime() {
        return time;
    }

    // Sets current time
    public void setTime(double time) {
        this.time = time;
    }

    // Adds time
    public void addTime(double time) {
        this.time += time;
    }

    // Returns time of last update
    public double getLastUpdateTime() {
        return lastUpdateTime;
    }

    // Sets last update time
    public void setLastUpdateTime(double lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    // Returns iterator over players
    public Iterator<PlayerState> getPlayers() {
        return players.values();
    }

    // Returns player by ID
    public PlayerState getPlayer(int id) {
        return players.get(id);
    }

    // Returns current player
    public PlayerState getCurrentPlayer() {
        return getPlayer(playerId);
    }

    // Adds player to game
    public void addPlayer(PlayerState player) {
        if (players.containsKey(player.getPlayerId()))
            throw new IllegalArgumentException("Cannot add player with existing ID");
        players.put(player.getPlayerId(), player);
    }

    // Removes player from game
    public void removePlayer(PlayerState player) {
        if (!players.containsKey(player.getPlayerId()))
            throw new IllegalArgumentException("Cannot remove player with specified ID");
        players.remove(player.getPlayerId());
    }

    // Returns iterator over attack scripts
    public Iterator<AttackScript> getAttackScripts() {
        return attackScripts.iterator();
    }

    // Adds attack script
    public void addAttackScript(AttackScript attackScript) {
        attackScripts.add(attackScript);
    }

    // Removes attack script
    public void removeAttackScript(AttackScript attackScript) {
        attackScripts.remove(attackScript);
    }

    public static GameState fromJson(JsonObject object) {
        float resources = object.getAsJsonPrimitive("resources").getAsFloat();
        int playerId = object.getAsJsonPrimitive("playerId").getAsInt();
        double time = object.getAsJsonPrimitive("time").getAsDouble();
        // ResearchInfo researchInfo = ResearchInfo.fromJson(object.getAsJsonObject("researchInfo"));

        GameState gameState = new GameState(resources, playerId, time);

        JsonArray jsonPlayers = object.getAsJsonArray("players");
        for (JsonElement jsonPlayer : jsonPlayers)
            gameState.addPlayer(PlayerState.fromJson(jsonPlayer.getAsJsonObject(), gameState.gameObjectTracker));

        JsonArray jsonAttackEvents = object.getAsJsonArray("attackEvents");
        for (JsonElement jsonAttackEvent : jsonAttackEvents)
            gameState.addAttackScript(AttackScript.fromJson(jsonAttackEvent.getAsJsonObject()));

        return gameState;
    }

    // Applies GameStateDiff to game state
    public void applyDiff(GameStateDiff diff) {
        for (PlayerState removed : diff.removedPlayers)
            removePlayer(removed);
        for (PlayerStateDiff playerStateDiff : diff.changedPlayers)
            getPlayer(playerStateDiff.playerId).applyDiff(playerStateDiff, gameObjectTracker);
        for (PlayerState added : diff.addedPlayers)
            addPlayer(added);
    }

    // Sets data of other game state
    public void setData(GameState other) {
        resources = other.resources;
        playerId = other.playerId;
        time = other.time;
        lastUpdateTime = time;

        GameStateDiff diff = new GameStateDiff(this, other);

        applyDiff(diff);

        attackScripts = other.attackScripts;
    }

    // Returns true if current player can building building of specified building type
    public boolean canBuildBuilding(BuildingType buildingType, int x, int y, GeoBattle game) {
        // Prevent BuildResult.NotEnoughResources
        if (getResources() < buildingType.cost) {
            if (game != null)
                game.showMessage(game.getI18NBundle().format("buildResultNotEnoughResources", buildingType.cost));
            return false;
        }

        // Prevent BuildResult.BuildingLimitExceeded
        if (getCurrentPlayer().getCount(buildingType) >= buildingType.maxCount) {
            if (game != null)
                game.showMessage(game.getI18NBundle().format("buildResultBuildingLimitExceeded", buildingType.maxCount));
            return false;
        }

        // Prevent BuildResult.NotInTerritory
        ReadOnlyArrayList<Sector> sectors = getCurrentPlayer().getAllSectors();
        for (int sector = 0; sector < sectors.size(); sector++) {
            Sector next = sectors.get(sector);
            if (next.containsRect(
                    x - 1, y - 1,
                    buildingType.sizeX + 2, buildingType.sizeY + 2
            )) {
                if (next.isBlocked()) {
                    if (game != null)
                        game.showMessage(game.getI18NBundle().get("buildResultSectorBlocked"));
                    return false;
                }

                // Prevent BuildResult.CollisionFound
                ReadOnlyArrayList<Building> buildings = next.getAllBuildings();
                for (int buildingIndex = 0; buildingIndex < buildings.size(); buildingIndex++) {
                    Building building = buildings.get(buildingIndex);

                    if (GeoBattleMath.tileRectanglesIntersect(
                            building.x, building.y, building.getSizeX(), building.getSizeY(),
                            x - 1, y - 1, buildingType.sizeX + 2, buildingType.sizeY + 2
                    )) {
                        if (game != null)
                            game.showMessage(game.getI18NBundle().get("buildResultCollisionFound"));
                        return false;
                    }
                }

                return true;
            }
        }

        if (game != null)
            game.showMessage(game.getI18NBundle().get("buildResultNotInTerritory"));
        return false;
    }

    // Returns true if current player can build sector at specified point
    public boolean canBuildSector(int x, int y, GeoBattle game, IntPoint playerPosition) {
        PlayerState current = getCurrentPlayer();

        if (!GeoBattleMath.tileRectangleContains(
                x, y, Sector.SECTOR_SIZE, Sector.SECTOR_SIZE,
                playerPosition.x, playerPosition.y
        )) {
            if (game != null)
                game.showMessage(game.getI18NBundle().get("sectorBuildResultWrongGeolocation"));
            return false;
        }

        if (current.getSectorCount() > 0) {
            int cost = 50 + current.getSectorCount() * 25;

            if (resources < cost) {
                if (game != null)
                    game.showMessage(game.getI18NBundle().format("sectorBuildResultNotEnoughResources", cost));
                return false;
            }

            Sector sector = getCurrentPlayer().getAllSectors().get(0);

            if ((x - sector.x) % Sector.SECTOR_SIZE != 0 || (y - sector.y) % Sector.SECTOR_SIZE != 0) {
                if (game != null)
                    game.showMessage(game.getI18NBundle().get("sectorBuildResultWrongPosition"));
                return false;
            }

            boolean isNeighbour = false;

            ReadOnlyArrayList<Sector> sectors = getCurrentPlayer().getAllSectors();
            for (int sectorIndex = 0; sectorIndex < sectors.size(); sectorIndex++) {
                Sector next = sectors.get(sectorIndex);

                if (
                        Math.abs(next.x - x) == Sector.SECTOR_SIZE && next.y == y ||
                        Math.abs(next.y - y) == Sector.SECTOR_SIZE && next.x == x
                ) {
                    isNeighbour = true;
                    break;
                }
                if (next.x == x && next.y == y) {
                    if (game != null)
                        game.showMessage(game.getI18NBundle().get("sectorBuildResultWrongPosition"));
                    return false;
                }
            }

            if (!isNeighbour) {
                if (game != null)
                    game.showMessage(game.getI18NBundle().get("sectorBuildResultWrongPosition"));
                return false;
            }
        }

        for (PlayerState enemy : players.values()) {
            if (enemy == getCurrentPlayer())
                continue;

            ReadOnlyArrayList<Sector> enemySectors = enemy.getAllSectors();
            for (int sectorIndex = 0; sectorIndex < enemySectors.size(); sectorIndex++) {
                Sector next = enemySectors.get(sectorIndex);

                if (GeoBattleMath.tileRectanglesIntersect(
                        next.x, next.y, Sector.SECTOR_SIZE, Sector.SECTOR_SIZE,
                        x, y, Sector.SECTOR_SIZE, Sector.SECTOR_SIZE
                )) {
                    if (game != null)
                        game.showMessage(game.getI18NBundle().get("sectorBuildResultIntersectsWithEnemy"));
                    return false;
                }
            }
        }

        return true;
    }
}
