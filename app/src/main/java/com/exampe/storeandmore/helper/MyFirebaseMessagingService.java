package com.exampe.storeandmore.helper;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import com.exampe.storeandmore.activity.MainActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                sendPushNotification(json);
                Log.e(TAG, "Exception: " + remoteMessage.getData().toString());
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    void sendPushNotification(JSONObject json) {
        try {
            JSONObject data = json.getJSONObject(Constant.DATA);
            String id = data.getString("id");
            String type = data.getString("type");
            String imageUrl = data.getString("image");
            String title = data.getString("title");
            String message = data.getString("message");
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            switch (type) {
                case "category":
                    intent.putExtra("id", id);
                    intent.putExtra("name", title);
                    intent.putExtra(Constant.FROM, type);
                    break;
                case "product":
                    intent.putExtra("id", id);
                    intent.putExtra(Constant.VARIANT_POSITION, 0);
                    intent.putExtra(Constant.FROM, type);
                    break;
                case "order":
                    intent.putExtra(Constant.FROM, type);
                    intent.putExtra("model", "");
                    intent.putExtra("id", id);
                    break;
                default:
                    intent.putExtra(Constant.FROM, "");
                    break;
            }
            switch (type) {
                case "payment_transaction":
                    Session.setCount(Constant.UNREAD_TRANSACTION_COUNT, (Session.getCount(Constant.UNREAD_TRANSACTION_COUNT, getApplicationContext()) + 1), getApplicationContext());
                    break;
                case "wallet_transaction":
                    Session.setCount(Constant.UNREAD_WALLET_COUNT, (Session.getCount(Constant.UNREAD_WALLET_COUNT, getApplicationContext()) + 1), getApplicationContext());
                    break;
                case "default":
                case "category":
                case "product":
                    Session.setCount(Constant.UNREAD_NOTIFICATION_COUNT, (Session.getCount(Constant.UNREAD_NOTIFICATION_COUNT, getApplicationContext()) + 1), getApplicationContext());
                    break;
            }
            MyNotificationManager mNotificationManager = new MyNotificationManager(getApplicationContext());
            if (imageUrl.equals("null") || imageUrl.equals("")) {
                mNotificationManager.showSmallNotification(title, message, intent);
            } else {
                mNotificationManager.showBigNotification(title, message, imageUrl, intent);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}