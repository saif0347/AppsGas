package com.app.ahgas_401df96;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.app.ahgas_401df96.util.LogUtil;
import com.app.ahgas_401df96.util.PrefsUtil;
import com.app.ahgas_401df96.volley.CustomRequest;
import com.app.ahgas_401df96.volley.VolleyLibrary;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectProduct extends AppCompatActivity {
    @BindView(R.id.listView)
    ListView listView;
    ArrayList<ProductModel> models = new ArrayList<>();
    ProductsAdapter productsAdapter;
    @BindView(R.id.loader)
    ImageView loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_product);
        ButterKnife.bind(this);

        // testing
        JSONObject orderObj = PrefsUtil.getOrderObj(this);
        if (orderObj != null)
            LogUtil.loge("orderObj: " + orderObj.toString());

        getProducts();
    }

    public void getProducts() {
        showLoader();
        HashMap<String, String> hashMap = new HashMap<>();
        String url = MainActivity.baseUrl + "/product";
        CustomRequest customRequest = new CustomRequest(Request.Method.GET, url, hashMap, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideLoader();
                Log.e("success", response.toString());
                try {
                    JSONArray data = response.getJSONArray("data");
                    models.clear();
                    for(int i=0; i<data.length(); i++){
                        String name = data.getString(i);
                        models.add(new ProductModel(name));
                    }
                    productsAdapter = new ProductsAdapter(SelectProduct.this, models);
                    listView.setAdapter(productsAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //Utils.clickEffect(view);
                            PrefsUtil.setSelectedProduct(SelectProduct.this, models.get(position).getName());
                            Intent selectVendor = new Intent(SelectProduct.this, SelectVendor.class);
                            startActivity(selectVendor);
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
                Toast.makeText(SelectProduct.this, "Problema de Rede", Toast.LENGTH_SHORT).show();
                Log.e("error", error.toString());
            }
        });
        VolleyLibrary.getInstance(this).addToRequestQueue(customRequest, "", false);
    }

    private void hideLoader() {
        if(loader == null)
            return;
        loader.setVisibility(View.GONE);
    }

    private void showLoader() {
        loader.setVisibility(View.VISIBLE);
        Glide.with(this).asGif().load(R.raw.loading).into(loader);
    }
}
