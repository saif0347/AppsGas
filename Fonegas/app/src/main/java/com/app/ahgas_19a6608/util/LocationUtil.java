package com.app.ahgas_19a6608.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

//implementation 'com.google.android.gms:play-services-location:16.0.0'
//<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

public class LocationUtil {
    public interface LocationResult {
        void currentLocationSuccess(Location location);
        void currentLocationFailed(String message);
    }

    public static boolean isGpsOn(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        return false;
    }

    private static boolean dialogOpen = false;

    public static void showGpsDisabledAlert(final Context context, String title, String msg) {
        if(dialogOpen)
            return;
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(callGPSSettingIntent);
                dialogInterface.dismiss();
                dialogOpen = false;
            }
        });
        dialog.show();
        dialogOpen = true;
    }

    public static void getCurrentLocation(final Context context, final LocationResult locationResult) {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        client.getLastLocation().addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null)
                    locationResult.currentLocationSuccess(location);
                else
                    locationResult.currentLocationFailed("");
            }
        }).addOnFailureListener((Activity) context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                locationResult.currentLocationFailed(e.getMessage());
            }
        });
    }

    public static HashMap<String, String> getAddressFromCoords(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        HashMap<String, String> hashMap = new HashMap<>();
        try {
            List<Address> arrayList = geocoder.getFromLocation(latitude, longitude, 1);
            if(arrayList.get(0).getSubLocality() != null)
                hashMap.put("neighbor", arrayList.get(0).getSubLocality());
            else
                hashMap.put("neighbor", "");

            if(arrayList.get(0).getLocality() != null)
                hashMap.put("city", arrayList.get(0).getLocality());
            else
                hashMap.put("city", "");

            if(arrayList.get(0).getAdminArea() != null)
                hashMap.put("state", arrayList.get(0).getAdminArea());
            else
                hashMap.put("state", "");

            if(arrayList.get(0).getPostalCode() != null)
                hashMap.put("zip", arrayList.get(0).getPostalCode());
            else
                hashMap.put("zip", "");

        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.loge(""+e.getMessage());
        }
        return hashMap;
    }

    /*public static final String route = "route";
    public static final String streeNo = "streeNo";
    public static final String zipCode = "zipCode";
    public static final String neighbor = "neighbor";
    public static final String city = "city";
    public static final String state = "state";
    public static final String country = "country";

    public interface AddressResponse{
        void response(HashMap<String, String> addressMap);
    }

    // quota API
    public static void getAddressParams(Context context, String apiKey, double lat, double lng, final AddressResponse addressResponse) {
        HashMap<String, String> hashMap = new HashMap<>();
        final HashMap<String, String> addressMap = new HashMap<>();
        addressMap.put(route, "");
        addressMap.put(streeNo, "");
        addressMap.put(zipCode, "");
        addressMap.put(neighbor, "");
        addressMap.put(city, "");
        addressMap.put(state, "");
        addressMap.put(country, "");
        String url = "https://maps.googleapis.com/maps/api/geocode/json?key="+apiKey+"&latlng="+lat+","+lng+"&sensor=true";
        CustomRequest customRequest = new CustomRequest(Request.Method.GET, url, hashMap, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObj) {
                Log.e("getParams", jsonObj.toString());
                try {
                    JSONArray results = jsonObj.getJSONArray("results");
                    String addrss = "",streeNo="",zipCode="",neighbor="",city="",state="",country="";
                    for(int k=0; k < results.length(); k++) {

                        JSONArray addressComponents = results.getJSONObject(k).getJSONArray("address_components");

                        for (int i = 0; i < addressComponents.length(); i++) {

                            JSONArray typesArray = addressComponents.getJSONObject(i).getJSONArray("types");

                            for (int j = 0; j < typesArray.length(); j++) {
                                if (typesArray.get(j).toString().equalsIgnoreCase("route")) {
                                    if(addrss.isEmpty())
                                        addrss = addressComponents.getJSONObject(i).getString("long_name");
                                }
                                if (typesArray.get(j).toString().equalsIgnoreCase("street_number")) {
                                    if(streeNo.isEmpty())
                                        streeNo = addressComponents.getJSONObject(i).getString("long_name");
                                }
                                if (typesArray.get(j).toString().equalsIgnoreCase("postal_code")) {
                                    if(zipCode.isEmpty())
                                        zipCode = addressComponents.getJSONObject(i).getString("long_name");
                                }
                                if (typesArray.get(j).toString().equalsIgnoreCase("sublocality")) {
                                    if(neighbor.isEmpty())
                                        neighbor = addressComponents.getJSONObject(i).getString("long_name");
                                }
                                if (typesArray.get(j).toString().equalsIgnoreCase("neighborhood")) {
                                    if(neighbor.isEmpty())
                                        neighbor = addressComponents.getJSONObject(i).getString("long_name");
                                }
                                if (typesArray.get(j).toString().equalsIgnoreCase("administrative_area_level_3")) {
                                    if(neighbor.isEmpty())
                                        neighbor = addressComponents.getJSONObject(i).getString("long_name");
                                }
                                if (typesArray.get(j).toString().equalsIgnoreCase("locality")) {
                                    if(city.isEmpty())
                                        city = addressComponents.getJSONObject(i).getString("long_name");
                                }
                                if (typesArray.get(j).toString().equalsIgnoreCase("administrative_area_level_2")) {
                                    if(city.isEmpty())
                                        city = addressComponents.getJSONObject(i).getString("long_name");
                                }
                                if (typesArray.get(j).toString().equalsIgnoreCase("administrative_area_level_1")) {
                                    if(state.isEmpty())
                                        state = addressComponents.getJSONObject(i).getString("short_name");
                                }
                                if (typesArray.get(j).toString().equalsIgnoreCase("country")) {
                                    if(country.isEmpty())
                                        country = addressComponents.getJSONObject(i).getString("long_name");
                                }
                            }
                        }
                    }
                    addressMap.put(LocationUtil.route, addrss);
                    addressMap.put(LocationUtil.streeNo, streeNo);
                    addressMap.put(LocationUtil.zipCode, zipCode);
                    addressMap.put(LocationUtil.neighbor, neighbor);
                    addressMap.put(LocationUtil.city, city);
                    addressMap.put(LocationUtil.state, state);
                    addressMap.put(LocationUtil.country, country);

                    addressResponse.response(addressMap);
                } catch (Exception e) {
                    e.printStackTrace();
                    addressResponse.response(addressMap);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.toString());
                addressResponse.response(addressMap);
            }
        });
        VolleyLibrary.getInstance(context).addToRequestQueue(customRequest, "", false);
    }

    public void getAddressParams(Context context, String apiKey, String placeId, final AddressResponse addressResponse) {
        HashMap<String, String> hashMap = new HashMap<>();
        final HashMap<String, String> addressMap = new HashMap<>();
        addressMap.put(route, "");
        addressMap.put(streeNo, "");
        addressMap.put(zipCode, "");
        addressMap.put(neighbor, "");
        addressMap.put(city, "");
        addressMap.put(state, "");
        addressMap.put(country, "");
        String url = "https://maps.googleapis.com/maps/api/place/details/json?key="+apiKey+"&placeid="+placeId;
        CustomRequest customRequest = new CustomRequest(Request.Method.GET, url, hashMap, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObj) {
                Log.e("getParams", jsonObj.toString());
                try {
                    JSONArray addressComponents = jsonObj.getJSONObject("result").getJSONArray("address_components");
                    String addrss = "",streeNo="",zipCode="",neighbor="",city="",state="",country="";
                    for(int i = 0; i < addressComponents.length(); i++) {
                        JSONArray typesArray = addressComponents.getJSONObject(i).getJSONArray("types");
                        for (int j = 0; j < typesArray.length(); j++) {
                            if (typesArray.get(j).toString().equalsIgnoreCase("route")) {
                                addrss = addressComponents.getJSONObject(i).getString("long_name");
                            }
                            if (typesArray.get(j).toString().equalsIgnoreCase("street_number")) {
                                streeNo = addressComponents.getJSONObject(i).getString("long_name");
                            }
                            if (typesArray.get(j).toString().equalsIgnoreCase("postal_code")) {
                                zipCode = addressComponents.getJSONObject(i).getString("long_name");
                            }
                            if (typesArray.get(j).toString().equalsIgnoreCase("sublocality")) {
                                neighbor = addressComponents.getJSONObject(i).getString("long_name");
                            }
                            if (typesArray.get(j).toString().equalsIgnoreCase("neighborhood")) {
                                neighbor = addressComponents.getJSONObject(i).getString("long_name");
                            }
                            if (typesArray.get(j).toString().equalsIgnoreCase("locality")) {
                                city = addressComponents.getJSONObject(i).getString("long_name");
                            }
                            if (typesArray.get(j).toString().equalsIgnoreCase("administrative_area_level_2")) {
                                city = addressComponents.getJSONObject(i).getString("long_name");
                            }
                            if (typesArray.get(j).toString().equalsIgnoreCase("administrative_area_level_1")) {
                                state = addressComponents.getJSONObject(i).getString("short_name");
                            }
                            if (typesArray.get(j).toString().equalsIgnoreCase("country")) {
                                if(country.isEmpty())
                                    country = addressComponents.getJSONObject(i).getString("long_name");
                            }
                        }
                    }
                    addressMap.put(LocationUtil.route, addrss);
                    addressMap.put(LocationUtil.streeNo, streeNo);
                    addressMap.put(LocationUtil.zipCode, zipCode);
                    addressMap.put(LocationUtil.neighbor, neighbor);
                    addressMap.put(LocationUtil.city, city);
                    addressMap.put(LocationUtil.state, state);
                    addressMap.put(LocationUtil.country, country);

                    addressResponse.response(addressMap);
                } catch (Exception e) {
                    e.printStackTrace();
                    addressResponse.response(addressMap);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.toString());
                addressResponse.response(addressMap);
            }
        });
        VolleyLibrary.getInstance(context).addToRequestQueue(customRequest, "", false);
    }*/

    public static String getCoordinatesFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        String coordinates = "0,0,0";
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            coordinates = location.getLatitude() + "," + location.getLongitude() + ",0";
            return coordinates;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coordinates;
    }

    //----------------------------------------GPS Service-------------------------------------------------------

    private static final int UPDATE_INTERVAL = 4000;
    private static final int FASTEST_UPDATE_INTERVAL = 2000;
    private static FusedLocationProviderClient mFusedLocationClient;
    private static LocationCallback locationCallback;

    public interface GpsResult {
        void gpsLocation(Location location);
    }

    public static void startGpsService(Context context, final GpsResult gpsResult) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                super.onLocationResult(locationResult);
                gpsResult.gpsLocation(locationResult.getLastLocation());
            }
        };
        mFusedLocationClient.requestLocationUpdates(createLocationRequest(), locationCallback, null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //
            }
        });
    }

    public static LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public static void stopGps(){
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public int getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return (int)d;
    }

    public double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }
}

