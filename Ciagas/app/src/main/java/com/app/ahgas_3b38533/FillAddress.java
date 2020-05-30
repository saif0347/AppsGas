package com.app.ahgas_3b38533;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.app.ahgas_3b38533.util.PrefsUtil;
import com.app.ahgas_3b38533.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FillAddress extends AppCompatActivity {
    @BindView(R.id.address)
    EditText address;
    @BindView(R.id.myLocation)
    LinearLayout myLocation;
    @BindView(R.id.streetNo)
    EditText streetNo;
    @BindView(R.id.complement)
    EditText complement;
    @BindView(R.id.submit)
    Button submit;
    @BindView(R.id.zipCode)
    EditText zipCode;
    @BindView(R.id.neighborhood)
    EditText neighborhood;
    @BindView(R.id.city)
    EditText city;
    @BindView(R.id.state)
    EditText state;
    @BindView(R.id.scrollView)
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_address);
        ButterKnife.bind(this);

        JSONObject orderObj = PrefsUtil.getOrderObj(this);
        if (orderObj != null) {
            try {
                address.setText(orderObj.getJSONObject("address").getString("address"));
                streetNo.setText(orderObj.getJSONObject("address").getString("street_number"));
                zipCode.setText(orderObj.getJSONObject("address").getString("zip_code"));
                neighborhood.setText(orderObj.getJSONObject("address").getString("neighborhood"));
                city.setText(orderObj.getJSONObject("address").getString("city"));
                state.setText(orderObj.getJSONObject("address").getString("state"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.submit)
    public void onViewClicked() {
        Utils.clickEffect(submit);
        if(address.getText().toString().isEmpty()){
            address.setError("Obrigatorio");
            return;
        }
        if (streetNo.getText().toString().isEmpty()) {
            streetNo.setError("Obrigatorio");
            return;
        }
        if (zipCode.getText().toString().isEmpty()) {
            zipCode.setError("Obrigatorio");
            return;
        }
        if (neighborhood.getText().toString().isEmpty()) {
            neighborhood.setError("Obrigatorio");
            return;
        }
        if (city.getText().toString().isEmpty()) {
            city.setError("Obrigatorio");
            return;
        }
        if (state.getText().toString().isEmpty()) {
            state.setError("Obrigatorio");
            return;
        }

        // save data
        JSONObject orderObj = PrefsUtil.getOrderObj(this);
        if (orderObj != null) {
            try {
                JSONObject addressObj = orderObj.getJSONObject("address");
                addressObj.put("address", Utils.txt(address));
                addressObj.put("street_number", Utils.txt(streetNo));
                addressObj.put("zip_code", Utils.txt(zipCode));
                addressObj.put("neighborhood", Utils.txt(neighborhood));
                addressObj.put("city", Utils.txt(city));
                addressObj.put("state", Utils.txt(state));
                addressObj.put("complement", Utils.txt(complement));

                PrefsUtil.setOrderObj(FillAddress.this, orderObj.toString());

                Intent selectProduct = new Intent(this, SelectProduct.class);
                startActivity(selectProduct);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
