package com.exampe.storeandmore.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.exampe.storeandmore.helper.ApiConfig.GetSettings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.adapter.ProductLoadMoreAdapter;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.FlashSalesList;
import com.exampe.storeandmore.model.Product;

@SuppressLint({"NotifyDataSetChanged", "StaticFieldLeak", "UseCompatLoadingForDrawables"})
public class ProductListFragment extends Fragment {
    public static ArrayList<Product> productArrayList;
    public static ProductLoadMoreAdapter productLoadMoreAdapter;
    View root;
    Session session;
    int total;
    NestedScrollView nestedScrollView;
    Activity activity;
    int offset = 0, offsetFlashSaleNames = 0;
    String id, filterBy, from;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;
    int filterIndex;
    TextView tvAlert;
    boolean isSort = false, isLoadMore = false;
    boolean isGrid = false;
    int resource;
    private ShimmerFrameLayout mShimmerViewContainer;
    LinearLayout tabLayout_, lytList, lytGrid;
    ArrayList<FlashSalesList> flashSalesLists;
    int totalFlashSales = 0;
    TabLayout tabLayout;
    private TabLayout.Tab tab;
    boolean tabLoading = false;
    ListView listView;
    EditText searchView;
    TextView noResult, msg;
    LinearLayout lytSearchView;
    String[] productsName;
    ArrayAdapter<String> arrayAdapter;
    String url = "";

