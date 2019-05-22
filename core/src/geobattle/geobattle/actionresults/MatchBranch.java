package geobattle.geobattle.actionresults;

// Match branch for BuildResult and DestroyResult matching
public interface MatchBranch<T> {
    // Function called on match
    void onMatch(T match);
}
