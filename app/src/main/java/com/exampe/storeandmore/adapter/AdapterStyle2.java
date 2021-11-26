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

public class AdapterStyle2 extends RecyclerView.Adapter<AdapterStyle2.VideoHolder> {

    public final ArrayList<Product> productList;
    public final Activity activity;
    final Context context;
    final Session session;

    public AdapterStyle2(Context context, Activity activity, ArrayList<Product> productList) {
        this.context = context;
        this.activity = activity;
        this.productList = productList;
        session = new Session(context);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull VideoHolder holder, final int position) {
        try {
            if (productList.size() >= 1) {
                holder.tvStyle2_1.setText(productList.get(0).getName());

                double price, oPrice;
                String taxPercentage = "0";
                try {
                    taxPercentage = (Double.parseDouble(productList.get(0).getTax_percentage()) > 0 ? productList.get(0).getTax_percentage() : "0");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (productList.get(0).getVariants().get(0).getDiscounted_price().equals("0") || productList.get(0).getVariants().get(0).getDiscounted_price().equals("")) {
                    holder.tvSubStyle2_1_.setVisibility(View.GONE);
                    price = ((Float.parseFloat(productList.get(0).getVariants().get(0).getPrice()) + ((Float.parseFloat(productList.get(0).getVariants().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                } else {
                    price = ((Float.parseFloat(productList.get(0).getVariants().get(0).getDiscounted_price()) + ((Float.parseFloat(productList.get(0).getVariants().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                    oPrice = (Float.parseFloat(productList.get(0).getVariants().get(0).getPrice()) + ((Float.parseFloat(productList.get(0).getVariants().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100));

                    holder.tvSubStyle2_1_.setPaintFlags(holder.tvSubStyle2_1_.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvSubStyle2_1_.setText(new Session(activity).getData(Constant.currency) + ApiConfig.StringFormat("" + oPrice));

                    holder.tvSubStyle2_1_.setVisibility(View.VISIBLE);
                }

                holder.tvSubStyle2_1.setText(new Session(activity).getData(Constant.currency) + ApiConfig.StringFormat("" + price));


                Picasso.get()
                        .load(productList.get(0).getImage())
                        .fit()
                        .centerInside()
                        .placeholder(R.drawable.app_icon)
                        .error(R.drawable.app_icon)
                        .into(holder.imgStyle2_1);

                holder.layoutStyle2_1.setOnClickListener(view -> {
                    AppCompatActivity activity1 = (AppCompatActivity) context;
                    Fragment fragment = new ProductDetailFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putString(Constant.FROM, "section");
                    bundle.putInt("variantPosition", 0);
                    bundle.putString(Constant.ID, productList.get(0).getVariants().get(0).getProduct_id());
                    fragment.setArguments(bundle);
                    activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                });
            }

            if (productList.size() >= 2) {
                holder.tvStyle2_2.setText(productList.get(1).getName());

                double price, oPrice;
                String taxPercentage = "0";
                try {
                    taxPercentage = (Double.parseDouble(productList.get(1).getTax_percentage()) > 0 ? productList.get(1).getTax_percentage() : "0");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (productList.get(1).getVariants().get(0).getDiscounted_price().equals("0") || productList.get(1).getVariants().get(0).getDiscounted_price().equals("")) {
                    holder.tvSubStyle2_2_.setVisibility(View.GONE);
                    price = ((Float.parseFloat(productList.get(1).getVariants().get(0).getPrice()) + ((Float.parseFloat(productList.get(1).getVariants().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                } else {
                    price = ((Float.parseFloat(productList.get(1).getVariants().get(0).getDiscounted_price()) + ((Float.parseFloat(productList.get(1).getVariants().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                    oPrice = (Float.parseFloat(productList.get(1).getVariants().get(0).getPrice()) + ((Float.parseFloat(productList.get(1).getVariants().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100));

                    holder.tvSubStyle2_2_.setPaintFlags(holder.tvSubStyle2_2_.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvSubStyle2_2_.setText(new Session(activity).getData(Constant.currency) + ApiConfig.StringFormat("" + oPrice));

                    holder.tvSubStyle2_2_.setVisibility(View.VISIBLE);
                }

                holder.tvSubStyle2_2.setText(new Session(activity).getData(Constant.currency) + ApiConfig.StringFormat("" + price));


                Picasso.get()
                        .load(productList.get(1).getImage())
                        .fit()
                        .centerInside()
                        .placeholder(R.drawable.app_icon)
                        .error(R.drawable.app_icon)
                        .into(holder.imgStyle2_2);

                holder.layoutStyle2_2.setOnClickListener(view -> {
                    AppCompatActivity activity1 = (AppCompatActivity) context;
                    Fragment fragment = new ProductDetailFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putString(Constant.FROM, "section");
                    bundle.putInt("variantPosition", 0);
                    bundle.putString(Constant.ID, productList.get(1).getVariants().get(0).getProduct_id());
                    fragment.setArguments(bundle);
                    activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();

                });
            }

            if (productList.size() >= 3) {
                holder.tvStyle2_3.setText(productList.get(2).getName());

                double price, oPrice;
                String taxPercentage = "0";
                try {
                    taxPercentage = (Double.parseDouble(productList.get(2).getTax_percentage()) > 0 ? productList.get(2).getTax_percentage() : "0");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (productList.get(2).getVariants().get(0).getDiscounted_price().equals("0") || productList.get(2).getVariants().get(0).getDiscounted_price().equals("")) {
                    holder.tvSubStyle2_3_.setVisibility(View.GONE);
                    price = ((Float.parseFloat(productList.get(2).getVariants().get(0).getPrice()) + ((Float.parseFloat(productList.get(2).getVariants().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                } else {
                    price = ((Float.parseFloat(productList.get(2).getVariants().get(0).getDiscounted_price()) + ((Float.parseFloat(productList.get(2).getVariants().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                    oPrice = (Float.parseFloat(productList.get(2).getVariants().get(0).getPrice()) + ((Float.parseFloat(productList.get(2).getVariants().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100));

                    holder.tvSubStyle2_3_.setPaintFlags(holder.tvSubStyle2_3_.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvSubStyle2_3_.setText(new Session(activity).getData(Constant.currency) + ApiConfig.StringFormat("" + oPrice));

                    holder.tvSubStyle2_3_.setVisibility(View.VISIBLE);
                }

                holder.tvSubStyle2_3.setText(new Session(activity).getData(Constant.currency) + ApiConfig.StringFormat("" + price));


                Picasso.get()
                        .load(productList.get(2).getImage())
                        .fit()
                        .centerInside()
                        .placeholder(R.drawable.app_icon)
                        .error(R.drawable.app_icon)
                        .into(holder.imgStyle2_3);

                holder.layoutStyle2_3.setOnClickListener(view -> {
                    AppCompatActivity activity1 = (AppCompatActivity) context;
                    Fragment fragment = new ProductDetailFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putString(Constant.FROM, "section");
                    bundle.putInt("variantPosition", 0);
                    bundle.putString(Constant.ID, productList.get(2).getVariants().get(0).getProduct_id());
                    fragment.setArguments(bundle);
                    activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();

                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lyt_style_2, parent, false);
        return new VideoHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class VideoHolder extends RecyclerView.ViewHolder {

        public final ImageView imgStyle2_1;
        public final ImageView imgStyle2_2;
        public final ImageView imgStyle2_3;
        public final TextView tvStyle2_1;
        public final TextView tvSubStyle2_1;
        public final TextView tvSubStyle2_1_;
        public final TextView tvStyle2_2;
        public final TextView tvSubStyle2_2;
        public final TextView tvSubStyle2_2_;
        public final TextView tvStyle2_3;
        public final TextView tvSubStyle2_3;
        public final TextView tvSubStyle2_3_;
        public final RelativeLayout layoutStyle2_1;
        public final RelativeLayout layoutStyle2_2;
        public final RelativeLayout layoutStyle2_3;

        public VideoHolder(View itemView) {
            super(itemView);
            imgStyle2_1 = itemView.findViewById(R.id.imgStyle2_1);
            tvStyle2_1 = itemView.findViewById(R.id.tvStyle2_1);
            tvSubStyle2_1 = itemView.findViewById(R.id.tvSubStyle2_1);
            tvSubStyle2_1_ = itemView.findViewById(R.id.tvSubStyle2_1_);

            imgStyle2_2 = itemView.findViewById(R.id.imgStyle2_2);
            tvStyle2_2 = itemView.findViewById(R.id.tvStyle2_2);
            tvSubStyle2_2 = itemView.findViewById(R.id.tvSubStyle2_2);
            tvSubStyle2_2_ = itemView.findViewById(R.id.tvSubStyle2_2_);

            imgStyle2_3 = itemView.findViewById(R.id.imgStyle2_3);
            tvStyle2_3 = itemView.findViewById(R.id.tvStyle2_3);
            tvSubStyle2_3 = itemView.findViewById(R.id.tvSubStyle2_3);
            tvSubStyle2_3_ = itemView.findViewById(R.id.tvSubStyle2_3_);

            layoutStyle2_1 = itemView.findViewById(R.id.layoutStyle2_1);
            layoutStyle2_2 = itemView.findViewById(R.id.layoutStyle2_2);
            layoutStyle2_3 = itemView.findViewById(R.id.layoutStyle2_3);
        }


    }
}
