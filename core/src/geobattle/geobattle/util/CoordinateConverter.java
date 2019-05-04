package geobattle.geobattle.util;

import com.badlogic.gdx.math.MathUtils;

// Converter for coordinates
// - world coordinates - coordinates in camera space
// - real world coordinates - coordinates in camera space + camera offset
// - sub tiles - like real world but has more subdivision
public final class CoordinateConverter {
    public static float worldToRealWorld(float point, int offset) {
        return point + offset;
    }

    public static int worldToRealWorldInt(float point, int offset) {
        return MathUtils.floor(point) + offset;
    }

    public static int worldToSubTiles(float point, int offset, int subdividePower) {
        return (MathUtils.floor(point) << subdividePower) +
                MathUtils.floor(GeoBattleMath.getFractionalPart(point) * (1 << subdividePower)) +
                (offset << subdividePower);
    }

    public static float realWorldToWorld(double realWorldPoint, int offset) {
        return (float) (realWorldPoint - offset);
    }

    public static float realWorldToWorld(float realWorldPoint, int offset) {
        return realWorldPoint - offset;
    }

    public static int realWorldToWorld(int realWorldPoint, int offset) {
        return realWorldPoint - offset;
    }

    public static int realWorldToSubTiles(float realWorldPoint, int subdividePower) {
        return (MathUtils.floor(realWorldPoint) << subdividePower) +
                MathUtils.floor(GeoBattleMath.getFractionalPart(realWorldPoint) * (1 << subdividePower));
    }

    public static int realWorldToSubTiles(int realWorldPoint, int subdividePower) {
        return realWorldPoint << subdividePower;
    }

    public static float subTilesToWorld(int subTile, int offset, int subdividePower) {
        return (subTile - (offset << subdividePower)) / (float) (1 << subdividePower);
    }

    public static float subTilesToWorld(double subTile, int offset, int subdividePower) {
        return (float) ((subTile - (offset << subdividePower)) / (1 << subdividePower));
    }

    public static float subTilesToRealWorld(int subTile, int subdividePower) {
        return subTile / (float) (1 << subdividePower);
    }

    public static float subTilesToRealWorld(double subTile, int subdividePower) {
        return (float) (subTile / (1 << subdividePower));
    }
}
