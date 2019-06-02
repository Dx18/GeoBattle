package geobattle.geobattle.game.tasks;

import geobattle.geobattle.util.BinaryHeap;

public final class TimedObject<T> extends BinaryHeap.Node {
    // Timed object time
    public final double time;

    // Timed object
    public final T object;

    public TimedObject(double time, T object) {
        super(time);
        this.time = time;
        this.object = object;
    }
}
