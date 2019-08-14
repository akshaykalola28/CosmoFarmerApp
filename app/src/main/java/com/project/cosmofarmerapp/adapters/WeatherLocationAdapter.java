package com.project.cosmofarmerapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.R;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;
import com.project.cosmofarmerapp.services.Config;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherLocationAdapter extends RecyclerView.Adapter<WeatherLocationAdapter.LocationViewHolder> {

    private Context context;
    private Fragment fragment;
    private List<JsonObject> items;

    private ForecastAdapter forecastAdapter;

    public WeatherLocationAdapter(Context context, Fragment fragment, List<JsonObject> items) {
        this.context = context;
        this.fragment = fragment;
        this.items = items;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_weather_setup, viewGroup, false);

        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final LocationViewHolder locationViewHolder, int i) {
        JsonObject item = items.get(i);

        String landName = item.get("landName").getAsString();
        String address = item.get("location").getAsJsonObject().get("address").getAsString();
        double lat = item.get("location").getAsJsonObject().get("lat").getAsDouble();
        double lon = item.get("location").getAsJsonObject().get("lon").getAsDouble();
        locationViewHolder.locationLabel.setText("\"" + landName + "\"" + "" + ", " + address);

        locationViewHolder.forecastView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        locationViewHolder.forecastView.setLayoutManager(linearLayoutManager);
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
                    forecastAdapter = new ForecastAdapter(context, fragment, forecastList);
                    locationViewHolder.forecastView.setAdapter(forecastAdapter);
                    Log.d("FORECAST", forecastList.toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class LocationViewHolder extends RecyclerView.ViewHolder {

        TextView locationLabel;
        RecyclerView forecastView;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);

            locationLabel = itemView.findViewById(R.id.location_label);
            forecastView = itemView.findViewById(R.id.forecast_recyclerview);
        }
    }
}
