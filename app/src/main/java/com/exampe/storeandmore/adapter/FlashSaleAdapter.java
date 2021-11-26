package com.exampe.storeandmore.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.activity.MainActivity;
import com.exampe.storeandmore.fragment.ProductDetailFragment;
import com.exampe.storeandmore.fragment.ProductListFragment;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.helper.Utils;
import com.exampe.storeandmore.model.FlashSale;
import com.exampe.storeandmore.model.Product;
import com.exampe.storeandmore.model.Variants;

/**
 * Created by shree1 on 3/16/2017.
 */

public class FlashSaleAdapter extends RecyclerView.Adapter<FlashSaleAdapter.HolderItems> {

    public final ArrayList<Product> productList;
    public final Activity activity;
    final Session session;

    public FlashSaleAdapter(Activity activity, ArrayList<Product> productList) {
        this.activity = activity;
        this.productList = productList;
        this.session = new Session(activity);
    }

    @Override
    public int getItemCount() {
        return Math.min(productList.size(),6);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HolderItems holder, final int position) {
        try {
            final Product product = productList.get(position);
            holder.setIsRecyclable(false);
            if (position == 5) {
                holder.tvViewAll.setVisibility(View.VISIBLE);
                holder.lytMain_.setVisibility(View.INVISIBLE);
            } else {
                holder.tvViewAll.setVisibility(View.INVISIBLE);
                holder.lytMain_.setVisibility(View.VISIBLE);
            }

            Picasso.get()
                    .load(product.getImage())
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.app_icon)
                    .error(R.drawable.app_icon)
                    .into(holder.imgThumb);

            holder.productName.setText(product.getName());

            double OriginalPrice = 0, DiscountedPrice = 0;
            String taxPercentage = "0";

            try {
                taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Variants variants = product.getVariants().get(0);
            FlashSale flashSale = variants.getFlash_sales().get(0);

            Date startDate, endDate;
            long different;

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (flashSale.isIs_start()) {

                startDate = df.parse(session.getData(Constant.current_date));
                endDate = df.parse(flashSale.getEnd_date());
                different = (endDate != null ? endDate.getTime() : 0) - (startDate != null ? startDate.getTime() : 0);
                long days = (different / (60 * 60 * 24 * 1000));
                if (Utils.setFormatTime(days).equalsIgnoreCase("00")) {
                    StartTimer(holder, variants, different);
                } else {
                    holder.tvTimer.setText(ApiConfig.CalculateDays(activity, days));
                }
                holder.tvTimerTitle.setText(activity.getString(R.string.ends_in));
                if (flashSale.getDiscounted_price().equals("0") || flashSale.getDiscounted_price().equals("")) {
                    holder.showDiscount.setVisibility(View.GONE);
                    holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                } else {
                    holder.showDiscount.setVisibility(View.VISIBLE);
                    DiscountedPrice = ((Float.parseFloat(flashSale.getDiscounted_price()) + ((Float.parseFloat(flashSale.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                    OriginalPrice = (Float.parseFloat(flashSale.getPrice()) + ((Float.parseFloat(flashSale.getPrice()) * Float.parseFloat(taxPercentage)) / 100));
                    holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvOriginalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                    holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + DiscountedPrice));
                }

            } else {
                startDate = df.parse(session.getData(Constant.current_date));
                endDate = df.parse(flashSale.getStart_date());
                different = (endDate != null ? endDate.getTime() : 0) - (startDate != null ? startDate.getTime() : 0);
                long days = (different / (60 * 60 * 24 * 1000));

                if (Utils.setFormatTime(days).equalsIgnoreCase("00")) {
                    StartTimer(holder, variants, different);
                } else {
                    holder.tvTimer.setText(ApiConfig.CalculateDays(activity, days));
                }
                holder.tvTimerTitle.setText(activity.getString(R.string.starts_in));
                holder.tvTimer.setText(ApiConfig.CalculateDays(activity, Math.abs(days)));
                if (variants.getDiscounted_price().equals("0") || variants.getDiscounted_price().equals("")) {
                    holder.showDiscount.setVisibility(View.GONE);
                    holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                } else {
                    holder.showDiscount.setVisibility(View.VISIBLE);
                    DiscountedPrice = ((Float.parseFloat(variants.getDiscounted_price()) + ((Float.parseFloat(variants.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                    OriginalPrice = (Float.parseFloat(variants.getPrice()) + ((Float.parseFloat(variants.getPrice()) * Float.parseFloat(taxPercentage)) / 100));
                    holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvOriginalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                    holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + DiscountedPrice));
                }
            }
            holder.showDiscount.setText("-" + ApiConfig.GetDiscount(OriginalPrice, DiscountedPrice));

            holder.lytMain_.setOnClickListener(view -> {
                if (variants.getProduct_id() != null) {
                    AppCompatActivity activity1 = (AppCompatActivity) activity;
                    Fragment fragment = new ProductDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.ID, variants.getProduct_id());
                    bundle.putString(Constant.FROM, "section");
                    bundle.putInt("variantsPosition", 0);
                    fragment.setArguments(bundle);
                    activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                }
            });

            holder.tvViewAll.setOnClickListener(view -> {
                Fragment fragment = new ProductListFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.FROM, "flash_sale");
                bundle.putString(Constant.NAME, activity.getString(R.string.flash_sale));
                bundle.putString(Constant.ID, flashSale.getFlash_sales_id());
                fragment.setArguments(bundle);
                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public HolderItems onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_flash_item_grid, parent, false);
        return new HolderItems(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class HolderItems extends RecyclerView.ViewHolder {
        final RelativeLayout lytMain_;
        final TextView showDiscount;
        final TextView productName;
        final TextView tvOriginalPrice;
        final TextView tvPrice;
        final TextView tvTimer;
        final TextView tvTimerTitle;
        final TextView tvViewAll;
        final ImageView imgThumb;

        public HolderItems(View itemView) {
            super(itemView);
            lytMain_ = itemView.findViewById(R.id.lytMain_);
            showDiscount = itemView.findViewById(R.id.showDiscount);
            productName = itemView.findViewById(R.id.productName);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvTimer = itemView.findViewById(R.id.tvTimer);
            tvTimerTitle = itemView.findViewById(R.id.tvTimerTitle);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            tvViewAll = itemView.findViewById(R.id.tvViewAll);
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n", "SimpleDateFormat"})
    public void StartTimer(HolderItems holder, Variants variants, long duration) {
        try {

            new CountDownTimer(duration, 1000) {
                @Override
                public void onTick(long different) {
                    int seconds = (int) (different / 1000) % 60;
                    int minutes = (int) ((different / (1000 * 60)) % 60);
                    int hours = (int) ((different / (1000 * 60 * 60)) % 24);

                    holder.tvTimer.setText(Utils.setFormatTime(hours) + ":" + Utils.setFormatTime(minutes) + ":" + Utils.setFormatTime(seconds));

                }

                @Override
                public void onFinish() {
                    if (!variants.getFlash_sales().get(0).isIs_start()) {
                        variants.getFlash_sales().get(0).setIs_start(true);
                        variants.setIs_flash_sales("true");
                    } else {
                        variants.setIs_flash_sales("false");
                    }
                    notifyDataSetChanged();
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}