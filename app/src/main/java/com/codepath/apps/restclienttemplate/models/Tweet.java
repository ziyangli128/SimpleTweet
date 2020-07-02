package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
@Entity(foreignKeys = @ForeignKey(entity=User.class, parentColumns="id", childColumns="userId"))
public class Tweet {
    public static final String TAG = "Tweet";

    // some fields for a Tweet object
    @ColumnInfo
    public String body;
    @ColumnInfo
    public String createdAt;
    @ColumnInfo
    public String mediaUrl;
    @Ignore
    public User user;
    @ColumnInfo
    public long userId;
    @ColumnInfo
    @PrimaryKey
    public long id;

    // empty constructor needed by the Parceler libarary
    public Tweet() {}

    // turn a JsonObject into a java Tweet Object
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        User user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.user = user;
        tweet.userId = user.id;
        tweet.id = jsonObject.getLong("id");

        if (jsonObject.getJSONObject("entities").has("media")) {
            Log.i(TAG, "fromJson: get media url");
            tweet.mediaUrl = jsonObject.getJSONObject("entities")
                    .getJSONArray("media")
                    .getJSONObject(0).getString("media_url_https");
        }
        return tweet;
    }

    // turn a JsonArray into a list of Tweet objects
    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        long lowestId = jsonArray.getJSONObject(0).getLong("id");
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getLong("id") < lowestId) {
                lowestId = jsonArray.getJSONObject(i).getLong("id");
            }
            tweets.add(fromJson((jsonArray.getJSONObject(i))));
        }
        return tweets;
    }
}
