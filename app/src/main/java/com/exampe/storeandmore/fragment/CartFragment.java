package com.exampe.storeandmore.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.activity.LoginActivity;
import com.exampe.storeandmore.activity.MainActivity;
import com.exampe.storeandmore.adapter.CartAdapter;
import com.exampe.storeandmore.adapter.OfflineCartAdapter;
import com.exampe.storeandmore.adapter.OfflineSaveForLaterAdapter;
import com.exampe.storeandmore.adapter.SaveForLaterAdapter;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.DatabaseHelper;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Cart;
import com.exampe.storeandmore.model.OfflineCart;

public class CartFragment extends Fragment {
    @SuppressLint("StaticFieldLeak")
    public static LinearLayout lytEmpty;
    @SuppressLint("StaticFieldLeak")
    public static RelativeLayout lytTotal;
    public static ArrayList<Cart> carts, saveForLater;
    public static ArrayList<OfflineCart> items, saveForLaterItems;
    @SuppressLint("StaticFieldLeak")
    public static CartAdapter cartAdapter;
    @SuppressLint("StaticFieldLeak")
    public static SaveForLaterAdapter saveForLaterAdapter;
    @SuppressLint("StaticFieldLeak")
    public static OfflineCartAdapter offlineCartAdapter;
    @SuppressLint("StaticFieldLeak")
    public static OfflineSaveForLaterAdapter offlineSaveForLaterAdapter;
    public static HashMap<String, String> values, saveForLaterValues;
    public static boolean isSoldOut = false;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvTotalAmount, tvTotalItems, tvConfirmOrder, tvSaveForLaterTitle;
    Activity activity;
    @SuppressLint("StaticFieldLeak")
    static Session session;
    static JSONObject jsonObject;
    View root;
    RecyclerView cartRecycleView, saveForLaterRecyclerView;
    NestedScrollView scrollView;
    double total;
    Button btnShowNow;
    DatabaseHelper databaseHelper;
    private ShimmerFrameLayout mShimmerViewContainer;
    @SuppressLint("StaticFieldLeak")
    public static LinearLayout lytSaveForLater;
    ArrayList<String> variantIdList, qtyList;
    RadioGroup rgOrderType;

