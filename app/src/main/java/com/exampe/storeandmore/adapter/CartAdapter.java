package com.exampe.storeandmore.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.fragment.CartFragment;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Cart;

@SuppressLint("NotifyDataSetChanged")
public class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final Session session;
    String taxPercentage;
    final String from;


    public CartAdapter(Activity activity, String from) {
        this.activity = activity;
        this.from = from;
        session = new Session(activity);
        taxPercentage = "0";
    }

    public void add(int position, Cart item) {
        CartFragment.carts.add(item);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        Cart cart = CartFragment.carts.get(position);

        totalCalculate(cart);

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


        CartFragment.carts.remove(cart);
        CartFragment.isSoldOut = false;
        notifyDataSetChanged();
        Constant.TOTAL_CART_ITEM = getItemCount();
        CartFragment.SetData(activity);
        activity.invalidateOptionsMenu();
        if (getItemCount() == 0 && CartFragment.saveForLater.size() == 0) {
            CartFragment.lytEmpty.setVisibility(View.VISIBLE);
            CartFragment.lytTotal.setVisibility(View.GONE);
        } else {
            CartFragment.lytEmpty.setVisibility(View.GONE);
            CartFragment.lytTotal.setVisibility(View.VISIBLE);
        }
        showUndoSnackBar(cart, position);
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

        Constant.FLOAT_TOTAL_AMOUNT -= (price * Integer.parseInt(cart.getQty()));
    }

    @SuppressLint("SetTextI18n")
    public void moveItem(int position) {
        try {
            Cart cart = CartFragment.carts.get(position);
            totalCalculate(cart);

            CartFragment.carts.remove(cart);
            CartFragment.cartAdapter.notifyDataSetChanged();

            CartFragment.saveForLater.add(cart);
            CartFragment.saveForLaterAdapter.notifyDataSetChanged();

            if (CartFragment.lytSaveForLater.getVisibility() == View.GONE)
                CartFragment.lytSaveForLater.setVisibility(View.VISIBLE);

            CartFragment.tvSaveForLaterTitle.setText(activity.getResources().getString(R.string.save_for_later) + " (" + CartFragment.saveForLater.size() + ")");

            CartFragment.saveForLaterValues.put(cart.getProduct_variant_id(), cart.getQty());

            Constant.TOTAL_CART_ITEM = getItemCount();
            CartFragment.SetData(activity);

            if (getItemCount() == 0)
                CartFragment.lytTotal.setVisibility(View.GONE);

            ApiConfig.AddMultipleProductInSaveForLater(session, activity, CartFragment.saveForLaterValues);

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
                view = LayoutInflater.from(activity).inflate(R.layout.lyt_cartlist, parent, false);
                return new HolderItems(view);
            case (VIEW_TYPE_LOADING):
                view = LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false);
                return new ViewHolderLoading(view);
            default:
                throw new IllegalArgumentException("unexpected viewType: " + viewType);
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holderParent, final int position) {

        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                final HolderItems holder = (HolderItems) holderParent;
                final Cart cart = CartFragment.carts.get(position);

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
                    holder.lytQuantity.setVisibility(View.GONE);
                    CartFragment.isSoldOut = true;
                } else if (Float.parseFloat(cart.getQty()) > Float.parseFloat(cart.getItems().get(0).getStock())) {
                    holder.tvStatus.setVisibility(View.VISIBLE);
                    holder.tvStatus.setText(activity.getString(R.string.low_stock_warning1) + cart.getItems().get(0).getStock() + activity.getString(R.string.low_stock_warning2));
                    CartFragment.isSoldOut = true;
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

                holder.tvQuantity.setText(cart.getQty());

                holder.tvTotalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + price * Integer.parseInt(cart.getQty())));

                if (from.equals("cart")) {
                    final double finalPrice = price;
                    holder.btnAddQty.setOnClickListener(view -> {

                        if (ApiConfig.isConnected(activity)) {
                            if (!(Integer.parseInt(holder.tvQuantity.getText().toString()) >= Float.parseFloat(cart.getItems().get(0).getStock()))) {
                                if (!(Integer.parseInt(holder.tvQuantity.getText().toString()) + 1 > Integer.parseInt(session.getData(Constant.max_cart_items_count)))) {
                                    int count = Integer.parseInt(holder.tvQuantity.getText().toString());
                                    count++;
                                    cart.setQty("" + count);
                                    holder.tvQuantity.setText("" + count);
                                    holder.tvTotalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + finalPrice * count));
                                    Constant.FLOAT_TOTAL_AMOUNT = Constant.FLOAT_TOTAL_AMOUNT + finalPrice;
                                    if (CartFragment.values.containsKey(cart.getProduct_variant_id())) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            CartFragment.values.replace(cart.getProduct_variant_id(), "" + count);
                                        } else {
                                            CartFragment.values.remove(cart.getProduct_variant_id());
                                            CartFragment.values.put(cart.getProduct_variant_id(), "" + count);
                                        }
                                    } else {
                                        CartFragment.values.put(cart.getProduct_variant_id(), "" + count);
                                    }
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
                                cart.setQty("" + count);
                                if (Float.parseFloat(cart.getQty()) <= Float.parseFloat(cart.getItems().get(0).getStock()) && holder.tvStatus.getVisibility() == View.VISIBLE) {
                                    holder.tvStatus.setVisibility(View.GONE);
                                    CartFragment.isSoldOut = false;
                                    CartFragment.cartAdapter.notifyDataSetChanged();
                                }
                                holder.tvQuantity.setText("" + count);
                                holder.tvTotalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + finalPrice * count));
                                Constant.FLOAT_TOTAL_AMOUNT = Constant.FLOAT_TOTAL_AMOUNT - finalPrice;
                                if (CartFragment.values.containsKey(cart.getProduct_variant_id())) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        CartFragment.values.replace(cart.getProduct_variant_id(), "" + count);
                                    } else {
                                        CartFragment.values.remove(cart.getProduct_variant_id());
                                        CartFragment.values.put(cart.getProduct_variant_id(), "" + count);
                                    }
                                } else {
                                    CartFragment.values.put(cart.getProduct_variant_id(), "" + count);
                                }
                                CartFragment.SetData(activity);
                            }
                        }
                    });
                } else {
                    holder.btnAddQty.setVisibility(View.INVISIBLE);
                    holder.btnMinusQty.setVisibility(View.INVISIBLE);
                    holder.tvQuantity.setVisibility(View.INVISIBLE);
                    holder.tvTotalPrice.setVisibility(View.GONE);
                    holder.tvAction.setText(activity.getString(R.string.move_to_cart));
                }
                break;
            case VIEW_TYPE_LOADING:
                ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
                loadingViewHolder.progressBar.setIndeterminate(true);
                break;
        }


    }

    @Override
    public int getItemCount() {
        return CartFragment.carts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return CartFragment.carts.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        Cart cart = CartFragment.carts.get(position);
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
        final TextView tvStatus;
        final TextView tvDelete;
        final TextView tvAction;
        final LinearLayout lytQuantity;
        final RelativeLayout lytMain;

        public HolderItems(@NonNull View itemView) {
            super(itemView);
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
            tvStatus = itemView.findViewById(R.id.tvStatus);

            lytQuantity = itemView.findViewById(R.id.lytQuantity);
            lytMain = itemView.findViewById(R.id.lytMain);
        }
    }

    void showUndoSnackBar(Cart cart, int position) {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.undo_message), Snackbar.LENGTH_LONG);
        snackbar.setAction(activity.getString(R.string.undo), view -> {
            snackbar.dismiss();
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

            CartFragment.values.put(cart.getProduct_variant_id(), cart.getQty());

            add(position, cart);
            notifyDataSetChanged();
            CartFragment.SetData(activity);
            CartFragment.isSoldOut = false;
            Constant.TOTAL_CART_ITEM = getItemCount();
            CartFragment.SetData(activity);
            if (getItemCount() != 0) {
                CartFragment.lytTotal.setVisibility(View.VISIBLE);
                CartFragment.lytEmpty.setVisibility(View.GONE);
            }
            ApiConfig.AddMultipleProductInCart(session, activity, CartFragment.values);
            activity.invalidateOptionsMenu();
        });
        snackbar.setActionTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }
}