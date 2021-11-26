package com.exampe.storeandmore.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;
import com.exampe.storeandmore.R;
import com.exampe.storeandmore.fragment.WalletTransactionFragment;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.PaymentModelClass;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.ui.CreditCardEditText;


@SuppressWarnings("unchecked")
public class PayStackActivity extends AppCompatActivity {
    public String email, cardNumber, cvv;
    public int expiryMonth, expiryYear;
    Toolbar toolbar;
    Session session;
    Activity activity;
    TextView tvPayable;
    Map<String, String> sendParams;
    PaymentModelClass paymentModelClass;
    double payableAmount = 0;
    String from;
    //variables
    private Card card;
    private Charge charge;
    private EditText emailField;
    private CreditCardEditText cardNumberField;
    private EditText expiryMonthField;
    private EditText expiryYearField;
    private EditText cvvField;

    TextView toolbarTitle;
    ImageView imageMenu;

    public static void setPaystackKey(String publicKey) {
        PaystackSdk.setPublicKey(publicKey);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init payStack sdk
        PaystackSdk.initialize(getApplicationContext());
        setContentView(R.layout.activity_pay_stack);
        getAllWidgets();
        setPaystackKey(Constant.PAY_STACK_KEY);
        activity = PayStackActivity.this;
        session = new Session(activity);

        paymentModelClass = new PaymentModelClass(activity);
        sendParams = (Map<String, String>) getIntent().getSerializableExtra("params");
        payableAmount = Double.parseDouble(Objects.requireNonNull(sendParams.get(Constant.FINAL_TOTAL)));
        from = sendParams.get(Constant.FROM);


        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        toolbarTitle.setText(getString(R.string.paystack));

        imageMenu.setImageResource(R.drawable.ic_arrow_back);
        imageMenu.setOnClickListener(view -> onBackPressed());

        emailField.setText(session.getData(Constant.EMAIL));
        tvPayable.setText(session.getData(Constant.currency) + payableAmount);

        findViewById(R.id.btnPay).setOnClickListener(v -> {
            if (!validateForm()) {
                return;
            }
            //noinspection CommentedOutCode
            try {
                email = emailField.getText().toString().trim();
                cardNumber = Objects.requireNonNull(cardNumberField.getText()).toString().trim();
                expiryMonth = Integer.parseInt(expiryMonthField.getText().toString().trim());
                expiryYear = Integer.parseInt(expiryYearField.getText().toString().trim());
                cvv = cvvField.getText().toString().trim();

            /*String cardNumber = "4084 0840 8408 4081";
            int expiryMonth = 11; //any month in the future
            int expiryYear = 18; // any year in the future
            String cvv = "408";*/
                card = new Card(cardNumber, expiryMonth, expiryYear, cvv);

                paymentModelClass.showProgressDialog();
                if (card.isValid()) {
                    performCharge();
                } else {
                    paymentModelClass.hideProgressDialog();
                    Toast.makeText(PayStackActivity.this, "Card is not Valid", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void getAllWidgets() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tvPayable = findViewById(R.id.tvPayable);
        emailField = findViewById(R.id.edit_email_address);
        cardNumberField = findViewById(R.id.edit_card_number);
        expiryMonthField = findViewById(R.id.edit_expiry_month);
        expiryYearField = findViewById(R.id.edit_expiry_year);
        cvvField = findViewById(R.id.edit_cvv);

        imageMenu = findViewById(R.id.imageMenu);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        imageMenu = findViewById(R.id.imageMenu);
    }

    /**
     * Method to perform the charging of the card
     */
    private void performCharge() {
        //create a  Charge object
        String[] amount = String.valueOf(payableAmount * 100).split("\\.");
        charge = new Charge();
        charge.setCard(card); //set the card to charge
        charge.setEmail(email); //dummy email address
        charge.setAmount(Integer.parseInt(amount[0])); //test amount
        PaystackSdk.chargeCard(PayStackActivity.this, charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                String paymentReference = transaction.getReference();
                verifyReference(String.valueOf(charge.getAmount()), paymentReference, charge.getEmail());
            }

            @Override
            public void beforeValidate(Transaction transaction) {
                System.out.println(">>>>>> transaction : "+transaction);
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                System.out.println(">>>>>> error : "+error.getMessage());
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = emailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Required.");
            valid = false;
        } else {
            emailField.setError(null);
        }

        String cardNumber = Objects.requireNonNull(cardNumberField.getText()).toString();
        if (TextUtils.isEmpty(cardNumber)) {
            cardNumberField.setError("Required.");
            valid = false;
        } else {
            cardNumberField.setError(null);
        }


        String expiryMonth = expiryMonthField.getText().toString();
        if (TextUtils.isEmpty(expiryMonth)) {
            expiryMonthField.setError("Required.");
            valid = false;
        } else {
            expiryMonthField.setError(null);
        }

        String expiryYear = expiryYearField.getText().toString();
        if (TextUtils.isEmpty(expiryYear)) {
            expiryYearField.setError("Required.");
            valid = false;
        } else {
            expiryYearField.setError(null);
        }

        String cvv = cvvField.getText().toString();
        if (TextUtils.isEmpty(cvv)) {
            cvvField.setError("Required.");
            valid = false;
        } else {
            cvvField.setError(null);
        }

        return valid;
    }


    public void verifyReference(String amount, String reference, String email) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.VERIFY_PAY_STACK, Constant.GetVal);
        params.put(Constant.AMOUNT, amount);
        params.put(Constant.REFERENCE, reference);
        params.put(Constant.EMAIL, email);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {

                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString(Constant.STATUS);
                    if (from.equals(Constant.WALLET)) {
                        onBackPressed();
                        new WalletTransactionFragment().AddWalletBalance(activity, new Session(activity), WalletTransactionFragment.amount, WalletTransactionFragment.msg);
                    } else if (from.equals(Constant.PAYMENT)) {
                        paymentModelClass.PlaceOrder(activity, getString(R.string.paystack), reference, status.equalsIgnoreCase("success"), sendParams, status);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.VERIFY_PAYMENT_REQUEST, params, false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}