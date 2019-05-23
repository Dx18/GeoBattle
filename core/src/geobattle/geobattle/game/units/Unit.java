package geobattle.geobattle.game.units;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.google.gson.JsonObject;

import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.map.UnitTextures;
import geobattle.geobattle.util.GeoBattleMath;

// Base class for all units
public abstract class Unit {
    // Max move speed of unit
    public final double MAX_MOVE_SPEED = 10;

    // Min move speed of unit
    public final double MIN_MOVE_SPEED = 3;

    // Max rotate speed of unit
    public final double MAX_ROTATION_SPEED = Math.PI / 2;

    // X coordinate of unit
    public double x;

    // Y coordinate of unit
    public double y;

    // Direction of unit (in radians)
    public double direction;

    // ID of unit
    public final int id;

    // ID of hangar this unit bound to
    public final int hangarId;

    // Slot of hangar this unit bound to
    public final int hangarSlot;

    // Type of unit
    public final UnitType unitType;

    // Returns true if was updated at least once
    private boolean wasUpdated;

    public static class ServerSide {
        public final UnitType type;

        public final int id;

        public final int hangarId;

        public final int hangarSlot;

        public ServerSide(UnitType type, int id, int hangarId, int hangarSlot) {
            this.type = type;
            this.id = id;
            this.hangarId = hangarId;
            this.hangarSlot = hangarSlot;
        }

        // Creates server-side unit from JSON
        public static ServerSide fromJson(JsonObject object) {
            String type = object.getAsJsonPrimitive("type").getAsString();
            UnitType unitType = UnitType.from(type);
            if (unitType == null)
                throw new IllegalArgumentException(
                        String.format("Invalid type of building: %s", type)
                );

            int id = object.getAsJsonPrimitive("id").getAsInt();
            int hangarId = object.getAsJsonPrimitive("hangarId").getAsInt();
            int hangarSlot = object.getAsJsonPrimitive("hangarSlot").getAsInt();

            return new ServerSide(unitType, id, hangarId, hangarSlot);
        }
    }

    public Unit(double x, double y, double direction, int id, int hangarId, int hangarSlot, UnitType unitType) {
        if (hangarSlot < 0 || hangarSlot >= 4)
            throw new IllegalArgumentException("Hangar slot must be at least 0 and at most 3. Got " + hangarSlot);

        this.x = x;
        this.y = y;
        this.direction = direction;
        this.id = id;
        this.hangarId = hangarId;
        this.hangarSlot = hangarSlot;
        this.unitType = unitType;
        this.wasUpdated = false;
    }

    // Updates unit
    public void update(float delta, double destX, double destY) {
        if (!wasUpdated) {
            Gdx.app.log("GeoBattle", "Was not updated");
            x = destX;
            y = destY;
            wasUpdated = true;
            return;
        }

        if (destX == x && destY == y)
            return;

        double maxAngleDelta = MAX_ROTATION_SPEED * delta;
        double targetDirection = GeoBattleMath.getDirection(destX - x, destY - y);
        double actualAngleDelta = GeoBattleMath.normalizeAngle(targetDirection - direction);

        if (actualAngleDelta > maxAngleDelta) {
            direction = ((direction + maxAngleDelta) % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);

            double minDistance = MIN_MOVE_SPEED * delta;

            x += Math.cos(direction) * minDistance;
            y += Math.sin(direction) * minDistance;
        } else if (actualAngleDelta < -maxAngleDelta) {
            direction = ((direction - maxAngleDelta) % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);

            double minDistance = MIN_MOVE_SPEED * delta;

            x += Math.cos(direction) * minDistance;
            y += Math.sin(direction) * minDistance;
        } else {
            direction = targetDirection;

            double maxDistance = MAX_MOVE_SPEED * delta;
            double actualDistance = Math.sqrt(Math.pow(destX - x, 2) + Math.pow(destY - y, 2));

            if (actualDistance > maxDistance) {
                x += maxDistance / actualDistance * (destX - x);
                y += maxDistance / actualDistance * (destY - y);
            } else {
                x = destX;
                y = destY;
            }
        }
    }

    // Draws unit
    public void draw(Batch batch, GeoBattleMap map, UnitTextures unitTextures, Color color, boolean drawIcons) {
        if (drawIcons) {

        } else {
            map.drawCenteredTextureSubTiles(
                    batch, x, y, getSizeX(), getSizeY(), direction,
                    unitTextures.getTexture(getUnitType()), Color.WHITE
            );
            map.drawCenteredTextureSubTiles(
                    batch, x, y, getSizeX(), getSizeY(), direction,
                    unitTextures.getTeamColorTexture(getUnitType()), color
            );
        }
    }

    // Returns width of unit
    public int getSizeX() {
        return unitType.sizeX;
    }

    // Returns height of unit
    public int getSizeY() {
        return unitType.sizeY;
    }

    // Returns type of unit
    public UnitType getUnitType() {
        return unitType;
    }

    // Creates unit from JSON
    public static Unit fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();
        UnitType unitType = UnitType.from(type);
        if (unitType == null)
            throw new IllegalArgumentException(
                    String.format("Invalid type of building: %s", type)
            );

        double x = object.getAsJsonPrimitive("x").getAsDouble();
        double y = object.getAsJsonPrimitive("y").getAsDouble();
        double direction = object.getAsJsonPrimitive("direction").getAsDouble();
        int id = object.getAsJsonPrimitive("id").getAsInt();
        int hangarId = object.getAsJsonPrimitive("hangarId").getAsInt();
        int hangarSlot = object.getAsJsonPrimitive("hangarSlot").getAsInt();

        switch (unitType) {
            case BOMBER: return Bomber.fromJson(object, x, y, direction, id, hangarId, hangarSlot);
            case SPOTTER: return Spotter.fromJson(object, x, y, direction, id, hangarId, hangarSlot);
        }

        return null;
    }

    // Clones unit
    public abstract Unit clone();

    // Creates unit from unit type and some parameters
    public static Unit from(UnitType unitType, double x, double y, double direction, int id, int hangarId, int hangarSlot) {
        switch (unitType) {
            case BOMBER: return new Bomber(x, y, direction, id, hangarId, hangarSlot);
            case SPOTTER: return new Spotter(x, y, direction, id, hangarId, hangarSlot);
        }
        return null;
    }

    public static Unit from(Unit.ServerSide unit, int hangarX, int hangarY) {
        double[] homeOffsetX = { 1.5, 1.5, 5.5, 5.5 };
        double[] homeOffsetY = { 1.5, 5.5, 5.5, 1.5 };

        switch (unit.type) {
            case BOMBER: return new Bomber(
                    hangarX + homeOffsetX[unit.hangarSlot],
                    hangarY + homeOffsetY[unit.hangarSlot],
                    0, unit.id,
                    unit.hangarId, unit.hangarSlot
            );
            case SPOTTER: return new Spotter(
                    hangarX + homeOffsetX[unit.hangarSlot],
                    hangarY + homeOffsetY[unit.hangarSlot],
                    0, unit.id,
                    unit.hangarId, unit.hangarSlot
            );
        }

        return null;
    }
}
