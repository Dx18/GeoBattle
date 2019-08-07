package geobattle.geobattle.util;

import java.util.ArrayList;
import java.util.HashSet;

// Quad tree
public final class QuadTree<T> {
    // Point of quad tree
    private final class QuadTreePoint {
        // X of point
        public final int x;

        // Y of point
        public final int y;

        // Data
        public final T data;

        public QuadTreePoint(int x, int y, T data) {
            this.x = x;
            this.y = y;
            this.data = data;
        }
    }

    // Capacity of quad tree
    public final int capacity;

    // Rect of quad tree
    private final IntRect rect;

    // Points which quad tree contains
    private final ArrayList<QuadTreePoint> points;

    // Top-left subtree
    private QuadTree<T> topLeft;

    // Top-right subtree
    private QuadTree<T> topRight;

    // Bottom-right subtree
    private QuadTree<T> bottomRight;

    // Bottom-left subtree
    private QuadTree<T> bottomLeft;

    public QuadTree(int capacity, IntRect rect) {
        this.capacity = capacity;
        this.rect = rect;
        this.points = new ArrayList<QuadTreePoint>();
    }

    // Returns top-left sub rect
    private IntRect getTopLeftRect() {
        return new IntRect(
                rect.x, rect.y + rect.height / 2,
                rect.width / 2, rect.height - rect.height / 2
        );
    }

    // Returns top-right sub rect
    private IntRect getTopRightRect() {
        return new IntRect(
                rect.x + rect.width / 2, rect.y + rect.height / 2,
                rect.width - rect.width / 2, rect.height - rect.height / 2
        );
    }

    // Returns bottom-right sub rect
    private IntRect getBottomRightRect() {
        return new IntRect(
                rect.x + rect.width / 2, rect.y,
                rect.width - rect.width / 2, rect.height / 2
        );
    }

    // Returns bottom-left sub rect
    private IntRect getBottomLeftRect() {
        return new IntRect(
                rect.x, rect.y,
                rect.width / 2, rect.height / 2
        );
    }

    // Inserts item as point
    public void insert(T item, IntPoint point) {
        if (!GeoBattleMath.tileRectangleContains(rect, point.x, point.y))
            return;

        if (points.size() >= capacity) {
            IntRect topLeftRect = getTopLeftRect();
            if (GeoBattleMath.tileRectangleContains(topLeftRect, point.x, point.y)) {
                if (topLeft == null)
                    topLeft = new QuadTree<T>(capacity, topLeftRect);
                topLeft.insert(item, point);
                return;
            }

            IntRect topRightRect = getTopRightRect();
            if (GeoBattleMath.tileRectangleContains(topRightRect, point.x, point.y)) {
                if (topRight == null)
                    topRight = new QuadTree<T>(capacity, topRightRect);
                topRight.insert(item, point);
                return;
            }

            IntRect bottomRightRect = getBottomRightRect();
            if (GeoBattleMath.tileRectangleContains(bottomRightRect, point.x, point.y)) {
                if (bottomRight == null)
                    bottomRight = new QuadTree<T>(capacity, bottomRightRect);
                bottomRight.insert(item, point);
                return;
            }

            IntRect bottomLeftRect = getBottomLeftRect();
            if (GeoBattleMath.tileRectangleContains(bottomLeftRect, point.x, point.y)) {
                if (bottomLeft == null)
                    bottomLeft = new QuadTree<T>(capacity, bottomLeftRect);
                bottomLeft.insert(item, point);
            }
        } else {
            points.add(new QuadTreePoint(point.x, point.y, item));
        }
    }

    // Removes points equal to given point
    public void remove(T item, IntPoint point) {
        int newLength = 0;
        for (int index = 0; index < points.size(); index++) {
            QuadTreePoint existingPoint = points.get(index);

            if (existingPoint.data != item || existingPoint.x != point.x || existingPoint.y != point.y) {
                points.set(newLength, existingPoint);
                newLength++;
            }
        }
        while (points.size() > newLength)
            points.remove(points.size() - 1);

        if (topLeft != null)
            if (GeoBattleMath.tileRectanglesIntersect(rect, topLeft.rect)) {
                topLeft.remove(item, point);
                return;
            }

        if (topRight != null)
            if (GeoBattleMath.tileRectanglesIntersect(rect, topRight.rect)) {
                topRight.remove(item, point);
                return;
            }

        if (bottomRight != null)
            if (GeoBattleMath.tileRectanglesIntersect(rect, bottomRight.rect)) {
                bottomRight.remove(item, point);
                return;
            }

        if (bottomLeft != null)
            if (GeoBattleMath.tileRectanglesIntersect(rect, bottomLeft.rect))
                bottomLeft.remove(item, point);
    }

    // Queries all items from quad tree
    public HashSet<T> queryAll() {
        HashSet<T> result = new HashSet<T>();
        queryAll(result);
        return result;
    }

    // Queries all items from quad tree
    private void queryAll(HashSet<T> result) {
        for (QuadTreePoint point : points)
            result.add(point.data);

        if (topLeft != null)
            topLeft.queryAll(result);

        if (topRight != null)
            topRight.queryAll(result);

        if (bottomRight != null)
            bottomRight.queryAll(result);

        if (bottomLeft != null)
            bottomLeft.queryAll(result);
    }

    // Queries all items in rect
    public HashSet<T> queryByRect(IntRect rect) {
        HashSet<T> result = new HashSet<T>();
        queryByRect(result, rect, Integer.MAX_VALUE);
        return result;
    }

    // Queries all items in rect
    public HashSet<T> queryByRect(IntRect rect, int maxCount) {
        HashSet<T> result = new HashSet<T>();
        queryByRect(result, rect, maxCount);
        return result;
    }

    // Queries all items in rect
    private void queryByRect(HashSet<T> result, IntRect rect, int maxCount) {
        for (QuadTreePoint point : points) {
            if (result.size() >= maxCount)
                return;
            if (GeoBattleMath.tileRectangleContains(rect, point.x, point.y))
                result.add(point.data);
        }

        if (result.size() >= maxCount)
            return;

        if (topLeft != null) {
            if (GeoBattleMath.tileRectangleContains(rect, topLeft.rect))
                topLeft.queryAll(result);
            else if (GeoBattleMath.tileRectanglesIntersect(rect, topLeft.rect))
                topLeft.queryByRect(result, rect, maxCount);
        }

        if (topRight != null) {
            if (GeoBattleMath.tileRectangleContains(rect, topRight.rect))
                topRight.queryAll(result);
            else if (GeoBattleMath.tileRectanglesIntersect(rect, topRight.rect))
                topRight.queryByRect(result, rect, maxCount);
        }

        if (bottomRight != null) {
            if (GeoBattleMath.tileRectangleContains(rect, bottomRight.rect))
                bottomRight.queryAll(result);
            else if (GeoBattleMath.tileRectanglesIntersect(rect, bottomRight.rect))
                bottomRight.queryByRect(result, rect, maxCount);
        }

        if (bottomLeft != null) {
            if (GeoBattleMath.tileRectangleContains(rect, bottomLeft.rect))
                bottomLeft.queryAll(result);
            else if (GeoBattleMath.tileRectanglesIntersect(rect, bottomLeft.rect))
                bottomLeft.queryByRect(result, rect, maxCount);
        }
    }
}
