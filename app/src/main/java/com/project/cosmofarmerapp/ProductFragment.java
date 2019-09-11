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
import com.project.cosmofarmerapp.adapters.ProductAdapter;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProductFragment extends Fragment {

    View mainView;
    RecyclerView productRecyclerView;

    ProductAdapter mAdapter;
    List<JsonObject> productList;
    APIServices services;
    ProgressBar loading;

    public ProductFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_product, container, false);

        loading = mainView.findViewById(R.id.loading_product);

        productRecyclerView = mainView.findViewById(R.id.product_recyclerview);
        productRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        productRecyclerView.setLayoutManager(layoutManager);
        productList = new ArrayList<>();

        getProductList();

        return mainView;
    }

    private void getProductList() {
        services = APIClient.getClient().create(APIServices.class);
        Call<List<JsonObject>> call = services.getProductList();
        call.enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.code() == 204) {
                    Snackbar.make(mainView, "Product not Available.", Snackbar.LENGTH_SHORT).show();
                    loading.setVisibility(View.GONE);
                } else if (response.isSuccessful()) {
                    productList = response.body();
                    mAdapter = new ProductAdapter(getContext(), ProductFragment.this, productList);
                    productRecyclerView.setAdapter(mAdapter);
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