    @SuppressLint("ClickableViewAccessibility")
    @Override

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_product_list, container, false);
        getAllWidgets();
        setHasOptionsMenu(true);
        offset = 0;

        activity = getActivity();
        session = new Session(activity);

        assert getArguments() != null;
        from = getArguments().getString(Constant.FROM);
        id = getArguments().getString(Constant.ID);

        setHasOptionsMenu(true);

        if (session.getBoolean("grid")) {
            lytGrid.setVisibility(View.VISIBLE);
            lytList.setVisibility(View.GONE);
            resource = R.layout.lyt_item_grid;
            isGrid = true;
            recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
        } else {
            lytGrid.setVisibility(View.GONE);
            lytList.setVisibility(View.VISIBLE);
            resource = R.layout.lyt_item_list;
            isGrid = false;
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        }

        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();

        GetSettings(activity);

        filterIndex = -1;

        getProductData();

        swipeLayout.setColorSchemeResources(R.color.colorPrimary);

        swipeLayout.setOnRefreshListener(() -> {
            swipeLayout.setRefreshing(false);
            offset = 0;
            offsetFlashSaleNames = 0;
            startShimmer();
            switch (from) {
                case "regular":
                case "sub_cate":
                case "similar":
                case "section":
                case "flash_sale":
                case "flash_sale_all":
                    GetData();
                    break;
                case "search":
                    stopShimmer();
                    lytSearchView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    Constant.CartValues = new HashMap<>();
                    productsName = session.getData(Constant.GET_ALL_PRODUCTS_NAME).replace("\"", "").replace("[","").replace("]","").split(",");
                    searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);
                    arrayAdapter = new ArrayAdapter<>(activity, R.layout.spinner_search_item, new ArrayList<>(Arrays.asList(productsName)));
                    listView.setAdapter(arrayAdapter);
                    break;
            }
        });

        flashSalesLists = new ArrayList<>();
        tabLayout.getViewTreeObserver().addOnScrollChangedListener(() -> {
            Point windowSize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(windowSize);
            int scrollX = tabLayout.getScrollX();
            int maxScrollWidth = tabLayout.getChildAt(0).getMeasuredWidth() - windowSize.x;

            if (maxScrollWidth == scrollX && !tabLoading) {
                tab = tabLayout.newTab().setText("Loading...").setTag("0");
                tabLayout.addTab(tab);
                offsetFlashSaleNames += Constant.LOAD_ITEM_LIMIT;
                GetFlashSales(offsetFlashSaleNames, tab);
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (flashSalesLists.size() != 0) {
                    offset = 0;
                    tabLayout_.setVisibility(View.INVISIBLE);
                    id = flashSalesLists.get(tab.getPosition()).getId();
                    GetData();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                arrayAdapter.getFilter().filter(searchView.getText().toString().trim());
                if (searchView.getText().toString().trim().length() > 0) {
                    listView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close, 0);
                } else {
                    listView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchView.setOnEditorActionListener((v, actionId, event) -> {
            SearchRequest(v.getText().toString().trim());
            return true;
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            searchView.setText(arrayAdapter.getItem(position));
            SearchRequest(arrayAdapter.getItem(position));
        });

        searchView.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (searchView.getText().toString().trim().length() > 0) {
                    if (event.getRawX() >= (searchView.getRight() - searchView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);
                        searchView.setText("");
                    }
//                        return true;
                }
            }
            return false;
        });

        return root;
    }

    public void getProductData() {
        switch (from) {
            case "regular":
            case "sub_cate":
            case "similar":
            case "section":
            case "flash_sale":
                GetData();
                break;
            case "search":
                stopShimmer();
                lytSearchView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Constant.CartValues = new HashMap<>();
                productsName = session.getData(Constant.GET_ALL_PRODUCTS_NAME).replace("\"", "").split(",");
                searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);
                arrayAdapter = new ArrayAdapter<>(activity, R.layout.spinner_search_item, new ArrayList<>(Arrays.asList(productsName)));
                listView.setAdapter(arrayAdapter);

                break;
            case "flash_sale_all":
                tabLayout_.setVisibility(View.VISIBLE);

                tab = tabLayout.newTab().setText(activity.getString(R.string.loading)).setTag("0");
                tabLayout.addTab(tab);

                GetFlashSales(offsetFlashSaleNames, tab);

                tabLayout.getViewTreeObserver().addOnScrollChangedListener(() -> {
                    Point windowSize = new Point();
                    activity.getWindowManager().getDefaultDisplay().getSize(windowSize);
                    int scrollX = tabLayout.getScrollX();
                    int maxScrollWidth = tabLayout.getChildAt(0).getMeasuredWidth() - windowSize.x;

                    if (maxScrollWidth == scrollX && !tabLoading) {
                        tab = tabLayout.newTab().setText(activity.getString(R.string.loading)).setTag("0");
                        tabLayout.addTab(tab);
                        offsetFlashSaleNames += Constant.LOAD_ITEM_LIMIT;
                        GetFlashSales(offsetFlashSaleNames, tab);
                    }
                });
                break;
        }
    }

    public void getAllWidgets() {
        recyclerView = root.findViewById(R.id.recyclerView);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvAlert = root.findViewById(R.id.tvAlert);
        nestedScrollView = root.findViewById(R.id.nestedScrollView);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);
        tabLayout_ = root.findViewById(R.id.tabLayout_);
        lytList = root.findViewById(R.id.lytList);
        lytGrid = root.findViewById(R.id.lytGrid);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvAlert = root.findViewById(R.id.tvAlert);
        nestedScrollView = root.findViewById(R.id.nestedScrollView);
        tabLayout = root.findViewById(R.id.tabLayout);
        listView = root.findViewById(R.id.listView);
        searchView = root.findViewById(R.id.searchView);
        noResult = root.findViewById(R.id.noResult);
        msg = root.findViewById(R.id.msg);
        lytSearchView = root.findViewById(R.id.lytSearchView);
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }


    void GetFlashSales(int offset, TabLayout.Tab tab) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ALL_FLASH_SALES, Constant.GetVal);
        params.put(Constant.OFFSET, "" + offset);
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    tabLayout.removeTab(tab);
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        totalFlashSales = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                        if (flashSalesLists.size() == 0 || flashSalesLists.size() < totalFlashSales) {
                            JSONObject object = new JSONObject(response);
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                            tabLayout.setVisibility(View.VISIBLE);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                FlashSalesList flashSale = new Gson().fromJson(jsonObject1.toString(), FlashSalesList.class);
                                flashSalesLists.add(flashSale);
                                TabLayout.Tab tab_ = tabLayout.newTab().setText(flashSale.getTitle()).setTag(flashSale.getId());
                                tabLayout.addTab(tab_);
                            }
                        }
                    } else {
                        tabLoading = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                tabLoading = true;
            }
        }, activity, Constant.FLASH_SALES_URL, params, false);
    }

    void SearchRequest(final String query) {
        listView.setVisibility(View.GONE);
        productArrayList = new ArrayList<>();
        startShimmer();

        Map<String, String> params = new HashMap<>();
        params.put(Constant.TYPE, Constant.PRODUCT_SEARCH);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.SEARCH, query);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    if (!jsonObject1.getBoolean(Constant.ERROR)) {
                        total = Integer.parseInt(jsonObject1.getString(Constant.TOTAL));
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        try {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Product product = new Gson().fromJson(jsonArray.getJSONObject(i).toString(), Product.class);
                                productArrayList.add(product);
                            }
                        } catch (Exception e) {
                            stopShimmer();
                            recyclerView.setVisibility(View.GONE);
                            noResult.setVisibility(View.VISIBLE);
                            msg.setVisibility(View.VISIBLE);
                        }
                        if (offset == 0) {
                            productLoadMoreAdapter = new ProductLoadMoreAdapter(activity, productArrayList, resource, from);
                            recyclerView.setAdapter(productLoadMoreAdapter);
                            stopShimmer();
                            recyclerView.setVisibility(View.VISIBLE);
                            noResult.setVisibility(View.GONE);
                            msg.setVisibility(View.GONE);
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
                                                offset += Integer.parseInt("" + Constant.LOAD_ITEM_LIMIT);

                                                Map<String, String> params1 = new HashMap<>();
                                                params1.put(Constant.TYPE, Constant.PRODUCT_SEARCH);
                                                params1.put(Constant.USER_ID, session.getData(Constant.ID));
                                                params1.put(Constant.SEARCH, query);

                                                ApiConfig.RequestToVolley((result1, response1) -> {
                                                    if (result1) {
                                                        productArrayList.remove(productArrayList.size() - 1);
                                                        productLoadMoreAdapter.notifyItemRemoved(productArrayList.size());
                                                        try {
                                                            JSONObject jsonObject11 = new JSONObject(response1);
                                                            if (!jsonObject11.getBoolean(Constant.ERROR)) {
                                                                JSONObject object1 = new JSONObject(response1);
                                                                JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);
                                                                for (int i = 0; i < jsonArray1.length(); i++) {
                                                                    Product product = new Gson().fromJson(jsonArray1.getJSONObject(i).toString(), Product.class);
                                                                    productArrayList.add(product);
                                                                }
                                                                productLoadMoreAdapter.notifyDataSetChanged();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }, activity, Constant.PRODUCT_SEARCH_URL, params1, false);
                                                isLoadMore = true;
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        stopShimmer();
                        recyclerView.setVisibility(View.GONE);
                        noResult.setVisibility(View.VISIBLE);
                        msg.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    stopShimmer();
                    recyclerView.setVisibility(View.GONE);
                    noResult.setVisibility(View.VISIBLE);
                    msg.setVisibility(View.VISIBLE);
                }
            }
        }, activity, Constant.PRODUCT_SEARCH_URL, params, false);
    }

    void GetData() {
        productArrayList = new ArrayList<>();
        recyclerView.setAdapter(new ProductLoadMoreAdapter(activity, productArrayList, resource, from));
        startShimmer();
        Map<String, String> params = new HashMap<>();
        switch (from) {
            case "regular":
            case "sub_cate":
                url = Constant.GET_PRODUCT_BY_SUB_CATE;
                params.put(Constant.SUB_CATEGORY_ID, id);
                if (filterIndex != -1) {
                    params.put(Constant.SORT, filterBy);
                }
                isSort = true;
                break;
            case "similar":
                url = Constant.GET_SIMILAR_PRODUCT_URL;
                assert getArguments() != null;
                params.put(Constant.GET_SIMILAR_PRODUCT, Constant.GetVal);
                params.put(Constant.PRODUCT_ID, id);
                params.put(Constant.CATEGORY_ID, getArguments().getString("cat_id"));
                break;
            case "section":
                url = Constant.GET_SECTION_URL;
                params.put(Constant.GET_ALL_SECTIONS, Constant.GetVal);
                params.put(Constant.SECTION_ID, id);
                break;
            case "flash_sale":
            case "flash_sale_all":
                url = Constant.FLASH_SALES_URL;
                params.put(Constant.GET_ALL_FLASH_SALES_PRODUCTS, Constant.GetVal);
                params.put(Constant.FLASH_SALES_ID, id);
                break;
        }
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
        params.put(Constant.OFFSET, "" + offset);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    if (!jsonObject1.getBoolean(Constant.ERROR)) {
                        total = Integer.parseInt(jsonObject1.getString(Constant.TOTAL));
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        try {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Product product = new Gson().fromJson(jsonArray.getJSONObject(i).toString(), Product.class);
                                productArrayList.add(product);
                            }
                        } catch (Exception e) {
                            stopShimmer();
                            recyclerView.setVisibility(View.GONE);
                            tvAlert.setVisibility(View.VISIBLE);
                        }
                        if (offset == 0) {
                            productLoadMoreAdapter = new ProductLoadMoreAdapter(activity, productArrayList, resource, from);
                            recyclerView.setAdapter(productLoadMoreAdapter);
                            stopShimmer();
                            recyclerView.setVisibility(View.VISIBLE);
                            tvAlert.setVisibility(View.GONE);
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
                                                offset += Integer.parseInt("" + Constant.LOAD_ITEM_LIMIT);
                                                Map<String, String> params1 = new HashMap<>();
                                                switch (from) {
                                                    case "regular":
                                                    case "sub_cate":
                                                        params1.put(Constant.SUB_CATEGORY_ID, id);
                                                        if (filterIndex != -1) {
                                                            params1.put(Constant.SORT, filterBy);
                                                        }
                                                        isSort = true;
                                                        break;
                                                    case "similar":
                                                        assert getArguments() != null;
                                                        params1.put(Constant.GET_SIMILAR_PRODUCT, Constant.GetVal);
                                                        params1.put(Constant.PRODUCT_ID, id);
                                                        params1.put(Constant.CATEGORY_ID, getArguments().getString("cat_id"));
                                                        break;
                                                    case "section":
                                                        params1.put(Constant.GET_ALL_SECTIONS, Constant.GetVal);
                                                        params1.put(Constant.SECTION_ID, id);
                                                        break;
                                                    case "flash_sale":
                                                    case "flash_sale_all":
                                                        params1.put(Constant.GET_ALL_FLASH_SALES_PRODUCTS, Constant.GetVal);
                                                        params1.put(Constant.FLASH_SALES_ID, id);
                                                        break;
                                                }
                                                params1.put(Constant.USER_ID, session.getData(Constant.ID));
                                                params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
                                                params1.put(Constant.OFFSET, "" + offset);

                                                ApiConfig.RequestToVolley((result1, response1) -> {
                                                    if (result1) {
                                                        productArrayList.remove(productArrayList.size() - 1);
                                                        productLoadMoreAdapter.notifyItemRemoved(productArrayList.size());
                                                        try {
                                                            JSONObject jsonObject11 = new JSONObject(response1);
                                                            if (!jsonObject11.getBoolean(Constant.ERROR)) {
                                                                JSONObject object1 = new JSONObject(response1);
                                                                JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);
                                                                for (int i = 0; i < jsonArray1.length(); i++) {
                                                                    Product product = new Gson().fromJson(jsonArray1.getJSONObject(i).toString(), Product.class);
                                                                    productArrayList.add(product);
                                                                }

                                                                productLoadMoreAdapter.notifyDataSetChanged();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
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
                        stopShimmer();
                        recyclerView.setVisibility(View.GONE);
                        tvAlert.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    stopShimmer();
                    recyclerView.setVisibility(View.GONE);
                    tvAlert.setVisibility(View.VISIBLE);
                }
            }
        }, activity, url, params, false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.toolbar_sort) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getResources().getString(R.string.filter_by));
            builder.setSingleChoiceItems(Constant.filterValues, filterIndex, (dialog, item1) -> {
                filterIndex = item1;
                switch (item1) {
                    case 0:
                        filterBy = Constant.NEW;
                        break;
                    case 1:
                        filterBy = Constant.OLD;
                        break;
                    case 2:
                        filterBy = Constant.HIGH;
                        break;
                    case 3:
                        filterBy = Constant.LOW;
                        break;
                }
                if (item1 != -1)
                    GetData();
                dialog.dismiss();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else if (item.getItemId() == R.id.toolbar_layout) {
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
            productLoadMoreAdapter = new ProductLoadMoreAdapter(activity, productArrayList, resource, from);
            recyclerView.setAdapter(productLoadMoreAdapter);
            productLoadMoreAdapter.notifyDataSetChanged();
            activity.invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        activity.getMenuInflater().inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.toolbar_sort).setVisible(isSort);
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
        assert getArguments() != null;
        Constant.TOOLBAR_TITLE = getArguments().getString(Constant.NAME);
        activity.invalidateOptionsMenu();
        if (getArguments().getString(Constant.FROM).equals("search")) {
            recyclerView.setVisibility(View.GONE);
            searchView.requestFocus();
            showSoftKeyboard(searchView);
        } else {
            hideKeyboard();
        }
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
    public void onPause() {
        super.onPause();
        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
    }
}