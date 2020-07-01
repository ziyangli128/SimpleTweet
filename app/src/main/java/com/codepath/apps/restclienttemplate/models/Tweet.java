package com.codepath.apps.restclienttemplate.models;

import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {
    public static final String TAG = "Tweet";

    // some fields for a Tweet object
    public String body;
    public String createdAt;
    public String mediaUrl;
    public User user;

    // empty constructor needed by the Parceler libarary
    public Tweet() {}

    // turn a JsonObject into a java Tweet Object
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));

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
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson((jsonArray.getJSONObject(i))));
        }
        return tweets;
    }
}
