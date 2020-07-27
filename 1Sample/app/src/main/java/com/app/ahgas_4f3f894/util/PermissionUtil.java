package com.app.ahgas_4f3f894.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtil {
    public static boolean checkPermissions(Activity activity, String [] permissions){
        int i=100;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{p}, i);
                return false;
            }
            i++;
        }
        return true;
    }
}
