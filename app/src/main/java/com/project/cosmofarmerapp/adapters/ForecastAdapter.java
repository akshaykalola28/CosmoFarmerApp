package com.project.cosmofarmerapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private Context context;
    private Fragment fragment;
    private List<JsonObject> items;

    public ForecastAdapter(Context context, Fragment fragment, List<JsonObject> items) {
        this.context = context;
        this.fragment = fragment;
        this.items = items;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_forecast, viewGroup, false);

        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ForecastViewHolder forecastViewHolder, int i) {
        JsonObject item = items.get(i);

        String date = item.get("dt_txt").getAsString();
        JsonObject jsonWeather = item.get("weather").getAsJsonArray().get(0).getAsJsonObject();
        String weather = jsonWeather.get("main").getAsString();
        String weatherDesc = jsonWeather.get("description").getAsString();
        String iconId = jsonWeather.get("icon").getAsString();
        JsonObject jsonMain = item.get("main").getAsJsonObject();
        String tempString = jsonMain.get("temp").getAsString();
        double temp = Double.parseDouble(tempString) - 273.15;

        forecastViewHolder.dateField.setText(date);
        forecastViewHolder.tempField.setText(new DecimalFormat("###.#").format(temp));
        forecastViewHolder.weatherField.setText(weather);
        forecastViewHolder.descField.setText(weatherDesc);
        Picasso.with(context).load("https://openweathermap.org/img/wn/" + iconId + ".png").into(forecastViewHolder.loadImage);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ForecastViewHolder extends RecyclerView.ViewHolder {

        TextView dateField, tempField, weatherField, descField;
        ImageView loadImage;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);

            dateField = itemView.findViewById(R.id.forecast_date);
            tempField = itemView.findViewById(R.id.temp_text_view);
            weatherField = itemView.findViewById(R.id.weather);
            descField = itemView.findViewById(R.id.weather_desc);
            loadImage = itemView.findViewById(R.id.load_image);
        }
    }
}
