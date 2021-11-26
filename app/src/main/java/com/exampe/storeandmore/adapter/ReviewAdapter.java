package com.exampe.storeandmore.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import com.exampe.storeandmore.R;
import com.exampe.storeandmore.model.Review;

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final ArrayList<Review> Reviews;


    public ReviewAdapter(Activity activity, ArrayList<Review> Reviews) {
        this.activity = activity;
        this.Reviews = Reviews;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view;
        switch (viewType) {
            case (VIEW_TYPE_ITEM):
                view = LayoutInflater.from(activity).inflate(R.layout.lyt_review_list, parent, false);
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
            final Review review = Reviews.get(position);

            Picasso.get()
                    .load(review.getUser_profile().isEmpty() ? "-" : review.getUser_profile())
                    .fit()
                    .centerInside()
                    .transform(new RoundedCornersTransformation(20, 0))
                    .placeholder(R.drawable.app_icon)
                    .error(R.drawable.app_icon)
                    .into(holder.imgProfile);

            holder.ratingReview.setRating(Float.parseFloat(review.getRatings()));
            holder.tvDate.setText(activity.getString(R.string.reviewed_on) + review.getDate_added().split(" ")[0]);
            holder.tvName.setText(review.getUsername());
            holder.tvMessage.setText(review.getReview());

        } else if (holderParent instanceof ViewHolderLoading) {
            ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return Reviews.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Reviews.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        Review product = Reviews.get(position);
        if (product != null)
            return Integer.parseInt(product.getProduct_id());
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

        final ImageView imgProfile;
        final TextView tvName, tvDate, tvMessage;
        final RatingBar ratingReview;


        public HolderItems(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.imgProfile);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            ratingReview = itemView.findViewById(R.id.ratingReview);
        }
    }
}
