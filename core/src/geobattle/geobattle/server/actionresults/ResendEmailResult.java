package geobattle.geobattle.server.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.actionresults.MatchBranch;

// Result of email resend
public abstract class ResendEmailResult {
    // Email successfully resent
    public static final class EmailResent extends ResendEmailResult {
        public EmailResent() {}

        public static EmailResent fromJson(JsonObject object) {
            return new EmailResent();
        }
    }

    // Player with same name already confirmed email or does not exist
    public static final class DoesNotExist extends ResendEmailResult {
        public DoesNotExist() {}

        public static DoesNotExist fromJson(JsonObject object) {
            return new DoesNotExist();
        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends ResendEmailResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }
    }

    // Value of field is incorrect
    public static final class IncorrectData extends ResendEmailResult {
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

    // Creates ResendEmailResult from JSON
    public static ResendEmailResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("EmailResent"))
            return EmailResent.fromJson(object);
        else if (type.equals("DoesNotExist"))
            return DoesNotExist.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        else if (type.equals("IncorrectData"))
            return IncorrectData.fromJson(object);
        return null;
    }

    // Matches ResendEmailResult
    public void match(
            MatchBranch<EmailResent> emailResent,
            MatchBranch<DoesNotExist> doesNotExist,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (emailResent != null && this instanceof EmailResent)
            emailResent.onMatch((EmailResent) this);
        else if (doesNotExist != null && this instanceof DoesNotExist)
            doesNotExist.onMatch((DoesNotExist) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}
