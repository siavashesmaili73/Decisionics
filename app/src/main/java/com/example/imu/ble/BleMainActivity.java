package com.example.imu.ble;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.example.imu.MainActivity;
import com.example.imu.R;
import com.example.imu.ShareDataHelper;
import com.example.imu.ble.adapter.DeviceAdapter;
import com.example.imu.ble.comm.ObserverManager;
import com.example.imu.ble.operation.OperationActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
/*import android.os.Build;

settings.editBleConnParams()
        .maxConnectionInterval(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 11.25f : 7.5f)
        .commit();*/

public class BleMainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_TEXT = "com.example.imu.ble.EXTRA_TEXT"  ;
    public static final double PI = 3.141592653589793;
    Button alignment;
    Button spatiotemporal ;
    public  ArrayList<Entry> yaws = new ArrayList<>() ;
    public  ArrayList<Entry> rolls = new ArrayList<>() ;
    public  ArrayList<Entry> pitches = new ArrayList<>() ;

    public  ArrayList<Entry> yaws_left = new ArrayList<>() ;
    public  ArrayList<Entry> rolls_left = new ArrayList<>() ;
    public  ArrayList<Entry> pitches_left = new ArrayList<>() ;


    public ArrayList<Float> counter = new ArrayList<Float>() ;
    public ArrayList<Float> counter_left = new ArrayList<Float>() ;
    private LineChart mChart;
    private static final String TAG = BleMainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private LinearLayout layout_setting;
    private TextView txt_setting;
    private Button btn_scan;
    private EditText et_name, et_mac, et_uuid;
    private Switch sw_auto;
    private ImageView img_loading;
    private Animation operatingAnim;
    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog progressDialog;
    private Button storeDataButton;
    private boolean shouldStoreData = false;

    //////////orientation implementation

    // angular speeds from gyro
    public float[] gyro = new float[3]; public float[] gyro_left = new float[3];

    // rotation matrix from gyro data
    public float[]  gyroMatrix = new float[9]; public float[]  gyroMatrix_left = new float[9];

    // orientation angles from gyro matrix
    public float[] gyroOrientation = new float[3]; public float[] gyroOrientation_left = new float[3];

    // magnetic field vector
    public float[] magnet = new float[3]; public float[] magnet_left = new float[3];

    // perssure sensor
    public float[] pressure = new float[3]; public float[] pressure_left = new float[3];



    // accelerometer vector
    private float[] accel = new float[3]; public float[] accel_left= new float[3];
    private float[] global_accel = new float[3]; public float[] global_accel_left = new float[3];
    private float[] linear_acceleration = new float[3] ; private float[] linear_acceleration_left = new float[3] ;


    public float[] right_data = new float[12] ;
    public float[] left_data = new float[12] ;


    ////////////////////string for the internal storage file

    private StringBuilder rightFootDataBuilder = new StringBuilder();
    private StringBuilder leftFootDataBuilder = new StringBuilder();
    private StringBuilder spatiotemporalDataBuilder = new StringBuilder();
    private StringBuilder accelerationDataBuilder = new StringBuilder();

    private long first_time= 0;

    // orientation angles from accel and magnet
    public float[] transpose(float[] rotationMatrix) {
        float[] original = new float[9] ;
        original[0] = rotationMatrix[0];
        original[1] = rotationMatrix[3];
        original[2] = rotationMatrix[6];
        original[3] = rotationMatrix[1];
        original[4] = rotationMatrix[4];
        original[5] = rotationMatrix[7];
        original[6] = rotationMatrix[2];
        original[7] = rotationMatrix[5];
        original[8] =  rotationMatrix[8];

        return original ;

    }


    public float[] accMagOrientation = new float[3]; public float[] accMagOrientation_left = new float[3];

    // final orientation angles from sensor fusion
    public float[] fusedOrientation = new float[3]; public float[] fusedOrientation_left = new float[3];

    // accelerometer and magnetometer based rotation matrix
    public  float[] rotationMatrix = new float[9]; public float[] rotationMatrix_left = new float[9];


    public static final int TIME_CONSTANT = 30;
    public static final float FILTER_COEFFICIENT = 0.98f;
    public Timer fuseTimer = new Timer(); public Timer fuseTimer_left = new Timer();
    public float position1 = 0, position2;public float position1_left = 0, position2_left;
    public float velocity1 = 0 , velocity2; public float velocity1_left = 0 , velocity2_left;
    long timer1 = new Date().getTime(); long timer1_left = new Date().getTime();


    int repeatCounter = 0;
    int repeatCounter1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_activity_main);
        initView();
        alignment = findViewById(R.id.alignment);
        spatiotemporal = findViewById(R.id.spatiotemporal);

        requestPerm();

        alignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BleMainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        storeDataButton.setOnClickListener(v -> {
            if (shouldStoreData) {
                shouldStoreData = false;
                storeDataButton.setText(getString(R.string.store_data_start));
                Log.i("DATA-F", rightFootDataBuilder.toString());
                String finalData = rightFootDataBuilder.toString() +"\n\n" + leftFootDataBuilder.toString()+"\n\n" + spatiotemporalDataBuilder.toString()+"\n\n"+ accelerationDataBuilder;
                ShareDataHelper.shareData(BleMainActivity.this, finalData);
                rightFootDataBuilder = new StringBuilder();
                leftFootDataBuilder = new StringBuilder();
                spatiotemporalDataBuilder = new StringBuilder();
                accelerationDataBuilder = new StringBuilder();
            } else {
                first_time = new Date().getTime();
                shouldStoreData = true;
                storeDataButton.setText(getString(R.string.store_data_stop));
                rightFootDataBuilder.append("\n");

                rightFootDataBuilder.append("Time,Accel_right,Gyro_right,Magnet_right(micro Tesla),Pressure_right\n");

                leftFootDataBuilder.append("\n");
                leftFootDataBuilder.append("------------------------------------------------------------------------------------------------------------------\n");
                leftFootDataBuilder.append("Sensor: Left foot  # ");
                leftFootDataBuilder.append("\n\n");
                leftFootDataBuilder.append("Time\t\tAccel_left\t\tGyro_left\t\tMagnet_left\t\tPressure_left\n");

                spatiotemporalDataBuilder.append("\n");
                spatiotemporalDataBuilder.append("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                leftFootDataBuilder.append("temporal  # ");
                spatiotemporalDataBuilder.append("\n\n");
                spatiotemporalDataBuilder.append("Time \t\t step_time \t\t stride_time_right \t\t stride_time_left \t\t stance_time_right\t\t stance_time_left\t\t swing_time_right\t\t swing_time_left\t\t cadance\n");


                accelerationDataBuilder.append("\n");
                accelerationDataBuilder.append("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                accelerationDataBuilder.append("linear acc # ");
                accelerationDataBuilder.append("\n\n");
                accelerationDataBuilder.append("Time \t\t linear_acc_right \t\tlinear_acc_left");
            }
       });

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

        mChart = findViewById(R.id.linechart2) ;

        //////////// orientation

        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;

        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                1000, TIME_CONSTANT);

    }

    @Override
    protected void onResume() {
        super.onResume();
        showConnectedDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (btn_scan.getText().equals(getString(R.string.start_scan))) {
                    checkPermissions();
                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) {
                    BleManager.getInstance().cancelScan();
                }
                break;

            case R.id.txt_setting:
/*                if (layout_setting.getVisibility() == View.VISIBLE) {
                    layout_setting.setVisibility(View.GONE);
                    txt_setting.setText(getString(R.string.expand_search_settings));
                } else {
                    layout_setting.setVisibility(View.VISIBLE);
                    txt_setting.setText(getString(R.string.retrieve_search_settings));
                }*/
                break;
        }
    }

    private void initView() {


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_scan.setText(getString(R.string.start_scan));
        btn_scan.setOnClickListener(this);
        et_name = (EditText) findViewById(R.id.et_name);

        et_mac = (EditText) findViewById(R.id.et_mac);
        et_uuid = (EditText) findViewById(R.id.et_uuid);
        sw_auto = (Switch) findViewById(R.id.sw_auto);
        storeDataButton = findViewById(R.id.store_data);
        layout_setting = (LinearLayout) findViewById(R.id.layout_setting);
        txt_setting = (TextView) findViewById(R.id.txt_setting);
        txt_setting.setOnClickListener(this);
        layout_setting.setVisibility(View.GONE);
        txt_setting.setText(getString(R.string.expand_search_settings));

        img_loading = (ImageView) findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        progressDialog = new ProgressDialog(this);

        mDeviceAdapter = new DeviceAdapter(this);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);


                }
            }

            @Override
            public void onDisConnect(final BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().disconnect(bleDevice);

                }
            }

            @Override
            public void onDetail(BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    Intent intent = new Intent(BleMainActivity.this, OperationActivity.class);
                    intent.putExtra(OperationActivity.KEY_DATA, bleDevice);
                    startActivity(intent);
                }
            }


        });
        ListView listView_device = (ListView) findViewById(R.id.list_device);
        listView_device.setAdapter(mDeviceAdapter);


    }

    private void showConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {

            mDeviceAdapter.addDevice(bleDevice);
        }
        mDeviceAdapter.notifyDataSetChanged();
    }

    private void setScanRule() {
        String[] uuids;
        String str_uuid = et_uuid.getText().toString();
        if (TextUtils.isEmpty(str_uuid)) {
            uuids = null;
        } else {
            uuids = str_uuid.split(",");
        }
        UUID[] serviceUuids = null;
        if (uuids != null && uuids.length > 0) {
            serviceUuids = new UUID[uuids.length];
            for (int i = 0; i < uuids.length; i++) {
                String name = uuids[i];
                String[] components = name.split("-");
                if (components.length != 5) {
                    serviceUuids[i] = null;
                } else {
                    serviceUuids[i] = UUID.fromString(uuids[i]);
                }
            }
        }

        String[] names;
        String str_name = et_name.getText().toString();
        if (TextUtils.isEmpty(str_name)) {
            names = null;
        } else {
            names = str_name.split(",");
        }

        String mac = et_mac.getText().toString();
        boolean isAutoConnect = sw_auto.isChecked();

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()

                .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
                .setDeviceName(true, names)   // 只扫描指定广播名的设备，可选
                .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private void startScan() {

        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);
                btn_scan.setText(getString(R.string.stop_scan));
            }


            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
            }
        });
    }

    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
                progressDialog.dismiss();
                Toast.makeText(BleMainActivity.this, getString(R.string.connect_fail), Toast.LENGTH_LONG).show();
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    gatt.requestConnectionPriority(1);
                }

                //we can start to look for services now
                List<BluetoothGattService> services = gatt.getServices();

                for (BluetoothGattService service : services) {
                    if (Constants.BLE_SERVICE_UUID.equals(service.getUuid().toString()) || Constants.BLE_SERVICE_UUID1.equals(service.getUuid().toString())) {
                        getCharacteristics(bleDevice, service);
                        /*getCharacteristics1(bleDevice, service);*/
                    }
                }
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                if (isActiveDisConnected) {
                    Toast.makeText(BleMainActivity.this, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(BleMainActivity.this, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
                    ObserverManager.getInstance().notifyObserver(bleDevice);
                }

            }
        });
    }

    float magnitude_of_acceleration_0 = 0 ; float magnitude_of_acceleration_0_left = 0 ;
    float time0 = 0 ;
    /*StringBuilder step_lenghts = new StringBuilder();*/
    private float  step_lenghts = 0;private float  step_lenghts_left = 0;
    private void getCharacteristics(BleDevice bleDevice, BluetoothGattService service) {


        List<BluetoothGattCharacteristic> list = service.getCharacteristics();
        BluetoothGattCharacteristic characteristic = list.get(repeatCounter);

            BleManager.getInstance().notify(
                    bleDevice,
                    characteristic.getService().getUuid().toString(),
                    characteristic.getUuid().toString(),
                    new BleNotifyCallback() {

                        @Override
                        public void onNotifySuccess() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //addText(txt, "notify success");
                                    //Log.i("notify", "notify success");
                                }
                            });
                        }

                        @Override
                        public void onNotifyFailure(final BleException exception) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //addText(txt, exception.toString());
                                }
                            });
                        }
                        boolean firstValAdded = false;
                        @Override
                        public void onCharacteristicChanged(byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("frequency", "freq") ;

                                    if (characteristic.getUuid().toString().equals("19b10001-e8f2-537e-4f6c-d104768a1214")) {

                                        if (shouldStoreData) {
                                            String accelVal =  (new Date().getTime() - first_time)/1000.0 +"\t\t"+ ((imuvalues(characteristic.getStringValue(0), 0)[0])+","+" " +((imuvalues(characteristic.getStringValue(0), 0)[1])+","+" " + ((imuvalues(characteristic.getStringValue(0), 0)[2]))));
                                            if (service.getUuid().toString().equals(Constants.BLE_SERVICE_UUID)) {
                                                rightFootDataBuilder.append(accelVal);
                                                accel = imuvalues(characteristic.getStringValue(0), 0);
                                            }
                                            else {
                                                leftFootDataBuilder.append(accelVal);
                                                accel_left = imuvalues(characteristic.getStringValue(0), 0);
                                            }
                                        }
                                    } else if (characteristic.getUuid().toString().equals("19b10011-e8f2-537e-4f6c-d104768a1214")) {

                                        gyroFunction(gyro);
                                        if (shouldStoreData) {
                                            String gyroVal = "\t\t"+((imuvalues(characteristic.getStringValue(0), 1)[0])+","+" " +((imuvalues(characteristic.getStringValue(0), 1)[1])+","+" " + ((imuvalues(characteristic.getStringValue(0), 1)[2]))));
                                            if (service.getUuid().toString().equals(Constants.BLE_SERVICE_UUID)) {
                                                rightFootDataBuilder.append(gyroVal);
                                                gyro = imuvalues(characteristic.getStringValue(0), 1);
                                            }
                                            else {
                                                leftFootDataBuilder.append(gyroVal);
                                                gyro_left = imuvalues(characteristic.getStringValue(0), 1);
                                            }
                                        }
                                        Log.i("gyro", ((imuvalues(characteristic.getStringValue(0), 1)[0])+","+" " +((imuvalues(characteristic.getStringValue(0), 1)[1])+","+" " + ((imuvalues(characteristic.getStringValue(0), 1)[2])))));
                                    } else if(characteristic.getUuid().toString().equals("19b10111-e8f2-537e-4f6c-d104768a1214")) {

                                        if (shouldStoreData) {
                                            String magnetVal = "\t\t"+((imuvalues(characteristic.getStringValue(0), 2)[0])+","+" " +((imuvalues(characteristic.getStringValue(0), 2)[1])+","+" " + ((imuvalues(characteristic.getStringValue(0), 2)[2]))));
                                            if (service.getUuid().toString().equals(Constants.BLE_SERVICE_UUID)) {
                                                rightFootDataBuilder.append(magnetVal);
                                                magnet = imuvalues(characteristic.getStringValue(0), 2);
                                                Log.i("magnet right", ((imuvalues(characteristic.getStringValue(0), 2)[0])+","+((imuvalues(characteristic.getStringValue(0), 2)[1])+","+ ((imuvalues(characteristic.getStringValue(0), 2)[2])))));
                                            }
                                            else {
                                                leftFootDataBuilder.append(magnetVal);
                                                magnet_left = imuvalues(characteristic.getStringValue(0), 2);
                                                Log.i("magnet left", ((imuvalues(characteristic.getStringValue(0), 2)[0])+","+" " +((imuvalues(characteristic.getStringValue(0), 2)[1])+","+" " + ((imuvalues(characteristic.getStringValue(0), 2)[2])))));
                                            }
                                        }
                                    } else if(characteristic.getUuid().toString().equals("19b11111-e8f2-537e-4f6c-d104768a1214")) {


                                        if (shouldStoreData) {
                                            String pressureVal = "\t\t"+characteristic.getStringValue(0)+"\n";

                                            if (service.getUuid().toString().equals(Constants.BLE_SERVICE_UUID)) {
                                                rightFootDataBuilder.append(pressureVal);
                                                pressure = imuvalues(characteristic.getStringValue(0), 3);
                                                String magacc =  (new Date().getTime() - first_time)/1000.0 +"\t\t"+accelerationMagnitude(linear_acceleration)+"\t\t"+ accelerationMagnitude(linear_acceleration_left)+ "\n";
                                                accelerationDataBuilder.append(magacc);
                                            }
                                            else {
                                                leftFootDataBuilder.append(pressureVal);
                                                pressure_left = imuvalues(characteristic.getStringValue(0), 3);


                                            }
                                        }
                                    }


                                    if(characteristic.getUuid().toString().equals("19b11111-e8f2-537e-4f6c-d104768a1214")){

                                        SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet);
                                        SensorManager.getOrientation(rotationMatrix, accMagOrientation);
                                        Log.i("orioright", String.format("Orientation: %f, %f, %f",
                                                accMagOrientation[0]*180/PI, accMagOrientation[1]*180/PI, accMagOrientation[2]*180/PI));
                                        float[] data = IMU_9DOF(accel, gyro, magnet);
                                        counter.add(accMagOrientation[0]);
                                        yaws.add(new Entry(counter.size(), (float) (fusedOrientation[0])));
                                        rolls.add(new Entry(counter.size(), (float) (fusedOrientation[1])));
                                        pitches.add(new Entry(counter.size(), (float) (fusedOrientation[2])));
                                        //float [] rotation_transpose = transpose(rotationMatrix) ;
                                        global_accel = matrixMultiplication1(rotationMatrix , accel);
                                        Log.i("maman", String.valueOf((global_accel[0]+"," + global_accel[1] +"," +global_accel[2]))) ;
                                        yaws.add(new Entry(counter.size(),global_accel[0]));
                                        rolls.add(new Entry(counter.size(),global_accel[1]));
                                        pitches.add(new Entry(counter.size(), global_accel[2]));
                                        //// removing gravity
                                        /*global_accel =*/
                                        float a = global_accel[0];;
                                        float b = global_accel[1];;
                                        float c = global_accel[2];;
                                        float cc = c - 1.0f ;

                                        linear_acceleration[0] = (a) ;
                                        linear_acceleration[0] = (b) ;
                                        linear_acceleration[0] = (cc) ;



                                        float magnitude_of_acceleration =  accelerationMagnitude(linear_acceleration);
                                        Log.i("magnitude4_right", String.valueOf(magnitude_of_acceleration));







                                        yaws.add(new Entry(counter.size(),magnitude_of_acceleration));
                                        long timer2 = new Date().getTime();
                                        Log.i("asas", String.valueOf(timer2)) ;
                                        long time_difference = ((timer2)- timer1) ;
                                        timer1= timer2 ;
                                        velocity2 = velocity1 + ((magnitude_of_acceleration+ magnitude_of_acceleration_0)* time_difference)/2 ;
                                        position2 = position1 + ((velocity1+velocity2) * time_difference)/2 ;
                                        magnitude_of_acceleration_0 = magnitude_of_acceleration ;
                                        velocity1= velocity2 ;
                                        position1= position2 ;
                                        if (magnitude_of_acceleration> 2){
                                            step_lenghts= position2/1000000.0f  ;
                                            velocity1= 0;
                                            velocity2 = 0 ;
                                            position1= 0;
                                            position2 = 0;

                                            Log.i("step_length", String.valueOf(position2)) ;

                                        }




                                        SensorManager.getRotationMatrix(rotationMatrix_left, null, accel_left, magnet_left);
                                        SensorManager.getOrientation(rotationMatrix_left, accMagOrientation_left);
                                        Log.i("orioleft", String.format("Orientation: %f, %f, %f",
                                                accMagOrientation_left[0]*180/PI, accMagOrientation_left[1]*180/PI, accMagOrientation_left[2]*180/PI));
                                        float[] data_left = IMU_9DOF(accel_left, gyro_left, magnet_left);
                                        counter.add(accMagOrientation_left[0]);
                                        yaws_left.add(new Entry(counter_left.size(), (float) (fusedOrientation_left[0])));
                                        rolls_left.add(new Entry(counter_left.size(), (float) (fusedOrientation_left[1])));
                                        pitches_left.add(new Entry(counter_left.size(), (float) (fusedOrientation_left[2])));
                                        //float [] rotation_transpose = transpose(rotationMatrix) ;
                                        global_accel_left = matrixMultiplication1(rotationMatrix_left , accel_left);
                                        Log.i("maman", String.valueOf((global_accel_left[0]+"," + global_accel_left[1] +"," +global_accel_left[2]))) ;
                                        yaws_left.add(new Entry(counter_left.size(),global_accel_left[0]));
                                        rolls_left.add(new Entry(counter_left.size(),global_accel_left[1]));
                                        pitches_left.add(new Entry(counter_left.size(), global_accel_left[2]));
                                        //// removing gravity
                                        /*global_accel =*/
                                        float a_left = global_accel_left[0];;
                                        float b_left = global_accel_left[1];;
                                        float c_left = global_accel_left[2];;
                                        float cc_left = c_left - 1.0f ;

                                        linear_acceleration_left[0] = (a_left) ;
                                        linear_acceleration_left[0] = (b_left) ;
                                        linear_acceleration_left[0] = (cc_left) ;



                                        float magnitude_of_acceleration_left =  accelerationMagnitude(linear_acceleration_left);
                                        Log.i("magnitude4_left", String.valueOf(magnitude_of_acceleration_left));
                                        yaws_left.add(new Entry(counter_left.size(),magnitude_of_acceleration_left));
                                        long timer2_left = new Date().getTime();
                                        Log.i("asas", String.valueOf(timer2_left)) ;
                                        long time_difference_left = ((timer2_left)- timer1_left) ;
                                        timer1_left= timer2_left ;
                                        velocity2_left = velocity1_left + ((magnitude_of_acceleration_left+ magnitude_of_acceleration_0_left)* time_difference_left)/2 ;
                                        position2_left = position1_left + ((velocity1_left+velocity2_left) * time_difference_left)/2 ;
                                        magnitude_of_acceleration_0_left = magnitude_of_acceleration_left ;
                                        velocity1_left= velocity2_left ;
                                        position1_left= position2_left ;
                                        if (magnitude_of_acceleration_left> 1.5){
                                            step_lenghts_left = position2_left/1000000.0f ;
                                            velocity1_left= 0;
                                            velocity2_left = 0 ;
                                            position1_left= 0;
                                            position2_left = 0;

                                            Log.i("step_length_lef", String.valueOf(position2_left)) ;

                                        }

                                  /*      String acc = "\t\t"+ Arrays.toString(linear_acceleration) +"\n";
                                        String accleft = "\t\t"+ Arrays.toString(linear_acceleration_left) +"\n";

*/
                                        /*rightFootDataBuilder.append(acc);
                                        leftFootDataBuilder.append(accleft);*/

                                        final_Results() ;

                                       /* refreshData();*/


                                    }


                                    calculateAccMagOrientation() ;

                                    Log.i("rotation matrix" , (fusedOrientation[0]*180/PI+" "+fusedOrientation[1]*180/PI+" "+fusedOrientation[2]*180/PI));
                                    Log.i("rotation matrix" , (fusedOrientation[0]*180/PI+" "+fusedOrientation[1]*180/PI+" "+fusedOrientation[2]*180/PI));

                                    Log.i("roll pitch yaw", String.valueOf(accMagOrientation)) ;

