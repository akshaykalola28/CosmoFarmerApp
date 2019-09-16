package com.project.cosmofarmerapp;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.adapters.YouTubeThumbnailAdapter;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrganicFarmingFragment extends Fragment {

    private static final String TAG = "OrganicFarmingFragment";
    View mainView;

    RecyclerView thumbnailRecyclerView;
    List<JsonObject> dataList;
    APIServices services;
    ProgressBar loading;

    YouTubeThumbnailAdapter mAdapter;

    public OrganicFarmingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_organic_farming, container, false);
        loading = mainView.findViewById(R.id.loading_video);

        thumbnailRecyclerView = mainView.findViewById(R.id.thumbnail_recycler_view);
        thumbnailRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        thumbnailRecyclerView.setLayoutManager(layoutManager);
        dataList = new ArrayList<>();
        fetchVideoList();

        return mainView;
    }

    private void fetchVideoList() {
        services = APIClient.getClient().create(APIServices.class);
        Call<List<JsonObject>> call = services.getOrganicFramingVideoList();
        call.enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.code() == 204) {
                    Snackbar.make(mainView, "Video not Available. Please Come back after Sometimes.",
                            Snackbar.LENGTH_SHORT).show();
                    loading.setVisibility(View.GONE);
                } else if (response.isSuccessful()) {
                    dataList = response.body();
                    mAdapter = new YouTubeThumbnailAdapter(getContext(), OrganicFarmingFragment.this, dataList);
                    thumbnailRecyclerView.setAdapter(mAdapter);
                    loading.setVisibility(View.GONE);
                } else {
                    loading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {

            }
        });
    }

}
