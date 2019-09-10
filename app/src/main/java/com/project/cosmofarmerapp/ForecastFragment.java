package com.project.cosmofarmerapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.adapters.ForecastAdapter;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;
import com.project.cosmofarmerapp.services.Config;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {

    View mainView;
    TextView locationLabel;
    RecyclerView forecastView;

    public ForecastAdapter forecastAdapter;

    public ForecastFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_forecast, container, false);

        locationLabel = mainView.findViewById(R.id.location_label);
        forecastView = mainView.findViewById(R.id.forecast_recyclerview);

        setForecastCard();
        return mainView;
    }

    private void setForecastCard() {
        Gson gson = new Gson();
        Bundle bundle = this.getArguments();
        try {
            String data = bundle.getString("landData");
            JsonObject item = gson.fromJson(data, JsonObject.class);
            String landName = item.get("landName").getAsString();
            String address = item.get("location").getAsJsonObject().get("address").getAsString();
            double lat = item.get("location").getAsJsonObject().get("lat").getAsDouble();
            double lon = item.get("location").getAsJsonObject().get("lon").getAsDouble();
            locationLabel.setText("\"" + landName + "\"" + "" + ", " + address);

            forecastView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            forecastView.setLayoutManager(linearLayoutManager);
            final List<JsonObject> forecastList = new ArrayList<>();

            APIServices services = APIClient.getWeatherClient().create(APIServices.class);
            Call<JsonObject> weatherCall = services.getForecast(lat, lon, Config.WEATHER_APP_ID);
            weatherCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        JsonArray jsonListArray = response.body().get("list").getAsJsonArray();
                        for (int i = 0; i < jsonListArray.size(); i += 8) {
                            forecastList.add(jsonListArray.get(i).getAsJsonObject());
                        }
                        forecastAdapter = new ForecastAdapter(getActivity(), ForecastFragment.this, forecastList);
                        forecastView.setAdapter(forecastAdapter);
                        Log.d("FORECAST", forecastList.toString());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
