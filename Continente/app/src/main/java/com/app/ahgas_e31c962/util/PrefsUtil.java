package com.app.ahgas_e31c962.util;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;

public class PrefsUtil {
    private static final String name = "copagaz";

    private static SharedPreferences getSharedPrefs(Context context){
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static boolean clearAllPrefs(Context context) {
        try {
            getSharedPrefs(context).edit().clear().apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removeValue(Context context, String key) {
        getSharedPrefs(context).edit().remove(key).apply();
    }

    private static final String OrderObj = "OrderObj";

    public static void setOrderObj(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(OrderObj, s).apply();
    }

    public static JSONObject getOrderObj(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        String objString = sp.getString(OrderObj, "");
        if(objString.isEmpty()){
            return null;
        }
        JSONObject object = null;
        try {
            object = new JSONObject(objString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    private static final String SelectedProduct = "SelectedProduct";
    public static void setSelectedProduct(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(SelectedProduct, s).apply();
    }
    public static String getSelectedProduct(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(SelectedProduct, "");
    }

    private static final String HashId = "HashId";
    public static void setHashId(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(HashId, s).apply();
    }
    public static String getHashId(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(HashId, "");
    }

    private static final String OrderNumber = "OrderNumber";
    public static void setOrderNumber(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(OrderNumber, s).apply();
    }
    public static String getOrderNumber(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(OrderNumber, "");
    }
    

    private static final String SelectedVendor = "SelectedVendor";
    public static void setSelectedVendor(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(SelectedVendor, s).apply();
    }
    public static String getSelectedVendor(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(SelectedVendor, "");
    }

    private static final String SelectedVendorPhone = "SelectedVendorPhone";
    public static void setSelectedVendorPhone(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(SelectedVendorPhone, s).apply();
    }
    public static String getSelectedVendorPhone(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(SelectedVendorPhone, "");
    }

    private static final String EvalDone = "EvalDone";
    public static void setEvalDone(Context context, boolean b){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putBoolean(EvalDone, b).apply();
    }
    public static boolean isEvalDone(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getBoolean(EvalDone, false);
    }

    private static final String SpecialPrice = "SpecialPrice";
    public static void setSpecialPrice(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(SpecialPrice, s).apply();
    }
    public static String getSpecialPrice(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(SpecialPrice, "0");
    }

    private static final String ContainerPrice = "ContainerPrice";
    public static void setContainerPrice(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(ContainerPrice, s).apply();
    }
    public static String getContainerPrice(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(ContainerPrice, "0");
    }


}
