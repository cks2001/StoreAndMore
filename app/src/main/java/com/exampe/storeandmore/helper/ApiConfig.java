package com.exampe.storeandmore.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import co.paystack.android.PaystackSdk;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.exampe.storeandmore.R;
import com.exampe.storeandmore.helper.album.Album;
import com.exampe.storeandmore.helper.album.AlbumConfig;
import com.exampe.storeandmore.model.OrderTracker;
import com.exampe.storeandmore.model.Product;
import com.exampe.storeandmore.model.Slider;

@SuppressLint("SetTextI18n")
public class ApiConfig extends Application {

    public static final String TAG = ApiConfig.class.getSimpleName();
    static ApiConfig mInstance;
    static AppEnvironment appEnvironment;
    static boolean isDialogOpen = false;
    RequestQueue mRequestQueue;

    public static String VolleyErrorMessage(VolleyError error) {
        String message = "";
        try {
            if (error instanceof NetworkError) {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (error instanceof ServerError) {
                message = "The server could not be found. Please try again after some time!!";
            } else if (error instanceof AuthFailureError) {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (error instanceof ParseError) {
                message = "Parsing error! Please try again after some time!!";
            } else if (error instanceof TimeoutError) {
                message = "Connection TimeOut! Please check your internet connection.";
            } else
                message = "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    public static long dayBetween(String date1, String date2) {
        long days = 0;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date Date1, Date2;
        try {
            Date1 = sdf.parse(date1);
            Date2 = sdf.parse(date2);
            days = (Objects.requireNonNull(Date2).getTime() - Objects.requireNonNull(Date1).getTime()) / (1000 * 60 * 60 * 24);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return days;
    }

    public static String CalculateDays(Activity activity, long days) {

        long weeks = (days / 7);
        long months = (weeks / 4);
        long years = (months / 12);

        if (days == 1) {
            return days + activity.getString(R.string.day);
        } else {
            if (days <= 6) {
                return days + activity.getString(R.string.days);
            } else {
                if (weeks <= 3) {
                    return weeks + ((weeks == 1) ? activity.getString(R.string.week) : activity.getString(R.string.weeks));
                } else {
                    if (months <= 12) {
                        return months + ((months == 1) ? activity.getString(R.string.month) : activity.getString(R.string.months));
                    } else {
                        return years + ((years == 1) ? activity.getString(R.string.year) : activity.getString(R.string.years));
                    }
                }
            }
        }
    }

    public static ArrayList<OrderTracker> GetOrders(JSONArray jsonArray) {
        ArrayList<OrderTracker> orderTrackerArrayList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                OrderTracker orderTracker = new Gson().fromJson(jsonArray.get(i).toString(), OrderTracker.class);
                orderTrackerArrayList.add(orderTracker);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return orderTrackerArrayList;
    }

    @SuppressLint("DefaultLocale")
    public static String StringFormat(String number) {
        return String.format("%.2f", Double.parseDouble(number));
    }

    public static String getMonth(Activity activity, int monthNo) {
        String month = "";

        switch (monthNo) {
            case 1:
                month = activity.getString(R.string.january);
                break;
            case 2:
                month = activity.getString(R.string.february);
                break;
            case 3:
                month = activity.getString(R.string.march);
                break;
            case 4:
                month = activity.getString(R.string.april);
                break;
            case 5:
                month = activity.getString(R.string.may);
                break;
            case 6:
                month = activity.getString(R.string.june);
                break;
            case 7:
                month = activity.getString(R.string.july);
                break;
            case 8:
                month = activity.getString(R.string.august);
                break;
            case 9:
                month = activity.getString(R.string.september);
                break;
            case 10:
                month = activity.getString(R.string.october);
                break;
            case 11:
                month = activity.getString(R.string.november);
                break;
            case 12:
                month = activity.getString(R.string.december);
                break;
            default:
                break;
        }
        return month;
    }

    public static String getDayOfWeek(Activity activity, int dayNo) {
        String month = "";

        switch (dayNo) {
            case 1:
                month = activity.getString(R.string.sunday);
                break;
            case 2:
                month = activity.getString(R.string.monday);
                break;
            case 3:
                month = activity.getString(R.string.tuesday);
                break;
            case 4:
                month = activity.getString(R.string.wednesday);
                break;
            case 5:
                month = activity.getString(R.string.thursday);
                break;
            case 6:
                month = activity.getString(R.string.friday);
                break;
            case 7:
                month = activity.getString(R.string.saturday);
                break;
            default:
                break;
        }
        return month;
    }

    public static ArrayList<String> getDates(String startDate, String endDate) {
        ArrayList<String> dates = new ArrayList<>();
        @SuppressLint("SimpleDateFormat")
        DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = null;
        Date date2 = null;

        try {
            date1 = df1.parse(startDate);
            date2 = df1.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        assert date1 != null;
        cal1.setTime(date1);


        Calendar cal2 = Calendar.getInstance();
        assert date2 != null;
        cal2.setTime(date2);

        while (!cal1.after(cal2)) {
            dates.add(cal1.get(Calendar.DATE) + "-" + (cal1.get(Calendar.MONTH) + 1) + "-" + cal1.get(Calendar.YEAR) + "-" + cal1.get(Calendar.DAY_OF_WEEK));
            cal1.add(Calendar.DATE, 1);
        }
        return dates;
    }

    public static void removeAddress(final Activity activity, String addressId) {

        Map<String, String> params = new HashMap<>();
        params.put(Constant.DELETE_ADDRESS, Constant.GetVal);
        params.put(Constant.ID, addressId);
        ApiConfig.RequestToVolley((result, response) -> {
        }, activity, Constant.GET_ADDRESS_URL, params, false);
    }

    public static void getCartItemCount(final Activity activity, Session session) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_CART, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        Constant.TOTAL_CART_ITEM = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                    } else {
                        Constant.TOTAL_CART_ITEM = 0;
                    }
                    activity.invalidateOptionsMenu();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, activity, Constant.CART_URL, params, false);
    }

    public static void AddOrRemoveFavorite(Activity activity, Session session, String productID, boolean isAdd) {
        Map<String, String> params = new HashMap<>();
        if (isAdd) {
            params.put(Constant.ADD_TO_FAVORITES, Constant.GetVal);
        } else {
            params.put(Constant.REMOVE_FROM_FAVORITES, Constant.GetVal);
        }
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.PRODUCT_ID, productID);
        ApiConfig.RequestToVolley((result, response) -> {
        }, activity, Constant.GET_FAVORITES_URL, params, false);
    }

    public static void RequestToVolley(final VolleyCallback callback, final Activity activity, final String url, final Map<String, String> params, final boolean isProgress) {
        if (ProgressDisplay.mProgressBar != null) {
            ProgressDisplay.mProgressBar.setVisibility(View.GONE);
        }
        final ProgressDisplay progressDisplay = new ProgressDisplay(activity);
        progressDisplay.hideProgress();
        if (ApiConfig.isConnected(activity)) {
            if (isProgress)
                progressDisplay.showProgress();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
                callback.onSuccess(true, response);
                if (isProgress) progressDisplay.hideProgress();
            },
                    error -> {
                        if (isProgress) progressDisplay.hideProgress();
                        callback.onSuccess(false, "");
                        String message = VolleyErrorMessage(error);
                        if (!message.equals(""))
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params1 = new HashMap<>();
                    params1.put(Constant.AUTHORIZATION, "Bearer " + createJWT("eKart", "eKart Authentication"));
                    return params1;
                }

                @Override
                protected Map<String, String> getParams() {
                    params.put(Constant.AccessKey, Constant.AccessKeyVal);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, 0));
            ApiConfig.getInstance().getRequestQueue().getCache().clear();
            ApiConfig.getInstance().addToRequestQueue(stringRequest);
        }

    }

    public static String toTitleCase(String str) {
        if (str == null) {
            return null;
        }
        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }

    public static String createJWT(String issuer, String subject) {
        try {
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            byte[] apiKeySecretBytes = Constant.JWT_KEY.getBytes();
            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
            JwtBuilder builder = Jwts.builder()
                    .setIssuedAt(now)
                    .setSubject(subject)
                    .setIssuer(issuer)
                    .signWith(signatureAlgorithm, signingKey);

            return builder.compact();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean CheckValidation(String item, boolean isMailValidation, boolean isMobileValidation) {
        boolean result = false;
        if (item.length() == 0) {
            result = true;
        } else if (isMailValidation) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(item).matches()) {
                result = true;
            }
        } else if (isMobileValidation) {
            if (!android.util.Patterns.PHONE.matcher(item).matches()) {
                result = true;
            }
        }
        return result;
    }

    @SuppressLint("DefaultLocale")
    public static String GetDiscount(double OriginalPrice, double DiscountedPrice) {
        return String.format("%.0f", Double.parseDouble("" + (((((OriginalPrice - DiscountedPrice) + OriginalPrice) / OriginalPrice) - 1) * 100))) + "%";
    }


    public static void AddMultipleProductInCart(final Session session, final Activity activity, HashMap<String, String> map) {
        try {
            if (map.size() > 0) {
                String ids = map.keySet().toString().replace("[", "").replace("]", "").replace(" ", "");
                String qty = map.values().toString().replace("[", "").replace("]", "").replace(" ", "");

                Map<String, String> params = new HashMap<>();
                params.put(Constant.ADD_MULTIPLE_ITEMS, Constant.GetVal);
                params.put(Constant.USER_ID, session.getData(Constant.ID));
                params.put(Constant.PRODUCT_VARIANT_ID, ids);
                params.put(Constant.QTY, qty);
                ApiConfig.RequestToVolley((result, response) -> {
                    if (result) {
                        getCartItemCount(activity, session);
                    }
                }, activity, Constant.CART_URL, params, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void AddMultipleProductInSaveForLater(final Session session, final Activity activity, HashMap<String, String> map) {
        if (map.size() > 0) {
            String ids = map.keySet().toString().replace("[", "").replace("]", "").replace(" ", "");
            String qty = map.values().toString().replace("[", "").replace("]", "").replace(" ", "");

            Map<String, String> params = new HashMap<>();
            params.put(Constant.SAVE_FOR_LATER_ITEMS, Constant.GetVal);
            params.put(Constant.USER_ID, session.getData(Constant.ID));
            params.put(Constant.PRODUCT_VARIANT_ID, ids);
            params.put(Constant.QTY, qty);

            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    getCartItemCount(activity, session);
                }
            }, activity, Constant.CART_URL, params, false);
        }
    }

    public static void addMarkers(int currentPage, ArrayList<Slider> imageList, LinearLayout mMarkersLayout, Context context) {

        if (context != null) {
            TextView[] markers = new TextView[imageList.size()];

            mMarkersLayout.removeAllViews();

            for (int i = 0; i < markers.length; i++) {
                markers[i] = new TextView(context);
                markers[i].setText(Html.fromHtml("&#8226;"));
                markers[i].setTextSize(35);
                markers[i].setTextColor(ContextCompat.getColor(context, R.color.gray));
                mMarkersLayout.addView(markers[i]);
            }
            if (markers.length > 0)
                markers[currentPage].setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
    }

    public static Drawable buildCounterDrawable(int count, Activity activity) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.counter_menuitem_layout, null);
        TextView textView = view.findViewById(R.id.tvCounter);
        RelativeLayout lytCount = view.findViewById(R.id.lytCount);
        if (count == 0) {
            lytCount.setVisibility(View.GONE);
        } else {
            lytCount.setVisibility(View.VISIBLE);
            textView.setText("" + count);
        }
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(activity.getResources(), bitmap);
    }

    public static void GetSettings(final Activity activity) {
        Session session = new Session(activity);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SETTINGS, Constant.GetVal);
        params.put(Constant.GET_TIMEZONE, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        JSONObject object = jsonObject.getJSONObject(Constant.SETTINGS);

                        session.setData(Constant.minimum_version_required, object.getString(Constant.minimum_version_required));
                        session.setData(Constant.is_version_system_on, object.getString(Constant.is_version_system_on));

                        session.setData(Constant.currency, object.getString(Constant.currency));
                        session.setData(Constant.current_date, object.getString(Constant.current_date));

                        session.setData(Constant.min_order_amount, object.getString(Constant.min_order_amount));
                        session.setData(Constant.max_cart_items_count, object.getString(Constant.max_cart_items_count));
                        session.setData(Constant.area_wise_delivery_charge, object.getString(Constant.area_wise_delivery_charge));

                        session.setData(Constant.is_refer_earn_on, object.getString(Constant.is_refer_earn_on));
                        session.setData(Constant.refer_earn_bonus, object.getString(Constant.refer_earn_bonus));
                        session.setData(Constant.refer_earn_bonus, object.getString(Constant.refer_earn_bonus));
                        session.setData(Constant.refer_earn_method, object.getString(Constant.refer_earn_method));
                        session.setData(Constant.max_refer_earn_amount, object.getString(Constant.max_refer_earn_amount));

                        session.setData(Constant.max_product_return_days, object.getString(Constant.max_product_return_days));
                        session.setData(Constant.user_wallet_refill_limit, object.getString(Constant.user_wallet_refill_limit));
                        session.setData(Constant.min_refer_earn_order_amount, object.getString(Constant.min_refer_earn_order_amount));

                        session.setData(Constant.ratings, object.getString(Constant.ratings));
                        session.setData(Constant.local_pickup, object.getString(Constant.local_pickup));

                        session.setData(Constant.support_number, object.getString(Constant.support_number));
                        session.setData(Constant.map_latitude, object.getString(Constant.map_latitude));
                        session.setData(Constant.map_longitude, object.getString(Constant.map_longitude));
                        session.setData(Constant.store_address, object.getString(Constant.store_address));

                        String versionName;
                        try {
                            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                            versionName = packageInfo.versionName;
                            if (ApiConfig.compareVersion(versionName, session.getData(Constant.minimum_version_required)) < 0) {
                                ApiConfig.OpenBottomDialog(activity);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }

    public static void OpenBottomDialog(final Activity activity) {
        try {
            @SuppressLint("InflateParams") View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_update_app, null);
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
            Button btnNotNow = sheetView.findViewById(R.id.btnNotNow);
            Button btnUpdateNow = sheetView.findViewById(R.id.btnUpdateNow);
            if (new Session(activity).getData(Constant.is_version_system_on).equals("0")) {
                btnNotNow.setVisibility(View.VISIBLE);
                imgClose.setVisibility(View.VISIBLE);
                mBottomSheetDialog.setCancelable(true);
            } else {
                mBottomSheetDialog.setCancelable(false);
            }


            imgClose.setOnClickListener(v -> {
                if (mBottomSheetDialog.isShowing())
                    new Session(activity).setBoolean("update_skip", true);
                mBottomSheetDialog.dismiss();
            });
            btnNotNow.setOnClickListener(v -> {
                new Session(activity).setBoolean("update_skip", true);
                if (mBottomSheetDialog.isShowing())
                    mBottomSheetDialog.dismiss();
            });

            btnUpdateNow.setOnClickListener(view -> activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.PLAY_STORE_LINK + activity.getPackageName()))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getWalletBalance(final Activity activity, Session session) {
        try {
            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.GET_USER_DATA, Constant.GetVal);
                params.put(Constant.USER_ID, session.getData(Constant.ID));
                ApiConfig.RequestToVolley((result, response) -> {
                    if (result) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean(Constant.ERROR)) {
                                Constant.WALLET_BALANCE = Double.parseDouble(object.getString(Constant.KEY_BALANCE));
                                session.setData(Constant.STATUS, object.getString(Constant.STATUS));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, activity, Constant.USER_DATA_URL, params, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getAddress(double lat, double lng, Activity activity) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
            return addresses.get(0).getAddressLine(0);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int compareVersion(String version1, String version2) {
        String[] arr1 = version1.split("\\.");
        String[] arr2 = version2.split("\\.");

        int i = 0;
        while (i < arr1.length || i < arr2.length) {
            if (i < arr1.length && i < arr2.length) {
                if (Integer.parseInt(arr1[i]) < Integer.parseInt(arr2[i])) {
                    return -1;
                } else if (Integer.parseInt(arr1[i]) > Integer.parseInt(arr2[i])) {
                    return 1;
                }
            } else if (i < arr1.length) {
                if (Integer.parseInt(arr1[i]) != 0) {
                    return 1;
                }
            } else {
                if (Integer.parseInt(arr2[i]) != 0) {
                    return -1;
                }
            }

            i++;
        }

        return 0;
    }

    public static synchronized ApiConfig getInstance() {
        return mInstance;
    }

    public static Boolean isConnected(final Activity activity) {
        boolean check = false;
        try {
            ConnectivityManager ConnectionManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = ConnectionManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                check = true;
            } else {
                try {
                    if (!isDialogOpen) {
                        @SuppressLint("InflateParams") View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_no_internet, null);
                        ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
                        if (parentViewGroup != null) {
                            parentViewGroup.removeAllViews();
                        }

                        final Dialog mBottomSheetDialog = new Dialog(activity);
                        mBottomSheetDialog.setContentView(sheetView);
                        mBottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        mBottomSheetDialog.show();
                        isDialogOpen = true;
                        Button btnRetry = sheetView.findViewById(R.id.btnRetry);
                        mBottomSheetDialog.setCancelable(false);

                        btnRetry.setOnClickListener(view -> {
                            if (isConnected(activity)) {
                                isDialogOpen = false;
                                mBottomSheetDialog.dismiss();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return check;
    }


    public static ArrayList<Product> GetProductList(JSONArray jsonArray) {
        ArrayList<Product> productArrayList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Product product = new Gson().fromJson(jsonArray.getJSONObject(i).toString(), Product.class);
                productArrayList.add(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productArrayList;
    }

    public static ArrayList<Product> GetFavoriteProductList(JSONArray jsonArray) {
        ArrayList<Product> productArrayList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Product product = new Gson().fromJson(jsonArray.getJSONObject(i).toString(), Product.class);
                productArrayList.add(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productArrayList;
    }

    public static void SetAppEnvironment(Activity activity) {
        if (Constant.PAYUMONEY_MODE.equals("production")) {
            appEnvironment = AppEnvironment.PRODUCTION;
        } else {
            appEnvironment = AppEnvironment.SANDBOX;
        }
        PaystackSdk.initialize(activity);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        Album.initialize(AlbumConfig.newBuilder(this)
                .setAlbumLoader(new MediaLoader())
                .setLocale(Locale.getDefault())
                .build()
        );
    }

    public AppEnvironment getAppEnvironment() {
        return appEnvironment;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
}