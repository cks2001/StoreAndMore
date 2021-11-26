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
import android.widget.TextView;

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
import com.exampe.storeandmore.adapter.TransactionAdapter;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Transaction;


public class TransactionFragment extends Fragment {
    View root;
    RecyclerView recyclerView;
    ArrayList<Transaction> transactions;
    RelativeLayout tvAlert;
    SwipeRefreshLayout swipeLayout;
    NestedScrollView scrollView;
    TransactionAdapter transactionAdapter;
    int total = 0;
    Activity activity;
    TextView tvAlertTitle, tvAlertSubTitle;
    int offset = 0;
    Session session;
    boolean isLoadMore = false;
    private ShimmerFrameLayout mShimmerViewContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_transection, container, false);

        offset = 0;

        activity = getActivity();
        session = new Session(activity);

        setHasOptionsMenu(true);


        scrollView = root.findViewById(R.id.scrollView);
        recyclerView = root.findViewById(R.id.recyclerView);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvAlert = root.findViewById(R.id.tvAlert);
        tvAlertTitle = root.findViewById(R.id.tvAlertTitle);
        tvAlertSubTitle = root.findViewById(R.id.tvAlertSubTitle);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);

        tvAlertTitle.setText(getString(R.string.no_transaction_history_found));
        tvAlertSubTitle.setText(getString(R.string.you_have_not_any_transactional_history_yet));

        if (ApiConfig.isConnected(activity)) {
            getTransactionData();
        }

        swipeLayout.setColorSchemeResources(R.color.colorPrimary);

        swipeLayout.setOnRefreshListener(() -> {
            swipeLayout.setRefreshing(false);
            offset = 0;
            getTransactionData();
        });


        return root;
    }


    @SuppressLint("NotifyDataSetChanged")
    void getTransactionData() {
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        transactions = new ArrayList<>();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_TRANSACTION, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.TYPE, Constant.TYPE_TRANSACTION);
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
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                        Gson g = new Gson();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                            if (jsonObject1 != null) {
                                Transaction Transaction = g.fromJson(jsonObject1.toString(), Transaction.class);
                                transactions.add(Transaction);
                            } else {
                                break;
                            }

                        }
                        if (offset == 0) {
                            transactionAdapter = new TransactionAdapter(activity, activity, transactions);
                            recyclerView.setAdapter(transactionAdapter);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    LinearLayoutManager linearLayoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
                                    if (transactions.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager1 != null && linearLayoutManager1.findLastCompletelyVisibleItemPosition() == transactions.size() - 1) {
                                                //bottom of list!
                                                transactions.add(null);
                                                transactionAdapter.notifyItemInserted(transactions.size() - 1);
                                                offset += Constant.LOAD_ITEM_LIMIT;
                                                Map<String, String> params1 = new HashMap<>();
                                                params1.put(Constant.GET_USER_TRANSACTION, Constant.GetVal);
                                                params1.put(Constant.USER_ID, session.getData(Constant.ID));
                                                params1.put(Constant.TYPE, Constant.TYPE_TRANSACTION);
                                                params1.put(Constant.OFFSET, "" + offset);
                                                params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

                                                ApiConfig.RequestToVolley((result1, response1) -> {

                                                    if (result1) {
                                                        try {
                                                            JSONObject jsonObject2 = new JSONObject(response1);
                                                            transactions.remove(transactions.size() - 1);
                                                            transactionAdapter.notifyItemRemoved(transactions.size());
                                                            if (!jsonObject2.getBoolean(Constant.ERROR)) {

                                                                session.setData(Constant.TOTAL, jsonObject2.getString(Constant.TOTAL));


                                                                JSONObject object1 = new JSONObject(response1);
                                                                JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);

                                                                Gson g1 = new Gson();


                                                                for (int i = 0; i < jsonArray1.length(); i++) {
                                                                    JSONObject jsonObject1 = jsonArray1.getJSONObject(i);

                                                                    if (jsonObject1 != null) {
                                                                        Transaction Transaction = g1.fromJson(jsonObject1.toString(), Transaction.class);
                                                                        transactions.add(Transaction);
                                                                    } else {
                                                                        break;
                                                                    }

                                                                }
                                                                transactionAdapter.notifyDataSetChanged();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            mShimmerViewContainer.stopShimmer();
                                                            mShimmerViewContainer.setVisibility(View.GONE);
                                                            recyclerView.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                }, activity, Constant.TRANSACTION_URL, params1, false);

                                                isLoadMore = true;
                                            }

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
        }, activity, Constant.TRANSACTION_URL, params, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.transaction_history);
        activity.invalidateOptionsMenu();
        Session.setCount(Constant.UNREAD_TRANSACTION_COUNT, 0, activity);
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