package com.project.cosmofarmerapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.adapters.LandAdapter;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyLandFragment extends Fragment {

    View mainView;
    LandAdapter mAdapter;
    List<JsonObject> landList;
    JSONObject userDataJson;
    RecyclerView landRecyclerView;
    ProgressBar loading;

    APIServices services;

    public MyLandFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_my_land, container, false);

        services = APIClient.getClient().create(APIServices.class);

        userDataJson = ((NavigationActivity) getActivity()).getUser();

        loading = mainView.findViewById(R.id.loading_my_land);
        landRecyclerView = mainView.findViewById(R.id.my_land_recyclerview);
        landRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        landRecyclerView.setLayoutManager(linearLayoutManager);
        landList = new ArrayList<>();

        try {
            fetchLandList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mainView;
    }

    private void fetchLandList() throws JSONException {
        Call<List<JsonObject>> call = services.getLandList(userDataJson.getString("phone"));
        call.enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.code() == 204) {
                    Snackbar.make(mainView, "Land not Available.", Snackbar.LENGTH_SHORT).show();
                    loading.setVisibility(View.GONE);
                } else if (response.isSuccessful()) {
                    landList = response.body();
                    mAdapter = new LandAdapter(getContext(), MyLandFragment.this, landList);
                    landRecyclerView.setAdapter(mAdapter);
                    loading.setVisibility(View.GONE);
                } else {
                    loading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                loading.setVisibility(View.GONE);
            }
        });
    }

}
