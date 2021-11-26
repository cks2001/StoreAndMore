package com.exampe.storeandmore.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.activity.MainActivity;
import com.exampe.storeandmore.fragment.TrackerDetailFragment;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.OrderTracker;


public class TrackerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final ArrayList<OrderTracker> orderTrackerArrayList;
    final Context context;


    public TrackerAdapter(Context context, Activity activity, ArrayList<OrderTracker> orderTrackerArrayList) {
        this.context = context;
        this.activity = activity;
        this.orderTrackerArrayList = orderTrackerArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view;
        switch (viewType) {
            case (VIEW_TYPE_ITEM):
                view = LayoutInflater.from(activity).inflate(R.layout.lyt_trackorder, parent, false);
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
            final OrderTracker order = orderTrackerArrayList.get(position);
            try {
                holder.tvOrderID.setText(activity.getString(R.string.order_number) + order.getId());
                String[] date = order.getDate_added().split("\\s+");
                holder.tvOrderDate.setText(activity.getString(R.string.ordered_on) + date[0]);
                holder.txtOrderAmount.setText(new Session(context).getData(Constant.currency) + ApiConfig.StringFormat(order.getTotal()));

                holder.lytMain.setOnClickListener(v -> {
                    Fragment fragment = new TrackerDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("id", "");
                    bundle.putSerializable("model", order);
                    fragment.setArguments(bundle);
                    MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                });

                holder.tvStatus.setText(ApiConfig.toTitleCase(order.getActive_status()));

                if (order.getLocal_pickup().equals("1")) {
                    holder.tvOrderType.setText(activity.getString(R.string.pickup_from_store));
                }else{
                    holder.tvOrderType.setText(activity.getString(R.string.door_step_delivery));
                }

                if (order.getActive_status().equals(Constant.RECEIVED)) {
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.received_status_bg));
                    holder.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.received_status_txt));
                    holder.tvStatus.setText(R.string.received);
                } else if (order.getActive_status().equals(Constant.PROCESSED)) {
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.processed_status_bg));
                    holder.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.processed_status_txt));
                    holder.tvStatus.setText(R.string.processed);
                } else if (order.getActive_status().equals(Constant.SHIPPED)) {
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.shipped_status_bg));
                    holder.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.shipped_status_txt));
                    holder.tvStatus.setText(R.string.shipped1);
                } else if (order.getActive_status().equals(Constant.DELIVERED)) {
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.delivered_status_bg));
                    holder.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.delivered_status_txt));
                    holder.tvStatus.setText(R.string.delivered1);
                } else if (order.getActive_status().equals(Constant.CANCELLED)) {
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.returned_and_cancel_status_bg));
                    holder.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.returned_and_cancel_status_txt));
                    holder.tvStatus.setText(R.string.cancelled1);
                } else if (order.getActive_status().equals(Constant.RETURNED)) {
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.returned_and_cancel_status_bg));
                    holder.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.returned_and_cancel_status_txt));
                    holder.tvStatus.setText(R.string.returned);
                } else if (order.getActive_status().equalsIgnoreCase(Constant.AWAITING_PAYMENT)) {
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.awaiting_status_bg));
                    holder.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.awaiting_status_txt));
                    holder.tvStatus.setText(R.string.awaiting_payment_);
                }


                ArrayList<String> items = new ArrayList<>();
                for (int i = 0; i < order.getItems().size(); i++) {
                    items.add(order.getItems().get(i).getName());
                }
                holder.tvItems.setText(Arrays.toString(items.toArray()).replace("]", "").replace("[", ""));
                holder.tvTotalItems.setText(items.size() > 1 ? items.size() + activity.getString(R.string.items) : items.size() + activity.getString(R.string.item));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (holderParent instanceof ViewHolderLoading) {
            ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return orderTrackerArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return orderTrackerArrayList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolderLoading extends RecyclerView.ViewHolder {
        public final ProgressBar progressBar;

        public ViewHolderLoading(View view) {
            super(view);
            progressBar = view.findViewById(R.id.itemProgressbar);
        }
    }

    public static class HolderItems extends RecyclerView.ViewHolder {
        final TextView tvOrderID;
        final TextView tvOrderDate;
        final RelativeLayout lytMain;
        final TextView txtOrderAmount;
        final TextView tvTotalItems;
        final TextView tvItems;
        final TextView tvStatus;
        final TextView tvOrderType;
        final CardView cardView;

        public HolderItems(View itemView) {
            super(itemView);
            tvOrderID = itemView.findViewById(R.id.tvOrderID);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            lytMain = itemView.findViewById(R.id.lytMain);
            txtOrderAmount = itemView.findViewById(R.id.txtOrderAmount);
            tvTotalItems = itemView.findViewById(R.id.tvTotalItems);
            tvItems = itemView.findViewById(R.id.tvItems);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvOrderType = itemView.findViewById(R.id.tvOrderType);
            cardView = itemView.findViewById(R.id.cardView);

        }
    }
}