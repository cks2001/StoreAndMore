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
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.adapter.FaqAdapter;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Faq;


public class FaqFragment extends Fragment {
    View root;
    RecyclerView recyclerView;
    ArrayList<Faq> Faqs;
    SwipeRefreshLayout swipeLayout;
    NestedScrollView scrollView;
    RelativeLayout tvAlert;
    FaqAdapter FaqAdapter;
    int total = 0;
    Activity activity;
    int offset = 0;
    Session session;
    boolean isLoadMore = false;
    private ShimmerFrameLayout mShimmerViewContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_faq, container, false);

        activity = getActivity();
        session = new Session(activity);


        scrollView = root.findViewById(R.id.scrollView);
        recyclerView = root.findViewById(R.id.recyclerView);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvAlert = root.findViewById(R.id.tvAlert);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);

        setHasOptionsMenu(true);

        getFaqData();

        swipeLayout.setColorSchemeResources(R.color.colorPrimary);

        swipeLayout.setOnRefreshListener(() -> {
            swipeLayout.setRefreshing(false);
            offset = 0;
            getFaqData();
        });


        return root;
    }


    @SuppressLint("NotifyDataSetChanged")
    void getFaqData() {
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Faqs = new ArrayList<>();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_FAQS, Constant.GetVal);
        params.put(Constant.OFFSET, "" + offset);
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT + 10);

        ApiConfig.RequestToVolley((result, response) -> {
                    if (result) {
                        try {
//                        System.out.println("====transaction " + response);
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                                session.setData(Constant.TOTAL, String.valueOf(total));

                                JSONObject object = new JSONObject(response);
                                JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                                Gson g = new Gson();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                    if (jsonObject1 != null) {
                                        Faq faq = g.fromJson(jsonObject1.toString(), Faq.class);
                                        Faqs.add(faq);
                                    } else {
                                        break;
                                    }

                                }
                                if (offset == 0) {
                                    FaqAdapter = new FaqAdapter(activity, Faqs);
                                    recyclerView.setAdapter(FaqAdapter);
                                    mShimmerViewContainer.stopShimmer();
                                    mShimmerViewContainer.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                        // if (diff == 0) {
                                        if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                            LinearLayoutManager linearLayoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
                                            if (Faqs.size() < total) {
                                                if (!isLoadMore) {
                                                    if (linearLayoutManager1 != null && linearLayoutManager1.findLastCompletelyVisibleItemPosition() == Faqs.size() - 1) {
                                                        //bottom of list!
                                                        Faqs.add(null);
                                                        FaqAdapter.notifyItemInserted(Faqs.size() - 1);

                                                        offset += Constant.LOAD_ITEM_LIMIT;
                                                        Map<String, String> params1 = new HashMap<>();
                                                        params1.put(Constant.GET_FAQS, Constant.GetVal);
                                                        params1.put(Constant.OFFSET, "" + offset);
                                                        params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT + 10);

                                                        ApiConfig.RequestToVolley((result1, response1) -> {

                                                            if (result1) {
                                                                try {
                                                                    // System.out.println("====product  " + response);
                                                                    JSONObject jsonObject2 = new JSONObject(response1);
                                                                    if (!jsonObject2.getBoolean(Constant.ERROR)) {

                                                                        session.setData(Constant.TOTAL, jsonObject2.getString(Constant.TOTAL));

                                                                        Faqs.remove(Faqs.size() - 1);
                                                                        FaqAdapter.notifyItemRemoved(Faqs.size());

                                                                        JSONObject object1 = new JSONObject(response1);
                                                                        JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);

                                                                        Gson g1 = new Gson();


                                                                        for (int i = 0; i < jsonArray1.length(); i++) {
                                                                            JSONObject jsonObject1 = jsonArray1.getJSONObject(i);

                                                                            if (jsonObject1 != null) {
                                                                                Faq Faq = g1.fromJson(jsonObject1.toString(), Faq.class);
                                                                                Faqs.add(Faq);
                                                                            } else {
                                                                                break;
                                                                            }

                                                                        }
                                                                        FaqAdapter.notifyDataSetChanged();
                                                                        isLoadMore = false;
                                                                    }
                                                                } catch (JSONException e) {
                                                                    mShimmerViewContainer.stopShimmer();
                                                                    mShimmerViewContainer.setVisibility(View.GONE);
                                                                    recyclerView.setVisibility(View.VISIBLE);
                                                                }
                                                            }
                                                        }, activity, Constant.FAQ_URL, params1, false);

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
                },
                activity,
                Constant.FAQ_URL,
                params,
                false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.faq);
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
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
    }

}