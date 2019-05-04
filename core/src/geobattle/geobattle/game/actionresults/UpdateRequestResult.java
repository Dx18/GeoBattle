package geobattle.geobattle.game.actionresults;

import java.util.ArrayList;

import geobattle.geobattle.game.GameStateUpdate;

// Result of update requesting
public abstract class UpdateRequestResult {
    // Successfully got updates
    public static final class UpdateRequestSuccess extends UpdateRequestResult {
        public final ArrayList<GameStateUpdate> updates;

        public UpdateRequestSuccess(ArrayList<GameStateUpdate> updates) {
            this.updates = updates;
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends UpdateRequestResult {
        public WrongAuthInfo() {}
    }

    // Matches UpdateRequestResult
    public void match(
            MatchBranch<UpdateRequestSuccess> updateRequestSuccess,
            MatchBranch<WrongAuthInfo> wrongAuthInfo
    ) {
        if (updateRequestSuccess != null && this instanceof UpdateRequestSuccess)
            updateRequestSuccess.onMatch((UpdateRequestSuccess) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
    }
}
