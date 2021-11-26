package com.exampe.storeandmore.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.fragment.ProductDetailFragment;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Product;

/**
 * Created by shree1 on 3/16/2017.
 */

public class AdapterStyle1 extends RecyclerView.Adapter<AdapterStyle1.VideoHolder> {

    public final ArrayList<Product> productList;
    public final Activity activity;
    public final int itemResource;
    final Context context;

    public AdapterStyle1(Context context, Activity activity, ArrayList<Product> productList, int itemResource) {
        this.context = context;
        this.activity = activity;
        this.productList = productList;
        this.itemResource = itemResource;

    }

    @Override
    public int getItemCount() {
        return Math.min(productList.size(), 4);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull VideoHolder holder, final int position) {
        final Product product = productList.get(position);
        try {

            Picasso.get()
                    .load(product.getImage())
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.app_icon)
                    .error(R.drawable.app_icon)
                    .into(holder.thumbnail);


            double price, oPrice;
            String taxPercentage = "0";
            try {
                taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (product.getVariants().get(0).getDiscounted_price().equals("0") || product.getVariants().get(0).getDiscounted_price().equals("")) {
                holder.tvDPrice.setVisibility(View.GONE);
                price = ((Float.parseFloat(product.getVariants().get(0).getPrice()) + ((Float.parseFloat(product.getVariants().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
            } else {
                price = ((Float.parseFloat(product.getVariants().get(0).getDiscounted_price()) + ((Float.parseFloat(product.getVariants().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                oPrice = (Float.parseFloat(product.getVariants().get(0).getPrice()) + ((Float.parseFloat(product.getVariants().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100));

                holder.tvDPrice.setPaintFlags(holder.tvDPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvDPrice.setText(new Session(activity).getData(Constant.currency) + ApiConfig.StringFormat("" + oPrice));

                holder.tvDPrice.setVisibility(View.VISIBLE);
            }
            holder.tvPrice.setText(new Session(activity).getData(Constant.currency) + ApiConfig.StringFormat("" + price));

            holder.tvTitle.setText(product.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.relativeLayout.setOnClickListener(view -> {

            AppCompatActivity activity1 = (AppCompatActivity) context;
            Fragment fragment = new ProductDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.ID, product.getVariants().get(0).getProduct_id());
            bundle.putString(Constant.FROM, "section");
            bundle.putInt("variantPosition", 0);
            fragment.setArguments(bundle);
            activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });
    }

    @NonNull
    @Override
    public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(itemResource, parent, false);
        return new VideoHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class VideoHolder extends RecyclerView.ViewHolder {

        public final ImageView thumbnail;
        public final TextView tvTitle;
        public final TextView tvPrice;
        public final TextView tvDPrice;
        public final RelativeLayout relativeLayout;

        public VideoHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDPrice = itemView.findViewById(R.id.tvDPrice);
            relativeLayout = itemView.findViewById(R.id.play_layout);

        }


    }
}