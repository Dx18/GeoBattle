package geobattle.geobattle.server.actionresults;

import com.google.gson.JsonObject;

import geobattle.geobattle.game.actionresults.MatchBranch;

// Result of registration
public abstract class RegistrationResult {
    // Successfully registered
    public static final class Success extends RegistrationResult {
        // Name of player
        public final String name;

        public Success(String name) {
            this.name = name;
        }

        public static Success fromJson(JsonObject object) {
            String name = object.getAsJsonPrimitive("name").getAsString();
            return new Success(name);
        }
    }

    // Email is invalid
    public static final class InvalidEmail extends RegistrationResult {
        public InvalidEmail() {}

        public static InvalidEmail fromJson(JsonObject object) {
            return new InvalidEmail();
        }
    }

    // Player with same email already exists
    public static final class EmailExists extends RegistrationResult {
        public EmailExists() {}

        public static EmailExists fromJson(JsonObject object) {
            return new EmailExists();
        }
    }

    // Invalid length of name
    public static final class InvalidNameLength extends RegistrationResult {
        // Actual length
        public final int actual;

        // Min length
        public final int min;

        // Max length
        public final int max;

        public InvalidNameLength(int actual, int min, int max) {
            this.actual = actual;
            this.min = min;
            this.max = max;
        }

        public static InvalidNameLength fromJson(JsonObject object) {
            int actual = object.getAsJsonPrimitive("actual").getAsInt();
            int min = object.getAsJsonPrimitive("min").getAsInt();
            int max = object.getAsJsonPrimitive("max").getAsInt();
            return new InvalidNameLength(actual, min, max);
        }
    }

    // Invalid length of password
    public static final class InvalidPasswordLength extends RegistrationResult {
        // Actual length
        public final int actual;

        // Min length
        public final int min;

        public InvalidPasswordLength(int actual, int min) {
            this.actual = actual;
            this.min = min;
        }

        public static InvalidPasswordLength fromJson(JsonObject object) {
            int actual = object.getAsJsonPrimitive("actual").getAsInt();
            int min = object.getAsJsonPrimitive("min").getAsInt();
            return new InvalidPasswordLength(actual, min);
        }
    }

    // Invalid symbols in name
    public static final class InvalidNameSymbols extends RegistrationResult {
        public InvalidNameSymbols() {}

        public static InvalidNameSymbols fromJson(JsonObject object) {
            return new InvalidNameSymbols();
        }
    }

    // Player with same name already exists
    public static final class NameExists extends RegistrationResult {
        public NameExists() {}

        public static NameExists fromJson(JsonObject object) {
            return new NameExists();
        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends RegistrationResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }
    }

    // Value of field is not valid
    public static final class IncorrectData extends RegistrationResult {
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

    // Creates RegistrationResult from JSON
    public static RegistrationResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("Success"))
            return Success.fromJson(object);
        else if (type.equals("InvalidEmail"))
            return InvalidEmail.fromJson(object);
        else if (type.equals("EmailExists"))
            return EmailExists.fromJson(object);
        else if (type.equals("InvalidNameLength"))
            return InvalidNameLength.fromJson(object);
        else if (type.equals("InvalidPasswordLength"))
            return InvalidPasswordLength.fromJson(object);
        else if (type.equals("InvalidNameSymbols"))
            return InvalidNameSymbols.fromJson(object);
        else if (type.equals("NameExists"))
            return NameExists.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        else if (type.equals("IncorrectData"))
            return IncorrectData.fromJson(object);
        return null;
    }

    // Matches RegistrationResult
    public void match(
            MatchBranch<Success> success,
            MatchBranch<InvalidEmail> invalidEmail,
            MatchBranch<EmailExists> emailExists,
            MatchBranch<InvalidNameLength> invalidNameLength,
            MatchBranch<InvalidPasswordLength> invalidPasswordLength,
            MatchBranch<InvalidNameSymbols> invalidNameSymbols,
            MatchBranch<NameExists> nameExists,
            MatchBranch<MalformedJson> malformedJson,
            MatchBranch<IncorrectData> incorrectData
    ) {
        if (success != null && this instanceof Success)
            success.onMatch((Success) this);
        else if (invalidEmail != null && this instanceof InvalidEmail)
            invalidEmail.onMatch((InvalidEmail) this);
        else if (emailExists != null && this instanceof EmailExists)
            emailExists.onMatch((EmailExists) this);
        else if (invalidNameLength != null && this instanceof InvalidNameLength)
            invalidNameLength.onMatch((InvalidNameLength) this);
        else if (invalidPasswordLength != null && this instanceof InvalidPasswordLength)
            invalidPasswordLength.onMatch((InvalidPasswordLength) this);
        else if (invalidNameSymbols != null && this instanceof InvalidNameSymbols)
            invalidNameSymbols.onMatch((InvalidNameSymbols) this);
        else if (nameExists != null && this instanceof NameExists)
            nameExists.onMatch((NameExists) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
        else if (incorrectData != null && this instanceof IncorrectData)
            incorrectData.onMatch((IncorrectData) this);
    }
}
