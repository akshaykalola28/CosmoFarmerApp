package com.project.cosmofarmerapp.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.ForecastFragment;
import com.project.cosmofarmerapp.R;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;
import com.project.cosmofarmerapp.services.Config;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherLocationAdapter extends RecyclerView.Adapter<WeatherLocationAdapter.LocationViewHolder> {

    private Context context;
    private Fragment fragment;
    private List<JsonObject> items;

    private ForecastAdapter forecastAdapter;
    APIServices services;

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
        final JsonObject item = items.get(i);

        final String landName = item.get("landName").getAsString();
        final String address = item.get("location").getAsJsonObject().get("address").getAsString();
        double lat = item.get("location").getAsJsonObject().get("lat").getAsDouble();
        double lon = item.get("location").getAsJsonObject().get("lon").getAsDouble();
        locationViewHolder.locationLabel.setText("\"" + landName + "\"" + "" + ", " + address);

        services = APIClient.getWeatherClient().create(APIServices.class);

        Call<JsonObject> weatherCall = services.getWeather(lat, lon, Config.WEATHER_APP_ID);
        weatherCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject jsonResponse = response.body();
                String cityName = jsonResponse.get("name").getAsString();
                JsonObject jsonWeather = jsonResponse.get("weather").getAsJsonArray().get(0).getAsJsonObject();
                String weather = jsonWeather.get("main").getAsString();
                String weatherDesc = jsonWeather.get("description").getAsString();
                String iconId = jsonWeather.get("icon").getAsString();
                JsonObject jsonMain = jsonResponse.get("main").getAsJsonObject();
                String tempString = jsonMain.get("temp").getAsString();
                double temp = Double.parseDouble(tempString) - 273.15;

                locationViewHolder.dateField.setText(landName); //Set Land Name Only
                locationViewHolder.tempField.setText(new DecimalFormat("###.#").format(temp));
                locationViewHolder.weatherField.setText(weather);
                locationViewHolder.descField.setText(weatherDesc);
                Picasso.with(context).load("https://openweathermap.org/img/wn/" + iconId + ".png").into(locationViewHolder.loadImage);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

        locationViewHolder.weatherCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment forecastFragment = new ForecastFragment();
                Bundle bundle = new Bundle();
                bundle.putString("landData", item.toString());
                forecastFragment.setArguments(bundle);
                fragment.getFragmentManager().beginTransaction()
                        .replace(R.id.navigation_frame, forecastFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class LocationViewHolder extends RecyclerView.ViewHolder {

        CardView weatherCard;
        TextView dateField, tempField, weatherField, descField, locationLabel;
        ImageView loadImage;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);

            weatherCard = itemView.findViewById(R.id.weather_card);
            locationLabel = itemView.findViewById(R.id.location_label);
            dateField = itemView.findViewById(R.id.forecast_date);
            tempField = itemView.findViewById(R.id.temp_text_view);
            weatherField = itemView.findViewById(R.id.weather);
            descField = itemView.findViewById(R.id.weather_desc);
            loadImage = itemView.findViewById(R.id.load_image);
        }
    }
}
