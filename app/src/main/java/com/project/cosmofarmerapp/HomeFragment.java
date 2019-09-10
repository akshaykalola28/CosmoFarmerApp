package com.project.cosmofarmerapp;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.TooltipCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.adapters.WeatherLocationAdapter;
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
public class HomeFragment extends Fragment {

    RecyclerView weatherRecycler;
    View mainView;
    Context mContext;

    APIServices services;
    JSONObject userDataJson;
    List<JsonObject> locationList;
    WeatherLocationAdapter mAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_home, container, false);

        mContext = getActivity();
        userDataJson = ((NavigationActivity) getActivity()).getUser();

        setWeatherRecycler();

        FloatingActionButton addCropFab = mainView.findViewById(R.id.add_crop_fab);
        TooltipCompat.setTooltipText(addCropFab, "Add Crop");
        addCropFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.navigation_frame, new AddCropFragment()).commit();
            }
        });

        FloatingActionButton addLandFab = mainView.findViewById(R.id.add_land_fab);
        TooltipCompat.setTooltipText(addLandFab, "Add Land");
        addLandFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.navigation_frame, new AddLandFragment()).commit();
            }
        });

        return mainView;
    }

    private void setWeatherRecycler() {
        weatherRecycler = mainView.findViewById(R.id.weather_recyclerview);
        weatherRecycler.setHasFixedSize(true);
        weatherRecycler.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        weatherRecycler.setLayoutManager(linearLayoutManager);
        locationList = new ArrayList<>();

        try {
            fetchLandList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchLandList() throws JSONException {
        services = APIClient.getClient().create(APIServices.class);

        Call<List<JsonObject>> call = services.getLandList(userDataJson.getString("phone"));
        call.enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.code() == 204) {
                    Snackbar.make(mainView, "Land not Available.", Snackbar.LENGTH_SHORT).show();
                } else if (response.isSuccessful()) {
                    locationList = response.body();
                    mAdapter = new WeatherLocationAdapter(getContext(), HomeFragment.this, locationList);
                    weatherRecycler.setAdapter(mAdapter);
                } else {
                    Snackbar.make(mainView, "Land not Available.", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
            }
        });
    }
}
