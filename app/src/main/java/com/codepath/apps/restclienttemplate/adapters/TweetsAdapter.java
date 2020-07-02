package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.activities.DetailActivity;
import com.codepath.apps.restclienttemplate.activities.ProfileActivity;
import com.codepath.apps.restclienttemplate.activities.TimelineActivity;
import com.codepath.apps.restclienttemplate.fragmens.ComposeTweetDialogFragment;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    public static final String TAG = "TweetAdapter";

    Context context;
    List<Tweet> tweets;

    TwitterClient client;
    //private final int REQUEST_CODE = 20;

    // pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    // for each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get the data at position
        Tweet tweet = tweets.get(position);
        // bind the tweet with the view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }


    // Define a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivProfileImage;
        ImageView ivMediaPhoto;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvRelativeTime;
        ImageView ivReply;
        ImageView ivLike;
        ImageView ivRetweet;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            ivMediaPhoto = itemView.findViewById(R.id.ivMediaPhoto);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvRelativeTime = itemView.findViewById(R.id.tvRelativeTime);
            ivReply = itemView.findViewById(R.id.ivReply);
            ivLike = itemView.findViewById(R.id.ivLike);
            ivRetweet = itemView.findViewById(R.id.ivRetweet);

            // set a click listener to get to detail page
            itemView.setOnClickListener(this);

        }

        int radius = 30; // corner radius, higher value = more rounded
        int margin = 10; // crop margin, set to 0 for corners with no crop
        public void bind(final Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.name);
            tvRelativeTime.setText(getRelativeTimeAgo(tweet.createdAt));

            final long id = tweet.id;

            Glide.with(context).load(tweet.user.profileImageUrl)
                    .transform(new RoundedCornersTransformation(radius, margin)).into(ivProfileImage);
            if (tweet.mediaUrl != null) {
                Glide.with(context).load(tweet.mediaUrl)
                        .into(ivMediaPhoto);
            }

            ivReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("edttext", tweet.user.screenName);
                    FragmentManager fm = ((TimelineActivity) context).getSupportFragmentManager();
                    ComposeTweetDialogFragment composeTweetDialogFragment = ComposeTweetDialogFragment.newInstance("Some Title");
                    composeTweetDialogFragment.show(fm, "fragment_compose_tweet");
                    composeTweetDialogFragment.setArguments(bundle);
                    Log.i("view holder", "onClick: ");
                }
            });

            ivProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ProfileActivity.class);
                    // serialize the movie using parceler, use its short name as a key
                    i.putExtra("user", Parcels.wrap(tweet.user));
                    context.startActivity(i);
                }
            });

            client = TwitterApp.getRestClient(context);

            ivLike.setOnClickListener(new View.OnClickListener() {
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
                                ivLike.setSelected(true);
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
                                ivLike.setSelected(false);
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

            ivRetweet.setOnClickListener(new View.OnClickListener() {
                boolean reTweeted = false;
                @Override
                public void onClick(View v) {
                    if (!reTweeted) {
                        client.reTweet(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i(TAG, "onSuccess: retweet");
                                ivRetweet.setSelected(true);
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
                                ivRetweet.setSelected(false);
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
        }

        @Override
        public void onClick(View v) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet clicked on
                Tweet tweet = tweets.get(position);
                Intent i = new Intent(context, DetailActivity.class);
                // serialize the movie using parceler, use its short name as a key
                i.putExtra("tweet", Parcels.wrap(tweet));
                context.startActivity(i);
            }

        }
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }
}
