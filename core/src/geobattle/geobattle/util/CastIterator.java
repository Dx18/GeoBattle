package geobattle.geobattle.util;

import java.util.Iterator;

// Iterator which casts elements of type S to type D
public final class CastIterator<S, D extends S> implements Iterator<D> {
    // Inner iterator
    private Iterator<S> inner;

    // Creates iterator
    public CastIterator(Iterator<S> inner) {
        this.inner = inner;
    }

    // Returns true if iterator has next element
    @Override
    public boolean hasNext() {
        return inner.hasNext();
    }

    // Returns next element casted to D
    @Override
    public D next() {
        return (D) inner.next();
    }
}
