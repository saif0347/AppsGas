package com.app.ahgas_6035b49.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import java.io.File;
import java.lang.reflect.Method;

public class IntentUtil {

    private void sendSms(Context context, String number) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:" + number));
        context.startActivity(sendIntent);
    }

    public static void uninstallApp(Activity activity, String packageName){
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        activity.startActivity(intent);
    }

    public static void openUrlInBrowser(Activity activity, String url){
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(i);
    }

    public static void openUrlInChrome(Activity activity, String url){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null);
            activity.startActivity(intent);
        }
    }

    public static void openApp(Activity activity, String packageName){
        Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            activity.startActivity(launchIntent);
        }
    }

    public static void openDocument(Activity activity, String path) {
        uriFix();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(path);
        String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (extension.equalsIgnoreCase("") || mimetype == null) {
            intent.setDataAndType(Uri.fromFile(file), "text/*");
        } else {
            intent.setDataAndType(Uri.fromFile(file), mimetype);
        }
        activity.startActivity(Intent.createChooser(intent, "Choose an Application:"));
    }

    public static void openExcelFile(Activity activity, String path) {
        uriFix();
        Uri uri = Uri.parse(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            activity.startActivity(intent);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "No Application Available to View Excel.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void openPdf(Activity activity, String path) {
        uriFix();
        File file = new File(path);
        Uri uri1  = Uri.fromFile(file);
        Uri uri2 = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".my.package.name.provider", file);
        Log.e("tag", "uri1: "+uri1);
        Log.e("tag", "uri2: "+uri2);
        try {
            Intent intentUrl = new Intent(Intent.ACTION_VIEW);
            if(Build.VERSION.SDK_INT < 24){
                intentUrl.setDataAndType(uri1, "application/pdf");
            }
            else {
                intentUrl.setDataAndType(uri2, "application/pdf");
                intentUrl.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intentUrl.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intentUrl);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "No PDF Viewer Installed", Toast.LENGTH_LONG).show();
        }
    }

    public static void shareImage(Activity activity, String path) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + path));
        activity.startActivity(Intent.createChooser(share, "Share"));
    }

    public static void shareText(Activity activity, String title, String text){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, title);
        i.putExtra(Intent.EXTRA_TEXT, text);
        activity.startActivity(Intent.createChooser(i, "Share"));
    }

    public static void openInstaller(Activity activity, String filePath, String packageName) {
        uriFix();
        File file = new File(filePath);
        if(!file.exists()){
            Log.e("tag", "");
        }
        file.setReadable(true, false);
        Uri path = Uri.fromFile(file);
        Uri path2 = FileProvider.getUriForFile(activity, activity.getPackageName() + ".my.package.name.provider", file);
        if (Build.VERSION.SDK_INT >= 24) {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(path2);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(intent);
        }
        else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(path, "application/vnd.android.package-archive");
            activity.startActivity(intent);
        }
    }

    private static void uriFix(){
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}

