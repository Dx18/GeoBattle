package geobattle.geobattle.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import geobattle.geobattle.game.attacking.AttackEvent;
import geobattle.geobattle.game.gamestatediff.GameStateDiff;
import geobattle.geobattle.game.gamestatediff.PlayerStateDiff;

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

    // Applies GameStateDiff to game state
    public void applyDiff(GameStateDiff diff) {
        for (PlayerStateDiff playerStateDiff : diff.changedPlayers)
            players.get(playerStateDiff.playerId).applyDiff(playerStateDiff);

        players.addAll(diff.addedPlayers);
    }

    // Sets data of other game state
    public void setData(GameState other) {
        resources = other.resources;
        playerId = other.playerId;
        time = other.time;

        GameStateDiff diff = new GameStateDiff(this, other);

        applyDiff(diff);

        attackEvents = other.attackEvents;
    }
}
