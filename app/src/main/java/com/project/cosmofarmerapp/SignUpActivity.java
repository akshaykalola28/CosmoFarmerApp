package com.project.cosmofarmerapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    EditText fnameField, lnameField, emailField, passField, cpassField, mobileField, addressField, dobField, bloodField;
    Button submitDataButton;

    String fname, lname, email, pass, cpass, address, mobileString, dobString, bloodGroup;
    private int mYear, mMonth, mDay;

    ProgressDialog mDialog;
    APIServices services;

    //Location Instance
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        changeStatusBarColor();

        services = APIClient.getClient().create(APIServices.class);

        fnameField = findViewById(R.id.input_fname);
        lnameField = findViewById(R.id.input_lname);
        emailField = findViewById(R.id.input_email);
        passField = findViewById(R.id.input_password);
        cpassField = findViewById(R.id.confirm_password);
        mobileField = findViewById(R.id.mo_number);
        addressField = findViewById(R.id.input_address);
        dobField = findViewById(R.id.select_dob);
        submitDataButton = findViewById(R.id.btn_signup);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        dobField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    selectDateDialog();
                }
            }
        });
        dobField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDateDialog();
            }
        });

        submitDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getValidData()) {
                    mDialog = new ProgressDialog(SignUpActivity.this);
                    mDialog.setMessage("Please Wait...");
                    mDialog.show();
                    submitData();
                }
            }
        });

        initLocationData();
    }

    private void initLocationData() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(SignUpActivity.this, Locale.getDefault());

                    try {
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                        addressField.setText(String.format("%s, %s, %s", city, state, postalCode));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            reqPermission();
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void reqPermission() {
        ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
    }

    private void submitData() {
        JsonObject user = new JsonObject();
        user.addProperty("fname", fname);
        user.addProperty("lname", lname);
        user.addProperty("email", email);
        user.addProperty("password", pass);
        user.addProperty("phone", mobileString);
        user.addProperty("address", address);
        user.addProperty("DOB", dobString);


        Call<JsonObject> call = services.registerUser(user);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject jsonResponse = response.body();
                    String status = jsonResponse.get("response").getAsString();
                    if (status.equals("success")) {
                        mDialog.dismiss();
                        Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content),
                                "Registration Successfully.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGIN", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(new Intent(SignUpActivity.this, LogInActivity.class));
                                        finish();
                                    }
                                }).show();
                    } else if (status.equals("dup")) {
                        mDialog.dismiss();
                        Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content),
                                "User Already Exists.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGIN", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(new Intent(SignUpActivity.this, LogInActivity.class));
                                        finish();
                                    }
                                }).show();
                    } else {
                        mDialog.dismiss();
                        Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content),
                                "Something is Wrong. Please, Try Again.", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    mDialog.dismiss();
                    Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content),
                            "Something is Wrong. Please, Try Again.", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                mDialog.dismiss();
                Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content),
                        "Something is Wrong. Please, Try Again.", Snackbar.LENGTH_SHORT).show();
                call.cancel();
            }
        });
    }

    private void selectDateDialog() {

        DatePickerDialog datePickerDialog = new DatePickerDialog(SignUpActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mYear = year;
                        mMonth = month;
                        mDay = dayOfMonth;

                        dobField.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        dobField.setError(null);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void onClickLogin(View view) {
        startActivity(new Intent(this, LogInActivity.class));
        finish();
    }

    private boolean getValidData() {
        boolean valid = false;
        fname = fnameField.getText().toString().trim();
        lname = lnameField.getText().toString().trim();
        email = emailField.getText().toString().trim();
        pass = passField.getText().toString().trim();
        cpass = cpassField.getText().toString().trim();
        mobileString = mobileField.getText().toString().trim();
        address = addressField.getText().toString().trim();
        dobString = dobField.getText().toString().trim();

        if (fname.isEmpty()) {
            fnameField.setError("Enter Name");
            fnameField.requestFocus();
        } else if (lname.isEmpty()) {
            lnameField.setError("Enter Name");
            lnameField.requestFocus();
        } else if (pass.isEmpty()) {
            passField.setError("Enter Password");
            passField.requestFocus();
        } else if (cpass.isEmpty() || !pass.equals(cpass)) {
            cpassField.setError("Password are not match");
            cpassField.requestFocus();
        } else if (mobileString.isEmpty() || mobileString.length() != 10) {
            mobileField.setError("Enter valid Mobile Number");
            mobileField.requestFocus();
        } else if (address.isEmpty()) {
            addressField.setError("Enter Address");
            addressField.requestFocus();
        } else if (dobString.isEmpty()) {
            dobField.setError("Select DOB");
            dobField.requestFocus();
        } else {
            valid = true;
        }
        return valid;
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
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
