package com.app.ahgas_cc19bcd.firebase;

import android.content.Intent;
import android.util.Log;

import com.app.ahgas_cc19bcd.MainActivity;
import com.app.ahgas_cc19bcd.util.LogUtil;
import com.app.ahgas_cc19bcd.util.NotificationUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("message", "new message came");
        if(remoteMessage.getData() != null){
            String body2 = remoteMessage.getData().get("body");;
            LogUtil.loge("onMessageReceived: body2 > "+body2);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            Random r = new Random();
            int notId = 1000+r.nextInt(1000);
            NotificationUtil.showNotification(
                    getApplicationContext(), intent, "Entrega de gás", body2, notId
            );
        }
    }
}