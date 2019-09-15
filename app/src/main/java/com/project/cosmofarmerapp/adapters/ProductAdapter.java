package com.project.cosmofarmerapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private Fragment fragment;
    private List<JsonObject> productList;

    public ProductAdapter(Context context, Fragment fragment, List<JsonObject> productList) {
        this.context = context;
        this.fragment = fragment;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_product, viewGroup, false);

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder productViewHolder, int i) {
        JsonObject item = productList.get(i);

        productViewHolder.productNameField.setText(item.get("name").getAsString());
        productViewHolder.productRateField.setText(item.get("rate").getAsString());
        productViewHolder.updatedOnField.setText(item.get("updatedOn").getAsString());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView productNameField, productRateField, updatedOnField;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            productNameField = itemView.findViewById(R.id.product_name);
            productRateField = itemView.findViewById(R.id.product_rate);
            updatedOnField = itemView.findViewById(R.id.update_on_date);
        }
    }
}
