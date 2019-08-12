package com.project.cosmofarmerapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.R;

import java.util.List;

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
        JsonObject item = items.get(i);

        landViewHolder.landNameField.setText(item.get("landName").getAsString());
        landViewHolder.landAreaField.setText(item.get("totalLand").getAsString());
        landViewHolder.accountNumberField.setText(item.get("accountNumber").getAsString());
        landViewHolder.surveyNumberField.setText(item.get("surveyNumber").getAsString());

        double lat = item.get("location").getAsJsonObject().get("lat").getAsDouble();
        double lag = item.get("location").getAsJsonObject().get("lon").getAsDouble();
        landViewHolder.locationField.setText(item.get("location").getAsJsonObject().get("address").getAsString());

        /*landViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                landViewHolder.updateButton.setVisibility(View.VISIBLE);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class LandViewHolder extends RecyclerView.ViewHolder {

        TextView landNameField, landAreaField, accountNumberField, surveyNumberField, locationField;
        Button updateButton;

        public LandViewHolder(@NonNull View itemView) {
            super(itemView);
            landNameField = itemView.findViewById(R.id.land_name);
            landAreaField = itemView.findViewById(R.id.land_area);
            accountNumberField = itemView.findViewById(R.id.account_number);
            surveyNumberField = itemView.findViewById(R.id.survey_number);
            locationField = itemView.findViewById(R.id.land_location);
            updateButton = itemView.findViewById(R.id.update_land);

            updateButton.setVisibility(View.GONE);
        }
    }
}
