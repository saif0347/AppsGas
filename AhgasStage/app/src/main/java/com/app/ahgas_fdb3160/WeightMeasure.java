package com.app.ahgas_fdb3160;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.app.ahgas_fdb3160.scan.DeviceDialog;
import com.app.ahgas_fdb3160.util.T;
import aicare.net.cn.iweightlibrary.bleprofile.BleProfileService;
import aicare.net.cn.iweightlibrary.bleprofile.BleProfileServiceReadyActivity;
import aicare.net.cn.iweightlibrary.entity.AlgorithmInfo;
import aicare.net.cn.iweightlibrary.entity.BM09Data;
import aicare.net.cn.iweightlibrary.entity.BM15Data;
import aicare.net.cn.iweightlibrary.entity.BodyFatData;
import aicare.net.cn.iweightlibrary.entity.BroadData;
import aicare.net.cn.iweightlibrary.entity.DecimalInfo;
import aicare.net.cn.iweightlibrary.entity.User;
import aicare.net.cn.iweightlibrary.entity.WeightData;
import aicare.net.cn.iweightlibrary.utils.AicareBleConfig;
import aicare.net.cn.iweightlibrary.utils.L;
import aicare.net.cn.iweightlibrary.wby.WBYService;
import butterknife.BindView;
import butterknife.ButterKnife;

public class WeightMeasure extends BleProfileServiceReadyActivity implements DeviceDialog.OnDeviceScanListener {
    @BindView(R.id.weight)
    TextView weight;
    @BindView(R.id.home)
    Button home;
    @BindView(R.id.loader)
    ImageView loader;
    private WBYService.WBYBinder binder;
    private User user = null;
    private byte unit = AicareBleConfig.UNIT_KG;
    private boolean isNewBM15TestData;
    private BroadData cacheBroadData;
    private String TAG = "WeightMeasure";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_measure);
        ButterKnife.bind(this);

        initData();
        initEvents();

        L.isDebug = true;

        if (!ensureBLESupported()) {
            T.showShort(this, R.string.not_support_ble);
            finish();
            return;
        }
        if (!isBLEEnabled()) {
            showBLEDialog();
            return;
        }

        startScanning();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.binder == null) {
            bindService(null);
        }
    }

    private void initData() {
        user = new User(1, 2, 28, 170, 768, 551);
    }

    private void initEvents() {
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        unit = AicareBleConfig.UNIT_KG;
        if(binder != null)
            binder.syncUnit(AicareBleConfig.UNIT_KG);
    }

    private void setDefault() {
        weight.setText("0");
    }

    private void startScanning(){
        if (!isBLEEnabled()) {
            showBLEDialog();
        } else {
            if (isDeviceConnected()) {
                Toast.makeText(WeightMeasure.this, "Device already connected!", Toast.LENGTH_SHORT).show();
                //binder.disconnect();
            } else {
                if (cacheBroadData == null) {
                    showDialog();
                    devicesDialog.startScan();
                } else {
                    cacheBroadData = null;
                    stopLeScan();
                }
            }
        }
    }

    private void syncUser(){
        if (isDeviceConnected()) {
            if(binder != null)
                binder.syncUser(user);
            else
                Toast.makeText(WeightMeasure.this, "Service Binder Error!", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(WeightMeasure.this, "Device not connected yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private DeviceDialog devicesDialog;

    private void showDialog() {
        if (devicesDialog == null) {
            devicesDialog = new DeviceDialog(this, this);
            devicesDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    hideDialog();
                }
            });
        }
        devicesDialog.show();
    }

    private void hideDialog() {
        if (devicesDialog != null) {
            devicesDialog.dismiss();
            devicesDialog = null;
        }
    }

    @Override
    protected void onServiceBinded(WBYService.WBYBinder binder) {
        this.binder = binder;
        this.binder.syncUnit(AicareBleConfig.UNIT_KG);
    }

    @Override
    protected void onServiceUnbinded() {
        this.binder = null;
    }

    @Override
    protected void getAicareDevice(final BroadData broadData) {
        if (broadData != null) {
            L.e(TAG, broadData.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (devicesDialog != null && devicesDialog.isShowing()) {
                        devicesDialog.setDevice(broadData);
                    }
                    if (cacheBroadData != null && TextUtils.equals(cacheBroadData.getAddress(), broadData.getAddress())) {
                        if (broadData.getDeviceType() == AicareBleConfig.BM_09) {
                            if (broadData.getSpecificData() != null) {
                                BM09Data data = AicareBleConfig.getBm09Data(broadData.getAddress(), broadData.getSpecificData());
                                if (isNewData(data) && data.getWeight() != 0) {
                                    showInfo(data.toString());
                                }
                            }
                        } else if (broadData.getDeviceType() == AicareBleConfig.BM_15) {
                            if (broadData.getSpecificData() != null) {
                                BM15Data data = AicareBleConfig.getBm15Data(broadData.getAddress(), broadData.getSpecificData());
                                WeightData weightData = new WeightData();
                                weightData.setWeight(data.getWeight());
                                weightData.setTemp(data.getTemp());
                                weightData.setAdc(data.getAdc());
                                weightData.setCmdType(data.getAgreementType());
                                weightData.setDeviceType(AicareBleConfig.BM_15);
                                switch (data.getUnitType()) {
                                    case 1:
                                    case 2:
                                    case 3:
                                        weightData.setDecimalInfo(new DecimalInfo(1, 1, 1, 1, 1, 2));
                                        break;
                                    case 4:
                                    case 5:
                                    case 6:
                                        weightData.setDecimalInfo(new DecimalInfo(2, 1, 1, 1, 1, 2));
                                        break;
                                }
                                onGetWeightData(weightData);
                            }
                        } else {
                            if (broadData.getSpecificData() != null) {
                                WeightData weightData = AicareBleConfig.getWeightData(broadData.getSpecificData());
                                onGetWeightData(weightData);
                            }
                        }
                    }
                }
            });
        }
    }

    private BM09Data bm09Data;

    private boolean isNewData(BM09Data data) {
        if (bm09Data == null) {
            bm09Data = data;
            return true;
        }
        if (bm09Data.getWeight() != data.getWeight()) {
            bm09Data = data;
            return true;
        }
        return false;
    }
