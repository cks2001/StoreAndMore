package com.exampe.storeandmore.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.adapter.TrackerAdapter;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.OrderTracker;


public class OrderListProcessedFragment extends Fragment {
    RecyclerView recyclerView;
    TextView tvNoData;
    Session session;
    Activity activity;
    View root;
    ArrayList<OrderTracker> orderTrackerArrayList;
    TrackerAdapter trackerAdapter;
    private int offset = 0;
    private int total = 0;
    private NestedScrollView scrollView;
    private ShimmerFrameLayout mShimmerViewContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_order_list, container, false);

        activity = getActivity();
        session = new Session(activity);
        recyclerView = root.findViewById(R.id.recyclerView);
        scrollView = root.findViewById(R.id.scrollView);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);
        tvNoData = root.findViewById(R.id.tvNoData);
        setHasOptionsMenu(true);
        SwipeRefreshLayout swipeLayout;
        swipeLayout = root.findViewById(R.id.swipeLayout);
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity,R.color.colorPrimary));
        swipeLayout.setOnRefreshListener(() -> {
            offset = 0;
            swipeLayout.setRefreshing(false);
            getAllOrders();
        });

        getAllOrders();

        return root;
    }

    void getAllOrders() {
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        orderTrackerArrayList = new ArrayList<>();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ORDERS, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.STATUS, Constant.PROCESSED);
        params.put(Constant.OFFSET, "" + offset);
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                        session.setData(Constant.TOTAL, String.valueOf(total));

                        JSONObject object = new JSONObject(response);
                        orderTrackerArrayList.addAll(ApiConfig.GetOrders(object.getJSONArray(Constant.DATA)));
                        if (offset == 0) {
                            trackerAdapter = new TrackerAdapter(activity, activity, orderTrackerArrayList);
                            recyclerView.setAdapter(trackerAdapter);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                private boolean isLoadMore;

                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                                    // if (diff == 0) {
                                    if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                        LinearLayoutManager linearLayoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
                                        if (orderTrackerArrayList.size() < total) {
                                            if (!isLoadMore) {
                                                if (linearLayoutManager1 != null && linearLayoutManager1.findLastCompletelyVisibleItemPosition() == orderTrackerArrayList.size() - 1) {
                                                    //bottom of list!
                                                    orderTrackerArrayList.add(null);
                                                    trackerAdapter.notifyItemInserted(orderTrackerArrayList.size() - 1);

                                                    offset += Constant.LOAD_ITEM_LIMIT;
                                                    Map<String, String> params1 = new HashMap<>();
                                                    params1.put(Constant.GET_ORDERS, Constant.GetVal);
                                                    params1.put(Constant.USER_ID, session.getData(Constant.ID));
                                                    params1.put(Constant.STATUS, Constant.PROCESSED);
                                                    params1.put(Constant.OFFSET, "" + offset);
                                                    params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

                                                    ApiConfig.RequestToVolley((result1, response1) -> {

                                                        if (result1) {
                                                            try {
                                                                // System.out.println("====product  " + response);
                                                                JSONObject jsonObject1 = new JSONObject(response1);
                                                                if (!jsonObject1.getBoolean(Constant.ERROR)) {

                                                                    session.setData(Constant.TOTAL, jsonObject1.getString(Constant.TOTAL));

                                                                    orderTrackerArrayList.remove(orderTrackerArrayList.size() - 1);
                                                                    trackerAdapter.notifyItemRemoved(orderTrackerArrayList.size());

                                                                    JSONObject object1 = new JSONObject(response1);
                                                                    orderTrackerArrayList.addAll(ApiConfig.GetOrders(object1.getJSONArray(Constant.DATA)));
                                                                    trackerAdapter.notifyDataSetChanged();
                                                                    isLoadMore = false;
                                                                }
                                                            } catch (JSONException e) {
                                                                mShimmerViewContainer.stopShimmer();
                                                                mShimmerViewContainer.setVisibility(View.GONE);
                                                                recyclerView.setVisibility(View.VISIBLE);
                                                            }
                                                        }
                                                    }, activity, Constant.ORDER_PROCESS_URL, params1, false);

                                                }
                                                isLoadMore = true;
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.VISIBLE);
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false);
    }


    @Override
    public void onResume() {
        super.onResume();
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
}