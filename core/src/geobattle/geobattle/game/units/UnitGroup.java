package geobattle.geobattle.game.units;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Predicate;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;

import geobattle.geobattle.actionresults.MatchBranch;
import geobattle.geobattle.game.attacking.HealthInterpolation;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.game.tasks.TimedObjectQueue;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.map.GeoBattleMapEvent;
import geobattle.geobattle.util.GeoBattleMath;

// Group of units
public final class UnitGroup {
    // Units
    private final Unit[] units;

    // Health of group
    private float health;

    // X coordinate of group
    public double x;

    // Y coordinate of group
    public double y;

    // Hangar ID
    public final int hangarId;

    // Health interpolation of group
    private HealthInterpolation healthInterpolation;

    // State of group
    private UnitGroupState state;

    // Last update time of group
    private double lastUpdateTime;

    // Health interpolations of units
    public final TimedObjectQueue<HealthInterpolation> healthInterpolations;

    // States of units
    public final TimedObjectQueue<UnitGroupState> states;

    public UnitGroup(Unit[] units, Hangar hangar) {
        if (units.length != 4)
            throw new IllegalArgumentException("Length of units must be 4");

        this.units = units;
        setHealth(getMaxHealth());
        this.x = hangar.x + hangar.getSizeX() / 2.0;
        this.y = hangar.y + hangar.getSizeY() / 2.0;
        setState(new UnitGroupState.Idle(hangar));
        this.hangarId = hangar.id;

        healthInterpolations = new TimedObjectQueue<HealthInterpolation>();
        states = new TimedObjectQueue<UnitGroupState>();
    }

    public UnitGroup(Hangar hangar) {
        this(new Unit[4], hangar);
    }

    // Updates group of units
    public void update(final float delta, final double currentTime, final GeoBattleMap map) {
        ArrayList<UnitGroupState> newStates = states.getObjects(currentTime);
        if (newStates.size() > 0)
            setState(newStates.get(newStates.size() - 1));

        ArrayList<HealthInterpolation> newHealth = healthInterpolations.getObjects(currentTime);
        if (newHealth.size() > 0)
            healthInterpolation = newHealth.get(newHealth.size() - 1);

        if (healthInterpolation != null)
            setHealth((float) healthInterpolation.getHealth(currentTime));

        if (state == null)
            return;

        state.match(
                new MatchBranch<UnitGroupState.Idle>() {
                    @Override
                    public void onMatch(UnitGroupState.Idle idle) {
                        updateIdle(delta, idle);
                    }
                },
                new MatchBranch<UnitGroupState.Moving>() {
                    @Override
                    public void onMatch(UnitGroupState.Moving moving) {
                        updateMoving(delta, currentTime, moving);
                    }
                },
                new MatchBranch<UnitGroupState.Attacking>() {
                    @Override
                    public void onMatch(UnitGroupState.Attacking attacking) {
                        updateAttacking(delta, attacking, map);
                    }
                }
        );

        this.lastUpdateTime = currentTime;
    }

    // Updates group of units in case if state is UnitGroupState.Idle
    private void updateIdle(float delta, UnitGroupState.Idle idle) {
        double[] homeOffsetX = { 1.5, 1.5, 5.5, 5.5 };
        double[] homeOffsetY = { 1.5, 5.5, 5.5, 1.5 };

        for (Unit unit : units) {
            if (unit != null)
                unit.update(
                        delta,
                        idle.hangar.x + homeOffsetX[unit.hangarSlot],
                        idle.hangar.y + homeOffsetY[unit.hangarSlot]
                );
        }
    }

    // Updates group of units in case if state is UnitGroupState.Moving
    private void updateMoving(float delta, double currentTime, UnitGroupState.Moving moving) {
        if (currentTime < moving.time1 || currentTime > moving.time2)
            return;

        double factor = (currentTime - moving.time1) / (moving.time2 - moving.time1);
        x = moving.x1 + factor * (moving.x2 - moving.x1);
        y = moving.y1 + factor * (moving.y2 - moving.y1);

        double direction;
        if (x != moving.x2 || y != moving.y2)
            direction = GeoBattleMath.getDirection(moving.x2 - x, moving.y2 - y);
        else
            direction = 0;

        for (Unit unit : units) {
            if (unit != null)
                unit.update(
                        delta,
                        x + 3 * unit.hangarSlot * Math.cos(direction),
                        y + 3 * unit.hangarSlot * Math.sin(direction)
                );
        }
    }

