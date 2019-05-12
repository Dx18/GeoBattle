package geobattle.geobattle.game.units;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Predicate;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;

import geobattle.geobattle.game.actionresults.MatchBranch;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.util.GeoBattleMath;

public final class UnitGroup {
    private final Unit[] units;

    private float health;

    private double x;

    private double y;

    private UnitGroupState state;

    private double lastUpdateTime;

    public UnitGroup(Unit[] units, Hangar hangar) {
        if (units.length != 4)
            throw new IllegalArgumentException("Length of units must be 4");

        this.units = units;
        setHealth(getMaxHealth());
        this.x = hangar.x + hangar.getSizeX() / 2.0;
        this.y = hangar.y + hangar.getSizeY() / 2.0;
        setState(new UnitGroupState.Idle(hangar));
    }

    public UnitGroup(Hangar hangar) {
        this(new Unit[4], hangar);
    }

    public void update(final float delta, final double currentTime) {
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
                        updateAttacking(delta, attacking);
                    }
                }
        );

        this.lastUpdateTime = currentTime;
    }

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

    private void updateAttacking(float delta, UnitGroupState.Attacking attacking) {
        Iterator<Building> buildingsIterator = attacking.sector.getAllBuildings();
        ArrayList<Building> buildings = new ArrayList<Building>();
        while (buildingsIterator.hasNext())
            buildings.add(buildingsIterator.next());

        for (int slot = 0; slot < 4; slot++) {
            if (units[slot] == null) {
                attacking.attackedBuildings[slot] = null;
                continue;
            }

            if (buildings.size() >= 2) {
                if (
                        attacking.attackedBuildings[slot] == null ||
                        units[slot].x == attacking.attackedBuildings[slot].x + attacking.attackedBuildings[slot].getSizeX() / 2.0 &&
                        units[slot].y == attacking.attackedBuildings[slot].y + attacking.attackedBuildings[slot].getSizeY() / 2.0
                ) {
                    int selectedIndex;
                    do {
                        selectedIndex = (int) (Math.random() * buildings.size());
                    } while (buildings.get(selectedIndex) == attacking.attackedBuildings[slot]);
                    attacking.attackedBuildings[slot] = buildings.get(selectedIndex);
                }

                units[slot].update(
                        delta,
                        attacking.attackedBuildings[slot].x + attacking.attackedBuildings[slot].getSizeX() / 2.0,
                        attacking.attackedBuildings[slot].y + attacking.attackedBuildings[slot].getSizeY() / 2.0
                );
            } else if (buildings.size() == 1) {
                units[slot].update(
                        delta,
                        buildings.get(0).x + Math.random() * buildings.get(0).getSizeX(),
                        buildings.get(0).y + Math.random() * buildings.get(0).getSizeY()
                );
            } else {
                units[slot].update(
                        delta,
                        attacking.sector.x + Math.random() * Sector.SECTOR_SIZE,
                        attacking.sector.y + Math.random() * Sector.SECTOR_SIZE
                );
            }
        }
    }

    public double getLastUpdateTime() {
        return lastUpdateTime;
    }

    public float getMaxHealth() {
        float result = 0;
        for (Unit unit : units) {
            if (unit != null)
                result += unit.getUnitType().maxHealth;
        }
        return result;
    }

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

    public float getHealth() {
        return health;
    }

    public void removeUnit(Unit unit) {
        this.units[unit.hangarSlot] = null;
    }

    public Unit addUnit(Unit unit) {
        Unit existing = this.units[unit.hangarSlot];
        this.units[unit.hangarSlot] = unit;

        // TODO Update health

        return existing;
    }

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

    public void setState(UnitGroupState state) {
        this.state = state;
    }

    public UnitGroupState getState() {
        return this.state;
    }

    public int getFreeSlot() {
        for (int index = 0; index < 4; index++)
            if (units[index] == null)
                return index;
        return -1;
    }

    public int getCount() {
        int count = 0;
        for (Unit unit : units)
            if (unit != null)
                count++;
        return count;
    }

    public UnitGroup clone(Hangar clonedHangar) {
        Unit[] units = new Unit[4];
        for (int index = 0; index < 4; index++)
            units[index] = this.units[index].clone();

        return new UnitGroup(units, clonedHangar);
    }

    public static UnitGroup fromJson(JsonObject object, Hangar hangar) {
        // float health = object.getAsJsonPrimitive("health").getAsFloat();
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
