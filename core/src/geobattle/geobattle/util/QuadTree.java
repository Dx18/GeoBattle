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

        // Parent rectangle
        public final IntRect parentRect;

        // Data
        public final T data;

        public QuadTreePoint(int x, int y, IntRect parentRect, T data) {
            this.x = x;
            this.y = y;
            this.parentRect = parentRect;
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

    // Insert item as rect
    public void insertAsRect(T item, IntRect rect) {
        insertAsPoint(item, new IntPoint(rect.x, rect.y), rect);
        insertAsPoint(item, new IntPoint(rect.x, rect.y + rect.height - 1), rect);
        insertAsPoint(item, new IntPoint(rect.x + rect.width - 1, rect.y), rect);
        insertAsPoint(item, new IntPoint(rect.x + rect.width - 1, rect.y + rect.height - 1), rect);
    }

    // Inserts item as point
    public void insertAsPoint(T item, IntPoint point, IntRect parentRect) {
        if (!GeoBattleMath.tileRectangleContains(rect, point.x, point.y))
            return;

        if (points.size() >= capacity) {
            IntRect topLeftRect = getTopLeftRect();
            if (GeoBattleMath.tileRectangleContains(topLeftRect, point.x, point.y)) {
                if (topLeft == null)
                    topLeft = new QuadTree<T>(capacity, topLeftRect);
                topLeft.insertAsPoint(item, point, parentRect);
                return;
            }

            IntRect topRightRect = getTopRightRect();
            if (GeoBattleMath.tileRectangleContains(topRightRect, point.x, point.y)) {
                if (topRight == null)
                    topRight = new QuadTree<T>(capacity, topRightRect);
                topRight.insertAsPoint(item, point, parentRect);
                return;
            }

            IntRect bottomRightRect = getBottomRightRect();
            if (GeoBattleMath.tileRectangleContains(bottomRightRect, point.x, point.y)) {
                if (bottomRight == null)
                    bottomRight = new QuadTree<T>(capacity, bottomRightRect);
                bottomRight.insertAsPoint(item, point, parentRect);
                return;
            }

            IntRect bottomLeftRect = getBottomLeftRect();
            if (GeoBattleMath.tileRectangleContains(bottomLeftRect, point.x, point.y)) {
                if (bottomLeft == null)
                    bottomLeft = new QuadTree<T>(capacity, bottomLeftRect);
                bottomLeft.insertAsPoint(item, point, parentRect);
            }
        } else {
            points.add(new QuadTreePoint(point.x, point.y, parentRect, item));
        }
    }

    public void removeAsRect(T item, IntRect rect) {
        removeAsPoint(item, new IntPoint(rect.x, rect.y));
        removeAsPoint(item, new IntPoint(rect.x, rect.y + rect.height - 1));
        removeAsPoint(item, new IntPoint(rect.x + rect.width - 1, rect.y));
        removeAsPoint(item, new IntPoint(rect.x + rect.width - 1, rect.y + rect.height - 1));
    }

    // Removes points equal to given point
    public void removeAsPoint(T item, IntPoint point) {
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
                topLeft.removeAsPoint(item, point);
                return;
            }

        if (topRight != null)
            if (GeoBattleMath.tileRectanglesIntersect(rect, topRight.rect)) {
                topRight.removeAsPoint(item, point);
                return;
            }

        if (bottomRight != null)
            if (GeoBattleMath.tileRectanglesIntersect(rect, bottomRight.rect)) {
                bottomRight.removeAsPoint(item, point);
                return;
            }

        if (bottomLeft != null)
            if (GeoBattleMath.tileRectanglesIntersect(rect, bottomLeft.rect))
                bottomLeft.removeAsPoint(item, point);
    }

    // Queries all items in rect
    public HashSet<T> queryByRectIntersection(IntRect rect) {
        HashSet<T> result = new HashSet<T>();
        queryByRectIntersection(result, rect, Integer.MAX_VALUE);
        return result;
    }

    // Queries all items in rect
    public HashSet<T> queryByRectIntersection(IntRect rect, int maxCount) {
        HashSet<T> result = new HashSet<T>();
        queryByRectIntersection(result, rect, maxCount);
        return result;
    }

    // Queries all items in rect
    private void queryByRectIntersection(HashSet<T> result, IntRect rect, int maxCount) {
        for (QuadTreePoint point : points) {
            if (result.size() >= maxCount)
                return;
            if (GeoBattleMath.tileRectangleContains(rect, point.x, point.y))
                result.add(point.data);
        }

        if (result.size() >= maxCount)
            return;

        if (topLeft != null)
            if (GeoBattleMath.tileRectanglesIntersect(rect, topLeft.rect))
                topLeft.queryByRectIntersection(result, rect, maxCount);

        if (topRight != null)
            if (GeoBattleMath.tileRectanglesIntersect(rect, topRight.rect))
                topRight.queryByRectIntersection(result, rect, maxCount);

        if (bottomRight != null)
            if (GeoBattleMath.tileRectanglesIntersect(rect, bottomRight.rect))
                bottomRight.queryByRectIntersection(result, rect, maxCount);

        if (bottomLeft != null)
            if (GeoBattleMath.tileRectanglesIntersect(rect, bottomLeft.rect))
                bottomLeft.queryByRectIntersection(result, rect, maxCount);
    }
}
