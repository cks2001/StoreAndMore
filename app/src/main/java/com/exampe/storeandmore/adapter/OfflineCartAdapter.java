package com.exampe.storeandmore.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.fragment.CartFragment;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.DatabaseHelper;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.OfflineCart;


@SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
public class OfflineCartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    final int VIEW_TYPE_ITEM = 0;
    final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final DatabaseHelper databaseHelper;
    final Session session;


    public OfflineCartAdapter(Activity activity) {
        this.activity = activity;
        databaseHelper = new DatabaseHelper(activity);
        session = new Session(activity);
    }


    public void add(int position, OfflineCart item) {
        if (position > 0) {
            CartFragment.items.add(position, item);
        } else {
            CartFragment.items.add(item);
        }

        CartFragment.offlineSaveForLaterAdapter.notifyDataSetChanged();
        CartFragment.offlineCartAdapter.notifyDataSetChanged();
    }

    public void removeItem(int position) {
        String taxPercentage1 = "0";
        OfflineCart cart = CartFragment.items.get(position);
        showUndoSnackBar(cart, position);
        CartFragment.items.remove(cart);
        try {
            taxPercentage1 = (Double.parseDouble(cart.getTax_percentage()) > 0 ? cart.getTax_percentage() : "0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        double price;
        if (cart.getDiscounted_price().equals("0") || cart.getDiscounted_price().equals("")) {
            price = ((Float.parseFloat(cart.getPrice()) + ((Float.parseFloat(cart.getPrice()) * Float.parseFloat(taxPercentage1)) / 100)));
        } else {
            price = ((Float.parseFloat(cart.getDiscounted_price()) + ((Float.parseFloat(cart.getDiscounted_price()) * Float.parseFloat(taxPercentage1)) / 100)));
        }

        databaseHelper.RemoveFromCart(cart.getId(), cart.getProduct_id());
        Constant.FLOAT_TOTAL_AMOUNT = Double.parseDouble(ApiConfig.StringFormat("" + (Constant.FLOAT_TOTAL_AMOUNT - (price * Integer.parseInt(databaseHelper.CheckCartItemExist(cart.getId(), cart.getProduct_id()))))));
        CartFragment.SetData(activity);

        databaseHelper.getTotalItemOfCart(activity);

        CartFragment.offlineCartAdapter.notifyDataSetChanged();
        if (getItemCount() == 0 && CartFragment.saveForLaterItems.size() == 0) {
            CartFragment.lytEmpty.setVisibility(View.VISIBLE);
            CartFragment.lytTotal.setVisibility(View.GONE);
        } else {
            CartFragment.lytEmpty.setVisibility(View.GONE);
            CartFragment.lytTotal.setVisibility(View.VISIBLE);
        }
    }

    public void moveItem(int position) {
        try {
            OfflineCart cart = CartFragment.items.get(position);
            databaseHelper.MoveToCartOrSaveForLater(cart.getId(), cart.getProduct_id(), "cart", activity);
            CartFragment.items.remove(cart);
            CartFragment.saveForLaterItems.add(cart);
            CartFragment.offlineSaveForLaterAdapter.notifyDataSetChanged();
            CartFragment.offlineCartAdapter.notifyDataSetChanged();

            if (CartFragment.saveForLaterItems.size() > 0)
                CartFragment.lytSaveForLater.setVisibility(View.VISIBLE);

            if (getItemCount() == 0)
                CartFragment.lytTotal.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case (VIEW_TYPE_ITEM):
                view = LayoutInflater.from(activity).inflate(R.layout.lyt_cartlist, parent, false);
                return new HolderItems(view);
            case (VIEW_TYPE_LOADING):
                view = LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false);
                return new ViewHolderLoading(view);
            default:
                throw new IllegalArgumentException("unexpected viewType: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holderParent, int position) {

        if (holderParent instanceof HolderItems) {
            HolderItems holder = (HolderItems) holderParent;
            OfflineCart cart = CartFragment.items.get(position);

            Picasso.get()
                    .load(cart.getItem().get(0).getImage())
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.app_icon)
                    .error(R.drawable.app_icon)
                    .into(holder.imgProduct);

            holder.tvProductName.setText(cart.getItem().get(0).getName());
            holder.tvMeasurement.setText(cart.getItem().get(0).getMeasurement() + "\u0020" + cart.getItem().get(0).getUnit());
            double price, oPrice;
            String taxPercentage = "0";
            try {
                taxPercentage = (Double.parseDouble(cart.getTax_percentage()) > 0 ? cart.getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.tvPrice.setText(new Session(activity).getData(Constant.currency) + (cart.getDiscounted_price().equals("0") ? cart.getPrice() : cart.getDiscounted_price()));

            if (cart.getDiscounted_price().equals("0") || cart.getDiscounted_price().equals("")) {
                price = ((Float.parseFloat(cart.getPrice()) + ((Float.parseFloat(cart.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
            } else {
                price = ((Float.parseFloat(cart.getDiscounted_price()) + ((Float.parseFloat(cart.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                oPrice = ((Float.parseFloat(cart.getPrice()) + ((Float.parseFloat(cart.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvOriginalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + oPrice));
            }

            holder.tvDelete.setOnClickListener(v -> removeItem(position));

            holder.tvAction.setOnClickListener(v -> moveItem(position));

            holder.tvPrice.setText(new Session(activity).getData(Constant.currency) + ApiConfig.StringFormat("" + price));

            holder.tvProductName.setText(cart.getItem().get(0).getName());

            holder.tvMeasurement.setText(cart.getItem().get(0).getMeasurement() + "\u0020" + cart.getItem().get(0).getUnit());

            holder.tvQuantity.setText(databaseHelper.CheckCartItemExist(cart.getId(), cart.getProduct_id()));
            cart.getItem().get(0).setCart_count(databaseHelper.CheckCartItemExist(cart.getId(), cart.getProduct_id()));

            holder.tvTotalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + price * Integer.parseInt(databaseHelper.CheckCartItemExist(cart.getId(), cart.getProduct_id()))));

            Constant.FLOAT_TOTAL_AMOUNT = Constant.FLOAT_TOTAL_AMOUNT + (price * Integer.parseInt(databaseHelper.CheckCartItemExist(cart.getId(), cart.getProduct_id())));
            CartFragment.SetData(activity);

            double finalPrice = price;
            holder.btnAddQty.setOnClickListener(view -> {
                if (ApiConfig.isConnected(activity)) {
                    if (!(Integer.parseInt(holder.tvQuantity.getText().toString()) >= Float.parseFloat(cart.getItem().get(0).getStock()))) {
                        if (!(Integer.parseInt(holder.tvQuantity.getText().toString()) + 1 > Integer.parseInt(session.getData(Constant.max_cart_items_count)))) {
                            int count = Integer.parseInt(holder.tvQuantity.getText().toString());
                            count++;
                            cart.getItem().get(0).setCart_count("" + count);
                            holder.tvQuantity.setText("" + count);
                            holder.tvTotalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + finalPrice * count));
                            Constant.FLOAT_TOTAL_AMOUNT = Constant.FLOAT_TOTAL_AMOUNT + finalPrice;
                            databaseHelper.AddToCart(cart.getId(), cart.getProduct_id(), "" + count);
                            CartFragment.SetData(activity);
                        } else {
                            Toast.makeText(activity, activity.getString(R.string.limit_alert), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(activity, activity.getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
                    }
                }

            });

            holder.btnMinusQty.setOnClickListener(view -> {
                if (ApiConfig.isConnected(activity)) {
                    if (Integer.parseInt(holder.tvQuantity.getText().toString()) > 1) {
                        int count = Integer.parseInt(holder.tvQuantity.getText().toString());
                        count--;
                        cart.getItem().get(0).setCart_count("" + count);
                        holder.tvQuantity.setText("" + count);
                        holder.tvTotalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + finalPrice * count));
                        Constant.FLOAT_TOTAL_AMOUNT = Constant.FLOAT_TOTAL_AMOUNT - finalPrice;
                        databaseHelper.AddToCart(cart.getId(), cart.getProduct_id(), "" + count);
                        CartFragment.SetData(activity);
                    }
                }
            });

            if (getItemCount() == 0) {
                CartFragment.lytTotal.setVisibility(View.GONE);
            } else {
                CartFragment.lytTotal.setVisibility(View.VISIBLE);
            }

        } else if (holderParent instanceof ViewHolderLoading) {
            ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return CartFragment.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return CartFragment.items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        OfflineCart cart = CartFragment.items.get(position);
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
        final ImageView btnMinusQty;
        final ImageView btnAddQty;
        final TextView tvProductName;
        final TextView tvMeasurement;
        final TextView tvPrice;
        final TextView tvOriginalPrice;
        final TextView tvQuantity;
        final TextView tvTotalPrice;
        final TextView tvDelete;
        final TextView tvAction;
        final RelativeLayout lytMain;

        public HolderItems(@NonNull View itemView) {
            super(itemView);
            lytMain = itemView.findViewById(R.id.lytMain);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvDelete = itemView.findViewById(R.id.tvDelete);
            tvAction = itemView.findViewById(R.id.tvAction);

            btnMinusQty = itemView.findViewById(R.id.btnMinusQty);
            btnAddQty = itemView.findViewById(R.id.btnAddQty);

            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvMeasurement = itemView.findViewById(R.id.tvMeasurement);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        }
    }

    void showUndoSnackBar(OfflineCart cart, int position) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.undo_message), Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, R.color.gray));
        snackbar.setAction(activity.getString(R.string.undo), view -> {
            snackbar.dismiss();
            databaseHelper.AddToCart(cart.getItem().get(0).getId(), cart.getItem().get(0).getProduct_id(), cart.getItem().get(0).getCart_count());

            String taxPercentage1 = "0";
            try {
                taxPercentage1 = (Double.parseDouble(cart.getTax_percentage()) > 0 ? cart.getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();

            }

            double price;
            if (cart.getItem().get(0).getDiscounted_price().equals("0") || cart.getItem().get(0).getDiscounted_price().equals("")) {
                price = ((Float.parseFloat(cart.getItem().get(0).getPrice()) + ((Float.parseFloat(cart.getItem().get(0).getPrice()) * Float.parseFloat(taxPercentage1)) / 100)));
            } else {
                price = ((Float.parseFloat(cart.getItem().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItem().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage1)) / 100)));
            }

            add(position, cart);

            notifyDataSetChanged();
            CartFragment.isSoldOut = false;
            Constant.TOTAL_CART_ITEM = getItemCount();
            Constant.FLOAT_TOTAL_AMOUNT += (price * Integer.parseInt(cart.getItem().get(0).getCart_count()));
            CartFragment.values.put(cart.getItem().get(0).getId(), databaseHelper.CheckCartItemExist(cart.getItem().get(0).getId(), cart.getItem().get(0).getProduct_id()));
            CartFragment.SetData(activity);
            activity.invalidateOptionsMenu();

        }).setActionTextColor(Color.WHITE);

        View snackBarView = snackbar.getView();
        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }
}