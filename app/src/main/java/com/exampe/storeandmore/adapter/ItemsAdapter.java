package com.exampe.storeandmore.adapter;

import static com.exampe.storeandmore.fragment.TrackerDetailFragment.pBar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.activity.MainActivity;
import com.exampe.storeandmore.fragment.TrackerDetailFragment;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.OrderItem;
import com.exampe.storeandmore.model.OrderTracker;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

    final Activity activity;
    final ArrayList<OrderItem> orderTrackerArrayList;
    final OrderTracker orderTracker;
    final Session session;
    final String from;

    public ItemsAdapter(Activity activity, OrderTracker orderTracker, String from) {
        this.activity = activity;
        this.orderTracker = orderTracker;
        this.orderTrackerArrayList = orderTracker.getItems();
        this.from = from;
        session = new Session(activity);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_items, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        try {

            final OrderItem orderItem = orderTrackerArrayList.get(position);

            String payType;
            if (orderTracker.getPayment_method().equalsIgnoreCase("cod"))
                payType = activity.getResources().getString(R.string.cod);
            else
                payType = orderTracker.getPayment_method();
            String activeStatus = orderTracker.getActive_status().substring(0, 1).toUpperCase() + orderTracker.getActive_status().substring(1).toLowerCase();
            holder.tvQuantity.setText(orderItem.getQuantity());

            String taxPercentage = orderTracker.getTax_percentage();
            double price;

            if (orderItem.getDiscounted_price().equals("0") || orderItem.getDiscounted_price().equals("")) {
                price = ((Float.parseFloat(orderItem.getPrice()) + ((Float.parseFloat(orderItem.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
            } else {
                price = ((Float.parseFloat(orderItem.getDiscounted_price()) + ((Float.parseFloat(orderItem.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
            }
            holder.tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + price));

            holder.tvPayType.setText(activity.getResources().getString(R.string.via) + payType);
            holder.tvStatus.setText(activeStatus);
            if (activeStatus.equalsIgnoreCase(Constant.AWAITING_PAYMENT)) {
                holder.tvStatus.setText(activity.getString(R.string.awaiting_payment));
            }
            holder.tvStatusDate.setText(orderTracker.getDate_added());
            holder.tvName.setText(orderItem.getName() + "(" + orderItem.getMeasurement() + orderItem.getUnit() + ")");

            Picasso.get().
                    load(orderItem.getImage())
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.app_icon)
                    .error(R.drawable.app_icon)
                    .into(holder.imgOrder);

            holder.tvCardDetail.setOnClickListener(v -> {
                Fragment fragment = new TrackerDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", "");
                bundle.putSerializable("model", orderTracker);
                fragment.setArguments(bundle);
                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            });

            holder.btnCancel.setOnClickListener(view -> updateOrderStatus(activity, orderTracker, Constant.CANCELLED, holder, from));

            holder.btnReturn.setOnClickListener(view -> {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                Date date = new Date();
                //System.out.println (myFormat.format (date));
                String inputString1 = orderTracker.getDate_added();
                String inputString2 = myFormat.format(date);
                try {
                    Date date1 = myFormat.parse(inputString1);
                    Date date2 = myFormat.parse(inputString2);
                    assert date1 != null;
                    assert date2 != null;
                    long diff = date2.getTime() - date1.getTime();
                    // System.out.println("Days: "+TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

                    if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= Integer.parseInt(new Session(activity).getData(Constant.max_product_return_days))) {
                        updateOrderStatus(activity, orderTracker, Constant.RETURNED, holder, from);

                    } else {
                        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getResources().getString(R.string.product_return) + Integer.parseInt(new Session(activity).getData(Constant.max_product_return_days)) + activity.getString(R.string.day_max_limit), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(activity.getResources().getString(R.string.ok), view1 -> snackbar.dismiss());
                        snackbar.setActionTextColor(Color.RED);
                        View snackBarView = snackbar.getView();
                        TextView textView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setMaxLines(5);
                        snackbar.show();

                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
            if (from.equals("detail")) {
                if (orderTracker.getActive_status().equalsIgnoreCase("delivered") && session.getData(Constant.ratings).equals("1")) {
                    holder.lytRatings.setVisibility(View.VISIBLE);
                    if (orderItem.isReview_status()) {
                        holder.ratingProduct.setRating(Float.parseFloat(orderItem.getRate()));
                        holder.tvAddUpdateReview.setText(R.string.update_review);
                    }
                } else {
                    holder.lytRatings.setVisibility(View.GONE);
                }

                holder.ratingProduct.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> AddUpdateReview(holder, orderItem, ratingBar.getRating(), orderItem.getReview(), Boolean.parseBoolean(orderItem.getReturn_status()), orderItem.getProduct_id()));

                holder.tvAddUpdateReview.setOnClickListener(v -> AddUpdateReview(holder, orderItem, holder.ratingProduct.getRating(), orderItem.getReview(), Boolean.parseBoolean(orderItem.getReturn_status()), orderItem.getProduct_id()));

                if (orderTracker.getActive_status().equalsIgnoreCase("cancelled")) {
                    holder.tvStatus.setTextColor(Color.RED);
                    holder.btnCancel.setVisibility(View.GONE);
                } else if (orderTracker.getActive_status().equalsIgnoreCase("delivered")) {
                    holder.btnCancel.setVisibility(View.GONE);
                    if (orderItem.getReturn_status().equalsIgnoreCase("1")) {
                        holder.btnReturn.setVisibility(View.VISIBLE);
                    } else {
                        holder.btnReturn.setVisibility(View.GONE);
                    }
                } else if (orderTracker.getActive_status().equalsIgnoreCase("returned")) {
                    holder.btnCancel.setVisibility(View.GONE);
                    holder.btnReturn.setVisibility(View.GONE);
                } else {
                    if (orderItem.getCancelable_status().equalsIgnoreCase("1")) {
                        if (orderItem.getTill_status().equalsIgnoreCase("received")) {
                            if (orderTracker.getActive_status().equalsIgnoreCase("received")) {
                                holder.btnCancel.setVisibility(View.VISIBLE);
                            } else {
                                holder.btnCancel.setVisibility(View.GONE);
                            }
                        } else if (orderItem.getTill_status().equalsIgnoreCase("processed")) {
                            if (orderTracker.getActive_status().equalsIgnoreCase("received") || orderTracker.getActive_status().equalsIgnoreCase("processed")) {
                                holder.btnCancel.setVisibility(View.VISIBLE);
                            } else {
                                holder.btnCancel.setVisibility(View.GONE);
                            }
                        } else if (orderItem.getTill_status().equalsIgnoreCase("shipped")) {
                            if (orderTracker.getActive_status().equalsIgnoreCase("received") || orderTracker.getActive_status().equalsIgnoreCase("processed") || orderTracker.getActive_status().equalsIgnoreCase("shipped")) {
                                holder.btnCancel.setVisibility(View.VISIBLE);
                            } else {
                                holder.btnCancel.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        holder.btnCancel.setVisibility(View.GONE);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateOrderStatus(final Activity activity, final OrderTracker orderTracker, final String status, final ViewHolder holder, final String from) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        if (status.equals(Constant.CANCELLED)) {
            alertDialog.setTitle(activity.getResources().getString(R.string.cancel_order));
            alertDialog.setMessage(activity.getResources().getString(R.string.cancel_msg));
        } else if (status.equals(Constant.RETURNED)) {
            alertDialog.setTitle(activity.getResources().getString(R.string.return_order));
            alertDialog.setMessage(activity.getResources().getString(R.string.return_msg));
        }
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(activity.getResources().getString(R.string.yes), (dialog, which) -> {
            final Map<String, String> params = new HashMap<>();
            params.put(Constant.UPDATE_ORDER_ITEM_STATUS, Constant.GetVal);
            params.put(Constant.ORDER_ITEM_ID, orderTracker.getId());
            params.put(Constant.ORDER_ID, orderTracker.getId());
            params.put(Constant.STATUS, status);
            if (pBar != null)
                pBar.setVisibility(View.VISIBLE);
            ApiConfig.RequestToVolley((result, response) -> {
                // System.out.println("================= " + response);
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean(Constant.ERROR)) {
                            if (status.equals(Constant.CANCELLED)) {
                                holder.btnCancel.setVisibility(View.GONE);
                                holder.tvStatus.setText(status);
                                holder.tvStatus.setTextColor(Color.RED);
                                if (from.equals("detail")) {
                                    if (orderTrackerArrayList.size() == 1) {
                                        TrackerDetailFragment.btnCancel.setVisibility(View.GONE);
                                        TrackerDetailFragment.lytTracker.setVisibility(View.GONE);
                                    }
                                }
                                ApiConfig.getWalletBalance(activity, new Session(activity));
                            } else {
                                holder.btnReturn.setVisibility(View.GONE);
                                holder.tvStatus.setText(status);
                            }
                            Constant.isOrderCancelled = true;
                        }
                        Toast.makeText(activity, object.getString("message"), Toast.LENGTH_LONG).show();
                        if (pBar != null)
                            pBar.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, activity, Constant.ORDER_PROCESS_URL, params, false);

        });
        alertDialog.setNegativeButton(activity.getResources().getString(R.string.no), (dialog, which) -> alertDialog1.dismiss());
        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return orderTrackerArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvQuantity;
        final TextView tvPrice;
        final TextView tvPayType;
        final TextView tvStatus;
        final TextView tvStatusDate;
        final TextView tvName;
        final ImageView imgOrder;
        final CardView tvCardDetail;
        final Button btnCancel;
        final Button btnReturn;
        final RelativeLayout lytRatings;
        final RatingBar ratingProduct;
        final TextView tvAddUpdateReview;

        public ViewHolder(View itemView) {
            super(itemView);

            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPayType = itemView.findViewById(R.id.tvPayType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvStatusDate = itemView.findViewById(R.id.tvStatusDate);
            tvName = itemView.findViewById(R.id.tvName);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            imgOrder = itemView.findViewById(R.id.imgOrder);
            tvCardDetail = itemView.findViewById(R.id.tvCardDetail);
            btnReturn = itemView.findViewById(R.id.btnReturn);
            lytRatings = itemView.findViewById(R.id.lytRatings);
            ratingProduct = itemView.findViewById(R.id.ratingProduct);
            tvAddUpdateReview = itemView.findViewById(R.id.tvAddUpdateReview);
        }
    }

    public void AddUpdateReview(ViewHolder holder, OrderItem orderItem, Float rating, String review, boolean isUpdate, String productId) {
        try {
            @SuppressLint("InflateParams") View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_review, null);
            ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }

            final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetTheme);
            mBottomSheetDialog.setContentView(sheetView);
            if (!new Session(activity).getBoolean("update_skip")) {
                mBottomSheetDialog.show();
            }

            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            ImageView imgClose = sheetView.findViewById(R.id.imgClose);
            RatingBar ratingProduct = sheetView.findViewById(R.id.ratingProduct);
            EditText edtReviewMessage = sheetView.findViewById(R.id.edtReviewMessage);
            Button btnCancel = sheetView.findViewById(R.id.btnCancel);
            Button btnVerify = sheetView.findViewById(R.id.btnVerify);

            mBottomSheetDialog.setCancelable(true);
            if (isUpdate) {
                edtReviewMessage.setText(review);
            }
            ratingProduct.setRating(rating);

            imgClose.setOnClickListener(v -> mBottomSheetDialog.dismiss());

            btnCancel.setOnClickListener(v -> mBottomSheetDialog.dismiss());

            btnVerify.setOnClickListener(view -> {
                if (pBar != null)
                    pBar.setVisibility(View.VISIBLE);
                try {
                    SetReview(holder, orderItem, ratingProduct.getRating(), edtReviewMessage.getText().toString(), productId, mBottomSheetDialog);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void SetReview(ViewHolder holder, OrderItem orderItem, Float rating, String review, String productId, BottomSheetDialog mBottomSheetDialog) {
        {
            final Map<String, String> params = new HashMap<>();
            params.put(Constant.ADD_PRODUCT_REVIEW, Constant.GetVal);
            params.put(Constant.PRODUCT_ID, productId);
            params.put(Constant.USER_ID, session.getData(Constant.ID));
            params.put(Constant.RATE, "" + rating);
            params.put(Constant.REVIEW, review);
            if (pBar != null)
                pBar.setVisibility(View.VISIBLE);
            ApiConfig.RequestToVolley((result, response) -> {
                // System.out.println("================= " + response);
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean(Constant.ERROR)) {
                            holder.ratingProduct.setRating(rating);
                            holder.tvAddUpdateReview.setText(R.string.update_review);
                            holder.tvAddUpdateReview.setText(R.string.update_review);
                            orderItem.setReview_status(true);
                            orderItem.setRate("" + rating);
                            orderItem.setReview(review);
                            notifyDataSetChanged();
                        }
                        Toast.makeText(activity, object.getString("message"), Toast.LENGTH_LONG).show();
                        mBottomSheetDialog.dismiss();
                        if (pBar != null)
                            pBar.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, activity, Constant.GET_ALL_PRODUCTS_URL, params, false);

        }
    }


}
