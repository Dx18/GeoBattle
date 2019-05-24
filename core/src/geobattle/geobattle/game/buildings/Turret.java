package geobattle.geobattle.game.buildings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.google.gson.JsonObject;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.map.BuildingTextures;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.GeoBattleMath;

// Turret
public final class Turret extends Building {
    // Speed of rotation in idle mode
    public final double IDLE_ROTATION_SPEED = Math.PI / 6;

    // Direction of turret
    public double direction;

    public Turret(BuildingParams params) {
        super(params, BuildingType.TURRET);
    }

    // Updates turret
    public void update(float delta, Unit unit) {
        if (unit == null) {
            direction += IDLE_ROTATION_SPEED * delta;
        } else {
            direction = GeoBattleMath.getDirection(
                    unit.x - x - getSizeX() / 2.0,
                    unit.y - y - getSizeY() / 2.0
            );
        }
    }

    @Override
    public void draw(Batch batch, GeoBattleMap map, BuildingTextures buildingTextures, Color color, boolean drawIcons) {
        super.draw(batch, map, buildingTextures, color, drawIcons);
        if (!drawIcons) {
            map.drawCenteredTextureSubTiles(
                    batch, x + getSizeX() / 2.0, y + getSizeY() / 2.0,
                    getSizeX() + 1, getSizeY() + 1,
                    direction, buildingTextures.turretTowerTexture, Color.WHITE
            );
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
