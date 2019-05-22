package geobattle.geobattle.util;

import java.util.Arrays;

// Queue which measures frequencies of events
public final class FrequencyQueue {
    // Data of queue
    private boolean[] data;

    // Current index
    private int index;

    // Count of true values
    private int trueCount;

    public FrequencyQueue(int capacity, boolean init) {
        data = new boolean[capacity];
        Arrays.fill(data, init);
        index = 0;
        trueCount = init ? capacity : 0;
    }

    // Adds event to queue
    public synchronized void add(boolean element) {
        if (element != data[index]) {
            if (element && !data[index]) {
                data[index] = true;
                trueCount++;
            } else if (!element && data[index]) {
                data[index] = false;
                trueCount--;
            }
        }
        index = (index + 1) % data.length;
    }

    // Returns count of true events
    public synchronized int getTrueCount() {
        return trueCount;
    }

    // Returns count of false events
    public synchronized int getFalseCount() {
        return data.length - trueCount;
    }
}
