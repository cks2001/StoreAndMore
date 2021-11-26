package com.exampe.storeandmore.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import com.exampe.storeandmore.R;
import com.exampe.storeandmore.activity.LoginActivity;
import com.exampe.storeandmore.activity.MainActivity;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.helper.Utils;

public class DrawerFragment extends Fragment {
    View root;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvName;
    TextView tvWallet;
    public TextView tvMobile, tvMenuHome, tvMenuCart, tvMenuNotification, tvMenuOrders, tvMenuWalletHistory, tvMenuTransactionHistory, tvMenuChangePassword, tvMenuManageAddresses, tvMenuReferEarn, tvMenuContactUs, tvMenuAboutUs, tvMenuRateUs, tvMenuShareApp, tvMenuFAQ, tvMenuTermsConditions, tvMenuPrivacyPolicy, tvMenuLogout;
    @SuppressLint("StaticFieldLeak")
    public static ImageView imgProfile;
    public ImageView imgEditProfile;
    Session session;
    Activity activity;
    LinearLayout lytMenuGroup, lytProfile;
    Fragment fragment;
    Bundle bundle;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_drawer, container, false);
        activity = getActivity();
        session = new Session(activity);

        imgProfile = root.findViewById(R.id.imgProfile);
        imgEditProfile = root.findViewById(R.id.imgEditProfile);
        tvName = root.findViewById(R.id.tvName);
        tvWallet = root.findViewById(R.id.tvWallet);
        tvMobile = root.findViewById(R.id.tvMobile);
        tvMenuHome = root.findViewById(R.id.tvMenuHome);
        tvMenuCart = root.findViewById(R.id.tvMenuCart);
        tvMenuNotification = root.findViewById(R.id.tvMenuNotification);
        tvMenuOrders = root.findViewById(R.id.tvMenuOrders);
        tvMenuWalletHistory = root.findViewById(R.id.tvMenuWalletHistory);
        tvMenuTransactionHistory = root.findViewById(R.id.tvMenuTransactionHistory);
        tvMenuChangePassword = root.findViewById(R.id.tvMenuChangePassword);
        tvMenuManageAddresses = root.findViewById(R.id.tvMenuManageAddresses);
        tvMenuReferEarn = root.findViewById(R.id.tvMenuReferEarn);
        tvMenuContactUs = root.findViewById(R.id.tvMenuContactUs);
        tvMenuAboutUs = root.findViewById(R.id.tvMenuAboutUs);
        tvMenuRateUs = root.findViewById(R.id.tvMenuRateUs);
        tvMenuShareApp = root.findViewById(R.id.tvMenuShareApp);
        tvMenuFAQ = root.findViewById(R.id.tvMenuFAQ);
        tvMenuTermsConditions = root.findViewById(R.id.tvMenuTermsConditions);
        tvMenuPrivacyPolicy = root.findViewById(R.id.tvMenuPrivacyPolicy);
        tvMenuLogout = root.findViewById(R.id.tvMenuLogout);
        lytMenuGroup = root.findViewById(R.id.lytMenuGroup);
        lytProfile = root.findViewById(R.id.lytProfile);

        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            tvName.setText(session.getData(Constant.NAME));
            tvMobile.setText(session.getData(Constant.MOBILE));
            tvWallet.setVisibility(View.VISIBLE);
            imgEditProfile.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(session.getData(Constant.PROFILE))
                    .fit()
                    .centerInside()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .transform(new RoundedCornersTransformation(20, 0))
                    .into(imgProfile);

            tvWallet.setText(activity.getResources().getString(R.string.wallet_balance) + "\t:\t" + session.getData(Constant.currency) + Constant.WALLET_BALANCE);

        } else {
            tvWallet.setVisibility(View.GONE);
            imgEditProfile.setVisibility(View.GONE);
            tvName.setText(getResources().getString(R.string.is_login));
            tvMobile.setText(getResources().getString(R.string.is_mobile));
            Picasso.get()
                    .load("-")
                    .fit()
                    .centerInside()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .transform(new RoundedCornersTransformation(20, 0))
                    .into(imgProfile);
        }

        imgEditProfile.setOnClickListener(v -> MainActivity.fm.beginTransaction().add(R.id.container, new ProfileFragment()).addToBackStack(null).commit());

        lytProfile.setOnClickListener(v -> {
            if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                startActivity(new Intent(activity, LoginActivity.class).putExtra(Constant.FROM, "drawer"));
            }
        });

        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            if (session.getData(Constant.is_refer_earn_on).equals("0")) {
                tvMenuReferEarn.setVisibility(View.GONE);
            } else {
                tvMenuReferEarn.setVisibility(View.VISIBLE);
            }
            tvMenuLogout.setVisibility(View.VISIBLE);
            lytMenuGroup.setVisibility(View.VISIBLE);
        } else {
            tvMenuLogout.setVisibility(View.GONE);
            lytMenuGroup.setVisibility(View.GONE);
        }

        tvMenuHome.setOnClickListener(v -> {
            MainActivity.homeClicked = false;
            MainActivity.categoryClicked = false;
            MainActivity.favoriteClicked = false;
            MainActivity.drawerClicked = false;
            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constant.FROM, "");
            startActivity(intent);
        });

        tvMenuCart.setOnClickListener(v -> {
            fragment = new CartFragment();
            bundle = new Bundle();
            bundle.putString(Constant.FROM, "mainActivity");
            fragment.setArguments(bundle);
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });

        tvMenuNotification.setOnClickListener(v -> MainActivity.fm.beginTransaction().add(R.id.container, new NotificationFragment()).addToBackStack(null).commit());

        tvMenuOrders.setOnClickListener(v -> MainActivity.fm.beginTransaction().add(R.id.container, new TrackOrderFragment()).addToBackStack(null).commit());

        tvMenuWalletHistory.setOnClickListener(v -> MainActivity.fm.beginTransaction().add(R.id.container, new WalletTransactionFragment()).addToBackStack(null).commit());

        tvMenuTransactionHistory.setOnClickListener(v -> MainActivity.fm.beginTransaction().add(R.id.container, new TransactionFragment()).addToBackStack(null).commit());

        tvMenuChangePassword.setOnClickListener(v -> OpenBottomDialog(activity));

        tvMenuManageAddresses.setOnClickListener(v -> {
            fragment = new AddressListFragment();
            bundle = new Bundle();
            bundle.putString(Constant.FROM, "MainActivity");
            fragment.setArguments(bundle);
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });

        tvMenuReferEarn.setOnClickListener(v -> {
            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                MainActivity.fm.beginTransaction().add(R.id.container, new ReferEarnFragment()).addToBackStack(null).commit();
            } else {
                startActivity(new Intent(activity, LoginActivity.class));
            }
        });

        tvMenuContactUs.setOnClickListener(v -> {
            fragment = new WebViewFragment();
            bundle = new Bundle();
            bundle.putString("type", "Contact Us");
            fragment.setArguments(bundle);
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });

        tvMenuAboutUs.setOnClickListener(v -> {
            fragment = new WebViewFragment();
            bundle = new Bundle();
            bundle.putString("type", "About Us");
            fragment.setArguments(bundle);
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });

        tvMenuRateUs.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.PLAY_STORE_RATE_US_LINK + activity.getPackageName())));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.PLAY_STORE_LINK + activity.getPackageName())));
            }
        });

        tvMenuShareApp.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.take_a_look) + "\"" + getString(R.string.app_name) + "\" - " + Constant.PLAY_STORE_LINK + activity.getPackageName());
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
        });

        tvMenuFAQ.setOnClickListener(v -> MainActivity.fm.beginTransaction().add(R.id.container, new FaqFragment()).addToBackStack(null).commit());

        tvMenuTermsConditions.setOnClickListener(v -> {
            fragment = new WebViewFragment();
            bundle = new Bundle();
            bundle.putString("type", "Terms & Conditions");
            fragment.setArguments(bundle);
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });

        tvMenuPrivacyPolicy.setOnClickListener(v -> {
            fragment = new WebViewFragment();
            bundle = new Bundle();
            bundle.putString("type", "Privacy Policy");
            fragment.setArguments(bundle);
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });

        tvMenuLogout.setOnClickListener(v -> session.logoutUserConfirmation(activity));

        return root;
    }

    public void OpenBottomDialog(final Activity activity) {
        try {
            @SuppressLint("InflateParams") View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_change_password, null);
            ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }

            final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetTheme);
            mBottomSheetDialog.setContentView(sheetView);
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mBottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            EditText edtOldPassword = sheetView.findViewById(R.id.edtOldPassword);
            EditText edtNewPassword = sheetView.findViewById(R.id.edtNewPassword);
            EditText edtConfirmPassword = sheetView.findViewById(R.id.edtConfirmPassword);
            ImageView imgChangePasswordClose = sheetView.findViewById(R.id.imgChangePasswordClose);
            Button btnChangePassword = sheetView.findViewById(R.id.btnChangePassword);

            edtOldPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);
            edtNewPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);
            edtConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);

            Utils.setHideShowPassword(edtOldPassword);
            Utils.setHideShowPassword(edtNewPassword);
            Utils.setHideShowPassword(edtConfirmPassword);
            mBottomSheetDialog.setCancelable(true);


            imgChangePasswordClose.setOnClickListener(v -> mBottomSheetDialog.dismiss());

            btnChangePassword.setOnClickListener(view -> {
                String oldPassword = edtOldPassword.getText().toString();
                String password = edtNewPassword.getText().toString();
                String confirmPassword = edtConfirmPassword.getText().toString();

                if (!password.equals(confirmPassword)) {
                    edtConfirmPassword.requestFocus();
                    edtConfirmPassword.setError(activity.getString(R.string.pass_not_match));
                } else if (ApiConfig.CheckValidation(oldPassword, false, false)) {
                    edtOldPassword.requestFocus();
                    edtOldPassword.setError(activity.getString(R.string.enter_old_pass));
                } else if (ApiConfig.CheckValidation(password, false, false)) {
                    edtNewPassword.requestFocus();
                    edtNewPassword.setError(activity.getString(R.string.enter_new_pass));
                } else if (!oldPassword.equals(new Session(activity).getData(Constant.PASSWORD))) {
                    edtOldPassword.requestFocus();
                    edtOldPassword.setError(activity.getString(R.string.no_match_old_pass));
                } else if (ApiConfig.isConnected(activity)) {
                    ChangePassword(password);
                }
            });

            mBottomSheetDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ChangePassword(String password) {

        final Map<String, String> params = new HashMap<>();
        params.put(Constant.TYPE, Constant.CHANGE_PASSWORD);
        params.put(Constant.PASSWORD, password);
        params.put(Constant.ID, session.getData(Constant.ID));

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        alertDialog.setTitle(getString(R.string.change_pass));
        alertDialog.setMessage(getString(R.string.reset_alert_msg));
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(getString(R.string.yes), (dialog, which) -> ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        session.logoutUser(activity);
                    }
                    Toast.makeText(activity, object.getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.RegisterUrl, params, true));
        alertDialog.setNegativeButton(getString(R.string.no), (dialog, which) -> alertDialog1.dismiss());
        // Showing Alert Message
        alertDialog.show();
    }

}