/*



                                    EventBus.getDefault().post(new MessageEvent(right_data,left_data));

                                    spatiotemporal.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(BleMainActivity.this, temporal_spatial.class);
                                            intent.putExtra(EXTRA_TEXT, String.valueOf(pressure));
                                            startActivityForResult(intent, 1);
                                        }
                                    });*/



                                }



                            });


                            if (repeatCounter == 3) {
                                repeatCounter = -1;
                            }
                            repeatCounter++ ;
                            getCharacteristics(bleDevice, service);


                        }
                    });


    }

    private void readRssi(BleDevice bleDevice) {
        BleManager.getInstance().readRssi(bleDevice, new BleRssiCallback() {
            @Override
            public void onRssiFailure(BleException exception) {
                Log.i(TAG, "onRssiFailure" + exception.toString());
            }

            @Override
            public void onRssiSuccess(int rssi) {
                Log.i(TAG, "onRssiSuccess: " + rssi);
            }
        });
    }

    private void setMtu(BleDevice bleDevice, int mtu) {
        BleManager.getInstance().setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {
                Log.i(TAG, "onsetMTUFailure" + exception.toString());
            }

            @Override
            public void onMtuChanged(int mtu) {
                Log.i(TAG, "onMtuChanged: " + mtu);
            }
        });
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    setScanRule();
                    startScan();
                }
                break;
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                setScanRule();
                startScan();
            }
        }
    }


    private void refreshData() {
        LineDataSet set1 = new LineDataSet(yaws,"yaw") ;
        LineDataSet set2 = new LineDataSet(rolls,"roll") ;
        LineDataSet set3 = new LineDataSet(pitches,"pitch") ;
        //set1.setFillAlpha(110);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>() ;
        dataSets.add(set1);
        set1.setDrawCircles(false);
        set1.setColor(Color.RED);
        dataSets.add(set2);
        set2.setDrawCircles(false);
        set2.setColor(Color.BLUE);
        dataSets.add(set3);
        set3.setDrawCircles(false);
        set3.setColor(Color.GREEN);
        LineData zata = new LineData(dataSets) ;

        mChart.setData(zata);
        mChart.notifyDataSetChanged(); // let the chart know it's data changed
        mChart.invalidate(); // refresh chart
        mChart.getDescription().setEnabled(true);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(Color.BLACK);
        mChart.moveViewToX(counter.size() - 100);
        mChart.setVisibleXRangeMaximum(100);

    }


    public float[] imuvalues(String line, Integer CASE){
        float [] value= new float[3] ;
        if(line != null) {
            String[] parts = line.split(",");
            int[] ints = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                ints[i] = Integer.parseInt(parts[i]);
            }
            Log.i("javab", String.valueOf((ints[0])));
            if (CASE == 0) {

                value[0] = ints[0]/100.0f;
                value[1] = ints[1]/100.0f;
                value[2] = ints[2]/100.0f;
                Log.i("accccceellee", String.valueOf(value[0])+","+String.valueOf( value[1])+","+String.valueOf(value[2]));

            }
            else if(CASE == 2){
                value[0] = ints[0] / 10.0f;
                value[1] = ints[1] / 10.0f;
                value[2] = ints[2] / 10.0f;




                Log.i("line", String.valueOf(value[0])+"," +String.valueOf(value[1])+ "," +String.valueOf(value[2])+ ","+Math.sqrt(value[0]*value[0]+ value[1]*value[1]+value[2]*value[2])) ;
            }
            else if(CASE == 3){
                value[0] = ints[0] ;
                value[1] = ints[1] ;
                value[2] = 0 ;
                Log.i("akhari1", String.valueOf(value[0]));
            } else {
                value[0] = ints[0] ;
                value[1] = ints[1] ;
                value[2] = ints[2] ;
            }
        }
        return  value ;
    }
    public float[]  IMU_9DOF(float[] acceleration_value, float[] gyroscope_value, float[] magnetometer_value){

        float[] imu = {acceleration_value[0],acceleration_value[1],acceleration_value[2],
                                gyroscope_value[0],gyroscope_value[1],gyroscope_value[2],
                                 magnetometer_value[0],magnetometer_value[1],magnetometer_value[2]};

        return imu ;
    }


    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
            Log.i("OrientationTestActivity", String.format("Orientation: %f, %f, %f",
                    accMagOrientation[0], accMagOrientation[1], accMagOrientation[2]));
        }
    }

    public static final float EPSILON = 0.000000001f;

    private void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor)
    {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    public static final float NS2S = 1.0f / 1000000000.0f;
    public float timestamp;
    public boolean initState = true;
    public void gyroFunction(float[] gyro) {
        Date date = new Date();
        long timer = date.getTime();
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if(initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if(timestamp != 0) {
            final float dT = (timer - timestamp) * NS2S;
            getRotationVectorFromGyro(this.gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timestamp = timer;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }
    private float[] matrixMultiplication1(float[] A, float[] B) {
        float[] result = new float[3];
        result[0] = A[0] * B[0] + A[1] * B[1] + A[2] * B[2];
        result[1] = A[3] * B[0] + A[4] * B[1] + A[5] * B[2];
        result[2] = A[6] * B[0] + A[7] * B[1] + A[8] * B[2];

        return result;
    }


    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
            fusedOrientation[0] =
                    FILTER_COEFFICIENT * gyroOrientation[0]
                            + oneMinusCoeff * accMagOrientation[0];

            fusedOrientation[1] =
                    FILTER_COEFFICIENT * gyroOrientation[1]
                            + oneMinusCoeff * accMagOrientation[1];

            fusedOrientation[2] =
                    FILTER_COEFFICIENT * gyroOrientation[2]
                            + oneMinusCoeff * accMagOrientation[2];

            // overwrite gyro matrix and orientation with fused orientation
            // to comensate gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
        }
    }
    private float accelerationMagnitude(float[] accelerationVector){

        double accmagnitude = Math.sqrt(accelerationVector[0]*accelerationVector[0] +accelerationVector[1]*accelerationVector[1]+ accelerationVector[2]*accelerationVector[2]);
        Log.i("magnitude", (accelerationVector[0]) +  "," + (accelerationVector[1]) +","+ (accelerationVector[2])) ;
        Log.i("magnitude3", String.valueOf(accmagnitude));

        return (float) accmagnitude;
    }

    private void requestPerm() {
        if (ContextCompat.checkSelfPermission(this ,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1000);
            }
        }
    }
    ///// initialization
    float right_pres_hill_pre = 0 ;
    float right_pres_toe_pre = 0 ;
    float left_pres_hill_pre = 0 ;
    float left_pres_toe_pre =  0 ;


    //////

    private long hill_right_time   ;private long hill_left_time ;private long toe_right_time ;private long toe_left_time ;
    private long hill_right_time__  ;private long hill_left_time__;private long toe_right_time__ ;private long toe_left_time__ ;
    private float step_time= 0  ; private float stride_time_right= 0  ; private float stride_time_left= 0  ;
    private float stance_time_right= 0  ; private float swing_time_right= 0  ; private float stance_time_left= 0  ; public float swing_time_left= 0  ;
    private float cadance_counter = 0 ;
    private long x = (long) 1000.0000;
    private int k = 0 ;
    private long start ;
    private float cadance;


    private void final_Results(){

        if( k==1){
            start = new Date().getTime();;
        }

        Log.i("abcdefg", Arrays.toString(pressure) + Arrays.toString(pressure_left)) ;
        ///toe and hill contact
        if((pressure[0] - right_pres_hill_pre > 40)&& (new Date().getTime() - hill_right_time__>500)&& (pressure[0]>50)){
            hill_right_time = new Date().getTime(); cadance_counter = cadance_counter+1 ;Log.i("hill contact right", String.valueOf(hill_right_time - hill_right_time__));
            step_time = (hill_right_time - hill_left_time__)/1000.0f;
            stride_time_right =( hill_right_time - hill_right_time__)/1000.0f ;
            swing_time_right= (hill_right_time - toe_right_time__)/1000.0f ;
        }
        if((pressure[1]-right_pres_toe_pre  < - 40)&& (new Date().getTime() - toe_right_time__>500) && (pressure[1]<60)){
            toe_right_time = new Date().getTime();  Log.i("right toe off", String.valueOf(toe_right_time - toe_right_time__));
            stance_time_left = (toe_right_time - hill_right_time)/1000.0f ;
        }
        if((pressure_left[0] - left_pres_hill_pre > 40) && (new Date().getTime() - hill_left_time__>500)&& (pressure_left[0]>50)){
            k=k+1;
            hill_left_time = new Date().getTime(); cadance_counter= cadance_counter + 1 ;Log.i("hill contact left", "now");
            stride_time_left = (hill_left_time - hill_left_time__)/1000.0f ;

            swing_time_left= (hill_left_time - toe_left_time__)/1000.0f ;
            long duration = (hill_left_time -  start )/x ;
            cadance = (float) ((cadance_counter/((duration)))*60.0);
            Log.i("cadance", String.valueOf(cadance)) ;

        }
        if(( pressure_left[1] - left_pres_toe_pre < - 40)&& (new Date().getTime() - toe_left_time__>500)  && (pressure_left[1]<60)){
            toe_left_time = new Date().getTime() ; Log.i("left toeoff", "now") ;
            stance_time_right = (toe_left_time - hill_left_time)/1000.0f ;
            String ST = ((new Date().getTime() - first_time)/1000.0) +"\t\t\t" + step_time+ "\t\t\t"+stride_time_right+ "\t\t\t\t"+stride_time_left+ "\t\t\t\t"+ stance_time_right+ "\t\t\t\t"+stance_time_left+ "\t\t\t\t"+swing_time_right+ "\t\t\t"+swing_time_left+ "\t\t\t\t"+cadance+"\n" ;

            spatiotemporalDataBuilder.append(ST);

        }




        //step lenghth
        //stride lenght
        //gait speed
        //stride speed




        right_pres_hill_pre = pressure[0]  ;
        right_pres_toe_pre = pressure[1] ;
        left_pres_hill_pre = pressure_left[0] ;
        left_pres_toe_pre =  pressure_left[1];

        hill_right_time__ = hill_right_time;
        toe_right_time__ = toe_right_time;
        hill_left_time__ = hill_left_time;
        toe_left_time__ =  toe_left_time ;

    }
}
