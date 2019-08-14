package com.project.cosmofarmerapp;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.adapters.WeatherLocationAdapter;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;
import com.project.cosmofarmerapp.services.Config;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    View mainView;
    Context mContext;
    TextView cityNameField, tempField, weatherField, descField;
    ImageView loadImage;
    RecyclerView weatherRecycler;

    APIServices services;
    JSONObject userDataJson;
    List<JsonObject> locationList;
    WeatherLocationAdapter mAdapter;

    //Location Instance
    FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

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

        cityNameField = mainView.findViewById(R.id.city_name);
        tempField = mainView.findViewById(R.id.temp_text_view);
        weatherField = mainView.findViewById(R.id.weather);
        descField = mainView.findViewById(R.id.weather_desc);
        loadImage = mainView.findViewById(R.id.load_image);

        //getWeatherData();
        setWeatherRecycler();

        FloatingActionButton fab = mainView.findViewById(R.id.add_crop_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.navigation_frame, new AddCropFragment())
                        .addToBackStack(null).commit();
            }
        });

        return mainView;
    }

    private void setWeatherRecycler() {
        weatherRecycler = mainView.findViewById(R.id.weather_recyclerview);
        weatherRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
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
                    //loading.setVisibility(View.GONE);
                } else if (response.isSuccessful()) {
                    locationList = response.body();
                    mAdapter = new WeatherLocationAdapter(getContext(), HomeFragment.this, locationList);
                    weatherRecycler.setAdapter(mAdapter);
                    //loading.setVisibility(View.GONE);
                } else {
                    //loading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                //loading.setVisibility(View.GONE);
            }
        });
    }

    private void getWeatherData() {
        final CardView weatherCard = mainView.findViewById(R.id.weather_card);
        weatherCard.setVisibility(View.GONE);

        Location location = Config.currentLocation;
        if (location == null) {
            initLocationData();
            return;
        }
        services = APIClient.getWeatherClient().create(APIServices.class);

        Call<JsonObject> weatherCall = services.getWeather(location.getLatitude(), location.getLongitude(), Config.WEATHER_APP_ID);
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

                weatherCard.setVisibility(View.VISIBLE);
                cityNameField.setText(cityName);
                tempField.setText(new DecimalFormat("###.#").format(temp));
                weatherField.setText(weather);
                descField.setText(weatherDesc);

                Picasso.with(mContext).load("https://openweathermap.org/img/wn/" + iconId + ".png").into(loadImage);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }

    private void initLocationData() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Config.currentLocation = location;
                    getWeatherData();
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        getWeatherData();
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    }
                }
        }
    }
}
