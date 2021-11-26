package com.exampe.storeandmore.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.adapter.ReviewAdapter;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Review;


public class ReviewFragment extends Fragment {
    View root;

    RecyclerView recyclerView;
    ArrayList<Review> reviewArrayList;
    ReviewAdapter reviewAdapter;
    SwipeRefreshLayout swipeLayout;
    NestedScrollView scrollView;
    RelativeLayout tvAlert;
    int total = 0;
    LinearLayoutManager linearLayoutManager;
    Activity activity;
    int offset = 0;
    Session session;
    boolean isLoadMore = false;
    String from, productId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_review, container, false);

        activity = getActivity();
        session = new Session(activity);

        assert getArguments() != null;
        from = getArguments().getString(Constant.FROM);
        productId = getArguments().getString(Constant.ID);

        recyclerView = root.findViewById(R.id.recyclerView);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvAlert = root.findViewById(R.id.tvAlert);
        scrollView = root.findViewById(R.id.scrollView);
        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        setHasOptionsMenu(true);


        if (ApiConfig.isConnected(activity)) {
            getNotificationData();
        }

        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity,R.color.colorPrimary));
        swipeLayout.setOnRefreshListener(() -> {
            if (reviewArrayList != null) {
                reviewArrayList = null;
            }
            offset = 0;
            getNotificationData();
            swipeLayout.setRefreshing(false);
        });


        return root;
    }


    @SuppressLint("NotifyDataSetChanged")
    void getNotificationData() {
        reviewArrayList = new ArrayList<>();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_PRODUCT_REVIEW, Constant.GetVal);
        params.put(Constant.LIMIT, "" + (Constant.LOAD_ITEM_LIMIT + 10));
        params.put(Constant.OFFSET, "" + offset);
        if (from.equals("share")) {
            params.put(Constant.SLUG, productId);
        } else {
            params.put(Constant.PRODUCT_ID, productId);
        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        total = Integer.parseInt(jsonObject.getString(Constant.NUMBER_OF_REVIEW));
                        session.setData(Constant.TOTAL, String.valueOf(total));

                        JSONArray jsonArrayReviews = jsonObject.getJSONArray(Constant.PRODUCT_REVIEW);

                        for (int i = 0; i < (Math.min(jsonArrayReviews.length(), 5)); i++) {
                            Review review = new Gson().fromJson(jsonArrayReviews.getJSONObject(i).toString(), Review.class);
                            reviewArrayList.add(review);
                        }
                        if (offset == 0) {
                            reviewAdapter = new ReviewAdapter(activity, reviewArrayList);
                            recyclerView.setAdapter(reviewAdapter);
                            scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    if (reviewArrayList.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == reviewArrayList.size() - 1) {
                                                //bottom of list!
                                                reviewArrayList.add(null);
                                                reviewAdapter.notifyItemInserted(reviewArrayList.size() - 1);
                                                offset += Constant.LOAD_ITEM_LIMIT + 10;
                                                Map<String, String> params1 = new HashMap<>();
                                                params1.put(Constant.GET_PRODUCT_REVIEW, Constant.GetVal);
                                                params1.put(Constant.LIMIT, "" + (Constant.LOAD_ITEM_LIMIT + 10));
                                                params1.put(Constant.OFFSET, "" + offset);
                                                if (from.equals("share")) {
                                                    params1.put(Constant.SLUG, productId);
                                                } else {
                                                    params1.put(Constant.PRODUCT_ID, productId);
                                                }

                                                ApiConfig.RequestToVolley((result1, response1) -> {

                                                    if (result1) {
                                                        try {
                                                            JSONObject jsonObject1 = new JSONObject(response1);

                                                            reviewArrayList.remove(reviewArrayList.size() - 1);
                                                            reviewAdapter.notifyItemRemoved(reviewArrayList.size());
                                                            if (!jsonObject1.getBoolean(Constant.ERROR)) {
                                                                session.setData(Constant.TOTAL, jsonObject1.getString(Constant.TOTAL));
                                                                JSONArray jsonArrayReviews1 = jsonObject.getJSONArray(Constant.PRODUCT_REVIEW);
                                                                for (int i = 0; i < (Math.min(jsonArrayReviews1.length(), 5)); i++) {
                                                                    Review review = new Gson().fromJson(jsonArrayReviews1.getJSONObject(i).toString(), Review.class);
                                                                    reviewArrayList.add(review);
                                                                }
                                                                reviewAdapter = new ReviewAdapter(activity, reviewArrayList);
                                                                recyclerView.setAdapter(reviewAdapter);

                                                                reviewAdapter.notifyDataSetChanged();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }, activity, Constant.GET_ALL_PRODUCTS_URL, params1, false);

                                            }
                                            isLoadMore = true;
                                        }

                                    }
                                }
                            });
                        }
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        tvAlert.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GET_ALL_PRODUCTS_URL, params, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.reviews);
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
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }


}