package com.exampe.storeandmore.adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Attachment;

@SuppressLint("NotifyDataSetChanged")
public class ProductImagesAdapter extends RecyclerView.Adapter<ProductImagesAdapter.ImageHolder> {

    final Activity activity;
    final ArrayList<Attachment> images;
    final Session session;
    String from;
    String orderId;

    public ProductImagesAdapter(Activity activity, ArrayList<Attachment> images, String from, String orderId) {
        this.activity = activity;
        this.images = images;
        this.from = from;
        this.orderId = orderId;
        session = new Session(activity);
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageHolder(LayoutInflater.from(activity).inflate(R.layout.lyt_image_list, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull final ImageHolder holder, @SuppressLint("RecyclerView") int position) {

        final Attachment image = images.get(position);
        if (from.equals("api")) {
            Picasso.get().
                    load(image.getImage())
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.app_icon)
                    .error(R.drawable.app_icon)
                    .into(holder.imgProductImage);
        } else {
            holder.imgProductImage.setImageBitmap(BitmapFactory.decodeFile(image.getImage()));
        }

        holder.imgProductImageDelete.setOnClickListener(v -> {
            if (orderId.equals("0")) {
                images.remove(image);
                notifyDataSetChanged();
            } else {
                removeImage(activity, image.getId(), image);
            }
        });
    }

    public void removeImage(final Activity activity, String id, Attachment image) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        alertDialog.setTitle(R.string.remove_image);
        alertDialog.setMessage(R.string.remove_image_msg);
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(R.string.yes, (dialog, which) -> {
            Map<String, String> params = new HashMap<>();
            params.put(Constant.DELETE_BANK_TRANSFER_ATTACHMENT, Constant.GetVal);
            params.put(Constant.ORDER_ID, orderId);
            params.put(Constant.ID, id);
            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            images.remove(image);
                            notifyDataSetChanged();
                        } else {
                            dialog.dismiss();
                        }
                    } catch (JSONException e) {
                        dialog.dismiss();
                        e.printStackTrace();
                    }
                }
            }, activity, Constant.ORDER_PROCESS_URL, params, false);
        });
        alertDialog.setNegativeButton(R.string.no, (dialog, which) -> alertDialog1.dismiss());
        // Showing Alert Message
        alertDialog.show();

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ImageHolder extends RecyclerView.ViewHolder {
        final ImageView imgProductImage, imgProductImageDelete;

        public ImageHolder(View itemView) {
            super(itemView);
            imgProductImage = itemView.findViewById(R.id.imgProductImage);
            imgProductImageDelete = itemView.findViewById(R.id.imgProductImageDelete);
        }
    }
}