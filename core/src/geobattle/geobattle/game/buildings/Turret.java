package geobattle.geobattle.game.buildings;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.units.Unit;

// Turret
public final class Turret extends Building {
    // Range of turret
    private static final int RANGE = 10;

    // Damage of turret
    private static final float DAMAGE = 66;

    // Turret's target
    private Unit target;

    public Turret(BuildingParams params) {
        super(params, BuildingType.TURRET);
    }

    @Override
    public void update(float delta, GameState gameState) {
//        if (target != null) {
//            double dist2 = Math.pow(x + getSizeX() / 2.0 - target.x, 2) + Math.pow(y + getSizeY() / 2.0 - target.y2, 2);
//
//            if (dist2 <= RANGE * RANGE)
//                target.setHealth(target.getHealth() - DAMAGE * delta);
//            else
//                target = null;
//        }
//
//        if (target == null) {
//            for (PlayerState player : gameState.getPlayers()) {
//                Unit unit = player.getUnit(id);
//                if (unit == null) {
//                    // If player is not owner
//                    Iterator<Unit> units = player.getAllUnits();
//                    while (units.hasNext()) {
//                        Unit next = units.next();
//
//                        double dist2 = Math.pow(x + getSizeX() / 2.0 - target.x, 2) + Math.pow(y + getSizeY() / 2.0 - target.y2, 2);
//
//                        if (dist2 <= RANGE * RANGE) {
//                            target = next;
//                            target.setHealth(target.getHealth() - DAMAGE * delta);
//                            break;
//                        }
//                    }
//                }
//            }
//        }
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
