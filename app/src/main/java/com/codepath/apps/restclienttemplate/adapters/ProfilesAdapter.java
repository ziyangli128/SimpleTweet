package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class ProfilesAdapter extends RecyclerView.Adapter<ProfilesAdapter.ViewHolder> {
    public static final String TAG = "TweetAdapter";

    Context context;
    List<User> followers;

    TwitterClient client;

    // pass in the context and list of tweets
    public ProfilesAdapter(Context context, List<User> followers) {
        this.context = context;
        this.followers = followers;
    }

    // for each row, inflate the layout
    @NonNull
    @Override
    public ProfilesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false);
        return new ProfilesAdapter.ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ProfilesAdapter.ViewHolder holder, int position) {
        // get the data at position
        User follower = followers.get(position);
        // bind the tweet with the view holder
        holder.bind(follower);
    }

    @Override
    public int getItemCount() {
        return followers.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        followers.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<User> followersList) {
        followers.addAll(followersList);
        notifyDataSetChanged();
    }


    // Define a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivProfileImage;
        TextView tvScreenName;
        TextView tvName;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvName = itemView.findViewById(R.id.tvName);

            // set a click listener to get to detail page
            itemView.setOnClickListener(this);

        }

        int radius = 30; // corner radius, higher value = more rounded
        int margin = 10; // crop margin, set to 0 for corners with no crop
        public void bind(final User follower) {
            tvScreenName.setText(follower.name);
            tvName.setText(follower.screenName);

            final long id = follower.id;

            Glide.with(context).load(follower.profileImageUrl)
                    .transform(new RoundedCornersTransformation(radius, margin)).into(ivProfileImage);
        }

        @Override
        public void onClick(View v) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet clicked on
                User follower = followers.get(position);
                Intent i = new Intent(context, ProfileActivity.class);
                // serialize the movie using parceler, use its short name as a key
                i.putExtra("user", Parcels.wrap(follower));
                context.startActivity(i);
            }

        }
    }
}
