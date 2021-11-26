package com.exampe.storeandmore.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.PaymentModelClass;
import com.exampe.storeandmore.helper.Session;

@SuppressWarnings({"ALL", "unchecked"})
public class MidtransActivity extends AppCompatActivity {
    Toolbar toolbar;
    WebView webView;
    String url;
    PaymentModelClass paymentModelClass;
    boolean isTxnInProcess = true;
    String orderId;
    Map<String, String> sendParams;
    String from;


    TextView toolbarTitle;
    ImageView imageMenu;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageMenu = findViewById(R.id.imageMenu);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        imageMenu = findViewById(R.id.imageMenu);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        toolbarTitle.setText(getString(R.string.midtrans));

        imageMenu.setImageResource(R.drawable.ic_arrow_back);
        imageMenu.setOnClickListener(view -> onBackPressed());

        paymentModelClass = new PaymentModelClass(MidtransActivity.this);
        url = getIntent().getStringExtra("url");
        orderId = getIntent().getStringExtra(Constant.ORDER_ID);
        sendParams = (Map<String, String>) getIntent().getSerializableExtra(Constant.PARAMS);
        from = getIntent().getStringExtra(Constant.FROM);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(Constant.MainBaseURL)) {
                    GetTransactionResponse(url);
                    return true;
                } else
                    isTxnInProcess = true;
                return false;
            }
        });
        webView.loadUrl(url);
    }

    public void GetTransactionResponse(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    isTxnInProcess = false;
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("transaction_status");
                        AddTransaction(MidtransActivity.this, orderId, getString(R.string.midtrans), orderId, status, jsonObject.getString(Constant.MESSAGE), sendParams);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                Throwable::printStackTrace);
        ApiConfig.getInstance().getRequestQueue().getCache().clear();
        ApiConfig.getInstance().addToRequestQueue(stringRequest);

    }

    public void AddTransaction(Activity activity, String orderId, String paymentType, String txnid, final String status, String message, Map<String, String> sendParams) {
        Map<String, String> transactionParams = new HashMap<>();
        transactionParams.put(Constant.ADD_TRANSACTION, Constant.GetVal);
        transactionParams.put(Constant.USER_ID, sendParams.get(Constant.USER_ID));
        transactionParams.put(Constant.ORDER_ID, orderId);
        transactionParams.put(Constant.TYPE, paymentType);
        transactionParams.put(Constant.TRANS_ID, txnid);
        transactionParams.put(Constant.AMOUNT, sendParams.get(Constant.FINAL_TOTAL));
        transactionParams.put(Constant.STATUS, status);
        transactionParams.put(Constant.MESSAGE, message);
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        transactionParams.put("transaction_date", df.format(c));
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {

                        if (from.equals(Constant.WALLET)) {
                            onBackPressed();
                            ApiConfig.getWalletBalance(activity, new Session(activity));
                            Toast.makeText(activity, getString(R.string.wallet_message), Toast.LENGTH_SHORT).show();
                        } else if (from.equals(Constant.PAYMENT)) {
                            if (status.equals("capture") || status.equals("challenge") || status.equals("pending")) {
                                finish();
                                Intent intent = new Intent(activity, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(Constant.FROM, "payment_success");
                                activity.startActivity(intent);
                            } else if (status.equals("deny") || status.equals("expire") || status.equals("cancel")) {
                                finish();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, MidtransActivity.this, Constant.ORDER_PROCESS_URL, transactionParams, false);
    }

    @Override
    public void onBackPressed() {
        if (isTxnInProcess) {
            ProcessAlertDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public void ProcessAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MidtransActivity.this);
        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.txn_cancel_msg));
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();
        alertDialog.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            DeleteTransaction(MidtransActivity.this, getIntent().getStringExtra(Constant.ORDER_ID));
            alertDialog1.dismiss();
        }).setNegativeButton(getString(R.string.no), (dialog, which) -> alertDialog1.dismiss());
        // Showing Alert Message
        alertDialog.show();
    }

    public void DeleteTransaction(Activity activity, String orderId) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.DELETE_ORDER, Constant.GetVal);
        params.put(Constant.ORDER_ID, orderId);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                MidtransActivity.super.onBackPressed();
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false);
    }
}
