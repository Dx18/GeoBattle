package geobattle.geobattle.game.events;

import com.google.gson.JsonObject;

import geobattle.geobattle.server.AuthInfo;
import geobattle.geobattle.util.JsonObjects;

public final class AttackEvent {
    // Auth info
    public final AuthInfo authInfo;

    // ID of attacker
    public final int attackerId;

    // ID of victim
    public final int victimId;

    // IDs of hangars
    public final int[] hangarIds;

    // ID of sector
    public final int sectorId;

    public AttackEvent(AuthInfo authInfo, int attackerId, int victimId, int[] hangarIds, int sectorId) {
        this.authInfo = authInfo;
        this.attackerId = attackerId;
        this.victimId = victimId;
        this.hangarIds = hangarIds;
        this.sectorId = sectorId;
    }

    // Converts AttackEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "AttackEvent");
        object.add("authInfo", authInfo.toJson());
        object.addProperty("attackerId", attackerId);
        object.addProperty("victimId", victimId);
        object.add("hangarIds", JsonObjects.toJson(hangarIds));
        object.addProperty("sectorId", sectorId);
        return object;
    }
}
