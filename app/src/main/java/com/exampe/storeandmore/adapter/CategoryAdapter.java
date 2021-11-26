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
import com.exampe.storeandmore.fragment.SubCategoryFragment;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.model.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    public final ArrayList<Category> categoryList;
    final int layout;
    final Context context;
    final String from;
    final int visibleNumber;


    public CategoryAdapter(Context context, ArrayList<Category> categoryList, int layout, String from, int visibleNumber) {
        this.context = context;
        this.categoryList = categoryList;
        this.layout = layout;
        this.from = from;
        this.visibleNumber = visibleNumber;
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
                .centerInside()
                .placeholder(R.drawable.app_icon)
                .error(R.drawable.app_icon)
                .into(holder.imgCategory);

        holder.lytMain.setOnClickListener(v -> {
            Fragment fragment = new SubCategoryFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.ID, model.getId());
            bundle.putString(Constant.NAME, model.getName());
            bundle.putString(Constant.FROM, "category");
            fragment.setArguments(bundle);
            ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });
    }

    @Override
    public int getItemCount() {
        int categories;
        if (categoryList.size() > visibleNumber && from.equals("home")) {
            categories = visibleNumber;
        } else {
            categories = categoryList.size();
        }
        return categories;
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
