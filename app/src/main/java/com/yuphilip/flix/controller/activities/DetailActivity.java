package com.yuphilip.flix.controller.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.yuphilip.flix.R;
import com.yuphilip.flix.model.Constant;
import com.yuphilip.flix.model.Movie;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class DetailActivity extends YouTubeBaseActivity {

    private static final String YOUTUBE_API_KEY = Constant.API_KEY;
    public static final String VIDEOS_URL = "https://api.themoviedb.org/3/movie/%d/videos?api_key=" + YOUTUBE_API_KEY;

    TextView tvTitle;
    TextView tvOverview;
    RatingBar ratingBar;
    YouTubePlayerView youTubePlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tvTitle = findViewById(R.id.tvTitle);
        tvOverview = findViewById(R.id.tvOverview);
        ratingBar = findViewById(R.id.ratingBar);
        youTubePlayerView = findViewById(R.id.player);

        final Movie movie = Parcels.unwrap(getIntent().getParcelableExtra("movie"));

        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());
        ratingBar.setRating((float) movie.getRating());

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(String.format(VIDEOS_URL, movie.getMovieId()), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    JSONArray results = json.jsonObject.getJSONArray("results");

                    if (results.length() == 0) {
                        return;
                    }
                    String youtubeKey = results.getJSONObject(0).getString("key");
                    Log.d("DetailActivity", youtubeKey);
                    initializeYoutube(youtubeKey, movie.getRating());
                } catch (JSONException e) {
                    Log.e("DetailActivity", "Failed to parse JSON.");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("DetailActivity", "Could not fetch video...");
                Log.d("YouTube", response);
            }
        });
    }

    private void initializeYoutube(final String youtubeKey, final double rating) {
        youTubePlayerView.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                double ratingThreshold = Constant.RATING_THRESHOLD;

                Log.d("DetailActivity", "onInitializationSuccess");

                if (rating < ratingThreshold) {
                    Log.d("YouTube", "Rating is below " + ratingThreshold + ". Display image preview.");
                    youTubePlayer.cueVideo(youtubeKey);
                } else {
                    Log.d("YouTube", "Rating is above " + ratingThreshold + ". Play video.");
                    youTubePlayer.loadVideo(youtubeKey);
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.d("DetailActivity", "onInitializationFailure");
            }
        });
    }

}
