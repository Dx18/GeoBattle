package geobattle.geobattle.server;

// Callback function
public interface Callback<T> {
    // Invokes on result
    void onResult(T result);
}