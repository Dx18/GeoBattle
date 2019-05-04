package geobattle.geobattle.util;

import java.util.Iterator;

// Iterator which used to iterate single element
public final class SingleElementIterator<T> implements Iterator<T> {
    // Element
    private T element;

    // State of iterator
    private boolean hasNext;

    // Creates iterator
    public SingleElementIterator(T element) {
        this.element = element;
        hasNext = true;
    }

    // Returns true if element was not iterated yet
    @Override
    public boolean hasNext() {
        return hasNext;
    }

    // Returns element
    @Override
    public T next() {
        hasNext = false;
        return element;
    }
}
