package com.example.imu;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imu.ble.operation.OperationActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.EulerAngles;
import com.mbientlab.metawear.module.GyroBmi160;
import com.mbientlab.metawear.module.SensorFusionBosch;

import java.util.ArrayList;

import bolts.Continuation;
import bolts.Task;

public class MainActivity<gyrovalue, seconds> extends AppCompatActivity  implements ServiceConnection{
//    private static int SPLASH_TIME_OUT = 4000;
//
//    @Override

    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private GyroBmi160 gyroBmi160;

    // Variables
    Button start;
    Button stop;
    Button ard;
    TextView gyroVal;
    public  ArrayList<Entry> yValues = new ArrayList<>() ;
    public ArrayList<Float> counter = new ArrayList<Float>() ;
    public float value ;

    public static final String TAG ="BleMainActivity" ;
    private LineChart mChart;
    Animation rotateAnimation;
    ImageView imageView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, BtleService.class),
                this, Context.BIND_AUTO_CREATE);
        setContentView(R.layout.activity_main);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        ard = findViewById(R.id.ard);
        gyroVal = findViewById(R.id.gyrovalue);

        imageView=(ImageView)findViewById(R.id.imageView);

        rotateAnimation();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("free fall","Start");
                gyroBmi160.angularVelocity().start();
                gyroBmi160.start();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("free fall", "Stop");
                gyroBmi160.stop();
                gyroBmi160.angularVelocity().stop();
            }

        });

        ard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OperationActivity.class);
                startActivity(intent);
            }
        });

        mChart = findViewById(R.id.linechart) ;



        /*        mChart.setOnChartValueSelectedListener(this);*/
        // enable description text
        mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

    }
    private void rotateAnimation() {

        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        imageView.startAnimation(rotateAnimation);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        retrieveBoard("DD:81:C7:68:ED:F2");
    }
    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }
    public void retrieveBoard(final String mac) {

        final BluetoothManager btManager=
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(mac);

        // Create a MetaWear board object for the Bluetooth Device
        board= serviceBinder.getMetaWearBoard(remoteDevice);
        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Void>>(){
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {

                Log.i("free fall", "connected to mac" + mac);

                /*configuring data range and rate for the accelerometer*/

                /*configuring data range and rate for the gyro*/

                SensorFusionBosch sensorFusion = board.getModule(SensorFusionBosch.class);
                sensorFusion.configure()
                        .mode(SensorFusionBosch.Mode.NDOF)
                        .accRange(SensorFusionBosch.AccRange.AR_2G)
                        .gyroRange(SensorFusionBosch.GyroRange.GR_250DPS)
                        .commit();



                /*retrieve angular velocity data*/
                return sensorFusion.eulerAngles().addRouteAsync(new RouteBuilder() {

                    @Override
                    public void configure(RouteComponent source) {
                        source.limit(33).stream(new Subscriber() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void apply(Data data, Object ... env) {
                                /*Log.i("knee angle", data.value(EulerAngles.class).toString());*/
                                //gyroVal.setText(data.value(EulerAngles.class).toString());
                                counter.add(data.value(EulerAngles.class).roll()) ;
                                float value = data.value(EulerAngles.class).roll() ;
                                yValues.add(new Entry(counter.size(), data.value(EulerAngles.class).roll()));
                                float z =   value ;
                                rotateImage(z);
                                refreshData();
                            }

                        });

                    }

                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        sensorFusion.eulerAngles().start();
                        sensorFusion.start();
                        return null;

                    }
                });
            }
        });
    }

    private void rotateImage(float angle) {
        Bitmap myImg = BitmapFactory.decodeResource(getResources(), R.drawable.ccapture);

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        Bitmap rotated = Bitmap.createBitmap(myImg, 0, 0, myImg.getWidth(), myImg.getHeight(),
                matrix, true);

        imageView.setImageBitmap(rotated);
    }

    private void refreshData() {
        LineDataSet set1 = new LineDataSet(yValues,"Angular Velocity") ;
        //set1.setFillAlpha(110);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>() ;
        dataSets.add(set1);
        LineData zata = new LineData(dataSets) ;
        mChart.setData(zata);
        mChart.notifyDataSetChanged(); // let the chart know it's data changed
        mChart.invalidate(); // refresh chart
    }
}


