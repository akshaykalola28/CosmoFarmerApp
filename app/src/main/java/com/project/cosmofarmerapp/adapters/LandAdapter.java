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
import com.project.cosmofarmerapp.AddLandFragment;
import com.project.cosmofarmerapp.MyLandFragment;
import com.project.cosmofarmerapp.NavigationActivity;
import com.project.cosmofarmerapp.R;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandAdapter extends RecyclerView.Adapter<LandAdapter.LandViewHolder> {

    private Context context;
    private Fragment fragment;
    private List<JsonObject> items;

    public LandAdapter(Context context, Fragment fragment, List<JsonObject> items) {
        this.context = context;
        this.fragment = fragment;
        this.items = items;
    }

    @NonNull
    @Override
    public LandViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.land_card, viewGroup, false);

        return new LandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final LandViewHolder landViewHolder, int i) {
        final JsonObject item = items.get(i);

        landViewHolder.landNameField.setText(item.get("landName").getAsString());
        landViewHolder.landAreaField.setText(item.get("totalLand").getAsString());
        landViewHolder.accountNumberField.setText(item.get("accountNumber").getAsString());
        landViewHolder.surveyNumberField.setText(item.get("surveyNumber").getAsString());

        double lat = item.get("location").getAsJsonObject().get("lat").getAsDouble();
        double lag = item.get("location").getAsJsonObject().get("lon").getAsDouble();
        landViewHolder.locationField.setText(item.get("location").getAsJsonObject().get("address").getAsString());

        landViewHolder.updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment addLandFragment = new AddLandFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("isForUpdate", true);
                bundle.putString("landData", item.toString());
                addLandFragment.setArguments(bundle);
                fragment.getFragmentManager().beginTransaction()
                        .replace(R.id.navigation_frame, addLandFragment)
                        .addToBackStack(null).commit();
            }
        });

        landViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
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
                    Call<JsonObject> call = services.deleteLand(userDataJson.getString("phone"), item.get("keyId").getAsString());
                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response.isSuccessful()) {
                                JsonObject jsonResponse = response.body();
                                if (jsonResponse.get("response").getAsString().equals("success")) {
                                    mDialog.dismiss();
                                    Toast.makeText(context, jsonResponse.get("data").getAsString(), Toast.LENGTH_SHORT).show();
                                    fragment.getFragmentManager().beginTransaction().replace(R.id.navigation_frame, new MyLandFragment())
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

    public class LandViewHolder extends RecyclerView.ViewHolder {

        TextView landNameField, landAreaField, accountNumberField, surveyNumberField, locationField;
        Button updateButton, deleteButton;

        public LandViewHolder(@NonNull View itemView) {
            super(itemView);
            landNameField = itemView.findViewById(R.id.land_name);
            landAreaField = itemView.findViewById(R.id.land_area);
            accountNumberField = itemView.findViewById(R.id.account_number);
            surveyNumberField = itemView.findViewById(R.id.survey_number);
            locationField = itemView.findViewById(R.id.land_location);
            updateButton = itemView.findViewById(R.id.update_land);
            deleteButton = itemView.findViewById(R.id.delete_land);
        }
    }
}
