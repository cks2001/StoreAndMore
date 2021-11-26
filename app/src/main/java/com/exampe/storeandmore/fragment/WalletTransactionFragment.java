package com.exampe.storeandmore.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.gson.Gson;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.razorpay.Checkout;
import com.sslcommerz.library.payment.model.datafield.MandatoryFieldModel;
import com.sslcommerz.library.payment.model.dataset.TransactionInfo;
import com.sslcommerz.library.payment.model.util.CurrencyType;
import com.sslcommerz.library.payment.model.util.ErrorKeys;
import com.sslcommerz.library.payment.model.util.SdkCategory;
import com.sslcommerz.library.payment.model.util.SdkType;
import com.sslcommerz.library.payment.viewmodel.listener.OnPaymentResultListener;
import com.sslcommerz.library.payment.viewmodel.management.PayUsingSSLCommerz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.activity.MidtransActivity;
import com.exampe.storeandmore.activity.PayPalWebActivity;
import com.exampe.storeandmore.activity.PayStackActivity;
import com.exampe.storeandmore.activity.StripeActivity;
import com.exampe.storeandmore.adapter.WalletTransactionAdapter;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.PaymentModelClass;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Address;
import com.exampe.storeandmore.model.WalletTransaction;

@SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
public class WalletTransactionFragment extends Fragment implements PaytmPaymentTransactionCallback {
    public static String amount, msg;
    public static boolean payFromWallet = false;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvBalance;
    View root;
    public static RecyclerView recyclerView;
    public static ArrayList<WalletTransaction> walletTransactions;
    SwipeRefreshLayout swipeLayout;
    NestedScrollView scrollView;
    public static RelativeLayout lytAlert;
    public static WalletTransactionAdapter walletTransactionAdapter;
    int total = 0;
    Activity activity;
    int offset = 0;
    TextView tvAlertTitle, tvAlertSubTitle;
    Button btnRechargeWallet;
    Session session;
    boolean isLoadMore = false;
    String paymentMethod = null;
    String customerId;
    private ShimmerFrameLayout mShimmerViewContainer;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_wallet_transection, container, false);

        activity = getActivity();
        session = new Session(activity);

        scrollView = root.findViewById(R.id.scrollView);
        recyclerView = root.findViewById(R.id.recyclerView);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        lytAlert = root.findViewById(R.id.lytAlert);
        tvAlertTitle = root.findViewById(R.id.tvAlertTitle);
        tvAlertSubTitle = root.findViewById(R.id.tvAlertSubTitle);
        tvBalance = root.findViewById(R.id.tvBalance);
        btnRechargeWallet = root.findViewById(R.id.btnRechargeWallet);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);

        tvAlertTitle.setText(getString(R.string.no_wallet_history_found));
        tvAlertSubTitle.setText(getString(R.string.you_have_not_any_wallet_history_yet));

        setHasOptionsMenu(true);

        getTransactionData(activity, session);

        swipeLayout.setColorSchemeResources(R.color.colorPrimary);

        swipeLayout.setOnRefreshListener(() -> {
            swipeLayout.setRefreshing(false);
            offset = 0;
            getTransactionData(activity, session);
        });

        ApiConfig.getWalletBalance(activity, session);

        tvBalance.setText(session.getData(Constant.currency) + Constant.WALLET_BALANCE);

        btnRechargeWallet.setOnClickListener(v -> {

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            LayoutInflater inflater1 = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final View dialogView = inflater1.inflate(R.layout.dialog_wallet_recharge, null);
            alertDialog.setView(dialogView);
            alertDialog.setCancelable(true);
            final AlertDialog dialog = alertDialog.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            TextView tvDialogSend, tvDialogCancel, edtAmount, edtMsg;
            LinearLayout lytPayOption;
            RadioGroup lytPayment;
            RadioButton rbPayU, rbPayPal, rbRazorPay, rbPayStack, rbFlutterWave, rbMidTrans, rbStripe, rbPayTm;

            edtAmount = dialogView.findViewById(R.id.edtAmount);
            edtMsg = dialogView.findViewById(R.id.edtMsg);
            tvDialogCancel = dialogView.findViewById(R.id.tvDialogCancel);
            tvDialogSend = dialogView.findViewById(R.id.tvDialogRecharge);
            lytPayOption = dialogView.findViewById(R.id.lytPayOption);

            rbPayStack = dialogView.findViewById(R.id.rbPayStack);
            rbFlutterWave = dialogView.findViewById(R.id.rbFlutterWave);
            rbPayPal = dialogView.findViewById(R.id.rbPayPal);
            rbRazorPay = dialogView.findViewById(R.id.rbRazorPay);
            rbMidTrans = dialogView.findViewById(R.id.rbMidTrans);
            rbStripe = dialogView.findViewById(R.id.rbStripe);
            rbPayTm = dialogView.findViewById(R.id.rbPayTm);
            rbPayU = dialogView.findViewById(R.id.rbPayU);
            lytPayment = dialogView.findViewById(R.id.lytPayment);

            lytPayment.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton rb = dialogView.findViewById(checkedId);
                paymentMethod = rb.getTag().toString();
            });

            Map<String, String> params = new HashMap<>();
            params.put(Constant.SETTINGS, Constant.GetVal);
            params.put(Constant.GET_PAYMENT_METHOD, Constant.GetVal);
            //  System.out.println("=====params " + params.toString());
            ApiConfig.RequestToVolley((result, response) -> {

                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            if (jsonObject.has("payment_methods")) {
                                JSONObject object = jsonObject.getJSONObject(Constant.PAYMENT_METHODS);
                                if (object.has(Constant.payu_method)) {
                                    Constant.PAYUMONEY = object.getString(Constant.payu_method);
                                    Constant.MERCHANT_KEY = object.getString(Constant.PAY_M_KEY);
                                    Constant.MERCHANT_ID = object.getString(Constant.PAYU_M_ID);
                                    Constant.MERCHANT_SALT = object.getString(Constant.PAYU_SALT);
                                    ApiConfig.SetAppEnvironment(activity);
                                }
                                if (object.has(Constant.razor_pay_method)) {
                                    Constant.RAZORPAY = object.getString(Constant.razor_pay_method);
                                    Constant.RAZOR_PAY_KEY_VALUE = object.getString(Constant.RAZOR_PAY_KEY);
                                }
                                if (object.has(Constant.paypal_method)) {
                                    Constant.PAYPAL = object.getString(Constant.paypal_method);
                                }
                                if (object.has(Constant.paystack_method)) {
                                    Constant.PAY_STACK = object.getString(Constant.paystack_method);
                                    Constant.PAY_STACK_KEY = object.getString(Constant.pay_stack_public_key);
                                }
                                if (object.has(Constant.flutter_wave_payment_method)) {
                                    Constant.FLUTTER_WAVE = object.getString(Constant.flutter_wave_payment_method);
                                    Constant.FLUTTER_WAVE_ENCRYPTION_KEY_VAL = object.getString(Constant.flutter_wave_encryption_key);
                                    Constant.FLUTTER_WAVE_PUBLIC_KEY_VAL = object.getString(Constant.flutter_wave_public_key);
                                    Constant.FLUTTER_WAVE_SECRET_KEY_VAL = object.getString(Constant.flutter_wave_secret_key);
                                    Constant.FLUTTER_WAVE_SECRET_KEY_VAL = object.getString(Constant.flutter_wave_secret_key);
                                    Constant.FLUTTER_WAVE_CURRENCY_CODE_VAL = object.getString(Constant.flutter_wave_currency_code);
                                }
                                if (object.has(Constant.midtrans_payment_method)) {
                                    Constant.MIDTRANS = object.getString(Constant.midtrans_payment_method);
                                }
                                if (object.has(Constant.stripe_payment_method)) {
                                    Constant.STRIPE = object.getString(Constant.stripe_payment_method);
                                    isAddressAvailable();
                                }
                                if (object.has(Constant.paytm_payment_method)) {
                                    Constant.PAYTM = object.getString(Constant.paytm_payment_method);
                                    Constant.PAYTM_MERCHANT_ID = object.getString(Constant.paytm_merchant_id);
                                    Constant.PAYTM_MERCHANT_KEY = object.getString(Constant.paytm_merchant_key);
                                    Constant.PAYTM_MODE = object.getString(Constant.paytm_mode);
                                }
                                if (object.has(Constant.ssl_method)) {
                                    Constant.SSLECOMMERZ = object.getString(Constant.ssl_method);
                                    Constant.SSLECOMMERZ_MODE = object.getString(Constant.ssl_mode);
                                    Constant.SSLECOMMERZ_STORE_ID = object.getString(Constant.ssl_store_id);
                                    Constant.SSLECOMMERZ_SECRET_KEY = object.getString(Constant.ssl_store_password);
                                }

                                if (Constant.FLUTTER_WAVE.equals("0") && Constant.PAYPAL.equals("0") && Constant.PAYUMONEY.equals("0") && Constant.COD.equals("0") && Constant.RAZORPAY.equals("0") && Constant.PAY_STACK.equals("0") && Constant.MIDTRANS.equals("0") && Constant.STRIPE.equals("0") && Constant.PAYTM.equals("0") && Constant.SSLECOMMERZ.equals("0")) {
                                    lytPayOption.setVisibility(View.GONE);
                                } else {
                                    lytPayOption.setVisibility(View.VISIBLE);

                                    if (Constant.PAYUMONEY.equals("1")) {
                                        rbPayU.setVisibility(View.VISIBLE);
                                    }
                                    if (Constant.RAZORPAY.equals("1")) {
                                        rbRazorPay.setVisibility(View.VISIBLE);
                                    }
                                    if (Constant.PAY_STACK.equals("1")) {
                                        rbPayStack.setVisibility(View.VISIBLE);
                                    }
                                    if (Constant.FLUTTER_WAVE.equals("1")) {
                                        rbFlutterWave.setVisibility(View.VISIBLE);
                                    }
                                    if (Constant.PAYPAL.equals("1")) {
                                        rbPayPal.setVisibility(View.VISIBLE);
                                    }
                                    if (Constant.MIDTRANS.equals("1")) {
                                        rbMidTrans.setVisibility(View.VISIBLE);
                                    }
                                    if (Constant.STRIPE.equals("1")) {
                                        rbStripe.setVisibility(View.VISIBLE);
                                    }
                                    if (Constant.PAYTM.equals("1")) {
                                        rbPayTm.setVisibility(View.VISIBLE);
                                    }
                                }
                            } else {
                                Toast.makeText(activity, getString(R.string.alert_payment_methods_blank), Toast.LENGTH_SHORT).show();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, activity, Constant.SETTING_URL, params, false);

            tvDialogSend.setOnClickListener(v1 -> {
                if (edtAmount.getText().toString().equals("")) {
                    edtAmount.requestFocus();
                    edtAmount.setError(getString(R.string.alert_enter_amount));
                } else if (Double.parseDouble(edtAmount.getText().toString()) > Double.parseDouble(session.getData(Constant.user_wallet_refill_limit))) {
                    Toast.makeText(activity, getString(R.string.max_wallet_amt_error), Toast.LENGTH_SHORT).show();
                } else if (Double.parseDouble(edtAmount.getText().toString().trim()) <= 0) {
                    edtAmount.requestFocus();
                    edtAmount.setError(getString(R.string.alert_recharge));
                } else {
                    if (paymentMethod != null) {
                        amount = edtAmount.getText().toString().trim();
                        msg = edtMsg.getText().toString().trim();
                        RechargeWallet();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(activity, getString(R.string.select_payment_method), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            tvDialogCancel.setOnClickListener(v12 -> dialog.dismiss());

            dialog.show();
        });

        return root;
    }


    public void isAddressAvailable() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ADDRESSES, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
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
                                Address address = g.fromJson(jsonObject1.toString(), Address.class);
                                if (address.getIs_default().equals("1")) {
                                    Constant.DefaultAddress = address.getAddress() + ", " + address.getLandmark() + ", " + address.getCity_name() + ", " + address.getArea_name() + ", " + address.getState() + ", " + address.getCountry() + ", " + activity.getString(R.string.pincode_) + address.getPincode();
                                    Constant.DefaultCity = address.getCity_name();
                                    Constant.DefaultPinCode = address.getPincode();
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GET_ADDRESS_URL, params, false);
    }

    @SuppressLint("SetTextI18n")
    public void AddWalletBalance(Activity activity, Session session, String amount, String msg) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.ADD_WALLET_BALANCE, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.AMOUNT, amount);
        params.put(Constant.TYPE, Constant.CREDIT);
        params.put(Constant.MESSAGE, msg);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        WalletTransaction walletTransaction = new Gson().fromJson(object.getJSONObject(Constant.DATA).toString(), WalletTransaction.class);

                        if (walletTransactions == null)
                            walletTransactions = new ArrayList<>();
                        if (walletTransactions.size() == 0) {
                            lytAlert.setVisibility(View.GONE);
                        }
                        walletTransactions.add(walletTransaction);
                        if (walletTransactionAdapter == null) {
                            walletTransactionAdapter = new WalletTransactionAdapter(activity, walletTransactions);
                            recyclerView.setAdapter(walletTransactionAdapter);
                        }
                        walletTransactionAdapter.notifyDataSetChanged();
                        tvBalance.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + Double.parseDouble(object.getString(Constant.NEW_BALANCE))));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.TRANSACTION_URL, params, false);
    }


    public void StartPayPalPayment(final Map<String, String> sendParams) {
        final Map<String, String> params = new HashMap<>();
        params.put(Constant.FIRST_NAME, session.getData(Constant.NAME));
        params.put(Constant.LAST_NAME, session.getData(Constant.NAME));
        params.put(Constant.PAYER_EMAIL, session.getData(Constant.EMAIL));
        params.put(Constant.ITEM_NAME, getString(R.string.wallet_recharge));
        params.put(Constant.ITEM_NUMBER, "wallet-refill-user-" + new Session(activity).getData(Constant.ID) + "-" + System.currentTimeMillis());
        params.put(Constant.AMOUNT, sendParams.get(Constant.FINAL_TOTAL));
        ApiConfig.RequestToVolley((result, response) -> {
            Intent intent = new Intent(activity, PayPalWebActivity.class);
            intent.putExtra(Constant.URL, response);
            intent.putExtra(Constant.ORDER_ID, "wallet-refill-user-" + new Session(activity).getData(Constant.ID) + "-" + System.currentTimeMillis());
            intent.putExtra(Constant.FROM, Constant.WALLET);
            intent.putExtra(Constant.PARAMS, (Serializable) sendParams);
            startActivity(intent);
        }, getActivity(), Constant.PAPAL_URL, params, true);
    }

    public void callPayStack(final Map<String, String> sendParams) {
        Intent intent = new Intent(activity, PayStackActivity.class);
        intent.putExtra(Constant.PARAMS, (Serializable) sendParams);
        startActivity(intent);
    }


    void StartFlutterWavePayment() {
        new RavePayManager(this)
                .setAmount(Double.parseDouble(amount))
                .setEmail(session.getData(Constant.EMAIL))
                .setCurrency(Constant.FLUTTER_WAVE_CURRENCY_CODE_VAL)
                .setfName(session.getData(Constant.FIRST_NAME))
                .setlName(session.getData(Constant.LAST_NAME))
                .setNarration(getString(R.string.app_name) + getString(R.string.shopping))
                .setPublicKey(Constant.FLUTTER_WAVE_PUBLIC_KEY_VAL)
                .setEncryptionKey(Constant.FLUTTER_WAVE_ENCRYPTION_KEY_VAL)
                .setTxRef(System.currentTimeMillis() + "Ref")
                .acceptAccountPayments(true)
                .acceptCardPayments(true)
                .acceptAccountPayments(true)
                .acceptAchPayments(true)
                .acceptBankTransferPayments(true)
                .acceptBarterPayments(true)
                .acceptGHMobileMoneyPayments(true)
                .acceptRwfMobileMoneyPayments(true)
                .acceptSaBankPayments(true)
                .acceptFrancMobileMoneyPayments(true)
                .acceptZmMobileMoneyPayments(true)
                .acceptUssdPayments(true)
                .acceptUkPayments(true)
                .acceptMpesaPayments(true)
                .shouldDisplayFee(true)
                .onStagingEnv(false)
                .showStagingLabel(false)
                .initialize();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RaveConstants.RAVE_REQUEST_CODE && data != null) {
            new PaymentModelClass(getActivity()).TransactionMethod(data, getActivity(), Constant.WALLET);
        } else if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                AddWalletBalance(activity, new Session(activity), amount, msg);
                Toast.makeText(activity, getString(R.string.wallet_recharged), Toast.LENGTH_LONG).show();

            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(activity, getString(R.string.order_error), Toast.LENGTH_LONG).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(activity, getString(R.string.order_cancel), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void RechargeWallet() {
        HashMap<String, String> sendParams = new HashMap<>();
        if (paymentMethod.equals(getString(R.string.pay_u))) {
            sendParams.put(Constant.MOBILE, session.getData(Constant.MOBILE));
            sendParams.put(Constant.USER_NAME, session.getData(Constant.NAME));
            sendParams.put(Constant.EMAIL, session.getData(Constant.EMAIL));
            new PaymentModelClass(getActivity()).OnPayClick(getActivity(), sendParams, Constant.WALLET, amount);
        } else if (paymentMethod.equals(getString(R.string.paypal))) {
            sendParams.put(Constant.FINAL_TOTAL, amount);
            sendParams.put(Constant.FIRST_NAME, session.getData(Constant.NAME));
            sendParams.put(Constant.LAST_NAME, session.getData(Constant.NAME));
            sendParams.put(Constant.PAYER_EMAIL, session.getData(Constant.EMAIL));
            sendParams.put(Constant.ITEM_NAME, getString(R.string.wallet_recharge_));
            sendParams.put(Constant.ITEM_NUMBER, System.currentTimeMillis() + Constant.randomNumeric(3));
            StartPayPalPayment(sendParams);
        } else if (paymentMethod.equals(getString(R.string.razor_pay))) {
            payFromWallet = true;
            CreateOrderId(Double.parseDouble(amount));
        } else if (paymentMethod.equals(getString(R.string.paystack))) {
            sendParams.put(Constant.FINAL_TOTAL, amount);
            sendParams.put(Constant.FROM, Constant.WALLET);
            callPayStack(sendParams);
        } else if (paymentMethod.equals(getString(R.string.flutterwave))) {
            StartFlutterWavePayment();
        } else if (paymentMethod.equals(getString(R.string.midtrans))) {
            sendParams.put(Constant.FINAL_TOTAL, amount);
            sendParams.put(Constant.USER_ID, session.getData(Constant.ID));
            CreateMidtransPayment(amount, sendParams);
        } else if (paymentMethod.equals(getString(R.string.stripe))) {
            if (!Constant.DefaultAddress.equals("")) {
                sendParams.put(Constant.FINAL_TOTAL, amount);
                sendParams.put(Constant.USER_ID, session.getData(Constant.ID));
                Intent intent = new Intent(activity, StripeActivity.class);
                intent.putExtra(Constant.ORDER_ID, "wallet-refill-user-" + new Session(activity).getData(Constant.ID) + "-" + System.currentTimeMillis());
                intent.putExtra(Constant.FROM, Constant.WALLET);
                intent.putExtra(Constant.PARAMS, sendParams);
                startActivity(intent);
            } else {
                Toast.makeText(activity, getString(R.string.address_msg), Toast.LENGTH_SHORT).show();
            }

        } else if (paymentMethod.equals(getString(R.string.paytm))) {
            startPayTmPayment();
        } else if (paymentMethod.equals(getString(R.string.sslecommerz))) {
            startSslCommerzPayment(amount, msg, System.currentTimeMillis() + Constant.randomNumeric(3));
        }
    }


    public void startSslCommerzPayment(String amount, String msg, String txnId) {
        String mode;
        if (Constant.SSLECOMMERZ_MODE.equals("sandbox")) {
            mode = SdkType.TESTBOX;
        } else {
            mode = SdkType.LIVE;
        }

        MandatoryFieldModel mandatoryFieldModel = new MandatoryFieldModel(Constant.SSLECOMMERZ_STORE_ID, Constant.SSLECOMMERZ_SECRET_KEY, amount, txnId, CurrencyType.BDT, mode, SdkCategory.BANK_LIST);

        /* Call for the payment */
        PayUsingSSLCommerz.getInstance().setData(activity, mandatoryFieldModel, new OnPaymentResultListener() {
            @Override
            public void transactionSuccess(TransactionInfo transactionInfo) {
                // If payment is success and risk label is 0.
                AddWalletBalance(activity, new Session(activity), amount, msg);
            }

            @Override
            public void transactionFail(String sessionKey) {
                Toast.makeText(activity, "transactionFail -> Session : " + sessionKey, Toast.LENGTH_LONG).show();
            }

            @Override
            public void error(int errorCode) {
                switch (errorCode) {
                    case ErrorKeys.USER_INPUT_ERROR:
                        Toast.makeText(activity, "User Input Error", Toast.LENGTH_LONG).show();
                        break;
                    case ErrorKeys.INTERNET_CONNECTION_ERROR:
                        Toast.makeText(activity, "Internet Connection Error", Toast.LENGTH_LONG).show();
                        break;
                    case ErrorKeys.DATA_PARSING_ERROR:
                        Toast.makeText(activity, "Data Parsing Error", Toast.LENGTH_LONG).show();
                        break;
                    case ErrorKeys.CANCEL_TRANSACTION_ERROR:
                        Toast.makeText(activity, "User Cancel The Transaction", Toast.LENGTH_LONG).show();
                        break;
                    case ErrorKeys.SERVER_ERROR:
                        Toast.makeText(activity, "Server Error", Toast.LENGTH_LONG).show();
                        break;
                    case ErrorKeys.NETWORK_ERROR:
                        Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });

    }

    public void startPayTmPayment() {
        Map<String, String> params = new HashMap<>();

        params.put(Constant.ORDER_ID_, Constant.randomAlphaNumeric(20));
        params.put(Constant.CUST_ID, Constant.randomAlphaNumeric(10));
        params.put(Constant.TXN_AMOUNT, "" + amount);

        if (Constant.PAYTM_MODE.equals("sandbox")) {
            params.put(Constant.INDUSTRY_TYPE_ID, Constant.INDUSTRY_TYPE_ID_DEMO_VAL);
            params.put(Constant.CHANNEL_ID, Constant.MOBILE_APP_CHANNEL_ID_DEMO_VAL);
            params.put(Constant.WEBSITE, Constant.WEBSITE_DEMO_VAL);
        } else if (Constant.PAYTM_MODE.equals("production")) {
            params.put(Constant.INDUSTRY_TYPE_ID, Constant.INDUSTRY_TYPE_ID_LIVE_VAL);
            params.put(Constant.CHANNEL_ID, Constant.MOBILE_APP_CHANNEL_ID_LIVE_VAL);
            params.put(Constant.WEBSITE, Constant.WEBSITE_LIVE_VAL);
        }
//        System.out.println("====" + params.toString());

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject object = jsonObject.getJSONObject(Constant.DATA);
                    PaytmPGService Service = null;

                    if (Constant.PAYTM_MODE.equals("sandbox")) {
                        Service = PaytmPGService.getStagingService(Constant.PAYTM_ORDER_PROCESS_DEMO_VAL);
                    } else if (Constant.PAYTM_MODE.equals("production")) {
                        Service = PaytmPGService.getProductionService();
                    }

                    customerId = object.getString(Constant.CUST_ID);
                    //creating a hashmap and adding all the values required

                    HashMap<String, String> paramMap = new HashMap<>();
                    paramMap.put(Constant.MID, Constant.PAYTM_MERCHANT_ID);
                    paramMap.put(Constant.ORDER_ID_, jsonObject.getString("order id"));
                    paramMap.put(Constant.CUST_ID, object.getString(Constant.CUST_ID));
                    paramMap.put(Constant.TXN_AMOUNT, "" + amount);

                    if (Constant.PAYTM_MODE.equals("sandbox")) {
                        paramMap.put(Constant.INDUSTRY_TYPE_ID, Constant.INDUSTRY_TYPE_ID_LIVE_VAL);
                        paramMap.put(Constant.CHANNEL_ID, Constant.MOBILE_APP_CHANNEL_ID_DEMO_VAL);
                        paramMap.put(Constant.WEBSITE, Constant.WEBSITE_DEMO_VAL);
                    } else if (Constant.PAYTM_MODE.equals("production")) {
                        paramMap.put(Constant.INDUSTRY_TYPE_ID, Constant.INDUSTRY_TYPE_ID_DEMO_VAL);
                        paramMap.put(Constant.CHANNEL_ID, Constant.MOBILE_APP_CHANNEL_ID_LIVE_VAL);
                        paramMap.put(Constant.WEBSITE, Constant.WEBSITE_LIVE_VAL);
                    }

                    paramMap.put(Constant.CALLBACK_URL, object.getString(Constant.CALLBACK_URL));
                    paramMap.put(Constant.CHECK_SUM_HASH, jsonObject.getString("signature"));

                    //creating a paytm order object using the hashmap
                    PaytmOrder order = new PaytmOrder(paramMap);

                    //initializing the paytm service
                    assert Service != null;
                    Service.initialize(order, null);

                    //finally starting the payment transaction
                    Service.startPaymentTransaction(getActivity(), true, true, this);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GENERATE_PAYTM_CHECKSUM, params, false);


    }

    @Override
    public void onTransactionResponse(Bundle bundle) {
        String orderId = bundle.getString(Constant.ORDERID);

        String status = bundle.getString(Constant.STATUS_);
        if (status.equalsIgnoreCase(Constant.TXN_SUCCESS)) {
            verifyTransaction(orderId);
        }
    }


    /**
     * Verifying the transaction status once PayTM transaction is over
     * This makes server(own) -> server(PayTM) call to verify the transaction status
     */
    public void verifyTransaction(String orderId) {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getJSONObject("body").getJSONObject("resultInfo").getString("resultStatus");
                    if (status.equalsIgnoreCase("TXN_SUCCESS")) {
                        AddWalletBalance(activity, new Session(activity), amount, msg);
                        Toast.makeText(activity, getString(R.string.wallet_recharged), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.VALID_TRANSACTION, params, false);
    }

    @Override
    public void networkNotAvailable() {

    }

    @Override
    public void clientAuthenticationFailed(String inErrorMessage) {

    }

    @Override
    public void someUIErrorOccurred(String inErrorMessage) {

    }

    @Override
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String
            inFailingUrl) {

    }

    @Override
    public void onBackPressedCancelTransaction() {

    }

    @Override
    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {

    }

    public void CreateOrderId(double payAmount) {

        String[] amount = String.valueOf(payAmount * 100).split("\\.");

        Map<String, String> params = new HashMap<>();
        params.put("amount", amount[0]);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        startPayment(object.getString("id"), object.getString("amount"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, getActivity(), Constant.Get_RazorPay_OrderId, params, true);

    }

    public void startPayment(String orderId, String payAmount) {
        Checkout checkout = new Checkout();
        checkout.setKeyID(Constant.RAZOR_PAY_KEY_VALUE);
        checkout.setImage(R.mipmap.ic_launcher);

        try {
            JSONObject options = new JSONObject();
            options.put(Constant.NAME, session.getData(Constant.NAME));
            options.put(Constant.ORDER_ID, orderId);
            options.put(Constant.CURRENCY, "INR");
            options.put(Constant.AMOUNT, payAmount);

            JSONObject preFill = new JSONObject();
            preFill.put(Constant.EMAIL, session.getData(Constant.EMAIL));
            preFill.put(Constant.CONTACT, session.getData(Constant.MOBILE));
            options.put("prefill", preFill);

            checkout.open(activity, options);
        } catch (Exception e) {
            Log.d("Payment : ", "Error in starting Razorpay Checkout", e);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void getTransactionData(Activity activity, Session session) {
        walletTransactions = new ArrayList<>();
        if(walletTransactionAdapter!=null) {
            recyclerView.setAdapter(new WalletTransactionAdapter(activity, walletTransactions));
            walletTransactionAdapter.notifyDataSetChanged();
        }
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_TRANSACTION, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.TYPE, Constant.TYPE_WALLET_TRANSACTION);
        params.put(Constant.OFFSET, "" + offset);
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        lytAlert.setVisibility(View.GONE);
                        total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                        session.setData(Constant.TOTAL, String.valueOf(total));

                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                        Gson g = new Gson();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            if (jsonObject1 != null) {
                                WalletTransaction transaction = g.fromJson(jsonObject1.toString(), WalletTransaction.class);
                                walletTransactions.add(transaction);
                            } else {
                                break;
                            }
                        }
                        if (offset == 0) {
                            walletTransactionAdapter = new WalletTransactionAdapter(activity, walletTransactions);
                            recyclerView.setAdapter(walletTransactionAdapter);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                                    if (walletTransactions.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == walletTransactions.size() - 1) {
                                                //bottom of list!
                                                walletTransactions.add(null);
                                                walletTransactionAdapter.notifyItemInserted(walletTransactions.size() - 1);
                                                offset += Constant.LOAD_ITEM_LIMIT;
                                                Map<String, String> params1 = new HashMap<>();
                                                params1.put(Constant.GET_USER_TRANSACTION, Constant.GetVal);
                                                params1.put(Constant.USER_ID, session.getData(Constant.ID));
                                                params1.put(Constant.TYPE, Constant.TYPE_WALLET_TRANSACTION);
                                                params1.put(Constant.OFFSET, "" + offset);
                                                params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

                                                ApiConfig.RequestToVolley((result1, response1) -> {

                                                    if (result1) {
                                                        try {
                                                            // System.out.println("====product  " + response);
                                                            JSONObject jsonObject12 = new JSONObject(response1);
                                                            if (!jsonObject12.getBoolean(Constant.ERROR)) {

                                                                session.setData(Constant.TOTAL, jsonObject12.getString(Constant.TOTAL));

                                                                walletTransactions.remove(walletTransactions.size() - 1);
                                                                walletTransactionAdapter.notifyItemRemoved(walletTransactions.size());

                                                                JSONObject object1 = new JSONObject(response1);
                                                                JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);

                                                                Gson g1 = new Gson();


                                                                for (int i = 0; i < jsonArray1.length(); i++) {
                                                                    JSONObject jsonObject1 = jsonArray1.getJSONObject(i);

                                                                    if (jsonObject1 != null) {
                                                                        WalletTransaction walletTransaction = g1.fromJson(jsonObject1.toString(), WalletTransaction.class);
                                                                        walletTransactions.add(walletTransaction);
                                                                    } else {
                                                                        break;
                                                                    }
                                                                }
                                                                walletTransactionAdapter.notifyDataSetChanged();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            mShimmerViewContainer.stopShimmer();
                                                            mShimmerViewContainer.setVisibility(View.GONE);
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
                        lytAlert.setVisibility(View.VISIBLE);
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);

                    }
                } catch (JSONException e) {
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                }

            }
        }, activity, Constant.TRANSACTION_URL, params, false);

    }


    public void CreateMidtransPayment(String grossAmount, Map<String, String> sendParams) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.ORDER_ID, "wallet-refill-user-" + new Session(activity).getData(Constant.ID) + "-" + System.currentTimeMillis());
        params.put(Constant.GROSS_AMOUNT, "" + (int) Math.round(Double.parseDouble(grossAmount)));
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        Intent intent = new Intent(activity, MidtransActivity.class);
                        intent.putExtra(Constant.URL, jsonObject.getJSONObject(Constant.DATA).getString(Constant.REDIRECT_URL));
                        intent.putExtra(Constant.ORDER_ID, "wallet-refill-user-" + new Session(activity).getData(Constant.ID) + "-" + System.currentTimeMillis());
                        intent.putExtra(Constant.FROM, Constant.WALLET);
                        intent.putExtra(Constant.PARAMS, (Serializable) sendParams);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.MIDTRANS_PAYMENT_URL, params, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.wallet_history);
        Session.setCount(Constant.UNREAD_WALLET_COUNT, 0, activity);
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