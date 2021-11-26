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

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.adapter.NotificationAdapter;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Notification;

public class NotificationFragment extends Fragment {
    View root;

    RecyclerView recyclerView;
    ArrayList<Notification> notifications;
    NotificationAdapter notificationAdapter;
    SwipeRefreshLayout swipeLayout;
    NestedScrollView scrollView;
    RelativeLayout tvAlert;
    int total = 0;
    LinearLayoutManager linearLayoutManager;
    Activity activity;
    int offset = 0;
    Session session;
    boolean isLoadMore = false;
    private ShimmerFrameLayout mShimmerViewContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_notification, container, false);

        activity = getActivity();

        session = new Session(activity);

        recyclerView = root.findViewById(R.id.recyclerView);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvAlert = root.findViewById(R.id.tvAlert);
        scrollView = root.findViewById(R.id.scrollView);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);
        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        setHasOptionsMenu(true);


        if (ApiConfig.isConnected(activity)) {
            getNotificationData();
        }

        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity,R.color.colorPrimary));
        swipeLayout.setOnRefreshListener(() -> {
            if (notifications != null) {
                notifications = null;
            }
            offset = 0;
            getNotificationData();
            swipeLayout.setRefreshing(false);
        });


        return root;
    }

    public void stopShimmer() {
        scrollView.setVisibility(View.VISIBLE);
        mShimmerViewContainer.setVisibility(View.GONE);
        mShimmerViewContainer.stopShimmer();
    }

    public void startShimmer() {
        scrollView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
    }


    @SuppressLint("NotifyDataSetChanged")
    void getNotificationData() {
        startShimmer();
        notifications = new ArrayList<>();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_NOTIFICATIONS, Constant.GetVal);
        params.put(Constant.OFFSET, "" + offset);
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT + 10);

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
                                Notification notification = g.fromJson(jsonObject1.toString(), Notification.class);
                                notifications.add(notification);
                            } else {
                                break;
                            }

                        }
                        if (offset == 0) {
                            notificationAdapter = new NotificationAdapter(activity, notifications);
                            recyclerView.setAdapter(notificationAdapter);
                            stopShimmer();
                            scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    if (notifications.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == notifications.size() - 1) {
                                                //bottom of list!
                                                notifications.add(null);
                                                notificationAdapter.notifyItemInserted(notifications.size() - 1);
                                                offset += Constant.LOAD_ITEM_LIMIT + 10;
                                                Map<String, String> params1 = new HashMap<>();
                                                params1.put(Constant.GET_NOTIFICATIONS, Constant.GetVal);
                                                params1.put(Constant.OFFSET, "" + offset);
                                                params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT + 10);

                                                ApiConfig.RequestToVolley((result1, response1) -> {

                                                    if (result1) {
                                                        try {
                                                            JSONObject jsonObject12 = new JSONObject(response1);
                                                            if (!jsonObject12.getBoolean(Constant.ERROR)) {

                                                                session.setData(Constant.TOTAL, jsonObject12.getString(Constant.TOTAL));

                                                                notifications.remove(notifications.size() - 1);
                                                                notificationAdapter.notifyItemRemoved(notifications.size());

                                                                JSONObject object1 = new JSONObject(response1);
                                                                JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);

                                                                Gson g1 = new Gson();


                                                                for (int i = 0; i < jsonArray1.length(); i++) {
                                                                    JSONObject jsonObject1 = jsonArray1.getJSONObject(i);

                                                                    if (jsonObject1 != null) {
                                                                        Notification notification = g1.fromJson(jsonObject1.toString(), Notification.class);
                                                                        notifications.add(notification);
                                                                    } else {
                                                                        break;
                                                                    }
                                                                }
                                                                notificationAdapter.notifyDataSetChanged();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }, activity, Constant.GET_SECTION_URL, params1, false);

                                            }
                                            isLoadMore = true;
                                        }

                                    }
                                }
                            });
                        }
                    } else {
                        stopShimmer();
                        tvAlert.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    stopShimmer();
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GET_SECTION_URL, params, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.notifications);
        activity.invalidateOptionsMenu();
        Session.setCount(Constant.UNREAD_NOTIFICATION_COUNT, 0, activity);
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