package geobattle.geobattle.util;

import java.util.ArrayList;
import java.util.Iterator;

// Iterator which joins iterators
public final class JoinIterator<T> implements Iterator<T> {
    // Inner iterators
    private ArrayList<Iterator<T>> innerIterators;

    // Index of current iterator
    private int currentIterator;

    // Creates iterator
    public JoinIterator(ArrayList<Iterator<T>> innerIterators) {
        this.innerIterators = innerIterators;
        this.currentIterator = 0;
    }

    // Returns true if iterator has next element
    @Override
    public boolean hasNext() {
        while (currentIterator < innerIterators.size() && !innerIterators.get(currentIterator).hasNext())
            currentIterator++;
        return currentIterator < innerIterators.size();
    }

    // Returns next element
    @Override
    public T next() {
        return innerIterators.get(currentIterator).next();
    }
}
