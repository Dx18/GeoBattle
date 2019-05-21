package geobattle.geobattle.game.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.attacking.AttackScript;

public abstract class AttackResult {
    // Attack successfully started
    public static final class AttackStarted extends AttackResult {
        // Attack script
        public final AttackScript attackScript;

        public AttackStarted(AttackScript attackScript) {
            this.attackScript = attackScript;
        }

        public static AttackStarted fromJson(JsonObject object) {
            AttackScript attackScript = AttackScript.fromJson(object.getAsJsonObject("attackScript"));
            return new AttackStarted(attackScript);
        }
    }

    // Cannot attack: sector blocked or one
    public static final class NotAttackable extends AttackResult {
        public NotAttackable() {}

        public static NotAttackable fromJson(JsonObject object) {
            return new NotAttackable();
        }
    }

    // Cannot attack: some hangars are already used
    public static final class HangarsAlreadyUsed extends AttackResult {
        public HangarsAlreadyUsed() {}

        public static HangarsAlreadyUsed fromJson(JsonObject object) {
            return new HangarsAlreadyUsed();
        }
    }

    // Wrong auth info
    public static final class WrongAuthInfo extends AttackResult {
        public WrongAuthInfo() {}

        public static WrongAuthInfo fromJson(JsonObject object) {
            return new WrongAuthInfo();
        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends AttackResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }
    }

    // Value of field is not valid
    public static final class IncorrectData extends AttackResult {
        // Field with error
        public final String field;

        public IncorrectData(String field) {
            this.field = field;
        }

        public static IncorrectData fromJson(JsonObject object) {
            String field = object.getAsJsonPrimitive("field").getAsString();
            return new IncorrectData(field);
        }
    }

    // Creates AttackResult from JSON
    public static AttackResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("AttackStarted"))
            return AttackStarted.fromJson(object);
        else if (type.equals("NotAttackable"))
            return NotAttackable.fromJson(object);
        else if (type.equals("HangarsAlreadyUsed"))
            return HangarsAlreadyUsed.fromJson(object);
        else if (type.equals("WrongAuthInfo"))
            return WrongAuthInfo.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        else if (type.equals("IncorrectData"))
            return IncorrectData.fromJson(object);
        return null;
    }

    // Matches AttackResult
    public void match(
            MatchBranch<AttackStarted> attackStarted,
            MatchBranch<NotAttackable> notAttackable,
            MatchBranch<HangarsAlreadyUsed> hangarsAlreadyUsed,
            MatchBranch<WrongAuthInfo> wrongAuthInfo,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (attackStarted != null && this instanceof AttackStarted)
            attackStarted.onMatch((AttackStarted) this);
        else if (notAttackable != null && this instanceof NotAttackable)
            notAttackable.onMatch((NotAttackable) this);
        else if (hangarsAlreadyUsed != null && this instanceof HangarsAlreadyUsed)
            hangarsAlreadyUsed.onMatch((HangarsAlreadyUsed) this);
        else if (wrongAuthInfo != null && this instanceof WrongAuthInfo)
            wrongAuthInfo.onMatch((WrongAuthInfo) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}
