package geobattle.geobattle.util;

import java.util.ArrayList;
import java.util.Collections;

// Wrapper for ArrayList<T> which supports getting size and getting element at index
public final class ReadOnlyArrayList<T> {
    // Inner array list
    private ArrayList<T> innerArrayList;

    public ReadOnlyArrayList(ArrayList<T> list) {
        this.innerArrayList = list;
    }

    // Returns size of list
    public int size() {
        return innerArrayList.size();
    }

    // Returns element at index
    public T get(int index) {
        return innerArrayList.get(index);
    }

    // Copies array list and returns it (does not copy contained elements though)
    public ArrayList<T> copy() {
        ArrayList<T> cloned = new ArrayList<T>(innerArrayList.size());
        Collections.copy(innerArrayList, cloned);
        return cloned;
    }
}
