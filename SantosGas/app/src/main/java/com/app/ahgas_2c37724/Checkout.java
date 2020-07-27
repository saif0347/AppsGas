package com.app.ahgas_2c37724;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.ahgas_2c37724.util.LogUtil;
import com.app.ahgas_2c37724.util.PopupUtil;
import com.app.ahgas_2c37724.util.PrefsUtil;
import com.app.ahgas_2c37724.util.TimeUtil;
import com.app.ahgas_2c37724.util.Utils;
import com.app.ahgas_2c37724.util.ValidUtil;
import com.app.ahgas_2c37724.volley.CustomRequest;
import com.app.ahgas_2c37724.volley.VolleyLibrary;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Checkout extends AppCompatActivity {
    @BindView(R.id.name)
    EditText name;
    @BindView(R.id.telefone)
    EditText telefone;
    @BindView(R.id.paymentMethod)
    BetterSpinner paymentMethod;
    @BindView(R.id.address)
    TextView address;
    @BindView(R.id.calendar)
    ImageView calendar;
    @BindView(R.id.vessel)
    TextView vessel;
    @BindView(R.id.vesselPanel)
    LinearLayout vesselPanel;
    @BindView(R.id.quantity)
    EditText quantity;
    @BindView(R.id.quantityPanel)
    LinearLayout quantityPanel;
    @BindView(R.id.price)
    TextView price;
    @BindView(R.id.pricePanel)
    LinearLayout pricePanel;
    @BindView(R.id.submit)
    Button submit;
    int vesselFlag = 0;
    @BindView(R.id.vendorName)
    TextView vendorName;
    @BindView(R.id.date)
    TextView date;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.loader)
    ImageView loader;
    ArrayList<String> payMethodIds = new ArrayList<>();
    ArrayList<String> payMethodNames = new ArrayList<>();
    ArrayList<String> times = new ArrayList<>();
    @BindView(R.id.productName)
    TextView productName;
    String tempDate, tempDay;
    JSONArray vendorTimeLimits;
    String firebase_token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        ButterKnife.bind(this);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                firebase_token = instanceIdResult.getToken();
                LogUtil.loge("firebase_token: "+firebase_token);
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter(Checkout.this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Loading..."});
        paymentMethod.setAdapter(adapter);

        quantity.setSelectAllOnFocus(true);

        quantity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do your stuff here
                    updateQuantity(Utils.txt(quantity));
                    quantity.setEnabled(false);
                    quantity.clearFocus();
                }
                return true;
            }
        });

        quantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    submit.setVisibility(View.GONE);
                }
                else{
                    submit.setVisibility(View.VISIBLE);
                }
            }
        });

        vendorName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.clickEffect(v);
                Intent vendors = new Intent(Checkout.this, SelectVendor.class);
                vendors.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(vendors);
            }
        });

        // reset product parameters
        JSONObject orderObj = PrefsUtil.getOrderObj(this);
        if (orderObj != null) {
            try {
                orderObj.getJSONArray("product").getJSONObject(0).put("vessel", "0");
                orderObj.getJSONArray("product").getJSONObject(0).put("quantity", "1");
                PrefsUtil.setOrderObj(Checkout.this, orderObj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        loadData(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(1);
        if(getIntent().hasExtra("repeat")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogUtil.loge("focus");
                    hideKeyboard(Checkout.this);
                }
            }, 500);
        }
        else{
            name.requestFocus();
        }
    }

    private void loadData(int flag) {
        JSONObject orderObj = PrefsUtil.getOrderObj(this);
        if (orderObj != null) {
            LogUtil.loge("orderObj: " + orderObj.toString());
            try {
                if (orderObj.has("order")) {
                    JSONObject ordObj = orderObj.getJSONObject("order");
                    if(flag == 0) {
                        if (ordObj.has("full_name"))
                            name.setText(ordObj.getString("full_name"));
                        if (ordObj.has("phone"))
                            telefone.setText(ordObj.getString("phone"));
                    }

                    /*if(flag == 2) {
                        if (ordObj.has("date"))
                            date.setText(ordObj.getString("date"));
                        if (ordObj.has("time"))
                            time.setText(ordObj.getString("time"));
                        LogUtil.loge("storedTime: "+ordObj.getString("time"));
                    }*/

                    if(flag == 1) {
                        String selectedPayId = "";
                        if (ordObj.has("mean_of_payment_id"))
                            selectedPayId = ordObj.getString("mean_of_payment_id");
                        if (ordObj.has("partner_hashid")) {
                            String hashId = ordObj.getString("partner_hashid");
                            getPayMethods(hashId, selectedPayId);
                            // times of today by default onResume
                            getTimes(
                                    TimeUtil.getSdf("yyyy-MM-dd").format(System.currentTimeMillis()),
                                    TimeUtil.getSdf("EEEE").format(System.currentTimeMillis()).toLowerCase(),
                                    hashId
                            );
                        }
                    }
                }

                if(flag == 0) {
                    if (orderObj.has("address")) {
                        JSONObject addressObj = orderObj.getJSONObject("address");

                        String temp = addressObj.getString("address");
                        if(temp.equalsIgnoreCase(addressObj.getString("neighborhood"))){
                            temp = "";
                        }
                        else if(temp.equalsIgnoreCase(addressObj.getString("city"))){
                            temp = "";
                        }
                        else if(temp.equalsIgnoreCase(addressObj.getString("state"))){
                            temp = "";
                        }
                        else{
                            temp = temp + "\n";
                        }

                        String comp = addressObj.getString("complement");
                        if(!comp.isEmpty()){
                            comp = comp + "\n";
                        }

                        String ad = temp +
                                addressObj.getString("street_number") + "\n" +
                                comp +
                                addressObj.getString("neighborhood") + "\n" +
                                addressObj.getString("city") + "\n" +
                                addressObj.getString("state") + "\n" +
                                addressObj.getString("zip_code");

                        address.setText(ad);
                    }
                }

                //if(flag == 3) {
                    if (orderObj.has("product")) {
                        JSONObject productObj = orderObj.getJSONArray("product").getJSONObject(0);
                        String vessel = "0";
                        if (productObj.has("vessel"))
                            vessel = productObj.getString("vessel");

                        int quantity = 1;
                        if (productObj.has("quantity"))
                            quantity = Integer.parseInt(productObj.getString("quantity"));
                        Checkout.this.quantity.setText(String.valueOf(quantity));
                        double specialPrice = Double.parseDouble(PrefsUtil.getSpecialPrice(Checkout.this).replace(",", "."));
                        double containerPrice = Double.parseDouble(PrefsUtil.getContainerPrice(Checkout.this).replace(",", "."));
                        double totalPrice = 0;
                        if (vessel.equals("0")) {
                            LogUtil.loge("show Recarga");
                            Checkout.this.vessel.setText("Recarga");
                            totalPrice = quantity * specialPrice;
                        } else {
                            LogUtil.loge("show Completo");
                            Checkout.this.vessel.setText("Completo");
                            totalPrice = (quantity * specialPrice)+(quantity * containerPrice);
                        }
                        LogUtil.loge("quantity:" + quantity);
                        LogUtil.loge("vessel:" + vessel);
                        LogUtil.loge("specialPrice:" + specialPrice);
                        LogUtil.loge("containerPrice:" + containerPrice);
                        price.setText("R$" + Utils.roundDouble(totalPrice));
                    }
                //}

                if(flag == 1) {
                    vendorName.setText("POR: " + PrefsUtil.getSelectedVendor(Checkout.this));
                    productName.setText(PrefsUtil.getSelectedProduct(Checkout.this).split(" ")[0]);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getPayMethods(String hashId, final String selectedPayId) {
        //showLoader();
        HashMap<String, String> hashMap = new HashMap<>();
        String url = MainActivity.baseUrl + "/partner/" + hashId + "/means-of-payment";
        CustomRequest customRequest = new CustomRequest(Request.Method.GET, url, hashMap, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //hideLoader();
                Log.e("success", response.toString());
                try {
                    JSONArray data = response.getJSONArray("data");
                    payMethodIds.clear();
                    payMethodNames.clear();
                    for (int i = 0; i < data.length(); i++) {
                        payMethodIds.add(data.getJSONObject(i).getString("id"));
                        payMethodNames.add(data.getJSONObject(i).getString("name"));
                        if (selectedPayId.equals(data.getJSONObject(i).getString("id"))) {
                            paymentMethod.setText(data.getJSONObject(i).getString("name"));
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter(Checkout.this, android.R.layout.simple_spinner_dropdown_item, Utils.getArrayFromList(payMethodNames));
                    paymentMethod.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //hideLoader();
                Toast.makeText(Checkout.this, "Problema de Rede", Toast.LENGTH_SHORT).show();
                Log.e("error", error.toString());
            }
        });
        VolleyLibrary.getInstance(this).addToRequestQueue(customRequest, "", false);
    }

    private void getTimes(final String date, final String d, final String hashId) {
        HashMap<String, String> hashMap = new HashMap<>();
        String url = MainActivity.baseUrl + "/partner/" + hashId + "/time";
        CustomRequest customRequest = new CustomRequest(Request.Method.GET, url, hashMap, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("success", response.toString());
                try {
                    vendorTimeLimits = response.getJSONArray("data");
                    loadDayTimes(date, d);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.toString());
            }
        });
        VolleyLibrary.getInstance(this).addToRequestQueue(customRequest, "", false);
    }

    private void loadDayTimes(String date, String d) {
        tempDate = date;
        tempDay = d;
        try {
            for (int i = 0; i < vendorTimeLimits.length(); i++) {
                times.clear();
                String weekday = vendorTimeLimits.getJSONObject(i).getString("weekday").toLowerCase();
                if(tempDay.equalsIgnoreCase(weekday)){
                    LogUtil.loge("weekday: "+weekday);
                    // get times
                    String openTime = vendorTimeLimits.getJSONObject(i).getString("opening_time");
                    String closeTime = vendorTimeLimits.getJSONObject(i).getString("closing_time");
                    LogUtil.loge("openTime: "+openTime);
                    LogUtil.loge("closeTime: "+closeTime);
                    int openHour = Integer.parseInt(openTime.split(":")[0]);
                    int openMin = Integer.parseInt(openTime.split(":")[1]);
                    int closeHour = Integer.parseInt(closeTime.split(":")[0]);
                    int closeMin = Integer.parseInt(closeTime.split(":")[1]);
                    if(openMin > 0 && openMin <30)
                        openMin = 30;
                    else if(openMin > 30) {
                        openMin = 0;
                        openHour++;
                    }
                    if(closeMin > 0 && closeMin <30)
                        closeMin = 0;
                    else if(closeMin > 30) {
                        closeMin = 30;
                    }
                    for(int j=openHour; j<=closeHour; j++){
                        int temp = openMin;
                        if(j != openHour)
                            temp = 0;
                        times.add(TimeUtil.twoDigit(j)+":"+ TimeUtil.twoDigit(temp));
                        temp = temp + 30;
                        if(temp == 60) {
                            continue;
                        }
                        if(temp > closeMin && j==closeHour)
                            break;
                        times.add(TimeUtil.twoDigit(j)+":"+ TimeUtil.twoDigit(temp));
                    }
                    LogUtil.loge("timesSize: "+times.size());

                    // find next available time if TODAY else take 1st time from list
                    String today = TimeUtil.getSdf("yyyy-MM-dd").format(System.currentTimeMillis());
                    if(tempDate.equals(today)){
                        LogUtil.loge("today");
                        boolean found = false;
                        String currentTime = TimeUtil.getSdf("HH:mm").format(System.currentTimeMillis());
                        //String currentTime = "18:30:00";
                        for (String t : times) {
                            int cHour = Integer.parseInt(currentTime.split(":")[0]);
                            int cMin = Integer.parseInt(currentTime.split(":")[1]);
                            int tHour = Integer.parseInt(t.split(":")[0]);
                            int tMin = Integer.parseInt(t.split(":")[1]);
                            if(((tHour*60)+tMin) > ((cHour*60)+cMin)){
                                LogUtil.loge("found");
                                LogUtil.loge("c: "+currentTime);
                                LogUtil.loge("t: "+t);
                                // found
                                // show date and time
                                Checkout.this.date.setText(tempDate);
                                Checkout.this.time.setText(t);
                                //updateDate(Utils.txt(Checkout.this.date));
                                //updateTime(Utils.txt(Checkout.this.time));
                                found = true;
                                return;
                            }
                        }

                        if(!found){
                            LogUtil.loge("not found");
                            // next day
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DAY_OF_YEAR, 1);
                            tempDate = TimeUtil.getSdf("yyyy-MM-dd").format(calendar.getTimeInMillis());
                            tempDay = TimeUtil.getSdf("EEEE").format(calendar.getTimeInMillis()).toLowerCase();
                            LogUtil.loge("tempDate: "+tempDate);
                            LogUtil.loge("tempDay: "+tempDay);
                            loadDayTimes(tempDate, tempDay);
                        }
                    }
                    else{
                        LogUtil.loge("future day");
                        // show date and time
                        Checkout.this.date.setText(tempDate);
                        if(times.size() > 0)
                            Checkout.this.time.setText(times.get(0));
                        //updateDate(Utils.txt(Checkout.this.date));
                        //updateTime(Utils.txt(Checkout.this.time));
                        return;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.calendar, R.id.vesselPanel, R.id.quantityPanel, R.id.submit})
    public void onViewClicked(View view) {
        Utils.clickEffect(view);
        switch (view.getId()) {
            case R.id.calendar:
                TimeUtil.showDatePicker(this, System.currentTimeMillis(), 0, new TimeUtil.MyDatePicker() {
                    @Override
                    public void onDateSelect(long date) {
                        loadDayTimes(
                                TimeUtil.getSdf("yyyy-MM-dd").format(date),
                                TimeUtil.getSdf("EEEE").format(date).toLowerCase()
                        );

                        final String date_ = TimeUtil.getSdf("yyyy-MM-dd").format(date);
                        final Dialog dialog = PopupUtil.showCustomPopup(Checkout.this, R.layout.pick_time);
                        final BetterSpinner spinner = dialog.findViewById(R.id.time);
                        LogUtil.loge("timesAdapter: "+times.size());
                        ArrayAdapter<String> adapter = new ArrayAdapter(Checkout.this, android.R.layout.simple_spinner_dropdown_item, Utils.getArrayFromList(times));
                        spinner.setAdapter(adapter);
                        spinner.setText(Checkout.this.time.getText().toString());
                        Button done = dialog.findViewById(R.id.done);
                        done.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Utils.clickEffect(v);
                                if(spinner.getText().toString().isEmpty()){
                                    spinner.setError("Required");
                                    return;
                                }
                                String time_ = spinner.getText().toString();
                                try {
                                    Date date = TimeUtil.getSdf("yyyy-MM-dd HH:mm").parse(date_+" "+time_);
                                    if(date.getTime() <= System.currentTimeMillis()){
                                        Toast.makeText(Checkout.this, "Selecione Horario Futuro", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Checkout.this.date.setText(date_);
                                    Checkout.this.time.setText(spinner.getText().toString());
                                    //updateDate(date_);
                                    //updateTime(spinner.getText().toString());
                                    dialog.dismiss();
                                }
                                catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                break;
            case R.id.vesselPanel:
                if (vesselFlag == 0) {
                    vesselFlag = 1;
                    vessel.setText("Completo");
                    updateVessel("1");
                } else if (vesselFlag == 1) {
                    vesselFlag = 0;
                    vessel.setText("Recarga");
                    updateVessel("0");
                }
                break;
            case R.id.quantityPanel:
                quantity.setEnabled(true);
                quantity.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(quantity, InputMethodManager.SHOW_IMPLICIT);
                break;
            case R.id.submit:
                if(name.getText().toString().isEmpty()){
                    name.setError("Obrigatorio");
                    return;
                }
                if(telefone.getText().toString().isEmpty()){
                    telefone.setError("Obrigatorio");
                    return;
                }
                if(!ValidUtil.checkNumberOnly(Utils.txt(telefone))){
                    telefone.setError("Inser apenas numeros");
                    return;
                }
                String number  = telefone.getText().toString();
                if(number.length() < 10 || number.length() > 11){
                    telefone.setError("Inserir DDD sem zero");
                    return;
                }
                if(paymentMethod.getText().toString().isEmpty()){
                    paymentMethod.setError("Obrigatorio");
                    return;
                }
                if(date.getText().toString().isEmpty()){
                    Toast.makeText(Checkout.this, "Date Obrigatorio!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(time.getText().toString().isEmpty()){
                    Toast.makeText(Checkout.this, "Time Obrigatorio!", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateDate(Utils.txt(date));
                updateTime(Utils.txt(time));
                updateOrder(Utils.txt(name), Utils.txt(telefone), payMethodIds.get(payMethodNames.indexOf(paymentMethod.getText().toString())), firebase_token);
                updateQuantity(Utils.txt(quantity));
                updateVessel(String.valueOf(vesselFlag));

                JSONObject orderObj = PrefsUtil.getOrderObj(this);
                if (orderObj != null) {
                    LogUtil.loge("orderObj: " + orderObj.toString());
                    // place order now
                    placeOrder(orderObj);
                }
                break;
        }
    }

    private void placeOrder(JSONObject orderObj) {
        showLoader();
        String url = MainActivity.baseUrl+"/partner/order";
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, orderObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        LogUtil.loge("response: "+response.toString());
                        hideLoader();
                        try {
                            if(response.has("return_message")) {
                                String returnMsg = response.getString("return_message");
                                Toast.makeText(Checkout.this, ""+returnMsg, Toast.LENGTH_LONG).show();
                                return;
                            }
                            int status = response.getInt("status_code");
                            if(status > 0) {
                                PrefsUtil.setEvalDone(Checkout.this, false);
                                PrefsUtil.setOrderNumber(Checkout.this, response.getJSONObject("data").getString("order_number"));
                                Toast.makeText(Checkout.this, "Pedido Enviado com Sucesso", Toast.LENGTH_SHORT).show();
                                Intent success = new Intent(Checkout.this, Success.class);
                                startActivity(success);
                            }
                            else{
                                Toast.makeText(Checkout.this, "Erro no Pedido", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Checkout.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoader();
                        Toast.makeText(Checkout.this, "Problema de Rede", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json");
                        headers.put("gd-group-hash", MainActivity.gdGroupHash);
                        return headers;
                    }
        };
        // Adding request to request queue
        Volley.newRequestQueue(this).add(jsonObjReq);
    }

    private void updateVessel(String val) {
        JSONObject orderObj = PrefsUtil.getOrderObj(this);
        if (orderObj != null) {
            try {
                orderObj.getJSONArray("product").getJSONObject(0).put("vessel", val);
                PrefsUtil.setOrderObj(Checkout.this, orderObj.toString());
                loadData(3);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateQuantity(String val) {
        JSONObject orderObj = PrefsUtil.getOrderObj(this);
        if (orderObj != null) {
            try {
                orderObj.getJSONArray("product").getJSONObject(0).put("quantity", val);
                PrefsUtil.setOrderObj(Checkout.this, orderObj.toString());
                loadData(3);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateDate(String val) {
        JSONObject orderObj = PrefsUtil.getOrderObj(this);
        if (orderObj != null) {
            try {
                orderObj.getJSONObject("order").put("date", val);
                PrefsUtil.setOrderObj(Checkout.this, orderObj.toString());
                loadData(2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateTime(String val) {
        JSONObject orderObj = PrefsUtil.getOrderObj(this);
        if (orderObj != null) {
            try {
                orderObj.getJSONObject("order").put("time", val);
                PrefsUtil.setOrderObj(Checkout.this, orderObj.toString());
                loadData(2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateOrder(String name, String phone, String payId, String firebase_token) {
        JSONObject orderObj = PrefsUtil.getOrderObj(this);
        if (orderObj != null) {
            try {
                orderObj.getJSONObject("order").put("full_name", name);
                orderObj.getJSONObject("order").put("phone", phone);
                orderObj.getJSONObject("order").put("mean_of_payment_id", payId);
                orderObj.getJSONObject("order").put("firebase_token", firebase_token);
                PrefsUtil.setOrderObj(Checkout.this, orderObj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
