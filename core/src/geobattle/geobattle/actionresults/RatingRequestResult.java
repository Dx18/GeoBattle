package geobattle.geobattle.actionresults;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import geobattle.geobattle.rating.RatingEntry;

// Rating request event
public abstract class RatingRequestResult {
    // Success of rating request
    public static final class RatingRequestSuccess extends RatingRequestResult {
        // Rating entries
        public final RatingEntry[] rating;

        public RatingRequestSuccess(RatingEntry[] rating) {
            this.rating = rating;
        }

        public static RatingRequestSuccess fromJson(JsonObject object) {
            JsonArray ratingJson = object.getAsJsonArray("rating");
            RatingEntry[] rating = new RatingEntry[ratingJson.size()];
            for (int i = 0; i < rating.length; i++)
                rating[i] = RatingEntry.fromJson(ratingJson.get(i).getAsJsonObject());

            return new RatingRequestSuccess(rating);
        }
    }

    // JSON request is not well-formed
    public static final class MalformedJson extends RatingRequestResult {
        public MalformedJson() {}

        public static MalformedJson fromJson(JsonObject object) {
            return new MalformedJson();
        }
    }

    // Creates RatingRequestResult from JSON
    public static RatingRequestResult fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();

        if (type.equals("RatingRequestSuccess"))
            return RatingRequestSuccess.fromJson(object);
        else if (type.equals("MalformedJson"))
            return MalformedJson.fromJson(object);
        return null;
    }

    // Matches RatingRequestResult
    public void match(
            MatchBranch<RatingRequestSuccess> ratingRequestSuccess,
            MatchBranch<MalformedJson> malformedJson
    ) {
        if (ratingRequestSuccess != null && this instanceof RatingRequestSuccess)
            ratingRequestSuccess.onMatch((RatingRequestSuccess) this);
        else if (malformedJson != null && this instanceof MalformedJson)
            malformedJson.onMatch((MalformedJson) this);
    }
}
