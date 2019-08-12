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
import com.project.cosmofarmerapp.adapters.CropAdapter;
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
public class MyCropFragment extends Fragment {

    View mainView;
    CropAdapter mAdapter;
    List<JsonObject> cropList;
    JSONObject userDataJson;
    RecyclerView cropRecyclerView;
    ProgressBar loading;

    APIServices services;

    public MyCropFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_my_crop, container, false);

        services = APIClient.getClient().create(APIServices.class);

        userDataJson = ((NavigationActivity) getActivity()).getUser();

        loading = mainView.findViewById(R.id.loading_my_crop);
        cropRecyclerView = mainView.findViewById(R.id.my_crop_recyclerview);
        cropRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        cropRecyclerView.setLayoutManager(linearLayoutManager);
        cropList = new ArrayList<>();

        try {
            fetchCropList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mainView;
    }

    private void fetchCropList() throws JSONException {
        Call<List<JsonObject>> call = services.getCropList(userDataJson.getString("phone"));
        call.enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.code() == 204) {
                    Snackbar.make(mainView, "Crop not Available.", Snackbar.LENGTH_SHORT).show();
                    loading.setVisibility(View.GONE);
                } else if (response.isSuccessful()) {
                    cropList = response.body();
                    mAdapter = new CropAdapter(getContext(), MyCropFragment.this, cropList);
                    cropRecyclerView.setAdapter(mAdapter);
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
