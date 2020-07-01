package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class DetailActivity extends AppCompatActivity {

    public static final String TAG = "DetailActivity";
    public static final int MAX_TWEET_LENGTH = 280;

    // the tweet to display
    Tweet tweet;
    Context context;

    EditText etReply;
    Button btnReply;
    ImageView ivProfileImage;
    ImageView ivMediaPhoto;
    TextView tvBody;
    TextView tvScreenName;
    TextView tvRelativeTime;

    // Get reference to the twitter client
    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // unwrap the tweet passed in via intent
        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        Log.d("DetailActivity", String.format("Showing details for '%s'", tweet.body));

        etReply = findViewById(R.id.etReply);
        btnReply = findViewById(R.id.btnReply);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivMediaPhoto = findViewById(R.id.ivMediaPhoto);
        tvBody = findViewById(R.id.tvBody);
        tvScreenName = findViewById(R.id.tvScreenName);
        tvRelativeTime = findViewById(R.id.tvRelativeTime);

        bind(tweet);

        client = TwitterApp.getRestClient(this);

        // Set click listener on button
        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweetContent = etReply.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(DetailActivity.this,
                            "Sorry, your tweet cannot be empty.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(DetailActivity.this,
                            "Sorry, your tweet is too long.", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(DetailActivity.this, tweetContent, Toast.LENGTH_LONG).show();

                // Make an API call to Twitter to publish the tweet
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess: publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Published tweet says: " + tweet.body);
                            // Prepare data intent
                            Intent i = new Intent();
                            // Pass tweet back as result
                            i.putExtra("tweet", Parcels.wrap(tweet));
                            // Set result code and bundle data for response
                            setResult(RESULT_OK, i);
                            // Closes the activity, pass the data to parent
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure: publish tweet", throwable);
                    }
                });

            }
        });
    }

    int radius = 30; // corner radius, higher value = more rounded
    int margin = 10; // crop margin, set to 0 for corners with no crop
    public void bind(Tweet tweet) {
        tvBody.setText(tweet.body);
        tvScreenName.setText(tweet.user.name);
        //tvRelativeTime.setText(getRelativeTimeAgo(tweet.createdAt));
        etReply.setText("@" + tweet.user.screenName + "  ");

        Glide.with(DetailActivity.this).load(tweet.user.profileImageUrl)
                .transform(new RoundedCornersTransformation(radius, margin)).into(ivProfileImage);
        Glide.with(DetailActivity.this).load(tweet.mediaUrl)
                    .into(ivMediaPhoto);
    }
}