package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

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
import com.codepath.apps.restclienttemplate.databinding.ActivityDetailBinding;
import com.codepath.apps.restclienttemplate.fragmens.ComposeTweetDialogFragment;
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

    // Get reference to the twitter client
    TwitterClient client;
    long id;

    ActivityDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         binding = ActivityDetailBinding.inflate(getLayoutInflater());

        // layout of activity is stored in a special property called root
        View view = binding.getRoot();
        setContentView(view);

        // unwrap the tweet passed in via intent
        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        Log.d("DetailActivity", String.format("Showing details for '%s'", tweet.body));

        bind(tweet);

        client = TwitterApp.getRestClient(this);
        id = tweet.id;

        // Set click listener on button
        binding.btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweetContent = binding.etReply.getText().toString();
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

        binding.ivLike.setOnClickListener(new View.OnClickListener() {
            boolean liked = false;
            @Override
            public void onClick(View v) {
                Log.i("likeTweet", "onClick: clicked");

                // check if the tweet is already liked
                if (!liked) {
                    // Make an API call to Twitter to like the tweet
                    client.likeTweet(id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess: like tweet");
                            binding.ivLike.setSelected(true);
                            liked = true;
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure: like tweet", throwable);
                            liked = true;
                        }
                    });
                } else {
                    // Make an API call to Twitter to like the tweet
                    client.unlikeTweet(id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess: unlike tweet");
                            binding.ivLike.setSelected(false);
                            liked = false;
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure: unlike tweet", throwable);
                            liked = false;
                        }
                    });
                }
            }
        });

        binding.ivRetweet.setOnClickListener(new View.OnClickListener() {
            boolean reTweeted = false;
            @Override
            public void onClick(View v) {
                if (!reTweeted) {
                    client.reTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess: retweet");
                            binding.ivRetweet.setSelected(true);
                            reTweeted = true;
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure: retweet", throwable);
                            reTweeted = true;
                        }
                    });
                } else {
                    client.unRetweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess: unRetweet");
                            binding.ivRetweet.setSelected(false);
                            reTweeted = false;
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure: unRetweet", throwable);
                            reTweeted = false;
                        }
                    });
                }
            }
        });

        binding.ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("edttext", tweet.user.screenName);
                FragmentManager fm = getSupportFragmentManager();
                ComposeTweetDialogFragment composeTweetDialogFragment = ComposeTweetDialogFragment.newInstance("Some Title");
                composeTweetDialogFragment.show(fm, "fragment_compose_tweet");
                composeTweetDialogFragment.setArguments(bundle);
                Log.i("view holder", "onClick: ");
            }
        });
    }

    int radius = 30; // corner radius, higher value = more rounded
    int margin = 10; // crop margin, set to 0 for corners with no crop
    public void bind(Tweet tweet) {
        binding.tvBody.setText(tweet.body);
        binding.tvScreenName.setText(tweet.user.name);
        binding.etReply.setText("@" + tweet.user.screenName + "  ");

        Glide.with(DetailActivity.this).load(tweet.user.profileImageUrl)
                .transform(new RoundedCornersTransformation(radius, margin)).into(binding.ivProfileImage);
        Glide.with(DetailActivity.this).load(tweet.mediaUrl)
                    .into(binding.ivMediaPhoto);


    }
}