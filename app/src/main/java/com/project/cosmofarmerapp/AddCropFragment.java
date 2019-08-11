package com.project.cosmofarmerapp;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddCropFragment extends Fragment {

    View mainView;
    EditText dateSelectField, cropNameField, landAreaField, quantityField;
    Button addCropButton;
    ProgressDialog mDialog;

    APIServices services;

    JSONObject userDataJson;
    String cropName, landArea, quantity, expectedDate;
    private int mYear, mMonth, mDay;

    public AddCropFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_add_crop, container, false);

        userDataJson = ((NavigationActivity) getActivity()).getUser();

        services = APIClient.getClient().create(APIServices.class);

        cropNameField = mainView.findViewById(R.id.input_crop_name);
        landAreaField = mainView.findViewById(R.id.input_land_area);
        quantityField = mainView.findViewById(R.id.input_quantity);
        dateSelectField = mainView.findViewById(R.id.date_select);
        addCropButton = mainView.findViewById(R.id.btn_add_crop);

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
                    mDialog = new ProgressDialog(getContext());
                    mDialog.setMessage("Please Wait...");
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
        crop.addProperty("name", cropName);
        crop.addProperty("area", landArea);
        crop.addProperty("date", expectedDate);
        crop.addProperty("quantity", quantity);

        Call<JsonObject> call = services.addCrop(crop);
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
                mDialog.dismiss();
                call.cancel();
            }
        });
    }

    private boolean getValidData() {
        cropName = cropNameField.getText().toString().trim();
        landArea = landAreaField.getText().toString().trim();
        expectedDate = dateSelectField.getText().toString().trim();
        quantity = quantityField.getText().toString().trim();

        if (cropName.equals("")) {
            cropNameField.setError("Enter Crop Name");
            cropNameField.requestFocus();
        } else if (landArea.equals("")) {
            landAreaField.setError("Enter Land Area");
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
