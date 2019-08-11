package com.project.cosmofarmerapp;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;
import com.project.cosmofarmerapp.services.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddLandFragment extends Fragment {

    View mainView;
    EditText landNameField, landAreaField, accountNumberField, surveyNumberField, locationField;
    Button addLandButton;

    JSONObject userDataJson;
    String landName, landArea, accountNumber, surveyNumber, locationString;
    Location mLocation;

    APIServices services;
    ProgressDialog mDialog;

    //Location Instance
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public AddLandFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_add_land, container, false);

        userDataJson = ((NavigationActivity) getActivity()).getUser();

        services = APIClient.getClient().create(APIServices.class);

        landNameField = mainView.findViewById(R.id.input_land_name);
        landAreaField = mainView.findViewById(R.id.input_land_area);
        accountNumberField = mainView.findViewById(R.id.input_account_number);
        surveyNumberField = mainView.findViewById(R.id.input_survey_number);
        locationField = mainView.findViewById(R.id.input_location);
        addLandButton = mainView.findViewById(R.id.btn_add_land);

        addLandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getValidData()) {
                    mDialog = new ProgressDialog(getContext());
                    mDialog.setMessage("Please Wait...");
                    mDialog.show();
                    submitLand();
                }
            }
        });

        initLocationData(getContext());

        return mainView;
    }

    private void submitLand() {
        JsonObject land = new JsonObject();
        try {
            land.addProperty("username", userDataJson.getString("phone"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        land.addProperty("landName", landName);
        land.addProperty("landArea", landArea);
        land.addProperty("accountNumber", accountNumber);
        land.addProperty("surveyNumber", surveyNumber);

        //TODO: Change it later
        mLocation = Config.currentLocation;
        JsonObject jsonLocation = new JsonObject();
        jsonLocation.addProperty("lat", mLocation.getLatitude());
        jsonLocation.addProperty("lag", mLocation.getLongitude());

        land.add("location", jsonLocation);

        Call<JsonObject> call = services.addLand(land);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject jsonResponse = response.body();
                    if (jsonResponse.get("response").getAsString().equals("success")) {
                        mDialog.dismiss();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setMessage(jsonResponse.get("data").getAsString())
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).show();
                    } else {
                        mDialog.dismiss();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setMessage(jsonResponse.get("data").getAsString())
                                .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).show();
                    }
                } else {
                    mDialog.dismiss();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage("Something is Wrong.")
                            .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }

    private boolean getValidData() {
        landName = landNameField.getText().toString().trim();
        landArea = landAreaField.getText().toString().trim();
        accountNumber = accountNumberField.getText().toString().trim();
        surveyNumber = surveyNumberField.getText().toString().trim();
        locationString = locationField.getText().toString().trim();

        if (landName.equals("")) {
            landNameField.setError("Enter Land Name");
            landNameField.requestFocus();
        } else if (landArea.equals("")) {
            landAreaField.setError("Enter Land Area");
            landAreaField.requestFocus();
        } else if (accountNumber.equals("")) {
            accountNumberField.setError("Enter Account Number");
            accountNumberField.requestFocus();
        } else if (surveyNumber.equals("")) {
            surveyNumberField.setError("Enter Survey Number");
            surveyNumberField.requestFocus();
        } else if (locationString.equals("")) {
            locationField.setError("Waiting for Location");
            locationField.requestFocus();
        } else {
            return true;
        }
        return false;
    }

    private void initLocationData(final Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    //addressField.setText(location.getLatitude() + "/" + location.getLongitude());
                    mLocation = location;
                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(context, Locale.getDefault());

                    try {
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                        //locationField.setText(String.format("%s, %s, %s", city, state, postalCode));
                        locationField.setText(address);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            reqPermission();
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void reqPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
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
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    }
                }
        }
    }
}
