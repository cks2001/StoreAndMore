package com.exampe.storeandmore.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;

public class WebViewFragment extends Fragment {
    public ProgressBar prgLoading;
    public WebView mWebView;
    public String type;
    View root;
    Activity activity;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_web_view, container, false);
        setHasOptionsMenu(true);
        activity = getActivity();
        assert getArguments() != null;
        type = getArguments().getString("type");

        prgLoading = root.findViewById(R.id.prgLoading);
        mWebView = root.findViewById(R.id.webView1);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null) {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });
        try {
            if (ApiConfig.isConnected(activity)) {
                switch (type) {
                    case "Privacy Policy":
                        GetContent(Constant.GET_PRIVACY, "privacy");
                        break;
                    case "Terms & Conditions":
                        GetContent(Constant.GET_TERMS, "terms");
                        break;
                    case "Contact Us":
                        GetContent(Constant.GET_CONTACT, "contact");
                        break;
                    case "About Us":
                        GetContent(Constant.GET_ABOUT_US, "about");
                        break;
                }
                activity.invalidateOptionsMenu();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root;
    }


    public void GetContent(final String type, final String key) {
        prgLoading.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SETTINGS, Constant.GetVal);
        params.put(type, Constant.GetVal);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean(Constant.ERROR)) {

                        String privacyStr = obj.getString(key);
                        mWebView.setVerticalScrollBarEnabled(true);
                        mWebView.loadDataWithBaseURL("", privacyStr, "text/html", "UTF-8", "");

                        prgLoading.setVisibility(View.GONE);
                    } else {
                        prgLoading.setVisibility(View.GONE);
                        Toast.makeText(activity, obj.getString(Constant.MESSAGE), Toast.LENGTH_LONG).show();
                    }
                    prgLoading.setVisibility(View.GONE);
                } catch (JSONException e) {

                    prgLoading.setVisibility(View.GONE);

                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        assert getArguments() != null;
        Constant.TOOLBAR_TITLE = getArguments().getString("type");
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
        menu.findItem(R.id.toolbar_search).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
}