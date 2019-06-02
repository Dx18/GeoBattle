package geobattle.geobattle.util;

import java.util.ArrayList;

// Simple binary min-heap
public final class BinaryHeap<T extends BinaryHeap.Node> {
    // Node of heap
    public static abstract class Node {
        public final double priority;

        protected Node(double priority) {
            this.priority = priority;
        }
    }

    // Array of nodes
    private final ArrayList<T> nodes;

    public BinaryHeap() {
        nodes = new ArrayList<T>();
    }

    // Heapify operation
    private void heapify(int index) {
        while (index < nodes.size()) {
            int min = index;
            int left = index * 2 + 1;
            int right = index * 2 + 2;
            if (left < nodes.size() && nodes.get(left).priority < nodes.get(min).priority)
                min = left;
            if (right < nodes.size() && nodes.get(right).priority < nodes.get(min).priority)
                min = right;
            if (index != min) {
                T temp = nodes.get(index);
                nodes.set(index, nodes.get(min));
                nodes.set(min, temp);

                index = min;
            } else
                break;
        }
    }

    // Inserts item into heap
    public void insert(T item) {
        int index = nodes.size();
        nodes.add(item);

        while (index > 0) {
            index = (index - 1) / 2;
            heapify(index);
        }
    }

    // Peeks item from heap
    public T peek() {
        if (nodes.size() == 0)
            return null;
        return nodes.get(0);
    }

    // Pops item from heap
    public T pop() {
        if (nodes.size() == 0)
            return null;

        T result = peek();
        nodes.set(0, nodes.get(nodes.size() - 1));
        nodes.remove(nodes.size() - 1);

        heapify(0);

        return result;
    }

    // Returns size of heap
    public int getSize() {
        return nodes.size();
    }
}
