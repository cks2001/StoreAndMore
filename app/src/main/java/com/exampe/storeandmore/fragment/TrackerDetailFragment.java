package com.exampe.storeandmore.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.exampe.storeandmore.R;
import com.exampe.storeandmore.adapter.ImageAdapter;
import com.exampe.storeandmore.adapter.ItemsAdapter;
import com.exampe.storeandmore.adapter.ProductImagesAdapter;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.helper.album.Album;
import com.exampe.storeandmore.helper.album.AlbumFile;
import com.exampe.storeandmore.helper.album.api.widget.Widget;
import com.exampe.storeandmore.helper.album.widget.divider.Api21ItemDivider;
import com.exampe.storeandmore.helper.album.widget.divider.Divider;
import com.exampe.storeandmore.model.OrderTracker;

@SuppressLint("NotifyDataSetChanged")
public class TrackerDetailFragment extends Fragment {
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar pBar;
    @SuppressLint("StaticFieldLeak")
    public static Button btnCancel;
    Button btnReorder, btnGetSellerDirection, btnCallToSeller;
    @SuppressLint("StaticFieldLeak")
    public static LinearLayout lytTracker, lytPickUp;
    View root;
    OrderTracker order;
    TextView tvOrderOTP, tvItemTotal, tvDeliveryCharge, tvTotal, tvPromoCode,
            tvPCAmount, tvWallet, tvFinalTotal, tvDPercent, tvDAmount, tvCancelDetail,
            tvOtherDetails, tvOrderID, tvOrderDate, tvPickUpAddress, btnOtherImages, btnSubmit,
            tvReceiptStatus, tvReceiptStatusReason, tvPickupTime, tvBankDetail;
    RecyclerView recyclerView, recyclerViewImageGallery, recyclerViewReceiptImages;
    RelativeLayout relativeLyt, lytReceipt;
    LinearLayout lytPromo, lytWallet, lytPriceDetail, lytOTP;
    double totalAfterTax = 0.0;
    Activity activity;
    String id;
    Session session;
    HashMap<String, String> hashMap;
    LinearLayout lytMainTracker;
    ScrollView scrollView;
    private ShimmerFrameLayout mShimmerViewContainer;
//    Toolbar toolbar;

