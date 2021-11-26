package com.exampe.storeandmore.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.exampe.storeandmore.helper.ApiConfig.GetSettings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.adapter.ProductLoadMoreAdapter;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.DatabaseHelper;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Product;


@SuppressLint("NotifyDataSetChanged")
public class FavoriteFragment extends Fragment {
    public static ArrayList<Product> productArrayList;
    @SuppressLint("StaticFieldLeak")
    public static ProductLoadMoreAdapter productLoadMoreAdapter;
    public static RecyclerView recyclerView;
    @SuppressLint("StaticFieldLeak")
    public static RelativeLayout tvAlert;
    View root;
    Session session;
    int total = 0;
    NestedScrollView nestedScrollView;
    Activity activity;
    boolean isLogin;
    DatabaseHelper databaseHelper;
    int offset = 0;
    SwipeRefreshLayout swipeLayout;
    boolean isLoadMore = false;
    boolean isGrid = false;
    LinearLayout lytList, lytGrid;
    int resource;
    private ShimmerFrameLayout mShimmerViewContainer;
    String url;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_favorite, container, false);
        setHasOptionsMenu(true);

        activity = getActivity();
        session = new Session(activity);
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN);
        databaseHelper = new DatabaseHelper(activity);


        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvAlert = root.findViewById(R.id.tvAlert);
        lytGrid = root.findViewById(R.id.lytGrid);
        lytList = root.findViewById(R.id.lytList);
        nestedScrollView = root.findViewById(R.id.nestedScrollView);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);

        if (isLogin) {
            url = Constant.GET_FAVORITES_URL;
        } else {
            url = Constant.GET_OFFLINE_FAVORITES_URL;
        }

        if (session.getBoolean("grid")) {
            resource = R.layout.lyt_item_grid;
            isGrid = true;

            recyclerView = root.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
        } else {
            resource = R.layout.lyt_item_list;
            isGrid = false;

            recyclerView = root.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        }

        GetSettings(activity);

        if (ApiConfig.isConnected(activity)) {
            GetData();
        }

        swipeLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeLayout.setOnRefreshListener(() -> {
            if (ApiConfig.isConnected(activity)) {
                offset = 0;
                total = 0;
                ApiConfig.getWalletBalance(activity, new Session(activity));
                if (isLogin) {
                    if (Constant.CartValues.size() > 0) {
                        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                    }
                }
                GetData();
            }
            swipeLayout.setRefreshing(false);
        });

        return root;
    }

    void GetData() {
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        if (isLogin) {
            params.put(Constant.GET_FAVORITES, Constant.GetVal);
            params.put(Constant.GET_ALL_PRODUCTS, Constant.GetVal);
            params.put(Constant.USER_ID, session.getData(Constant.ID));
            params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
            params.put(Constant.OFFSET, offset + "");
        } else {
            params.put(Constant.GET_FAVORITES_OFFLINE, Constant.GetVal);
            params.put(Constant.PRODUCT_IDs, String.valueOf(databaseHelper.getFavorite()).replace("[", "").replace("]", ""));
        }
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        if (isLogin) {
                            total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                        }
                        if (offset == 0) {
                            productArrayList = new ArrayList<>();
                            recyclerView.setVisibility(View.VISIBLE);
                            tvAlert.setVisibility(View.GONE);
                        }
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        productArrayList.addAll(ApiConfig.GetFavoriteProductList(jsonArray));
                        if (offset == 0) {
                            productLoadMoreAdapter = new ProductLoadMoreAdapter(activity, productArrayList, resource, "favorite");
                            recyclerView.setAdapter(productLoadMoreAdapter);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                                    if (productArrayList.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == productArrayList.size() - 1) {
                                                //bottom of list!
                                                productArrayList.add(null);
                                                productLoadMoreAdapter.notifyItemInserted(productArrayList.size() - 1);

                                                offset = offset + Constant.LOAD_ITEM_LIMIT;
                                                Map<String, String> params1 = new HashMap<>();
                                                if (isLogin) {
                                                    params1.put(Constant.GET_FAVORITES, Constant.GetVal);
                                                    params1.put(Constant.USER_ID, session.getData(Constant.ID));
                                                    params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
                                                    params1.put(Constant.OFFSET, offset + "");
                                                } else {
                                                    params1.put(Constant.GET_FAVORITES_OFFLINE, Constant.GetVal);
                                                    params1.put(Constant.PRODUCT_IDs, String.valueOf(databaseHelper.getFavorite()).replace("[", "").replace("]", ""));
                                                }

                                                ApiConfig.RequestToVolley((result1, response1) -> {
                                                    productArrayList.remove(productArrayList.size() - 1);
                                                    productLoadMoreAdapter.notifyItemRemoved(productArrayList.size());

                                                    if (result1) {
                                                        try {
                                                            JSONObject jsonObject1 = new JSONObject(response1);
                                                            if (!jsonObject1.getBoolean(Constant.ERROR)) {

                                                                JSONObject object1 = new JSONObject(response1);
                                                                JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);
                                                                productArrayList.addAll(ApiConfig.GetFavoriteProductList(jsonArray1));
                                                                productLoadMoreAdapter.notifyDataSetChanged();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            mShimmerViewContainer.stopShimmer();
                                                            mShimmerViewContainer.setVisibility(View.GONE);
                                                            recyclerView.setVisibility(View.VISIBLE);
                                                        }
                                                    } else {
                                                        isLoadMore = false;
                                                        productLoadMoreAdapter.notifyDataSetChanged();
                                                        mShimmerViewContainer.stopShimmer();
                                                        mShimmerViewContainer.setVisibility(View.GONE);
                                                        recyclerView.setVisibility(View.VISIBLE);
                                                        recyclerView.setVisibility(View.GONE);
                                                        tvAlert.setVisibility(View.VISIBLE);
                                                    }
                                                }, activity, url, params1, false);
                                                isLoadMore = true;
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        tvAlert.setVisibility(View.VISIBLE);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    tvAlert.setVisibility(View.VISIBLE);
                }
            } else {
                mShimmerViewContainer.stopShimmer();
                mShimmerViewContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                tvAlert.setVisibility(View.VISIBLE);
            }
        }, activity, url, params, false);
    }


    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.title_fav);
        requireActivity().invalidateOptionsMenu();
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
    public void onHiddenChanged(boolean hidden) {
        recyclerView.setVisibility(View.GONE);
        tvAlert.setVisibility(View.GONE);
        if (!hidden)
            GetData();
        super.onHiddenChanged(hidden);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.toolbar_layout) {
            if (isGrid) {
                lytGrid.setVisibility(View.GONE);
                lytList.setVisibility(View.VISIBLE);
                isGrid = false;
                recyclerView.setAdapter(null);
                resource = R.layout.lyt_item_list;
                recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            } else {
                lytGrid.setVisibility(View.VISIBLE);
                lytList.setVisibility(View.GONE);
                isGrid = true;
                recyclerView.setAdapter(null);
                resource = R.layout.lyt_item_grid;
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            }
            session.setBoolean("grid", isGrid);
            productLoadMoreAdapter = new ProductLoadMoreAdapter(activity, productArrayList, resource, "favorite");
            recyclerView.setAdapter(productLoadMoreAdapter);
            productLoadMoreAdapter.notifyDataSetChanged();
            activity.invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        activity.getMenuInflater().inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(true);
        menu.findItem(R.id.toolbar_cart).setIcon(ApiConfig.buildCounterDrawable(Constant.TOTAL_CART_ITEM, activity));
        menu.findItem(R.id.toolbar_layout).setVisible(true);

        Drawable myDrawable;
        if (isGrid) {
            myDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_list_); // The ID of your drawable
        } else {
            myDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_grid_); // The ID of your drawable.
        }
        menu.findItem(R.id.toolbar_layout).setIcon(myDrawable);

        super.onPrepareOptionsMenu(menu);
    }
}
