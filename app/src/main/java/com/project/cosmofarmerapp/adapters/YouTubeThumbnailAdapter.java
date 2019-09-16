package com.project.cosmofarmerapp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.R;
import com.project.cosmofarmerapp.YoutubeActivity;
import com.project.cosmofarmerapp.services.Config;

import java.util.List;

public class YouTubeThumbnailAdapter extends RecyclerView.Adapter<YouTubeThumbnailAdapter.YouTubeThumbnailViewHolder> {

    private static final String TAG = "YouTubeThumbnailAdapter";
    private Context context;
    private Fragment fragment;
    private List<JsonObject> dataList;

    ProgressDialog mDialog;

    public YouTubeThumbnailAdapter(Context context, Fragment fragment, List<JsonObject> dataList) {
        this.context = context;
        this.fragment = fragment;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public YouTubeThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_youtube_thumbnail, viewGroup, false);

        return new YouTubeThumbnailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YouTubeThumbnailViewHolder youTubeThumbnailViewHolder, int i) {
        JsonObject item = dataList.get(i);
        final String videoId = item.get("videoId").getAsString();
        youTubeThumbnailViewHolder.videoNameField.setText(item.get("name").getAsString());

        youTubeThumbnailViewHolder.thumbnailView.initialize(Config.YOUTUBE_APP_ID, new YouTubeThumbnailView.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
                youTubeThumbnailLoader.setVideo(videoId);
                Log.d(TAG, "onInitializationSuccess: Video Set.");
                //youTubeThumbnailLoader.release();
            }

            @Override
            public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });

        youTubeThumbnailViewHolder.thumbnailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, YoutubeActivity.class);
                intent.putExtra("videoId", videoId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class YouTubeThumbnailViewHolder extends RecyclerView.ViewHolder {

        YouTubeThumbnailView thumbnailView;
        TextView videoNameField;

        public YouTubeThumbnailViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbnailView = itemView.findViewById(R.id.thumbnail_view);
            videoNameField = itemView.findViewById(R.id.video_name);
        }
    }
}
