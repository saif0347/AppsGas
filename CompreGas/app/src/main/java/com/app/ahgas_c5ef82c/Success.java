package com.app.ahgas_c5ef82c;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.ahgas_c5ef82c.util.LogUtil;
import com.app.ahgas_c5ef82c.util.PrefsUtil;
import com.app.ahgas_c5ef82c.util.Utils;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Success extends AppCompatActivity {
    @BindView(R.id.sfsfs)
    LinearLayout sfsfs;
    @BindView(R.id.namePhone)
    TextView namePhone;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @BindView(R.id.submit)
    Button submit;
    @BindView(R.id.loader)
    ImageView loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);
        ButterKnife.bind(this);

        namePhone.setText(PrefsUtil.getSelectedVendor(this) + "\n" + PrefsUtil.getSelectedVendorPhone(this));

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.clickEffect(v);
                rateUser();
            }
        });
    }

    private void rateUser() {
        showLoader();
        String url = MainActivity.baseUrl + "/partner/"+PrefsUtil.getHashId(this)+"/review";
        LogUtil.loge("url: "+url);
        JSONObject object = new JSONObject();
        try {
            object.put("value", (int)ratingBar.getRating());
            object.put("order_number", PrefsUtil.getOrderNumber(this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtil.loge("sending: "+object.toString());
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        LogUtil.loge("response: " + response.toString());
                        hideLoader();
                        try {
                            int status = response.getInt("status_code");
                            if(status > 0){
                                PrefsUtil.setEvalDone(Success.this, true);
                                finishAffinity();
                                Intent repeat = new Intent(Success.this, MainActivity.class);
                                startActivity(repeat);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Success.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoader();
                        Toast.makeText(Success.this, "Problema de Rede", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        // Adding request to request queue
        Volley.newRequestQueue(this).add(jsonObjReq);
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
}
