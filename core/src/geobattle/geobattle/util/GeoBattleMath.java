package geobattle.geobattle.util;

import com.badlogic.gdx.math.Vector2;

// Some GeoBattle-specific math
public class GeoBattleMath {
    // Converts (longitude, latitude) coordinates to mercator coordinates
    public static Vector2 latLongToMercator(Vector2 latLong) {
        return new Vector2(
                (latLong.x + 180) / 360 * (1 << 19),
                (1 << 18) - (float)((1 << 18) * java.lang.Math.log(java.lang.Math.tan(java.lang.Math.PI / 4 + 0.5 * -latLong.y * java.lang.Math.PI / 180)) / java.lang.Math.PI)
        );
    }

    // Returns positive fractional part of number
    public static float getFractionalPart(float num) {
        return (num % 1 + 1) % 1;
    }

    // Returns true if rectangle of tiles contains tile
    public static boolean tileRectangleContains(IntRect rect, int x, int y) {
        return tileRectangleContains(rect.x, rect.y, rect.width, rect.height, x, y);
    }

    // Returns true if rectangle of tiles contains tile
    public static boolean tileRectangleContains(int xRect, int yRect, int width, int height, int x, int y) {
        return x >= xRect && x < xRect + width && y >= yRect && y < yRect + height;
    }

    public static boolean tileRectangleContains(IntRect rect1, IntRect rect2) {
        return tileRectangleContains(
                rect1.x, rect1.y, rect1.width, rect1.height,
                rect2.x, rect2.y, rect2.width, rect2.height
        );
    }

    public static boolean tileRectangleContains(int x1, int y1, int width1, int height1, int x2, int y2, int width2, int height2) {
        return
                x2 >= x1 && x2 + width2 <= x1 + width1 &&
                y2 >= y1 && y2 + height2 <= y1 + height1;
    }

    // Returns true if rectangles of tiles intersect
    public static boolean tileRectanglesIntersect(IntRect rect1, IntRect rect2) {
        return tileRectanglesIntersect(
                rect1.x, rect1.y, rect1.width, rect1.height,
                rect2.x, rect2.y, rect2.width, rect2.height
        );
    }

    // Returns true if rectangles of tiles intersect
    public static boolean tileRectanglesIntersect(int x1, int y1, int width1, int height1,
                                                  int x2, int y2, int width2, int height2) {
        return x1 + width1 > x2 && x2 + width2 > x1 && y1 + height1 > y2 && y2 + height2 > y1;
    }

    // Returns intersection of rectangles of tiles
    public static IntRect getTileRectangleIntersection(IntRect rect1, IntRect rect2) {
        return getTileRectangleIntersection(
                rect1.x, rect1.y, rect1.width, rect1.height,
                rect2.x, rect2.y, rect2.width, rect2.height
        );
    }

    // Returns intersection of rectangles of tiles
    public static IntRect getTileRectangleIntersection(int x1, int y1, int width1, int height1,
                                                       int x2, int y2, int width2, int height2) {
        int x = Math.max(x1, x2);
        int y = Math.max(y1, y2);
        int width = Math.min(x1 + width1, x2 + width2) - x;
        int height = Math.min(y1 + height1, y2 + height2) - y;
        return new IntRect(x, y, width, height);
    }
}
