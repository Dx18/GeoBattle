package geobattle.geobattle.game.tasks;

import java.util.ArrayList;

import geobattle.geobattle.util.BinaryHeap;

// Queue of timed objects
public final class TimedObjectQueue<T> {
    // Inner binary min-objects of timed objects
    private final BinaryHeap<TimedObject<T>> objects;

    public TimedObjectQueue() {
        this.objects = new BinaryHeap<TimedObject<T>>();
    }

    // Adds timed object
    public void addTimedObject(TimedObject<T> timedObject) {
        objects.insert(timedObject);
    }

    // Returns expired timed objects
    public ArrayList<TimedObject<T>> getTimedObjects(double currentTime) {
        ArrayList<TimedObject<T>> result = new ArrayList<TimedObject<T>>();
        while (objects.getSize() > 0 && objects.peek().time <= currentTime)
            result.add(objects.pop());
        return result;
    }

    // Returns expired timed objects without time
    public ArrayList<T> getObjects(double currentTime) {
        ArrayList<T> result = new ArrayList<T>();
        while (objects.getSize() > 0 && objects.peek().time <= currentTime)
            result.add(objects.pop().object);
        return result;
    }
}