    LinearLayout lytReceipt_;
    private ArrayList<AlbumFile> mAlbumFiles;
    private ImageAdapter mAdapter;
    ProductImagesAdapter productImagesAdapter;
    public static ProgressBar progressBar;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_tracker_detail, container, false);
        activity = getActivity();
        session = new Session(activity);

        ApiConfig.GetSettings(activity);

        pBar = root.findViewById(R.id.pBar);
        lytReceipt_ = root.findViewById(R.id.lytReceipt_);
        lytPriceDetail = root.findViewById(R.id.lytPriceDetail);
        btnOtherImages = root.findViewById(R.id.btnOtherImages);
        btnSubmit = root.findViewById(R.id.btnSubmit);
        recyclerViewImageGallery = root.findViewById(R.id.recyclerViewImageGallery);
        recyclerViewReceiptImages = root.findViewById(R.id.recyclerViewReceiptImages);
        lytPromo = root.findViewById(R.id.lytPromo);
        lytWallet = root.findViewById(R.id.lytWallet);
        tvItemTotal = root.findViewById(R.id.tvItemTotal);
        tvDeliveryCharge = root.findViewById(R.id.tvDeliveryCharge);
        tvDAmount = root.findViewById(R.id.tvDAmount);
        tvDPercent = root.findViewById(R.id.tvDPercent);
        tvTotal = root.findViewById(R.id.tvTotal);
        progressBar = root.findViewById(R.id.progressBar);
        tvPromoCode = root.findViewById(R.id.tvPromoCode);
        tvPCAmount = root.findViewById(R.id.tvPCAmount);
        tvWallet = root.findViewById(R.id.tvWallet);
        tvFinalTotal = root.findViewById(R.id.tvFinalTotal);
        tvOrderID = root.findViewById(R.id.tvOrderID);
        tvOrderDate = root.findViewById(R.id.tvOrderDate);
        tvBankDetail = root.findViewById(R.id.tvBankDetail);
        relativeLyt = root.findViewById(R.id.relativeLyt);
        tvOtherDetails = root.findViewById(R.id.tvOtherDetails);
        tvCancelDetail = root.findViewById(R.id.tvCancelDetail);
        tvReceiptStatusReason = root.findViewById(R.id.tvReceiptStatusReason);
        tvPickupTime = root.findViewById(R.id.tvPickupTime);
        lytReceipt = root.findViewById(R.id.lytReceipt);
        lytTracker = root.findViewById(R.id.lytTracker);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setNestedScrollingEnabled(false);
        btnCancel = root.findViewById(R.id.btnCancel);
        btnReorder = root.findViewById(R.id.btnReorder);
        tvOrderOTP = root.findViewById(R.id.tvOrderOTP);
        tvReceiptStatus = root.findViewById(R.id.tvReceiptStatus);
        lytOTP = root.findViewById(R.id.lytOTP);
        lytMainTracker = root.findViewById(R.id.lytMainTracker);
        scrollView = root.findViewById(R.id.scrollView);
        lytPickUp = root.findViewById(R.id.lytPickUp);
        btnGetSellerDirection = root.findViewById(R.id.btnGetSellerDirection);
        btnCallToSeller = root.findViewById(R.id.btnCallToSeller);
        tvPickUpAddress = root.findViewById(R.id.tvPickUpAddress);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);

        hashMap = new HashMap<>();

        GetPaymentConfig();

        mAlbumFiles = new ArrayList<>();
        recyclerViewImageGallery.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewReceiptImages = root.findViewById(R.id.recyclerViewReceiptImages);

        recyclerViewReceiptImages.setLayoutManager(new GridLayoutManager(activity, 3));
        Divider divider = new Api21ItemDivider(Color.TRANSPARENT, 10, 10);
        recyclerViewReceiptImages.addItemDecoration(divider);
        recyclerViewReceiptImages.setNestedScrollingEnabled(false);
        mAdapter = new ImageAdapter(activity, (view, position) -> previewImage(position));
        recyclerViewReceiptImages.setAdapter(mAdapter);


        assert getArguments() != null;
        id = getArguments().getString("id");
        if (id.equals("")) {
            order = (OrderTracker) getArguments().getSerializable("model");
            id = order.getId();
            SetData(order);
        } else {
            getOrderDetails(id);
        }

        tvBankDetail.setOnClickListener(v -> openBankDetails());

        setHasOptionsMenu(true);

        btnReorder.setOnClickListener(view -> new AlertDialog.Builder(activity)
                .setTitle(getString(R.string.re_order))
                .setMessage(getString(R.string.reorder_msg))
                .setPositiveButton(getString(R.string.proceed), (dialog, which) -> {
                    if (activity != null) {
                        GetReOrderData();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss()).show());

        btnOtherImages.setOnClickListener(v -> {
            lytReceipt_.setVisibility(View.VISIBLE);
            SelectImage("multi");
        });

        btnSubmit.setOnClickListener(v -> {
            if (mAlbumFiles != null && mAlbumFiles.size() > 0) {
                progressBar.setVisibility(View.VISIBLE);
                submitReceipt();
            } else {
                Toast.makeText(activity, activity.getString(R.string.no_receipt_select_message), Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(view -> {

            final Map<String, String> params = new HashMap<>();
            params.put(Constant.UPDATE_ORDER_STATUS, Constant.GetVal);
            params.put(Constant.ID, order.getId());
            params.put(Constant.STATUS, Constant.CANCELLED);
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            // Setting Dialog Message
            alertDialog.setTitle(activity.getResources().getString(R.string.cancel_order));
            alertDialog.setMessage(activity.getResources().getString(R.string.cancel_msg));
            alertDialog.setCancelable(false);
            final AlertDialog alertDialog1 = alertDialog.create();

            // Setting OK Button
            alertDialog.setPositiveButton(activity.getResources().getString(R.string.yes), (dialog, which) -> {
                if (pBar != null)
                    pBar.setVisibility(View.VISIBLE);
                ApiConfig.RequestToVolley((result, response) -> {
                    // System.out.println("================= " + response);
                    if (result) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean(Constant.ERROR)) {
                                btnCancel.setVisibility(View.GONE);
                                ApiConfig.getWalletBalance(activity, new Session(activity));
                            }
                            Toast.makeText(activity, object.getString(Constant.MESSAGE), Toast.LENGTH_LONG).show();
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
        });

        return root;
    }


    public void GetPaymentConfig() {
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SETTINGS, Constant.GetVal);
        params.put(Constant.GET_PAYMENT_METHOD, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        if (jsonObject.has(Constant.PAYMENT_METHODS)) {
                            JSONObject object = jsonObject.getJSONObject(Constant.PAYMENT_METHODS);

                            if (object.has(Constant.direct_bank_transfer_method)) {
                                Constant.DIRECT_BANK_TRANSFER = object.getString(Constant.direct_bank_transfer_method);
                                Constant.ACCOUNT_NAME = object.getString(Constant.account_name);
                                Constant.ACCOUNT_NUMBER = object.getString(Constant.account_number);
                                Constant.BANK_NAME = object.getString(Constant.bank_name);
                                Constant.BANK_CODE = object.getString(Constant.bank_code);
                                Constant.NOTES = object.getString(Constant.notes);
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }

    public void openBankDetails() {
        {
            @SuppressLint("InflateParams") View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_bank_detail, null);
            ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }

            final Dialog mBottomSheetDialog = new Dialog(activity);
            mBottomSheetDialog.setContentView(sheetView);
            mBottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mBottomSheetDialog.show();

            TextView tvAccountName = sheetView.findViewById(R.id.tvAccountName);
            TextView tvAccountNumber = sheetView.findViewById(R.id.tvAccountNumber);
            TextView tvBankName = sheetView.findViewById(R.id.tvBankName);
            TextView tvIFSCCode = sheetView.findViewById(R.id.tvIFSCCode);
            TextView tvExtraNote = sheetView.findViewById(R.id.tvExtraNote);

            tvAccountName.setText(Constant.ACCOUNT_NAME);
            tvAccountNumber.setText(Constant.ACCOUNT_NUMBER);
            tvBankName.setText(Constant.BANK_NAME);
            tvIFSCCode.setText(Constant.BANK_CODE);
            tvExtraNote.setText(Constant.NOTES);

        }
    }

    public void GetReOrderData() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_REORDER_DATA, Constant.GetVal);
        params.put(Constant.ID, id);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONObject(Constant.DATA).getJSONArray(Constant.ITEMS);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        hashMap.put(jsonArray.getJSONObject(i).getString(Constant.PRODUCT_VARIANT_ID), jsonArray.getJSONObject(i).getString(Constant.QUANTITY));
                    }
                    ApiConfig.AddMultipleProductInCart(session, activity, hashMap);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false);
    }

    public void getOrderDetails(String id) {
        scrollView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ORDERS, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.ORDER_ID, id);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        SetData(ApiConfig.GetOrders(jsonObject.getJSONArray(Constant.DATA)).get(0));
                    } else {
                        scrollView.setVisibility(View.VISIBLE);
                        mShimmerViewContainer.setVisibility(View.GONE);
                        mShimmerViewContainer.stopShimmer();
                    }
                } catch (JSONException e) {
                    scrollView.setVisibility(View.VISIBLE);
                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmer();
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false);
    }


    public void SelectImage(String type) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            if (type.equals("multi")) {
                selectOtherImage();
            }


        }
    }

    private void previewImage(int position) {
        if (mAlbumFiles == null || mAlbumFiles.size() == 0) {
            Toast.makeText(activity, R.string.no_selected, Toast.LENGTH_LONG).show();
        } else {
            Album.galleryAlbum(activity)
                    .checkable(true)
                    .checkedList(mAlbumFiles)
                    .currentPosition(position)
                    .widget(Widget.newDarkBuilder(activity).build()
                    )
                    .onResult(result -> {
                        mAlbumFiles = result;
                        mAdapter.notifyDataSetChanged(mAlbumFiles);
                        //mTvMessage.setVisibility(result.size() > 0 ? View.VISIBLE : View.GONE);
                    })
                    .start();
        }
    }

    private void selectOtherImage() {
        Album.image(activity)
                .multipleChoice()
                .camera(true)
                .columnCount(2)
                .selectCount(6)
                .checkedList(mAlbumFiles)
                .widget(Widget.newDarkBuilder(activity)
                        .build()
                )
                .onResult(result -> {
                    mAlbumFiles = result;
                    mAdapter.notifyDataSetChanged(mAlbumFiles);
                    //mTvMessage.setVisibility(result.size() > 0 ? View.VISIBLE : View.GONE);
                })
                .onCancel(result -> {
                    //Toast.makeText(AddBusinessActivity.this, "Cancel", Toast.LENGTH_LONG).show();
                })
                .start();
    }


    @SuppressLint("SetTextI18n")
    public void SetData(OrderTracker order) {
        try {
            tvOrderID.setText(order.getId());
            if (order.getOtp().equals("0") || order.getOtp().equals("")) {
                lytOTP.setVisibility(View.GONE);
            } else {
                tvOrderOTP.setText(order.getOtp());
            }
            tvOrderDate.setText(order.getDate_added());
            tvOtherDetails.setText(getString(R.string.name_1) + order.getUser_name() + getString(R.string.mobile_no_1) + order.getMobile() + getString(R.string.address_1) + order.getAddress());
            totalAfterTax = (Double.parseDouble(order.getTotal()) + Double.parseDouble(order.getDelivery_charge()) + Double.parseDouble(order.getTax_amount()));
            tvItemTotal.setText(session.getData(Constant.currency) + ApiConfig.StringFormat(order.getTotal()));
            tvDeliveryCharge.setText("+ " + session.getData(Constant.currency) + ApiConfig.StringFormat(order.getDelivery_charge()));
            tvDPercent.setText(getString(R.string.discount) + "(" + order.getDiscount() + "%) :");
            tvDAmount.setText("- " + session.getData(Constant.currency) + ApiConfig.StringFormat(order.getDiscount_rupees()));
            tvTotal.setText(session.getData(Constant.currency) + totalAfterTax);
            tvPCAmount.setText("- " + session.getData(Constant.currency) + ApiConfig.StringFormat(order.getPromo_discount()));
            tvWallet.setText("- " + session.getData(Constant.currency) + ApiConfig.StringFormat(order.getWallet_balance()));
            tvFinalTotal.setText(session.getData(Constant.currency) + ApiConfig.StringFormat(order.getFinal_total()));

            lytTracker.setWeightSum(order.getStatus_name().size() + (order.getStatus_name().size() - 1));

            tvReceiptStatus.setText(order.getBank_transfer_status().equalsIgnoreCase("0") ? getString(R.string.pending) : order.getBank_transfer_status().equalsIgnoreCase("1") ? getString(R.string.accepted) : getString(R.string.rejected));

            if (order.getBank_transfer_status().equalsIgnoreCase("2")) {
                tvReceiptStatusReason.setVisibility(View.VISIBLE);
                tvReceiptStatusReason.setText(order.getBank_transfer_message());
            }

            tvPickUpAddress.setText(session.getData(Constant.store_address));

            tvPickupTime.setText(order.getPickup_time().equals("0000-00-00 00:00:00") ? activity.getString(R.string.estimate_pickup_time_msg) : order.getPickup_time());

            productImagesAdapter = new ProductImagesAdapter(activity, order.getAttachment(), "api", order.getId());
            recyclerViewImageGallery.setAdapter(productImagesAdapter);

            lytPickUp.setVisibility(order.getLocal_pickup().equals("1") ? View.VISIBLE : View.GONE);
            lytReceipt.setVisibility(order.getPayment_method().equalsIgnoreCase("bank transfer") ? View.VISIBLE : View.GONE);
            if (order.getLocal_pickup().equals("1")) {
                btnCallToSeller.setOnClickListener(v -> {
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, 1);
                        } else {
                            callIntent.setData(Uri.parse("tel:" + session.getData(Constant.support_number).replace(" ", "")));
                            startActivity(callIntent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                btnGetSellerDirection.setOnClickListener(v -> {
                    android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(activity);
                    builder1.setMessage(R.string.map_open_message);
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            getString(R.string.yes),
                            (dialog, id) -> {
//                                com.google.android.apps.maps
                                try {
                                    Uri googleMapIntentUri = Uri.parse("google.navigation:q=" + session.getData(Constant.map_latitude) + "," + session.getData(Constant.map_longitude) + "");
                                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, googleMapIntentUri);
                                    mapIntent.setPackage("com.google.android.apps.maps");
                                    activity.startActivity(mapIntent);
                                } catch (Exception e) {
                                    android.app.AlertDialog.Builder builder11 = new android.app.AlertDialog.Builder(activity);
                                    builder11.setMessage("Please install google map first.");
                                    builder11.setCancelable(true);
                                    builder11.setPositiveButton(getString(R.string.ok), (dialog1, id1) -> dialog1.cancel());
                                    android.app.AlertDialog alert11 = builder11.create();
                                    alert11.show();
                                }
                            });

                    builder1.setNegativeButton(getString(R.string.no), (dialog, id) -> dialog.cancel());

                    android.app.AlertDialog alert11 = builder1.create();
                    alert11.show();


                });
            }


            for (int i = 0; i < order.getStatus_name().size(); i++) {
                createStatusUi(activity, lytTracker, order.getStatus_name().get(i), order.getStatus_time().get(i));
                if (i != order.getStatus_name().size() - 1) {
                    View view = new View(activity);
                    view.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
                    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen._2sdp));
                    params1.weight = 1.0f;
                    view.setLayoutParams(params1);
                    lytTracker.addView(view);
                }

            }

            scrollView.setVisibility(View.VISIBLE);
            mShimmerViewContainer.setVisibility(View.GONE);
            mShimmerViewContainer.stopShimmer();

            recyclerView.setAdapter(new ItemsAdapter(activity, order, "detail"));
            relativeLyt.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
            scrollView.setVisibility(View.VISIBLE);
            mShimmerViewContainer.setVisibility(View.GONE);
            mShimmerViewContainer.stopShimmer();
        }
    }

    public void createStatusUi(Activity activity, LinearLayout linearLayout, String status, String time) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1.0f;

        status = status.equalsIgnoreCase(Constant.AWAITING_PAYMENT) ? activity.getString(R.string.awaiting_payment) : status.equalsIgnoreCase("received") ? activity.getString(R.string.order_received) : status.equalsIgnoreCase("processed") ? activity.getString(R.string.order_processed) : status.equalsIgnoreCase("shipped") ? activity.getString(R.string.order_shipped) : status.equalsIgnoreCase("ready_to_pickup") ? activity.getString(R.string.order_ready_to_pickup) : status.equalsIgnoreCase("delivered") ? (order.getLocal_pickup().equals("1") ? activity.getString(R.string.order_picked_up) : activity.getString(R.string.order_delivered)) : status.equalsIgnoreCase("cancelled") ? activity.getString(R.string.order_cancel_) : activity.getString(R.string.order_returned);
        LinearLayout layout = new LinearLayout(activity);
        layout.setLayoutParams(params);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(activity);
        TextView textView1 = new TextView(activity);
        ImageView imageView = new ImageView(activity);

        imageView.setImageResource(R.drawable.ic_tracker_btn);

        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTextSize(activity.getResources().getDimension(R.dimen._3ssp));

        textView1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView1.setTextSize(activity.getResources().getDimension(R.dimen._3ssp));

        textView.setText(status);
        textView1.setText(!time.equals("") ? time.split("\\s")[0] + "\n" + time.split("\\s")[1] : "");

        imageView.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary));
        textView.setTextColor(ContextCompat.getColor(activity, R.color.txt_color));
        textView1.setTextColor(ContextCompat.getColor(activity, R.color.txt_color));

        layout.addView(textView, 0);
        layout.addView(imageView, 1);
        layout.addView(textView1, 2);
        linearLayout.addView(layout);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.order_track_detail);
        activity.invalidateOptionsMenu();
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    void submitReceipt() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart(Constant.AccessKey, Constant.AccessKeyVal);
            builder.addFormDataPart(Constant.UPLOAD_BANK_TRANSFER_ATTACHMENT, Constant.GetVal);
            builder.addFormDataPart(Constant.ORDER_ID, order.getId());

            for (int i = 0; i < mAlbumFiles.size(); i++) {
                File file = new File(mAlbumFiles.get(i).getPath());
                builder.addFormDataPart(Constant.IMAGES, file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
            }

            RequestBody body = builder.build();

            Request request = new Request.Builder()
                    .url(Constant.ORDER_PROCESS_URL)
                    .method("POST", body)
                    .addHeader(Constant.AUTHORIZATION, "Bearer " + ApiConfig.createJWT("eKart", "eKart Authentication"))
                    .build();

            Response response = client.newCall(request).execute();
            Toast.makeText(activity, new JSONObject(Objects.requireNonNull(response.body()).string()).getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();

            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.toolbar_layout).setVisible(false);
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(true);
    }
}