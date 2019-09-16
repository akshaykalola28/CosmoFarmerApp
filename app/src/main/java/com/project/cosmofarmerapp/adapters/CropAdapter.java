package com.project.cosmofarmerapp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.AddCropFragment;
import com.project.cosmofarmerapp.MyCropFragment;
import com.project.cosmofarmerapp.NavigationActivity;
import com.project.cosmofarmerapp.R;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> {

    private Context context;
    private Fragment fragment;
    private List<JsonObject> items;

    public CropAdapter(Context context, Fragment fragment, List<JsonObject> items) {
        this.context = context;
        this.fragment = fragment;
        this.items = items;
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.crop_card, viewGroup, false);

        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CropViewHolder cropViewHolder, int i) {
        final JsonObject item = items.get(i);

        cropViewHolder.cropNameField.setText(item.get("cropName").getAsString());
        cropViewHolder.areaField.setText(item.get("cropArea").getAsString());
        cropViewHolder.quantityField.setText(item.get("quantity").getAsString());
        cropViewHolder.dateField.setText(item.get("date").getAsString());

        cropViewHolder.updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment addCropFragment = new AddCropFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("isForUpdate", true);
                bundle.putString("cropData", item.toString());
                addCropFragment.setArguments(bundle);
                fragment.getFragmentManager().beginTransaction()
                        .replace(R.id.navigation_frame, addCropFragment)
                        .addToBackStack(null).commit();
            }
        });

        cropViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog mDialog = new ProgressDialog(context);
                mDialog.setMessage("Please Wait...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                JSONObject userDataJson;
                userDataJson = ((NavigationActivity) context).getUser();

                APIServices services = APIClient.getClient().create(APIServices.class);
                try {
                    Call<JsonObject> call = services.deleteCrop(userDataJson.getString("phone"), item.get("keyId").getAsString());
                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response.isSuccessful()) {
                                JsonObject jsonResponse = response.body();
                                if (jsonResponse.get("response").getAsString().equals("success")) {
                                    mDialog.dismiss();
                                    Toast.makeText(context, jsonResponse.get("data").getAsString(), Toast.LENGTH_SHORT).show();
                                    fragment.getFragmentManager().beginTransaction().replace(R.id.navigation_frame, new MyCropFragment())
                                            .addToBackStack(null).commit();
                                } else {
                                    mDialog.dismiss();
                                    Toast.makeText(context, jsonResponse.get("data").getAsString(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(context, "Something is Wrong. Please Try again...", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            mDialog.dismiss();
                            Toast.makeText(context, "Something is Wrong. Please Try again...", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class CropViewHolder extends RecyclerView.ViewHolder {

        TextView cropNameField, dateField, areaField, quantityField;
        Button updateButton, deleteButton;

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            cropNameField = itemView.findViewById(R.id.crop_name);
            dateField = itemView.findViewById(R.id.date);
            areaField = itemView.findViewById(R.id.land_area);
            quantityField = itemView.findViewById(R.id.quantity);
            updateButton = itemView.findViewById(R.id.update_crop);
            deleteButton = itemView.findViewById(R.id.delete_crop);
        }
    }
}
