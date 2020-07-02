package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.adapters.ProfilesAdapter;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class ProfileActivity extends AppCompatActivity {

    public static final String TAG = "ProfileActivity";

    User user;
    List<User> followers;
    List<User> followings;

    ImageView ivProfileImage;
    TextView tvScreenName;
    TextView tvName;

    RecyclerView rvFollowers;
    RecyclerView rvFollowings;
    ProfilesAdapter followerAdapter;
    ProfilesAdapter followingAdapter;
    TwitterClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = (User) Parcels.unwrap(getIntent().getParcelableExtra("user"));

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvScreenName = findViewById(R.id.tvScreenName);
        tvName = findViewById(R.id.tvName);

        rvFollowers = findViewById(R.id.rvFollowers);
        rvFollowings = findViewById(R.id.rvFollowing);

        tvScreenName.setText(user.name);
        tvName.setText("@" + user.screenName);

        int radius = 30; // corner radius, higher value = more rounded
        int margin = 10; // crop margin, set to 0 for corners with no crop
        Glide.with(this).load(user.profileImageUrl)
                .transform(new RoundedCornersTransformation(radius, margin)).into(ivProfileImage);

        // create a TwitterClient
        client = TwitterApp.getRestClient(this);

        // init the list of tweets and adapter
        followers = new ArrayList<>();
        followings = new ArrayList<>();
        followerAdapter = new ProfilesAdapter(this, followers);
        followingAdapter = new ProfilesAdapter(this, followings);
        // Recycler view setup: layout manager and adapter
        LinearLayoutManager followersLayoutManager = new LinearLayoutManager(this);
        LinearLayoutManager followingsLayoutManager = new LinearLayoutManager(this);

        rvFollowers.setLayoutManager(followersLayoutManager);
        rvFollowers.setAdapter(followerAdapter);
        rvFollowings.setLayoutManager(followingsLayoutManager);
        rvFollowings.setAdapter(followingAdapter);

        populateFollowers();

        populateFollowings();


    }

    // make API request to get followers
    private void populateFollowers() {
        client.getFollowers(user.id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess to get followers!" + json.toString());
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("users");
                    final List<User> followers = fromJsonArray(jsonArray);
                    followerAdapter.clear();
                    followerAdapter.addAll(followers);
                    // Now we call setRefreshing(false) to signal refresh has finished
                    //swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception to get followers", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure: " + response, throwable);

            }
        });
    }

    // make API request to get followings
    private void populateFollowings() {
        client.getFollowings(user.id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess to get followings!" + json.toString());
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("users");
                    final List<User> followings = fromJsonArray(jsonArray);
                    followingAdapter.clear();
                    followingAdapter.addAll(followings);
                    // Now we call setRefreshing(false) to signal refresh has finished
                    //swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception to get followings", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure: " + response, throwable);

            }
        });
    }

    public static List<User> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<User> follows = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            follows.add(User.fromJson((jsonArray.getJSONObject(i))));
        }
        return follows;
    }
}