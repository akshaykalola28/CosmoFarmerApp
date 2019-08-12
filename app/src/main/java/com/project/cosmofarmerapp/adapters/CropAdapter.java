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
        JsonObject item = items.get(i);

        cropViewHolder.cropNameField.setText(item.get("cropName").getAsString());
        cropViewHolder.areaField.setText(item.get("cropArea").getAsString());
        cropViewHolder.quantityField.setText(item.get("quantity").getAsString());
        cropViewHolder.dateField.setText(item.get("date").getAsString());

        /*cropViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropViewHolder.updateButton.setVisibility(View.VISIBLE);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class CropViewHolder extends RecyclerView.ViewHolder {

        TextView cropNameField, dateField, areaField, quantityField;
        Button updateButton;

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            cropNameField = itemView.findViewById(R.id.crop_name);
            dateField = itemView.findViewById(R.id.date);
            areaField = itemView.findViewById(R.id.land_area);
            quantityField = itemView.findViewById(R.id.quantity);
            updateButton = itemView.findViewById(R.id.update_crop);

            updateButton.setVisibility(View.GONE);
        }
    }
}
