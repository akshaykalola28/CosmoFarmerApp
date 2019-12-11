package com.project.cosmofarmerapp;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("SetTextI18n")
public class AddCropFragment extends Fragment {

    private static final String TAG = "AddCropFragment";
    View mainView;
    EditText dateSelectField, cropNameField, landAreaField, quantityField;
    TextView totalLandField, availLandField;
    Spinner landSpinner;
    Button addCropButton;
    ProgressDialog mDialog;

    APIServices services;

    List<JsonObject> landList;
    JSONObject userDataJson;
    String cropName, landArea, quantity, expectedDate, landKeyId;
    double cropLand, availableLand, totalLand;
    private int mYear, mMonth, mDay;
    boolean isForUpdate = false;
    String landIdForUpdate, keyIdForUpdate;

    public AddCropFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_add_crop, container, false);

        userDataJson = ((NavigationActivity) getActivity()).getUser();

        if (getArguments() != null && getArguments().getBoolean("isForUpdate")) {
            Log.d(TAG, "onCreateView: Fragment is used for Update: " + getArguments());
            isForUpdate = true;
        }

        services = APIClient.getClient().create(APIServices.class);

        cropNameField = mainView.findViewById(R.id.input_crop_name);
        landAreaField = mainView.findViewById(R.id.input_land_area);
        quantityField = mainView.findViewById(R.id.input_quantity);
        dateSelectField = mainView.findViewById(R.id.date_select);
        addCropButton = mainView.findViewById(R.id.btn_add_crop);
        totalLandField = mainView.findViewById(R.id.total_land_area);
        availLandField = mainView.findViewById(R.id.available_land_area);
        landSpinner = mainView.findViewById(R.id.land_spinner);

        if (isForUpdate) {
            Gson gson = new Gson();
            JsonObject cropData = gson.fromJson(getArguments().getString("cropData"), JsonObject.class);

            cropNameField.setText(cropData.get("cropName").getAsString());
            landAreaField.setText(cropData.get("cropArea").getAsString());
            quantityField.setText(cropData.get("quantity").getAsString());
            dateSelectField.setText(cropData.get("date").getAsString());
            landIdForUpdate = cropData.get("landKeyId").getAsString();
            keyIdForUpdate = cropData.get("keyId").getAsString();

            TextView addCropHeader = mainView.findViewById(R.id.header_add_crop);
            addCropHeader.setText("Update Crop");
            addCropButton.setText("Update Crop");
        }

        try {
            mDialog = new ProgressDialog(getContext());
            mDialog.setMessage("Please Wait...");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
            fetchLandList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        dateSelectField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    selectDateDialog();
                }
            }
        });
        dateSelectField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDateDialog();
            }
        });

        addCropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getValidData()) {
                    mDialog.show();
                    addCrop();
                }
            }
        });

        return mainView;
    }

    private void addCrop() {
        JsonObject crop = new JsonObject();
        try {
            crop.addProperty("username", userDataJson.getString("phone"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        crop.addProperty("cropName", cropName);
        crop.addProperty("cropArea", landArea);
        crop.addProperty("date", expectedDate);
        crop.addProperty("quantity", quantity);
        crop.addProperty("landKeyId", landKeyId);

        Call<JsonObject> call;
        if (isForUpdate) {
            crop.addProperty("keyId", keyIdForUpdate);
            call = services.updateCrop(crop);
        } else {
            call = services.addCrop(crop);
        }
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject jsonResponse = response.body();
                    if (jsonResponse.get("response").getAsString().equals("success")) {
                        mDialog.dismiss();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setMessage(jsonResponse.get("data").getAsString())
                                .setPositiveButton("Add New", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        getFragmentManager().beginTransaction().replace(R.id.navigation_frame, new AddCropFragment()).commit();
                                    }
                                })
                                .setNegativeButton("Home", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getFragmentManager().beginTransaction().replace(R.id.navigation_frame, new HomeFragment()).commit();
                                    }
                                }).show();
                    } else {
                        mDialog.dismiss();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setMessage(jsonResponse.get("data").getAsString())
                                .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        getFragmentManager().beginTransaction().replace(R.id.navigation_frame, new HomeFragment()).commit();
                                    }
                                }).show();
                    }
                } else {
                    mDialog.dismiss();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage("Something is Wrong.")
                            .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    getFragmentManager().beginTransaction().replace(R.id.navigation_frame, new HomeFragment()).commit();
                                }
                            }).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                mDialog.dismiss();
                call.cancel();
            }
        });
    }

    private void fetchLandList() throws JSONException {
        Call<List<JsonObject>> call = services.getLandList(userDataJson.getString("phone"));
        call.enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.code() == 204) {
                    Snackbar.make(mainView, "Land not Available.", Snackbar.LENGTH_SHORT).show();
                    mDialog.dismiss();
                } else if (response.isSuccessful()) {
                    mDialog.dismiss();
                    landList = response.body();
                    initLandSpinner();
                } else {
                    mDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                mDialog.dismiss();
                Snackbar.make(mainView, "Something is Wrong! Please Try Again...", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void initLandSpinner() {

        List<String> landName = new ArrayList<>();
        for (int i = 0; i < landList.size(); i++) {
            JsonObject land = landList.get(i).getAsJsonObject();
            landName.add(land.get("landName").getAsString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, landName);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        landSpinner.setAdapter(adapter);

        landSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                JsonObject land = landList.get(position).getAsJsonObject();
                availableLand = land.get("availableLand").getAsDouble();
                totalLand = land.get("totalLand").getAsDouble();
                landKeyId = land.get("keyId").getAsString();

//                totalLandField.setText(R.string.total_land_area +" : "+totalLand);
                availLandField.setText((int) (R.string.available_land_area + availableLand));
                if (!isForUpdate) {
                    landAreaField.setText(String.valueOf(availableLand));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (isForUpdate) {
            for (int i = 0; i < landList.size(); i++) {
                JsonObject land = landList.get(i).getAsJsonObject();
                if (land.get("keyId").getAsString().equals(landIdForUpdate)) {
                    landSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private boolean getValidData() {
        cropName = cropNameField.getText().toString().trim();
        landArea = landAreaField.getText().toString().trim();
        expectedDate = dateSelectField.getText().toString().trim();
        quantity = quantityField.getText().toString().trim();

        if (!landArea.equals("")) {
            cropLand = Double.parseDouble(landArea);
        }

        if (cropName.equals("")) {
            cropNameField.setError("Enter Crop Name");
            cropNameField.requestFocus();
        } else if (landKeyId == null || landKeyId.equals("")) {
            /*TextView errorView = (TextView) landSpinner.getSelectedView();
            errorView.setError("Select Land");
            errorView.setTextColor(Color.RED);
            errorView.setText("Select Land");
            errorView.requestFocus();*/
            Toast.makeText(getContext(), "Select Land", Toast.LENGTH_SHORT).show();
        } else if (landArea.equals("")) {
            landAreaField.setError("Enter Crop Area");
            landAreaField.requestFocus();
        } else if (cropLand > availableLand || cropLand <= 0.0) {
            landAreaField.setError("Insufficient Area");
            landAreaField.requestFocus();
        } else if (expectedDate.equals("")) {
            dateSelectField.setError("Select Date");
            dateSelectField.requestFocus();
        } else if (quantity.equals("")) {
            quantityField.setError("Enter Quantity");
            quantityField.requestFocus();
        } else {
            return true;
        }
        return false;
    }

    private void selectDateDialog() {

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mYear = year;
                        mMonth = month;
                        mDay = dayOfMonth;

                        dateSelectField.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        dateSelectField.setError(null);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
}
