package com.exampe.storeandmore.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.fragment.ProductDetailFragment;
import com.exampe.storeandmore.fragment.SubCategoryFragment;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final ArrayList<Notification> Notifications;
    Session session;


    public NotificationAdapter(Activity activity, ArrayList<Notification> Notifications) {
        this.activity = activity;
        this.session = new Session(activity);
        this.Notifications = Notifications;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view;
        switch (viewType) {
            case (VIEW_TYPE_ITEM):
                view = LayoutInflater.from(activity).inflate(R.layout.lyt_notification_list, parent, false);
                return new HolderItems(view);
            case (VIEW_TYPE_LOADING):
                view = LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false);
                return new ViewHolderLoading(view);
            default:
                throw new IllegalArgumentException("unexpected viewType: " + viewType);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderParent, final int position) {

        if (holderParent instanceof HolderItems) {
            final HolderItems holder = (HolderItems) holderParent;
            final Notification notification = Notifications.get(position);

            if (!notification.getImage().isEmpty()) {
                holder.image.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(notification.getImage())
                        .fit()
                        .centerInside()
                        .placeholder(R.drawable.app_icon)
                        .error(R.drawable.app_icon)
                        .into(holder.image);
            } else {
                holder.image.setVisibility(View.GONE);
            }

            if (!notification.getName().isEmpty()) {
                holder.tvTitle.setVisibility(View.VISIBLE);
            } else {
                holder.tvTitle.setVisibility(View.GONE);
            }

            if (!notification.getSubtitle().isEmpty()) {
                holder.tvMessage.setVisibility(View.VISIBLE);
            } else {
                holder.tvMessage.setVisibility(View.GONE);
            }

            holder.tvTitle.setText(Html.fromHtml(notification.getName()));
            holder.tvMessage.setText(Html.fromHtml(notification.getSubtitle()));
            String type = notification.getType();

            if (type.equalsIgnoreCase("category")) {
                holder.tvRedirect.setVisibility(View.VISIBLE);
                holder.tvRedirect.setText(activity.getString(R.string.go_to_category));

                holder.lytMain.setOnClickListener(v -> {
                    Fragment fragment = new SubCategoryFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.ID, notification.getType_id());
                    bundle.putString(Constant.NAME, notification.getName());
                    bundle.putString(Constant.FROM, "category");
                    fragment.setArguments(bundle);
                    ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                });

            } else if (type.equalsIgnoreCase("product")) {
                holder.tvRedirect.setVisibility(View.VISIBLE);
                holder.tvRedirect.setText(activity.getString(R.string.go_to_product));

                holder.lytMain.setOnClickListener(v -> {
                    AppCompatActivity activity1 = (AppCompatActivity) activity;
                    Fragment fragment = new ProductDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("variantsPosition", 0);
                    bundle.putString("id", notification.getType_id());
                    bundle.putString(Constant.FROM, "notification");
                    bundle.putInt("position", 0);
                    fragment.setArguments(bundle);
                    activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                });
            }


        } else if (holderParent instanceof ViewHolderLoading) {
            ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return Notifications.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Notifications.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        Notification product = Notifications.get(position);
        if (product != null)
            return Integer.parseInt(product.getId());
        else
            return position;
    }

    static class ViewHolderLoading extends RecyclerView.ViewHolder {
        public final ProgressBar progressBar;

        public ViewHolderLoading(View view) {
            super(view);
            progressBar = view.findViewById(R.id.itemProgressbar);
        }
    }

    static class HolderItems extends RecyclerView.ViewHolder {

        final ImageView image;
        final TextView tvTitle;
        final TextView tvMessage;
        final TextView tvRedirect;
        LinearLayout lytMain;

        public HolderItems(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvRedirect = itemView.findViewById(R.id.tvRedirect);
            lytMain = itemView.findViewById(R.id.lytMain);
        }
    }
}
