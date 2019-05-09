package geobattle.geobattle.game.events;

import com.google.gson.JsonObject;

import geobattle.geobattle.server.AuthInfo;

// Research event
public final class ResearchEvent {
    // Auth info
    public final AuthInfo authInfo;

    // Type of research
    public final String researchType;

    public ResearchEvent(AuthInfo authInfo, String researchType) {
        this.authInfo = authInfo;
        this.researchType = researchType;
    }

    // Converts ResearchEvent to JSON
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "ResearchEvent");
        object.add("authInfo", authInfo.toJson());
        object.addProperty("researchType", researchType);
        return object;
    }
}
