package com.exampe.storeandmore.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.fragment.CartFragment;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Cart;

@SuppressLint("NotifyDataSetChanged")
public class SaveForLaterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final Session session;
    String taxPercentage;
    final String from;


    public SaveForLaterAdapter(Activity activity, String from) {
        this.activity = activity;
        session = new Session(activity);
        this.from = from;
        taxPercentage = "0";
    }

    public void add(int position, Cart item) {
        CartFragment.saveForLater.add(position, item);
        notifyItemInserted(position);
    }

    @SuppressLint("SetTextI18n")
    public void removeItem(int position) {
        Cart cart = CartFragment.saveForLater.get(position);

        if (CartFragment.values.containsKey(cart.getProduct_variant_id())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                CartFragment.values.replace(cart.getProduct_variant_id(), "0");
            } else {
                CartFragment.values.remove(cart.getProduct_variant_id());
                CartFragment.values.put(cart.getProduct_variant_id(), "0");
            }
        } else {
            CartFragment.values.put(cart.getProduct_variant_id(), "0");
        }


        CartFragment.saveForLater.remove(cart);
        CartFragment.isSoldOut = false;
        notifyDataSetChanged();

        activity.invalidateOptionsMenu();
        CartFragment.tvSaveForLaterTitle.setText(activity.getResources().getString(R.string.save_for_later)+ " (" + getItemCount() + ")");
        CartFragment.lytEmpty.setVisibility(getItemCount() == 0 && CartFragment.carts.size() == 0 ? View.VISIBLE : View.GONE);
        if (getItemCount() == 0)
            CartFragment.lytSaveForLater.setVisibility(View.GONE);
    }

    public void totalCalculate(Cart cart) {
        String taxPercentage1 = "0";
        try {
            taxPercentage1 = (Double.parseDouble(cart.getItems().get(0).getTax_percentage()) > 0 ? cart.getItems().get(0).getTax_percentage() : "0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        double price;
        if (cart.getItems().get(0).getDiscounted_price().equals("0") || cart.getItems().get(0).getDiscounted_price().equals("")) {
            price = ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage1)) / 100)));
        } else {
            price = ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage1)) / 100)));
        }

        Constant.FLOAT_TOTAL_AMOUNT += (price * Integer.parseInt(cart.getQty()));
        Constant.TOTAL_CART_ITEM = CartFragment.carts.size();
        CartFragment.SetData(activity);
        activity.invalidateOptionsMenu();
    }

    @SuppressLint("SetTextI18n")
    public void moveItem(int position) {
        try {
            Cart cart = CartFragment.saveForLater.get(position);
            CartFragment.carts.add(cart);
            CartFragment.cartAdapter.notifyDataSetChanged();

            CartFragment.saveForLater.remove(cart);
            CartFragment.saveForLaterAdapter.notifyDataSetChanged();

            totalCalculate(cart);

            if (getItemCount() == 0)
                CartFragment.lytSaveForLater.setVisibility(View.GONE);

            if (CartFragment.carts.size() != 0)
                CartFragment.lytTotal.setVisibility(View.VISIBLE);

            CartFragment.tvSaveForLaterTitle.setText(activity.getResources().getString(R.string.save_for_later) + " (" + getItemCount() + ")");
            CartFragment.values.put(cart.getProduct_variant_id(), cart.getQty());
            ApiConfig.AddMultipleProductInCart(session, activity, CartFragment.values);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view;
        switch (viewType) {
            case (VIEW_TYPE_ITEM):
                view = LayoutInflater.from(activity).inflate(R.layout.lyt_save_for_later, parent, false);
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
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holderParent, final int position) {

        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                final HolderItems holder = (HolderItems) holderParent;
                final Cart cart = CartFragment.saveForLater.get(position);

                double price;
                double oPrice;

                try {
                    taxPercentage = (Double.parseDouble(cart.getItems().get(0).getTax_percentage()) > 0 ? cart.getItems().get(0).getTax_percentage() : "0");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Picasso.get()
                        .load(cart.getItems().get(0).getImage())
                        .fit()
                        .centerInside()
                        .placeholder(R.drawable.app_icon)
                        .error(R.drawable.app_icon)
                        .into(holder.imgProduct);

                holder.tvDelete.setOnClickListener(v -> removeItem(position));

                holder.tvAction.setOnClickListener(v -> moveItem(position));

                holder.tvProductName.setText(cart.getItems().get(0).getName());

                holder.tvMeasurement.setText(cart.getItems().get(0).getMeasurement() + "\u0020" + cart.getItems().get(0).getUnit());

                if (cart.getItems().get(0).getServe_for().equals(Constant.SOLD_OUT_TEXT)) {
                    holder.tvStatus.setVisibility(View.VISIBLE);
                }

                if (cart.getItems().get(0).getDiscounted_price().equals("0") || cart.getItems().get(0).getDiscounted_price().equals("")) {
                    price = ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                } else {
                    price = ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                    oPrice = ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                    holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvOriginalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + oPrice));
                }

                holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + price));

                break;
            case VIEW_TYPE_LOADING:
                ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
                loadingViewHolder.progressBar.setIndeterminate(true);
                break;
        }


    }

    @Override
    public int getItemCount() {
        return CartFragment.saveForLater.size();
    }

    @Override
    public int getItemViewType(int position) {
        return CartFragment.saveForLater.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        Cart cart = CartFragment.saveForLater.get(position);
        if (cart != null)
            return Integer.parseInt(cart.getId());
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
        final ImageView imgProduct;
        final TextView tvProductName;
        final TextView tvMeasurement;
        final TextView tvPrice;
        final TextView tvOriginalPrice;
        final TextView tvDelete;
        final TextView tvAction;
        final TextView tvStatus;
        final RelativeLayout lytMain;

        public HolderItems(@NonNull View itemView) {
            super(itemView);
            lytMain = itemView.findViewById(R.id.lytMain);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvDelete = itemView.findViewById(R.id.tvDelete);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvMeasurement = itemView.findViewById(R.id.tvMeasurement);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
        }
    }
}