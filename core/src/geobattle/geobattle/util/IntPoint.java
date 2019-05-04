package geobattle.geobattle.util;

// Point with int coordinates
public final class IntPoint {
    // X coordinate
    public int x;

    // Y coordinate
    public int y;

    public IntPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Clones point
    public IntPoint clone() {
        return new IntPoint(x, y);
    }
}
