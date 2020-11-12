package com.app.ahgas_c5ef82c.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LogUtil {
    private static final String tag = "MyLogger";
    private static final boolean showLogs = true;
    private static StringBuilder builder = new StringBuilder();
    private static final String email = "saif052m@gmail.com";

    public static void loge(String s){
        if(showLogs)
            Log.e(tag, s);
        if(builder == null)
            builder = new StringBuilder();
        builder.append(s);
        builder.append("\n");
    }

    public static String getLogs(){
        if(builder == null)
            return null;
        return builder.toString();
    }

    public static void clearLogs(){
        builder = null;
    }

    public static void sendLogs(Context context) {
        sendEmail(context, new String[]{email}, "Logs", "Logs", LogUtil.getLogs());
    }

    public static void sendEmail(Context context, String[] recipientList, String title, String subject, String body) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, recipientList);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
            context.startActivity(Intent.createChooser(emailIntent, title));
            builder = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
