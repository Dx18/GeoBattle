package geobattle.geobattle.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntFloatMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Locale;

import geobattle.geobattle.game.attacking.AttackEvent;
import geobattle.geobattle.game.attacking.TimePoint;
import geobattle.geobattle.game.attacking.UnitGroupMovingInfo;
import geobattle.geobattle.game.buildings.BuildingParams;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.units.Bomber;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.game.units.UnitGroup;

// State of game
public class GameState {
    // Player's resources
    private float resources;

    // Player's index in game state
    private int playerId;

    // Time
    private double time;

    // Players
    private ArrayList<PlayerState> players;

    // Attack events
    private ArrayList<AttackEvent> attackEvents;

    public GameState(float resources, int playerId, double time) {
        this.resources = resources;
        this.playerId = playerId;
        this.time = time;
        this.players = new ArrayList<PlayerState>();
        this.attackEvents = new ArrayList<AttackEvent>();

        UnitGroupMovingInfo[] unitGroupMoving = {
                new UnitGroupMovingInfo(100000, time + 20, 5232900, 5500960, time + 35)
        };

        IntFloatMap unitGroupHealth1 = new IntFloatMap();
        unitGroupHealth1.put(100000, 40);

        IntFloatMap unitGroupHealth2 = new IntFloatMap();
        unitGroupHealth1.put(100000, 40);

        IntFloatMap unitGroupHealth3 = new IntFloatMap();
        unitGroupHealth1.put(100000, 20);

        TimePoint[] timePoints = {
                new TimePoint(time + 10, 200, unitGroupHealth1),
                new TimePoint(time + 20, 200, unitGroupHealth2),
                new TimePoint(time + 25, 0, unitGroupHealth3)
        };

        attackEvents.add(new AttackEvent(
                0, 0, 0,
                unitGroupMoving, timePoints
        ));

//        Hangar hangar = new Hangar(new BuildingParams(5232810, 5500965, 100000, 0, 0), new UnitGroup(new Unit[4], 40));
//        hangar.units.addUnit(new Bomber(0, 0, 0, 0, 100000, 0));
//        hangar.units.addUnit(new Bomber(0, 0, 0, 0, 100000, 1));
//        hangar.units.addUnit(new Bomber(0, 0, 0, 0, 100000, 2));
//
//        players.get(0).addBuilding(hangar);
    }

    // Clones GameState
    public GameState clone() {
        GameState cloned = new GameState(resources, playerId, time);

        for (PlayerState player : players)
            cloned.players.add(player.clone());

        for (AttackEvent event : attackEvents)
            cloned.attackEvents.add(event.clone());

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

    // Returns players in game state
    public ArrayList<PlayerState> getPlayers() {
        return players;
    }

    public PlayerState getCurrentPlayer() {
        return players.get(getPlayerId());
    }

    public ArrayList<AttackEvent> getAttackEvents() {
        return attackEvents;
    }

    public static GameState fromJson(JsonObject object) {
        Gdx.app.log("GeoBattle", String.format(
                Locale.US,
                "Creating game state from: %s",
                object.toString()
        ));

        float resources = object.getAsJsonPrimitive("resources").getAsFloat();
        int playerId = object.getAsJsonPrimitive("playerId").getAsInt();
        double time = object.getAsJsonPrimitive("time").getAsDouble();

        GameState gameState = new GameState(resources, playerId, time);

        JsonArray jsonPlayers = object.getAsJsonArray("players");
        for (JsonElement jsonPlayer : jsonPlayers)
            gameState.players.add(PlayerState.fromJson(jsonPlayer.getAsJsonObject()));

        JsonArray jsonAttackEvents = object.getAsJsonArray("attackEvents");
        for (JsonElement jsonAttackEvent : jsonAttackEvents)
            gameState.attackEvents.add(AttackEvent.fromJson(jsonAttackEvent.getAsJsonObject()));

        return gameState;
    }

    // Sets data of other game state
    public void setData(GameState other) {
        resources = other.resources;
        playerId = other.playerId;
        time = other.time;

        players = other.players;
        // attackEvents = other.attackEvents;

        Hangar hangar = new Hangar(new BuildingParams(5232810, 5500965, 100000, 0, 0), new UnitGroup(new Unit[4]));
        hangar.units.addUnit(new Bomber(0, 0, 0, 0, 100000, 0));
        hangar.units.addUnit(new Bomber(0, 0, 0, 0, 100000, 1));
        hangar.units.addUnit(new Bomber(0, 0, 0, 0, 100000, 2));

        players.get(0).addBuilding(hangar);
    }
}
