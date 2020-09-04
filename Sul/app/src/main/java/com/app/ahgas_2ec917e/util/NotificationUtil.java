package com.app.ahgas_2ec917e.util;

import android.annotation.TargetApi;
import androidx.core.app.NotificationCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;

import com.app.ahgas_2ec917e.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class NotificationUtil {

    public static void showNotification(Context context, Intent clickIntent, String title, String msg, int notId){
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(msg);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        // include vibrate permission in AndroidManifest file
        notificationBuilder.setVibrate(new long[] {1000, 1000});
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(context.getResources().getColor(android.R.color.white));
            notificationBuilder.setSmallIcon(R.drawable.push);//36x36 and transparent white
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.push));
        }
        else {
            notificationBuilder.setSmallIcon(R.drawable.push);
        }
        notificationBuilder.setContentIntent(pendingIntent);
        //notificationBuilder.setDeleteIntent(createOnDismissedIntent(context));

        Random r = new Random();
        int mNotificationId = r.nextInt(10000)+r.nextInt(10000);
        if(notId > 0){
            mNotificationId = notId;
        }
        NotificationManager mNotifyMgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(mNotifyMgr, notificationBuilder, mNotificationId);
        }
        mNotifyMgr.notify(mNotificationId, notificationBuilder.build());
    }

    private static void createNotificationChannel(NotificationManager mNotifyMgr, NotificationCompat.Builder mBuilder, int mNotificationId) {
        String channelId = "channel-"+mNotificationId;
        String channelName = "channel-"+mNotificationId;
        int importance = NotificationManager.IMPORTANCE_HIGH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            mChannel.enableVibration(true);
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            mChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, att);
            mNotifyMgr.createNotificationChannel(mChannel);
            mBuilder.setChannelId(channelId);
        }
    }

    /*
    private static PendingIntent createOnDismissedIntent(Context context) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, 0);
        return pendingIntent;
    }

    public class NotificationDismissedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("tag", "dismiss onReceive");
        }
    }

    <receiver
            android:name=".receiver.NotificationDismissedReceiver"
            android:exported="false" />
    */

    public static void showImageNotification(Context context, String imgUrl, Intent clickIntent, String title, String msg, int notId){
        new DownloadImage(context, imgUrl, clickIntent, title, msg, notId).execute();
    }

    private static class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        private Context mContext;
        private String imageUrl, title, msg;
        private int notId;
        private Intent clickIntent;
        public DownloadImage(Context context, String imgUrl, Intent clickIntent, String title, String msg, int notId) {
            this.mContext = context;
            this.imageUrl = imgUrl;
            this.clickIntent = clickIntent;
            this.title = title;
            this.msg = msg;
            this.notId = notId;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream in;
            try {
                URL url = new URL(this.imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Bitmap bmp) {
            super.onPostExecute(bmp);
            showImageNotification(mContext, bmp, clickIntent, title, msg, notId);
        }
    }

    public static void showImageNotification(Context context, Bitmap bmp, Intent clickIntent, String title, String msg, int notId){
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(msg);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        // include vibrate permission in AndroidManifest file
        notificationBuilder.setVibrate(new long[] {1000, 1000});
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(context.getResources().getColor(android.R.color.white));
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);//36x36 and transparent white
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
            notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bmp));
        }
        else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bmp));
        }
        notificationBuilder.setContentIntent(pendingIntent);
        Random r = new Random();
        int mNotificationId = r.nextInt(10000)+r.nextInt(10000);
        if(notId > 0){
            mNotificationId = notId;
        }
        NotificationManager mNotifyMgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(mNotifyMgr, notificationBuilder, mNotificationId);
        }
        mNotifyMgr.notify(mNotificationId, notificationBuilder.build());
    }
}