//
//    private BM15Data bm15Data;
//
//    private boolean isNewBM15Data(BM15Data data) {
//        if (bm15Data == null) {
//            bm15Data = data;
//            return true;
//        }
//        if (bm15Data.getWeight() != data.getWeight() || bm15Data.getAdc() != data.getAdc() || bm15Data.getTemp() != data.getTemp() || bm15Data.getAgreementType() != data.getAgreementType()) {
//            bm15Data = data;
//            return true;
//        }
//        return false;
//    }

    @Override
    protected void onDestroy() {
        stopScan();
        if (isDeviceConnected()) {
            this.binder.disconnect();
        }
        super.onDestroy();
    }

    private void startLeScan() {
        startScan();
    }

    private void stopLeScan() {
        stopScan();
    }

    @Override
    public void scan() {
        startScan();
        devicesDialog.setScanning(true);
    }

    @Override
    public void stop() {
        stopScan();
        devicesDialog.setScanning(false);
    }

    @Override
    public void connect(BroadData device) {
        if (device.getDeviceType() == AicareBleConfig.TYPE_WEI_BROAD || device.getDeviceType() == AicareBleConfig.TYPE_WEI_TEMP_BROAD || device.getDeviceType() == AicareBleConfig.BM_09 || device.getDeviceType() == AicareBleConfig.BM_15) {
            cacheBroadData = device;
            showState(-1);
            startLeScan();
        } else {
            startConnect(device.getAddress());
        }
    }

    @Override
    public void onStateChanged(String deviceAddress, int state) {
        super.onStateChanged(deviceAddress, state);
        switch (state) {
            case BleProfileService.STATE_CONNECTED:
                showInfo("state_connected");
                Toast.makeText(WeightMeasure.this, "Connected with the device", Toast.LENGTH_SHORT).show();
                showState(state);
                syncUser();
                break;
            case BleProfileService.STATE_DISCONNECTED:
                showInfo("state_disconnected");
                showState(state);
                Toast.makeText(WeightMeasure.this, "Disconnected with the device", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BleProfileService.STATE_SERVICES_DISCOVERED:
                showInfo("state_service_discovered");
                break;
            case BleProfileService.STATE_INDICATION_SUCCESS:
                showInfo("state_indication_success");
                break;
            case BleProfileService.STATE_TIME_OUT:
                showInfo("state_time_out");
                break;
            case BleProfileService.STATE_CONNECTING:
                showInfo("state_connecting");
                break;
        }
    }

    private void showInfo(String str) {
        L.e(TAG, "info: "+str);
    }

    private void showState(final int state) {
        switch (state) {
            case BleProfileService.STATE_CONNECTED:
                L.e(TAG, "STATE_CONNECTED");
                break;
            case BleProfileService.STATE_DISCONNECTED:
                L.e(TAG, "STATE_DISCONNECTED");
                setDefault();
                break;
            case -1:
                L.e(TAG, "-1 unbound");
                break;
        }
    }

    @Override
    public void onError(final String errMsg, final int errCode) {
        L.e(TAG, "Message = " + errMsg + " errCode = " + errCode);
        showInfo(""+errMsg);
    }

    @Override
    public void onGetWeightData(final WeightData weightData) {
        if (weightData == null) return;
        L.e(TAG, weightData.toString());
        setWeighDataText(AicareBleConfig.getWeight(weightData.getWeight(), unit, weightData.getDecimalInfo()));
        if (weightData.getDeviceType() == AicareBleConfig.BM_15) {
            if (weightData.getCmdType() != 3) {
                isNewBM15TestData = true;
                showInfo(weightData.toString());
            }
            if (weightData.getCmdType() == 3 && weightData.getAdc() > 0 && isNewBM15TestData) {
                isNewBM15TestData = false;
                BodyFatData bodyFatData = AicareBleConfig.getBM15BodyFatData(weightData, user.getSex(), user.getAge(), user.getHeight());
                showInfo(bodyFatData.toString());
            }
        }
    }

    private void setWeighDataText(String weight) {
        double w = Double.parseDouble(weight);
        double result = (w - 14.5)/13;
        if(result > 1){
            result = 1;
        }
        else if(result < 0){
            result = 0;
        }
        int percentage = (int)(result*100);
        this.weight.setText(percentage+"%");
    }

    @Override
    public void onGetSettingStatus(@AicareBleConfig.SettingStatus int status) {
        L.e(TAG, "SettingStatus = " + status);
        switch (status) {
            case AicareBleConfig.SettingStatus.NORMAL:
                showInfo("NORMAL");
                break;
            case AicareBleConfig.SettingStatus.LOW_POWER:
                showInfo("LOW_POWER");
                break;
            case AicareBleConfig.SettingStatus.LOW_VOLTAGE:
                showInfo("LOW_VOLTAGE");
                break;
            case AicareBleConfig.SettingStatus.ERROR:
                showInfo("ERROR");
                break;
            case AicareBleConfig.SettingStatus.TIME_OUT:
                showInfo("TIME_OUT");
                break;
            case AicareBleConfig.SettingStatus.SET_UNIT_SUCCESS:
                showInfo("SET_UNIT_SUCCESS");
                break;
            case AicareBleConfig.SettingStatus.SET_UNIT_FAILED:
                showInfo("SET_UNIT_FAILED");
                break;
            case AicareBleConfig.SettingStatus.SET_USER_SUCCESS:
                showInfo("SET_USER_SUCCESS");
                break;
            case AicareBleConfig.SettingStatus.SET_USER_FAILED:
                showInfo("SET_USER_FAILED");
                break;
            case AicareBleConfig.SettingStatus.ADC_MEASURED_ING:
                showInfo("ADC_MEASURED_ING");
                break;
            case AicareBleConfig.SettingStatus.ADC_ERROR:
                showInfo("ADC_ERROR");
                break;
        }
    }

    @Override
    public void onGetResult(final int index, final String result) {
        L.e(TAG, "index = " + index + "; result = " + result);
    }


    @Override
    public void onGetFatData(boolean isHistory, final BodyFatData bodyFatData) {
        L.e(TAG, "isHistory = " + isHistory + "; BodyFatData = " + bodyFatDataToString(bodyFatData));
        showInfo(bodyFatDataToString(bodyFatData));
    }

    @Override
    protected void onGetDecimalInfo(DecimalInfo decimalInfo) {
        if (decimalInfo == null) return;
        L.e(TAG, decimalInfo.toString());
    }

    @Override
    protected void onGetAlgorithmInfo(AlgorithmInfo algorithmInfo) {
        if (algorithmInfo == null)
            return;
        showInfo("algorithmInfo Adc: "+algorithmInfo.getAdc());
    }

    @Override
    protected void bluetoothStateChanged(int state) {
        super.bluetoothStateChanged(state);
        switch (state) {
            case BluetoothAdapter.STATE_ON:
                startScanning();
                break;
            case BluetoothAdapter.STATE_OFF:
                Toast.makeText(WeightMeasure.this, "Bluetooth is OFF", Toast.LENGTH_SHORT).show();
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                break;
        }
    }

    public String bodyFatDataToString(BodyFatData bodyFatData) {
        if (bodyFatData == null) {
            return "";
        }
        return "BodyFatData{date='" + bodyFatData.getDate() + '\'' + ", time='" + bodyFatData.getTime() + '\'' + ", weight=" + AicareBleConfig.getWeight(bodyFatData.getWeight(), unit, bodyFatData.getDecimalInfo())
                + ", bmi=" + bodyFatData.getBmi() + ", bfr=" + bodyFatData.getBfr() + ", sfr=" + bodyFatData.getSfr() + ", uvi=" + bodyFatData.getUvi()
                + ", rom=" + bodyFatData.getRom() + ", bmr=" + bodyFatData.getBmr() + ", bm=" + bodyFatData.getBm() + ", vwc=" + bodyFatData.getVwc()
                + ", bodyAge=" + bodyFatData.getBodyAge() + ", pp=" + bodyFatData.getPp() + ", number=" + bodyFatData.getNumber() + ", sex=" + bodyFatData.getSex()
                + ", age=" + bodyFatData.getAge() + ", height=" + bodyFatData.getHeight() + ", adc=" + bodyFatData.getAdc() + ", decimalInfo=" + bodyFatData.getDecimalInfo().toString() + '}';
    }
}

