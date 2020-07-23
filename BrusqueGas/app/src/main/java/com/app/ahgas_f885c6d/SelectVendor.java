package com.app.ahgas_f885c6d;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.app.ahgas_f885c6d.util.PrefsUtil;
import com.app.ahgas_f885c6d.volley.CustomRequest;
import com.app.ahgas_f885c6d.volley.VolleyLibrary;
import com.bumptech.glide.Glide;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectVendor extends AppCompatActivity {
    @BindView(R.id.listView)
    ListView listView;
    ArrayList<VendorModel> models = new ArrayList<>();
    VendorsAdapter vendorsAdapter;
    @BindView(R.id.loader)
    ImageView loader;
    @BindView(R.id.noresult)
    TextView noresult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_vendor);
        ButterKnife.bind(this);

        getVendors();
    }

    public void getVendors() {
        showLoader();
        HashMap<String, String> hashMap = new HashMap<>();
        String zipcode = "";
        JSONObject orderObj = PrefsUtil.getOrderObj(this);
        if (orderObj != null) {
            try {
                zipcode = orderObj.getJSONObject("address").getString("zip_code");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String url = MainActivity.baseUrl + "/partner/" + zipcode + "/product/" + PrefsUtil.getSelectedProduct(this);
        CustomRequest customRequest = new CustomRequest(Request.Method.GET, url, hashMap, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("success", response.toString());
                models.clear();
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    Type listType = new TypeToken<ArrayList<VendorModel>>() {
                    }.getType();
                    final ArrayList<VendorModel> modelsNew = new GsonBuilder().create().fromJson(jsonArray.toString(), listType);
                    models.addAll(modelsNew);
                    vendorsAdapter = new VendorsAdapter(SelectVendor.this, models);
                    listView.setAdapter(vendorsAdapter);

                    if (models.size() == 0){
                        noresult.setVisibility(View.VISIBLE);
                        hideLoader();
                    }
                    else {
                        noresult.setVisibility(View.GONE);
                    }

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //Utils.clickEffect(view);
                            // save data
                            JSONObject orderObj = PrefsUtil.getOrderObj(SelectVendor.this);
                            if (orderObj != null) {
                                try {
                                    JSONArray productArr;
                                    JSONObject productObj;
                                    if (orderObj.has("product")) {
                                        productArr = orderObj.getJSONArray("product");
                                        productObj = productArr.getJSONObject(0);
                                    } else {
                                        productArr = new JSONArray();
                                        productObj = new JSONObject();
                                    }

                                    ArrayList<VendorModel> models = vendorsAdapter.getData();
                                    productObj.put("hashid", models.get(position).getProduct_hash_id());
                                    productArr.put(0, productObj);
                                    orderObj.put("product", productArr);

                                    JSONObject ordObj;
                                    if (orderObj.has("order")) {
                                        ordObj = orderObj.getJSONObject("order");
                                    } else {
                                        ordObj = new JSONObject();
                                    }
                                    ordObj.put("partner_hashid", models.get(position).getPartner_hash_id());
                                    orderObj.put("order", ordObj);

                                    PrefsUtil.setOrderObj(SelectVendor.this, orderObj.toString());
                                    PrefsUtil.setHashId(SelectVendor.this, models.get(position).getPartner_hash_id());
                                    PrefsUtil.setSelectedVendor(SelectVendor.this, models.get(position).getPartner_name());
                                    PrefsUtil.setSelectedVendorPhone(SelectVendor.this, models.get(position).getPhone());
                                    PrefsUtil.setSpecialPrice(SelectVendor.this, models.get(position).getProduct_special_price());
                                    PrefsUtil.setContainerPrice(SelectVendor.this, models.get(position).getProduct_container_price());

                                    Intent selectVendor = new Intent(SelectVendor.this, Checkout.class);
                                    selectVendor.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(selectVendor);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoader();
                Toast.makeText(SelectVendor.this, "Problema de Rede", Toast.LENGTH_SHORT).show();
                Log.e("error", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("gd-group-hash", MainActivity.gdGroupHash);
                return headers;
            }
        };
        VolleyLibrary.getInstance(this).addToRequestQueue(customRequest, "", false);
    }

    public void hideLoader() {
        if (loader == null)
            return;
        loader.setVisibility(View.GONE);
    }

    public void showLoader() {
        loader.setVisibility(View.VISIBLE);
        Glide.with(this).asGif().load(R.raw.loading).into(loader);
    }
}