    @SuppressLint("SetTextI18n")
    public static void SetData(Activity activity) {
        tvTotalAmount.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(String.valueOf(Constant.FLOAT_TOTAL_AMOUNT)));
        int count;
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            count = carts.size();
        } else {
            count = items.size();
        }
        tvTotalItems.setText(count + (count == 1 ? activity.getString(R.string.item) : activity.getString(R.string.items)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_cart, container, false);
        values = new HashMap<>();
        saveForLaterValues = new HashMap<>();
        activity = getActivity();
        session = new Session(activity);

        lytTotal = root.findViewById(R.id.lytTotal);
        lytEmpty = root.findViewById(R.id.lytEmpty);
        btnShowNow = root.findViewById(R.id.btnShowNow);
        tvTotalAmount = root.findViewById(R.id.tvTotalAmount);
        tvTotalItems = root.findViewById(R.id.tvTotalItems);
        scrollView = root.findViewById(R.id.scrollView);
        cartRecycleView = root.findViewById(R.id.cartRecycleView);
        saveForLaterRecyclerView = root.findViewById(R.id.saveForLaterRecyclerView);
        tvConfirmOrder = root.findViewById(R.id.tvConfirmOrder);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);
        lytSaveForLater = root.findViewById(R.id.lytSaveForLater);
        tvSaveForLaterTitle = root.findViewById(R.id.tvSaveForLaterTitle);
        rgOrderType = root.findViewById(R.id.rgOrderType);
        databaseHelper = new DatabaseHelper(activity);

        cartRecycleView.setLayoutManager(new LinearLayoutManager(activity));
        saveForLaterRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        if(session.getData(Constant.local_pickup).equals("1")){
            rgOrderType.setVisibility(View.VISIBLE);
        }

        setHasOptionsMenu(true);
        Constant.FLOAT_TOTAL_AMOUNT = 0.00;
        items = new ArrayList<>();
        saveForLaterItems = new ArrayList<>();
        if (ApiConfig.isConnected(activity)) {
            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                GetSettings(activity);
            } else {
                GetOfflineCart();
            }
        }

        tvConfirmOrder.setOnClickListener(v -> {
            if (ApiConfig.isConnected(requireActivity())) {
                if (!isSoldOut) {
                    if (Float.parseFloat(session.getData(Constant.min_order_amount)) <= Constant.FLOAT_TOTAL_AMOUNT) {
                        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                            if (rgOrderType.getCheckedRadioButtonId() == R.id.rbDoorStepDelivery) {
                                if (values.size() > 0) {
                                    ApiConfig.AddMultipleProductInCart(session, activity, values);
                                }
                                AddressListFragment.selectedAddress = "";
                                Fragment fragment = new AddressListFragment();
                                final Bundle bundle = new Bundle();
                                bundle.putString(Constant.FROM, "process");
                                bundle.putDouble("total", Constant.FLOAT_TOTAL_AMOUNT);
                                bundle.putSerializable("data", carts);
                                bundle.putStringArrayList("variantIdList", variantIdList);
                                bundle.putStringArrayList("qtyList", qtyList);
                                fragment.setArguments(bundle);
                                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                            } else {
                                Constant.SETTING_DELIVERY_CHARGE = 0.00;
                                if (values.size() > 0) {
                                    ApiConfig.AddMultipleProductInCart(session, activity, values);
                                }
                                AddressListFragment.selectedAddress = "";
                                Fragment fragment = new CheckoutFragment();
                                final Bundle bundle = new Bundle();
                                bundle.putString(Constant.FROM, "cart");
                                bundle.putDouble("total", Constant.FLOAT_TOTAL_AMOUNT);
                                bundle.putSerializable("data", carts);
                                bundle.putStringArrayList("variantIdList", variantIdList);
                                bundle.putStringArrayList("qtyList", qtyList);
                                fragment.setArguments(bundle);
                                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                            }
                        } else {
                            startActivity(new Intent(activity, LoginActivity.class).putExtra("total", Constant.FLOAT_TOTAL_AMOUNT).putExtra(Constant.FROM, "checkout"));
                        }
                    } else {
                        Toast.makeText(activity, getString(R.string.msg_minimum_order_amount) + session.getData(Constant.currency) + ApiConfig.StringFormat(session.getData(Constant.min_order_amount)), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, getString(R.string.msg_sold_out), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnShowNow.setOnClickListener(v -> MainActivity.fm.popBackStack());

        return root;
    }

    private void GetOfflineCart() {
        variantIdList = new ArrayList<>();
        qtyList = new ArrayList<>();
        items = new ArrayList<>();
        offlineCartAdapter = new OfflineCartAdapter(activity);
        cartRecycleView.setAdapter(offlineCartAdapter);

        saveForLaterItems = new ArrayList<>();
        offlineSaveForLaterAdapter = new OfflineSaveForLaterAdapter(activity);
        saveForLaterRecyclerView.setAdapter(offlineCartAdapter);

        startShimmer();

        if (databaseHelper.getTotalItemOfCart(activity) > 0) {
            items = new ArrayList<>();
            Map<String, String> params = new HashMap<>();
            params.put(Constant.GET_CART_OFFLINE, Constant.GetVal);
            params.put(Constant.VARIANT_IDs, databaseHelper.getCartList().toString().replace("[", "").replace("]", "").replace("\"", ""));

            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            session.setData(Constant.TOTAL, jsonObject.getString(Constant.TOTAL));

                            JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                            Gson g = new Gson();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                OfflineCart cart = g.fromJson(jsonObject1.toString(), OfflineCart.class);
                                items.add(cart);
                            }

                            offlineCartAdapter = new OfflineCartAdapter(activity);
                            cartRecycleView.setAdapter(offlineCartAdapter);

                            if (databaseHelper.getSaveForLaterList().size() > 0) {
                                GetOfflineSaveForLater();
                            } else {
                                saveForLaterItems = new ArrayList<>();
                                offlineSaveForLaterAdapter = new OfflineSaveForLaterAdapter(activity);
                                saveForLaterRecyclerView.setAdapter(offlineSaveForLaterAdapter);
                            }

                            if (carts != null && carts.size() > 0) {
                                lytTotal.setVisibility(View.VISIBLE);
                            }
                            stopShimmer();
                            lytEmpty.setVisibility(View.GONE);

                        } else {
                            GetOfflineSaveForLater();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        GetOfflineSaveForLater();
                    }
                }
            }, activity, Constant.GET_OFFLINE_CART_URL, params, false);
        } else {
            GetOfflineSaveForLater();
        }
    }

    private void GetOfflineSaveForLater() {
        saveForLaterItems = new ArrayList<>();
        if (items == null) {
            items = new ArrayList<>();
            offlineCartAdapter = new OfflineCartAdapter(activity);
            cartRecycleView.setAdapter(offlineCartAdapter);
        }
        if (databaseHelper.getTotalItemOfSaveForLater() >= 1) {
            Map<String, String> params = new HashMap<>();
            params.put(Constant.GET_CART_OFFLINE, Constant.GetVal);
            params.put(Constant.VARIANT_IDs, databaseHelper.getSaveForLaterList().toString().replace("[", "").replace("]", "").replace("\"", ""));

            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            session.setData(Constant.TOTAL, jsonObject.getString(Constant.TOTAL));

                            JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                OfflineCart cart = new Gson().fromJson(jsonObject1.toString(), OfflineCart.class);
                                saveForLaterItems.add(cart);
                            }

                            offlineSaveForLaterAdapter = new OfflineSaveForLaterAdapter(activity);
                            saveForLaterRecyclerView.setAdapter(offlineSaveForLaterAdapter);

                            stopShimmer();
                            if (carts != null && carts.size() > 0) {
                                lytTotal.setVisibility(View.VISIBLE);
                            }
                            lytSaveForLater.setVisibility(View.VISIBLE);
                            lytEmpty.setVisibility(View.GONE);

                        } else {
                            stopShimmer();
                            lytTotal.setVisibility(View.GONE);
                            lytSaveForLater.setVisibility(View.GONE);
                            lytEmpty.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        stopShimmer();
                        lytTotal.setVisibility(View.GONE);
                        lytSaveForLater.setVisibility(View.GONE);
                        lytEmpty.setVisibility(View.VISIBLE);
                    }
                }
            }, activity, Constant.GET_OFFLINE_CART_URL, params, false);
        } else {
            stopShimmer();
            lytTotal.setVisibility(View.GONE);
            lytSaveForLater.setVisibility(View.GONE);
            lytEmpty.setVisibility(View.VISIBLE);
        }
    }

    public void GetSettings(final Activity activity) {

        startShimmer();
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

                        session.setData(Constant.min_order_amount, object.getString(Constant.min_order_amount));
                        session.setData(Constant.max_cart_items_count, object.getString(Constant.max_cart_items_count));
                        session.setData(Constant.area_wise_delivery_charge, object.getString(Constant.area_wise_delivery_charge));

                        getCartData();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }

    @SuppressLint("SetTextI18n")
    private void getCartData() {
        variantIdList = new ArrayList<>();
        qtyList = new ArrayList<>();
        saveForLater = new ArrayList<>();
        saveForLaterAdapter = new SaveForLaterAdapter(activity, "save_for_later");
        saveForLaterRecyclerView.setAdapter(saveForLaterAdapter);

        carts = new ArrayList<>();
        cartAdapter = new CartAdapter(activity, "cart");
        cartRecycleView.setAdapter(cartAdapter);

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_CART, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
//                    System.out.println("====res "+response);
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        JSONObject object = new JSONObject(response);

                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                        if (jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                if (jsonObject1 != null) {
                                    try {
                                        Cart cart = new Gson().fromJson(jsonObject1.toString(), Cart.class);

                                        variantIdList.add(cart.getProduct_variant_id());
                                        qtyList.add(cart.getQty());

                                        float price;
                                        int qty = Integer.parseInt(cart.getQty());
                                        String taxPercentage = cart.getItems().get(0).getTax_percentage();

                                        if (cart.getItems().get(0).getDiscounted_price().equals("0") || cart.getItems().get(0).getDiscounted_price().equals("")) {
                                            price = ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                                        } else {
                                            price = ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                                        }
                                        Constant.FLOAT_TOTAL_AMOUNT += (price * qty);
                                        carts.add(cart);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    break;
                                }
                            }

                            cartAdapter = new CartAdapter(activity, "cart");
                            cartRecycleView.setAdapter(cartAdapter);

                        }

                        JSONArray jsonArraySaveForLater = object.getJSONArray(Constant.SAVE_FOR_LATER);
                        String count = jsonArraySaveForLater.length() > 1 ? jsonArraySaveForLater.length() + activity.getString(R.string.items) : jsonArraySaveForLater.length() + activity.getString(R.string.item);
                        tvSaveForLaterTitle.setText(activity.getString(R.string.save_for_later) + " (" + count + ")");
                        if (jsonArraySaveForLater.length() > 0) {
                            for (int i = 0; i < jsonArraySaveForLater.length(); i++) {
                                JSONObject jsonObject1 = jsonArraySaveForLater.getJSONObject(i);
                                try {
                                    Cart cart = new Gson().fromJson(jsonObject1.toString(), Cart.class);
                                    saveForLater.add(cart);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            saveForLaterAdapter = new SaveForLaterAdapter(activity, "save_for_later");
                            saveForLaterRecyclerView.setAdapter(saveForLaterAdapter);
                            lytSaveForLater.setVisibility(View.VISIBLE);
                        }

                        if (carts != null && carts.size() > 0) {
                            lytTotal.setVisibility(View.VISIBLE);
                        }

                        stopShimmer();
                        lytEmpty.setVisibility(View.GONE);
                        total = Double.parseDouble(jsonObject.getString(Constant.TOTAL));
                        session.setData(Constant.TOTAL, String.valueOf(total));
                        Constant.TOTAL_CART_ITEM = Integer.parseInt(jsonObject.getString(Constant.TOTAL));

                        SetData(activity);
                    } else {
                        stopShimmer();
                        lytSaveForLater.setVisibility(View.GONE);
                        lytEmpty.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    stopShimmer();
                    lytSaveForLater.setVisibility(View.GONE);
                    lytEmpty.setVisibility(View.VISIBLE);

                }
            }
        }, activity, Constant.CART_URL, params, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            if (values.size() > 0) {
                ApiConfig.AddMultipleProductInCart(session, activity, values);
            }
        }
    }

    public void startShimmer() {
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
    }

    public void stopShimmer() {
        mShimmerViewContainer.stopShimmer();
        mShimmerViewContainer.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.cart);
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

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.toolbar_layout).setVisible(false);
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
}