    // Updates group of units in case if state is UnitGroupState.Attacking
    private void updateAttacking(float delta, UnitGroupState.Attacking attacking, GeoBattleMap map) {
        for (int slot = 0; slot < 4; slot++) {
            if (units[slot] == null) {
                attacking.attackedBuildings[slot] = null;
                continue;
            }

            if (attacking.allBuildings.length >= 2) {
                if (
                        attacking.attackedBuildings[slot] == null ||
                        units[slot].x == attacking.attackedBuildings[slot].x + attacking.attackedBuildings[slot].getSizeX() / 2.0 &&
                        units[slot].y == attacking.attackedBuildings[slot].y + attacking.attackedBuildings[slot].getSizeY() / 2.0
                ) {
                    int selectedIndex;
                    do {
                        selectedIndex = (int) (Math.random() * attacking.allBuildings.length);
                    } while (attacking.allBuildings[selectedIndex] == attacking.attackedBuildings[slot]);
                    attacking.attackedBuildings[slot] = attacking.allBuildings[selectedIndex];
                }

                units[slot].update(
                        delta,
                        attacking.attackedBuildings[slot].x + attacking.attackedBuildings[slot].getSizeX() / 2.0,
                        attacking.attackedBuildings[slot].y + attacking.attackedBuildings[slot].getSizeY() / 2.0
                );

                if (GeoBattleMath.tileRectangleContains(
                        attacking.attackedBuildings[slot].x, attacking.attackedBuildings[slot].y,
                        attacking.attackedBuildings[slot].getSizeX(), attacking.attackedBuildings[slot].getSizeY(),
                        (int) units[slot].x, (int) units[slot].y
                ) && units[slot].getBombTimer() > 1) {
                    map.handleEvent(new GeoBattleMapEvent.BombDropped(units[slot].x, units[slot].y));
                    units[slot].resetBombTimer();
                }
            } else if (attacking.allBuildings.length == 1) {
                units[slot].update(
                        delta,
                        attacking.allBuildings[0].x + Math.random() * attacking.allBuildings[0].getSizeX(),
                        attacking.allBuildings[0].y + Math.random() * attacking.allBuildings[0].getSizeY()
                );

                if (GeoBattleMath.tileRectangleContains(
                        attacking.allBuildings[0].x, attacking.allBuildings[0].y,
                        attacking.allBuildings[0].getSizeX(), attacking.allBuildings[0].getSizeY(),
                        (int) units[slot].x, (int) units[slot].y
                ) && units[slot].getBombTimer() > 1) {
                    map.handleEvent(new GeoBattleMapEvent.BombDropped(units[slot].x, units[slot].y));
                    units[slot].resetBombTimer();
                }
            } else {
                units[slot].update(
                        delta,
                        attacking.sector.x + Math.random() * Sector.SECTOR_SIZE,
                        attacking.sector.y + Math.random() * Sector.SECTOR_SIZE
                );
            }
        }
    }

    // Returns time of last update
    public double getLastUpdateTime() {
        return lastUpdateTime;
    }

    // Returns max health
    public float getMaxHealth() {
        float result = 0;
        for (Unit unit : units) {
            if (unit != null)
                result += unit.getUnitType().maxHealth;
        }
        return result;
    }

    // Sets health
    public void setHealth(float health) {
        this.health = MathUtils.clamp(health, 0, getMaxHealth());

        int count = getCount();

        float avgHealth = getMaxHealth() / count;

        int maxUnitCount = (int) Math.ceil(this.health / avgHealth);

        int removed = 0;
        Iterator<Unit> units = getAllUnits();
        while (units.hasNext() && removed < count - maxUnitCount) {
            removeUnit(units.next());
            removed++;
        }
    }

    // Returns health
    public float getHealth() {
        return health;
    }

    // Removes unit and does not update health
    public void removeUnit(Unit unit) {
        this.units[unit.hangarSlot] = null;
    }

    // Adds unit and does not update health
    public Unit addUnit(Unit unit) {
        Unit existing = this.units[unit.hangarSlot];
        this.units[unit.hangarSlot] = unit;

        return existing;
    }

    // Returns all units
    public Iterator<Unit> getAllUnits() {
        return new Predicate.PredicateIterator<Unit>(new Iterator<Unit>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < units.length;
            }

            @Override
            public Unit next() {
                Unit next = units[index];
                index++;
                return next;
            }
        }, new Predicate<Unit>() {
            @Override
            public boolean evaluate(Unit unit) {
                return unit != null;
            }
        });
    }

    // Returns unit in slot
    public Unit getUnit(int slot) {
        return units[slot];
    }

    // Sets state of unit group
    public void setState(UnitGroupState state) {
        this.state = state;
    }

    // Returns state of unit group
    public UnitGroupState getState() {
        return this.state;
    }

    // Returns free slot
    public int getFreeSlot() {
        for (int index = 0; index < 4; index++)
            if (units[index] == null)
                return index;
        return -1;
    }

    // Returns count of units
    public int getCount() {
        int count = 0;
        for (Unit unit : units)
            if (unit != null)
                count++;
        return count;
    }

    // Clones unit group
    public UnitGroup clone(Hangar clonedHangar) {
        Unit[] units = new Unit[4];
        for (int index = 0; index < 4; index++)
            units[index] = this.units[index].clone();

        return new UnitGroup(units, clonedHangar);
    }

    // Creates unit group from JSON
    public static UnitGroup fromJson(JsonObject object, Hangar hangar) {
        JsonArray jsonUnits = object.getAsJsonArray("units");

        Unit[] units = new Unit[4];
        int index = 0;
        for (JsonElement jsonUnit : jsonUnits) {
            if (index >= 4)
                break;
            Unit.ServerSide serverSideUnit = Unit.ServerSide.fromJson(jsonUnit.getAsJsonObject());
            units[index] = Unit.from(serverSideUnit, hangar.x, hangar.y);
            index++;
        }

        return new UnitGroup(units, hangar);
    }
}
