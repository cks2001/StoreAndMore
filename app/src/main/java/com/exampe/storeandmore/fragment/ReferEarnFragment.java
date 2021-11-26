package com.exampe.storeandmore.fragment;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;


public class ReferEarnFragment extends Fragment {
    View root;
    TextView edtReferCoin, edtCode, edtCopy, edtInvite;
    Session session;
    String preText = "";
    Activity activity;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_refer_earn, container, false);
        activity = getActivity();
        setHasOptionsMenu(true);


        session = new Session(activity);
        edtReferCoin = root.findViewById(R.id.edtReferCoin);
        if (session.getData(Constant.refer_earn_method).equals("rupees")) {
            preText = session.getData(Constant.currency) + session.getData(Constant.refer_earn_bonus);
        } else {
            preText = session.getData(Constant.refer_earn_bonus) + "% ";
        }
        edtReferCoin.setText(getString(R.string.refer_text_1) + preText + getString(R.string.refer_text_2) + session.getData(Constant.currency) + session.getData(Constant.min_refer_earn_order_amount) + getString(R.string.refer_text_3) + session.getData(Constant.currency) + session.getData(Constant.max_refer_earn_amount) + ".");
        edtCode = root.findViewById(R.id.edtCode);
        edtCopy = root.findViewById(R.id.edtCopy);
        edtInvite = root.findViewById(R.id.edtInvite);

        edtInvite.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(activity, R.drawable.ic_share), null, null, null);
        edtCode.setText(session.getData(Constant.REFERRAL_CODE));
        edtCopy.setOnClickListener(v -> {

            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", edtCode.getText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(activity, R.string.refer_code_copied, Toast.LENGTH_SHORT).show();
        });

        edtInvite.setOnClickListener(view -> {
            if (!edtCode.getText().toString().equals("code")) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.refer_share_msg_1)
                            + getResources().getString(R.string.app_name) + getString(R.string.refer_share_msg_2)
                            + "\n " + Constant.WebsiteUrl + "refer/" + edtCode.getText().toString());
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.invite_friend_title)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(activity, getString(R.string.refer_code_alert_msg), Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.refer_and_earn);
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
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

}