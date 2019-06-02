package geobattle.geobattle.game;

import com.badlogic.gdx.utils.IntFloatMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import geobattle.geobattle.game.attacking.AttackScript;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.game.gamestatediff.GameStateDiff;
import geobattle.geobattle.game.gamestatediff.PlayerStateDiff;
import geobattle.geobattle.game.units.UnitType;
import geobattle.geobattle.util.GeoBattleMath;

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
    private ArrayList<PlayerState> players;

    // Attack events
    private ArrayList<AttackScript> attackScripts;

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
        this.players = new ArrayList<PlayerState>();
        this.attackScripts = new ArrayList<AttackScript>();

        IntFloatMap unitHealth1 = new IntFloatMap();
        unitHealth1.put(-1, UnitType.BOMBER.maxHealth * 4);
        IntFloatMap unitHealth2 = new IntFloatMap();
        unitHealth2.put(-1, UnitType.BOMBER.maxHealth * 2f);

        this.lastUpdateTime = time;

//        this.attackScripts.add(new AttackScript(
//                1, 0, 0,
//                new UnitGroupMovingInfo[] {
//                        new UnitGroupMovingInfo(-1, time + 20, 5234549, 5501466, time + 40)
//                },
//                new TimePoint[] {
//                        new TimePoint(time + 10, 200, unitHealth1),
//                        new TimePoint(time + 20, 200, unitHealth1),
//                        new TimePoint(time + 30, 0, unitHealth2),
//                        new TimePoint(time + 40, 0, unitHealth2)
//                }
//        ));
//        this.attackScripts.add(new AttackScript(
//                1, 0, 6,
//                new UnitGroupMovingInfo[] {
//                        new UnitGroupMovingInfo(-1, time + 5, 5234546, 5501505, time + 25)
//                },
//                new TimePoint[] {
//                        new TimePoint(time - 5, 200, unitHealth1),
//                        new TimePoint(time + 5, 200, unitHealth1),
//                        new TimePoint(time + 15, 0, unitHealth2),
//                        new TimePoint(time + 25, 0, unitHealth2)
//                }
//        ));
    }

    // Clones GameState
    public GameState clone() {
        GameState cloned = new GameState(resources, playerId, time);

        for (PlayerState player : players)
            cloned.players.add(player.clone());

        for (AttackScript event : attackScripts)
            cloned.attackScripts.add(event.clone());

        return cloned;
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
        return players.iterator();
    }

    // Returns player by ID
    public PlayerState getPlayer(int id) {
        int left = 0;
        int right = players.size() - 1;

        while (left <= right) {
            int mid = (left + right) >> 1;

            PlayerState midPlayer = players.get(mid);
            if (midPlayer.getPlayerId() < id)
                left = mid + 1;
            else if (midPlayer.getPlayerId() > id)
                right = mid - 1;
            else
                return midPlayer;
        }

        return null;
    }

    // Returns current player
    public PlayerState getCurrentPlayer() {
        return getPlayer(playerId);
    }

    // Adds player to game
    public void addPlayer(PlayerState player) {
        int index = Collections.binarySearch(players, player, playerComparator);
        if (index >= 0)
            throw new IllegalArgumentException("Cannot add player with existing ID");

        players.add(-index - 1, player);
    }

    // Removes player from game
    public void removePlayer(PlayerState player) {
        int index = Collections.binarySearch(players, player, playerComparator);
        if (index < 0)
            throw new IllegalArgumentException("Cannot remove player with specified ID");
        players.remove(index);
    }

    public ArrayList<AttackScript> getAttackScripts() {
        return attackScripts;
    }

    public static GameState fromJson(JsonObject object) {
        float resources = object.getAsJsonPrimitive("resources").getAsFloat();
        int playerId = object.getAsJsonPrimitive("playerId").getAsInt();
        double time = object.getAsJsonPrimitive("time").getAsDouble();
        // ResearchInfo researchInfo = ResearchInfo.fromJson(object.getAsJsonObject("researchInfo"));

        GameState gameState = new GameState(resources, playerId, time);

        JsonArray jsonPlayers = object.getAsJsonArray("players");
        for (JsonElement jsonPlayer : jsonPlayers)
            gameState.addPlayer(PlayerState.fromJson(jsonPlayer.getAsJsonObject()));

        JsonArray jsonAttackEvents = object.getAsJsonArray("attackEvents");
        for (JsonElement jsonAttackEvent : jsonAttackEvents)
            gameState.attackScripts.add(AttackScript.fromJson(jsonAttackEvent.getAsJsonObject()));

        return gameState;
    }

    // Applies GameStateDiff to game state
    public void applyDiff(GameStateDiff diff) {
        for (PlayerState removed : diff.removedPlayers)
            removePlayer(removed);
        for (PlayerStateDiff playerStateDiff : diff.changedPlayers)
            getPlayer(playerStateDiff.playerId).applyDiff(playerStateDiff);
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

    public boolean canBuildBuilding(BuildingType buildingType, int x, int y) {
        // Prevent BuildResult.NotEnoughResources
        if (getResources() < buildingType.cost) return false;

        // Prevent BuildResult.CollisionFound
        if (getCurrentPlayer().getBuildingsInRect(
                x - 1, y - 1,
                buildingType.sizeX + 2, buildingType.sizeY + 2
        ).hasNext()) return false;

        // Prevent BuildResult.BuildingLimitExceeded
        if (buildingType.maxCount != Integer.MAX_VALUE) {
            Iterator<Building> buildings = getCurrentPlayer().getAllBuildings();
            int count = 0;
            while (buildings.hasNext() && count < buildingType.maxCount) {
                Building next = buildings.next();
                if (next.getBuildingType() == buildingType) {
                    count++;
                    if (count >= buildingType.maxCount)
                        return false;
                }
            }
        }

        // Prevent BuildResult.NotInTerritory
        Iterator<Sector> sectors = getCurrentPlayer().getAllSectors();
        while (sectors.hasNext()) {
            Sector next = sectors.next();
            if (next.containsRect(
                    x - 1, y - 1,
                    buildingType.sizeX + 2, buildingType.sizeY + 2
            )) {
                return !next.isBlocked();
            }
        }

        return false;
    }

    public boolean canBuildSector(int x, int y) {
        PlayerState current = getCurrentPlayer();

        if (current.getAllSectors().hasNext()) {
            int cost = 50 + current.getSectorCount() * 25;

            if (resources < cost)
                return false;

            Sector sector = getCurrentPlayer().getAllSectors().next();

            if ((x - sector.x) % Sector.SECTOR_SIZE != 0 || (y - sector.y) % Sector.SECTOR_SIZE != 0)
                return false;

            boolean isNeighbour = false;

            Iterator<Sector> sectors = getCurrentPlayer().getAllSectors();
            while (sectors.hasNext()) {
                Sector next = sectors.next();

                if (
                        Math.abs(next.x - x) == Sector.SECTOR_SIZE && next.y == y ||
                        Math.abs(next.y - y) == Sector.SECTOR_SIZE && next.x == x
                ) {
                    isNeighbour = true;
                    break;
                }
                if (next.x == x && next.y == y)
                    return false;
            }

            if (!isNeighbour)
                return false;
        }

        for (PlayerState enemy : players) {
            if (enemy == getCurrentPlayer())
                continue;

            Iterator<Sector> enemySectors = enemy.getAllSectors();
            while (enemySectors.hasNext()) {
                Sector next = enemySectors.next();

                if (GeoBattleMath.tileRectanglesIntersect(
                        next.x, next.y, Sector.SECTOR_SIZE, Sector.SECTOR_SIZE,
                        x, y, Sector.SECTOR_SIZE, Sector.SECTOR_SIZE
                )) return false;
            }
        }

        return true;
    }
}
