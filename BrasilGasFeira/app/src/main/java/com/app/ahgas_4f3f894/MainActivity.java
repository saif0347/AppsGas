package com.app.ahgas_4f3f894;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.app.ahgas_4f3f894.util.LocationUtil;
import com.app.ahgas_4f3f894.util.LogUtil;
import com.app.ahgas_4f3f894.util.PrefsUtil;
import com.app.ahgas_4f3f894.util.Utils;
import com.app.ahgas_4f3f894.volley.CustomRequest;
import com.app.ahgas_4f3f894.volley.VolleyLibrary;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    //zipcode = 05412003
    public static final String baseUrl = "https://livefrontapp-api.gasdelivery.com.br";
    //public static final String baseUrl = "https://frontapp-api.gasdelivery.com.br";
    public static final String gdGroupHash = "4f3f8949ecba1fc3f713b758a15148d1";

    public static final String apiKey = "AIzaSyBcR9vwz_ifnVXikfdxwzzvKuLYry5I3RY";
    @BindView(R.id.address)
    EditText address;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.myLocation)
    LinearLayout myLocation;
    @BindView(R.id.loader)
    ImageView loader;
    double lat, lng;
    HashMap<String, String> hashMap = new HashMap<>();
    @BindView(R.id.repeat)
    Button repeat;
    String addrss = "";
    String streeNo = "";
    String zipCode = "";
    String neighbor = "";
    String city = "";
    String state = "";
    ArrayList<AutocompletePrediction> predictionModels = new ArrayList<>();
    MyPlacesAdapter myPlacesAdapter;
    TextWatcher textWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if(!PrefsUtil.getOrderNumber(this).isEmpty() && !PrefsUtil.isEvalDone(this)){
            LogUtil.loge("open success");
            Intent success = new Intent(this, Success.class);
            startActivity(success);
            return;
        }

        // places code
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.clickEffect(v);
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                    return;
                }
                getCurrentLocation();
            }
        });

        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.clickEffect(v);
                Intent checkout = new Intent(MainActivity.this, Checkout.class);
                checkout.putExtra("repeat", "repeat");
                startActivity(checkout);
            }
        });

        JSONObject orderObj = PrefsUtil.getOrderObj(this);
        if(orderObj != null) {
            try {
                if(orderObj.has("address")) {
                    if (orderObj.has("product")) {
                        if (orderObj.has("order")){
                            repeat.setVisibility(View.VISIBLE);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PlacesClient placesClient = Places.createClient(this);
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                Log.e("clear", "afterTextChanged");
                if(s.toString().isEmpty()) {
                    clearPlaces();
                    return;
                }
                AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
                    if(address.getText().toString().isEmpty()) {
                        clearPlaces();
                        return;
                    }
                    predictionModels.clear();
                    for(int i=0; i<5; i++){
                        if(i < response.getAutocompletePredictions().size())
                            predictionModels.add(response.getAutocompletePredictions().get(i));
                    }
                    if(myPlacesAdapter == null){
                        myPlacesAdapter = new MyPlacesAdapter(MainActivity.this, predictionModels);
                        listView.setAdapter(myPlacesAdapter);
                    }
                    else{
                        myPlacesAdapter.setData(predictionModels);
                    }
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e("tag", "Place not found: " + apiException.getStatusCode());
                    }
                });
            }
        };
        address.addTextChangedListener(textWatcher);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            address.removeTextChangedListener(textWatcher);
            AutocompletePrediction prediction = predictionModels.get(position);
            address.setText(prediction.getFullText(null));
            getParams(prediction.getPlaceId());
            clearPlaces();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearPlaces();
        if(textWatcher != null)
            address.removeTextChangedListener(textWatcher);
    }

    private void clearPlaces() {
        Log.e("clear", "clear");
        predictionModels.clear();
        if(myPlacesAdapter != null)
            myPlacesAdapter.setData(predictionModels);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // granted
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (!LocationUtil.isGpsOn(this)) {
            LocationUtil.showGpsDisabledAlert(this, "Enable GPS", "Please enable GPS for the app to get location.");
            return;
        }
        showLoader();
        LocationUtil.startGpsService(MainActivity.this, new LocationUtil.GpsResult() {
            @Override
            public void gpsLocation(Location location) {
                if(address == null)
                    return;
                LocationUtil.stopGps();
                //String address_ = LocationUtil.getAddressFromCoordinates(MainActivity.this, location.getLatitude(), location.getLongitude());
                hideLoader();
                lat = location.getLatitude();
                lng = location.getLongitude();
                getParamsPin();
            }
        });
    }

    public void getParamsPin() {
        resetParams();
        showLoader();
        HashMap<String, String> hashMap = new HashMap<>();
        String url = "https://maps.googleapis.com/maps/api/geocode/json?key="+apiKey+"&latlng="+lat+","+lng+"&sensor=true";
        CustomRequest customRequest = new CustomRequest(Request.Method.GET, url, hashMap, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObj) {
                hideLoader();
                Log.e("getParams", jsonObj.toString());
                try {
                    JSONArray results = jsonObj.getJSONArray("results");
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
                            }
                        }
                    }
                    moveToNextFromPlaces();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoader();
                Toast.makeText(MainActivity.this, "Problema de Rede", Toast.LENGTH_SHORT).show();
                Log.e("error", error.toString());
            }
        });
        VolleyLibrary.getInstance(this).addToRequestQueue(customRequest, "", false);
    }

    private void resetParams() {
        addrss = "";
        streeNo = "";
        zipCode = "";
        neighbor = "";
        city = "";
        state = "";
    }

    private void hideLoader() {
        if (loader == null)
            return;
        loader.setVisibility(View.GONE);
    }

    private void showLoader() {
        loader.setVisibility(View.VISIBLE);
        Glide.with(this).asGif().load(R.raw.loading).into(loader);
    }

    @Override
    public void onBackPressed() {
        if(loader != null) {
            if (loader.isShown()) {
                hideLoader();
                return;
            }
        }
        super.onBackPressed();
    }

    public void getParams(String placeId) {
        resetParams();
        showLoader();
        HashMap<String, String> hashMap = new HashMap<>();
        String url = "https://maps.googleapis.com/maps/api/place/details/json?key="+apiKey+"&placeid="+placeId;
        CustomRequest customRequest = new CustomRequest(Request.Method.GET, url, hashMap, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObj) {
                hideLoader();
                Log.e("getParams", jsonObj.toString());
                try {
                    JSONArray addressComponents = jsonObj.getJSONObject("result").getJSONArray("address_components");
                    JSONObject location = jsonObj.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
                    lat = location.getDouble("lat");
                    lng = location.getDouble("lng");
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
                        }
                    }
                    moveToNextFromPlaces();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoader();
                if(address != null && textWatcher != null)
                    address.addTextChangedListener(textWatcher);
                Toast.makeText(MainActivity.this, "Problema de Rede", Toast.LENGTH_SHORT).show();
                Log.e("error", error.toString());
            }
        });
        VolleyLibrary.getInstance(this).addToRequestQueue(customRequest, "", false);
    }

    private void moveToNextFromPlaces() {
        if(!PrefsUtil.getOrderNumber(this).isEmpty() && !PrefsUtil.isEvalDone(this)){
            Toast.makeText(this, "Cannot place new order before evaluation of the past order!", Toast.LENGTH_SHORT).show();
            LogUtil.loge("open success");
            Intent success = new Intent(this, Success.class);
            startActivity(success);
            return;
        }
        try {
            JSONObject mainObject;
            JSONObject addressObj;
            mainObject = PrefsUtil.getOrderObj(this);
            if (mainObject == null) {
                mainObject = new JSONObject();
                addressObj = new JSONObject();
            } else {
                addressObj = mainObject.getJSONObject("address");
            }

            addressObj.put("address", addrss);
            addressObj.put("lat", String.valueOf(lat));
            addressObj.put("lng", String.valueOf(lng));

            LogUtil.loge("lat: "+lat);
            LogUtil.loge("long: "+lng);

            streeNo  = streeNo.replaceAll("[^0-9]", "");
            addressObj.put("street_number", streeNo);
            addressObj.put("zip_code", zipCode.replace("-",""));
            addressObj.put("neighborhood", neighbor);
            addressObj.put("city", city);
            addressObj.put("state", state);

            mainObject.put("address", addressObj);

            LogUtil.loge("order: " + mainObject.toString());

            PrefsUtil.setOrderObj(MainActivity.this, mainObject.toString());
            Intent next = new Intent(MainActivity.this, FillAddress.class);
            startActivity(next);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static class MyPlacesAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<AutocompletePrediction> mList;
        private LayoutInflater mLayoutInflater = null;

        public MyPlacesAdapter(Context context, ArrayList<AutocompletePrediction> list) {
            mContext = context;
            mList = list;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        public void setData(ArrayList<AutocompletePrediction> predictionModels) {
            this.mList = predictionModels;
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return mList.size();
        }
        @Override
        public Object getItem(int pos) {
            return mList.get(pos);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            CompleteListViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(android.R.layout.simple_spinner_dropdown_item, null);
                viewHolder = new CompleteListViewHolder(v);
                v.setTag(viewHolder);
            }
            else {
                viewHolder = (CompleteListViewHolder) v.getTag();
            }
            AutocompletePrediction model = mList.get(position);
            // set views data here
            viewHolder.textView.setSingleLine(false);
            viewHolder.textView.setText(model.getFullText(null));
            viewHolder.textView.setPadding(10,10,10,10);
            viewHolder.textView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            return v;
        }

        class CompleteListViewHolder {
            // declare views here
            CheckedTextView textView;
            public CompleteListViewHolder(View view) {
                //initialize views here
                textView = view.findViewById(android.R.id.text1);
            }
        }
    }
}



