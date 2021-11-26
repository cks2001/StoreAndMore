package com.exampe.storeandmore.adapter;

import static com.exampe.storeandmore.helper.ApiConfig.toTitleCase;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.WalletTransaction;


public class WalletTransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final ArrayList<WalletTransaction> walletTransactions;

    public WalletTransactionAdapter(Activity activity, ArrayList<WalletTransaction> walletTransactions) {
        this.activity = activity;
        this.walletTransactions = walletTransactions;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view;
        switch (viewType) {
            case (VIEW_TYPE_ITEM):
                view = LayoutInflater.from(activity).inflate(R.layout.lyt_wallet_transection_list, parent, false);
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
            final WalletTransaction walletTransaction = walletTransactions.get(position);

            holder.tvTxDateAndTime.setText(walletTransaction.getDate_created());
            holder.tvTxMessage.setText(activity.getString(R.string.hash) + walletTransaction.getOrder_id() + " " + walletTransaction.getMessage());
            holder.tvTxAmount.setText(activity.getString(R.string.amount_) + new Session(activity).getData(Constant.currency) + " " + Float.parseFloat(walletTransaction.getAmount()));
            holder.tvTxNo.setText(activity.getString(R.string.hash) + walletTransaction.getId());
            holder.tvTxStatus.setText(toTitleCase(walletTransaction.getType()));

            if (walletTransaction.getType().equalsIgnoreCase(Constant.CREDIT)) {
                holder.cardViewTxStatus.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.tx_success_bg));
            } else {
                holder.cardViewTxStatus.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.tx_fail_bg));
            }

        } else if (holderParent instanceof ViewHolderLoading) {
            ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return walletTransactions.size();
    }

    @Override
    public int getItemViewType(int position) {
        return walletTransactions.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        WalletTransaction product = walletTransactions.get(position);
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

    public static class HolderItems extends RecyclerView.ViewHolder {

        final TextView tvTxNo;
        final TextView tvTxDateAndTime;
        final TextView tvTxMessage;
        final TextView tvTxAmount;
        final TextView tvTxStatus;
        final CardView cardViewTxStatus;

        public HolderItems(@NonNull View itemView) {
            super(itemView);

            tvTxNo = itemView.findViewById(R.id.tvTxNo);
            tvTxDateAndTime = itemView.findViewById(R.id.tvTxDateAndTime);
            tvTxMessage = itemView.findViewById(R.id.tvTxMessage);
            tvTxAmount = itemView.findViewById(R.id.tvTxAmount);
            tvTxStatus = itemView.findViewById(R.id.tvTxStatus);


            cardViewTxStatus = itemView.findViewById(R.id.cardViewTxStatus);

        }
    }
}