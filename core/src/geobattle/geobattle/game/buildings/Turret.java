package geobattle.geobattle.game.buildings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.google.gson.JsonObject;

import geobattle.geobattle.GeoBattleConst;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.map.BuildingTextures;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.map.animations.Animations;
import geobattle.geobattle.util.GeoBattleMath;

// Turret
public final class Turret extends Building {
    // Speed of rotation in idle mode
    public final double IDLE_ROTATION_SPEED = Math.PI / 2;

    // Direction of turret
    public double direction;

    // Target of turret
    private Unit target;

    // Attack time left
    private double targetTime;

    // ID of sound
    private long soundId;

    public Turret(BuildingParams params) {
        super(params, BuildingType.TURRET);
        soundId = -1;
    }

    // Returns current target
    public Unit getTarget() {
        return target;
    }

    // Sets current target
    public void setTarget(Unit target) {
        this.target = target;
    }

    // Returns turret target time
    public double getTargetTime() {
        return targetTime;
    }

    // Sets turret target time
    public void setTargetTime(double targetTime) {
        this.targetTime = targetTime;
    }

    // Updates turret
    public void update(float delta, GeoBattleMap map) {
        targetTime -= delta;

        if (target == null) {
            direction += IDLE_ROTATION_SPEED * delta;

            if (soundId != -1) {
                soundId = -1;
                map.stopShotsSound(soundId);
            }
        } else {
            direction = GeoBattleMath.getDirection(
                    target.x - x - getSizeX() / 2.0,
                    target.y - y - getSizeY() / 2.0
            );

            if (soundId == -1) {
                soundId = map.playShotsSound(x, y);
            }
        }
    }

    @Override
    public void draw(Batch batch, GeoBattleMap map, BuildingTextures buildingTextures, Animations animations, Color color, boolean drawIcons) {
        super.draw(batch, map, buildingTextures, animations, color, drawIcons);
        if (!drawIcons) {
            map.drawCenteredTexture(
                    batch, x + getSizeX() / 2.0, y + getSizeY() / 2.0,
                    getSizeX() + 1, getSizeY() + 1,
                    direction, buildingTextures.turretTowerTexture, Color.WHITE
            );

            if (target != null) {
                int frame = (int) (Math.random() * animations.turretFlash.getFrameCount());
                map.drawCenteredTexture(
                        batch,
                        x + getSizeX() / 2.0 + Math.cos(direction) * GeoBattleConst.TURRET_FLASH_ORIGIN,
                        y + getSizeY() / 2.0 + Math.sin(direction) * GeoBattleConst.TURRET_FLASH_ORIGIN,
                        1, 1, direction,
                        animations.turretFlash.getFrame(frame),
                        Color.WHITE
                );
            }
        }
    }

    // Clones turret
    @Override
    public Building clone() {
        return new Turret(getParams());
    }

    // Creates turret from JSON
    public static Turret fromJson(JsonObject object, BuildingParams params) {
        return new Turret(params);
    }
}
