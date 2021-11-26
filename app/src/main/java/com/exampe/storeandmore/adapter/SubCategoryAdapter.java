package com.exampe.storeandmore.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.fragment.ProductListFragment;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.model.Category;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.ViewHolder> {
    public final ArrayList<Category> categoryList;
    final int layout;
    final Context context;
    final String from;


    public SubCategoryAdapter(Context context, ArrayList<Category> categoryList, int layout, String from) {
        this.context = context;
        this.categoryList = categoryList;
        this.layout = layout;
        this.from = from;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Category model = categoryList.get(position);
        holder.tvTitle.setText(model.getName());

        Picasso.get()
                .load(model.getImage())
                .fit()
                .placeholder(R.drawable.app_icon)
                .centerInside()
                .into(holder.imgCategory);

        holder.lytMain.setOnClickListener(v -> {
            AppCompatActivity activity1 = (AppCompatActivity) context;
            Fragment fragment = new ProductListFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.ID, model.getId());
            bundle.putString(Constant.NAME, model.getName());
            bundle.putString(Constant.FROM, from);
            fragment.setArguments(bundle);
            activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView tvTitle;
        final ImageView imgCategory;
        final LinearLayout lytMain;

        public ViewHolder(View itemView) {
            super(itemView);
            lytMain = itemView.findViewById(R.id.lytMain);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }

    }
}
