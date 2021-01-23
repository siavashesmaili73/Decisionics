package com.example.imu;

import androidx.appcompat.app.AppCompatActivity;

public class temporal_spatial extends AppCompatActivity {


}


/*









*/
/*    public float z1;
    public float z2;

    public float z3;
    public float z4;
    public float z5;*//*

    public float z6;
    public float z7;

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(MessageEvent event) {
        Log.i("Incoming", event.getUuid() + " :: " + event.getValue());
        TextView accelx = findViewById(R.id.accelx);
        accelx.setText(String.valueOf(z));
*/
/*        mChart = findViewById(R.id.linechart_accx);
        mChart1 = findViewById(R.id.linechart_accy);
        mChart2 = findViewById(R.id.linechart_accz);
        mChart3 = findViewById(R.id.linechart_gyrox);
        mChart4 = findViewById(R.id.linechart_gyroy);
        mChart5 = findViewById(R.id.linechart_gyroz);*//*

        mChart6 = findViewById(R.id.linechart_pressh);
        mChart7 = findViewById(R.id.linechart_presst);


        if (event.getUuid().equals("19b10001-e8f2-537e-4f6c-d104768a1214")) {
            z = event.getValue();
            counter.add(z);
            yValues.add(new Entry(counter.size(), z));
            LineDataSet set1 = new LineDataSet(yValues, "acceleration");
            set1.setFillAlpha(110);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(new LineDataSet(yValues, "acceleration"));
            mChart.setData(new LineData(dataSets));
            mChart.notifyDataSetChanged(); // let the chart know it's data changed
            mChart.invalidate(); // refresh chart
            mChart.getDescription().setEnabled(true);
            mChart.setTouchEnabled(true);
            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            mChart.setDrawGridBackground(false);
            mChart.setPinchZoom(true);
            mChart.setBackgroundColor(Color.BLACK);
            mChart.moveViewToX(counter.size() - 10);
            mChart.setVisibleXRangeMaximum(10);
            mChart.resetViewPortOffsets() ;


        } */
/*else if (event.getUuid().equals("19b10011-e8f2-537e-4f6c-d104768a1214")) {
            z1 = event.getValue();
            counter1.add(z1);
            yValues1.add(new Entry(counter1.size(), z1));
            LineDataSet set1 = new LineDataSet(yValues1, "acceleration");
            set1.setFillAlpha(110);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(new LineDataSet(yValues1, "acceleration"));
            mChart1.setData(new LineData(dataSets));
            mChart1.notifyDataSetChanged(); // let the chart know it's data changed
            mChart1.invalidate(); // refresh chart
            mChart1.getDescription().setEnabled(true);
            mChart1.setTouchEnabled(true);
            mChart1.setDragEnabled(true);
            mChart1.setScaleEnabled(true);
            mChart1.setDrawGridBackground(false);
            mChart1.setPinchZoom(true);
            mChart1.setBackgroundColor(Color.BLACK);
            mChart1.moveViewToX(counter1.size() - 10);
            mChart1.setVisibleXRangeMaximum(10);
            mChart.resetViewPortOffsets() ;


        } else if (event.getUuid().equals("19b10111-e8f2-537e-4f6c-d104768a1214")) {
            z2 = event.getValue();
            counter2.add(z2);
            yValues2.add(new Entry(counter2.size(), z2));
            LineDataSet set1 = new LineDataSet(yValues2, "acceleration");
            set1.setFillAlpha(110);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(new LineDataSet(yValues2, "acceleration"));
            mChart2.setData(new LineData(dataSets));
            mChart2.notifyDataSetChanged(); // let the chart know it's data changed
            mChart2.invalidate(); // refresh chart
            mChart2.getDescription().setEnabled(true);
            mChart2.setTouchEnabled(true);
            mChart2.setDragEnabled(true);
            mChart2.setScaleEnabled(true);
            mChart2.setDrawGridBackground(false);
            mChart2.setPinchZoom(true);
            mChart2.setBackgroundColor(Color.BLACK);
            mChart2.moveViewToX(counter2.size() - 10);
            mChart2.setVisibleXRangeMaximum(10);
            mChart.resetViewPortOffsets() ;

        } else if (event.getUuid().equals("19b10002-e8f2-537e-4f6c-d104768a1214")) {

            z3 = event.getValue();
            counter3.add(z3);
            yValues3.add(new Entry(counter3.size(), z3));
            LineDataSet set1 = new LineDataSet(yValues3, "acceleration");
            set1.setFillAlpha(110);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(new LineDataSet(yValues3, "acceleration"));
            mChart3.setData(new LineData(dataSets));
            mChart3.notifyDataSetChanged(); // let the chart know it's data changed
            mChart3.invalidate(); // refresh chart
            mChart3.getDescription().setEnabled(true);
            mChart3.setTouchEnabled(true);
            mChart3.setDragEnabled(true);
            mChart3.setScaleEnabled(true);
            mChart3.setDrawGridBackground(false);
            mChart3.setPinchZoom(true);
            mChart3.setBackgroundColor(Color.BLACK);
            mChart3.moveViewToX(counter3.size() - 10);
            mChart3.setVisibleXRangeMaximum(10);
            mChart.resetViewPortOffsets() ;

        } else if (event.getUuid().equals("19b10022-e8f2-537e-4f6c-d104768a1214")) {

            z4 = event.getValue();
            counter4.add(z4);
            yValues4.add(new Entry(counter4.size(), z4));
            LineDataSet set1 = new LineDataSet(yValues4, "acceleration");
            set1.setFillAlpha(110);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(new LineDataSet(yValues4, "acceleration"));
            mChart4.setData(new LineData(dataSets));
            mChart4.notifyDataSetChanged(); // let the chart know it's data changed
            mChart4.invalidate(); // refresh chart
            mChart4.getDescription().setEnabled(true);
            mChart4.setTouchEnabled(true);
            mChart4.setDragEnabled(true);
            mChart4.setScaleEnabled(true);
            mChart4.setDrawGridBackground(false);
            mChart4.setPinchZoom(true);
            mChart4.setBackgroundColor(Color.BLACK);
            mChart4.moveViewToX(counter4.size() - 10);
            mChart4.setVisibleXRangeMaximum(10);
            mChart.resetViewPortOffsets() ;

        } else if (event.getUuid().equals("19b10222-e8f2-537e-4f6c-d104768a1214")) {

            z4 = event.getValue();
            counter4.add(z4);
            yValues4.add(new Entry(counter4.size(), z4));
            LineDataSet set1 = new LineDataSet(yValues4, "acceleration");
            set1.setFillAlpha(110);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(new LineDataSet(yValues4, "acceleration"));
            mChart4.setData(new LineData(dataSets));
            mChart4.notifyDataSetChanged(); // let the chart know it's data changed
            mChart4.invalidate(); // refresh chart
            mChart4.getDescription().setEnabled(true);
            mChart4.setTouchEnabled(true);
            mChart4.setDragEnabled(true);
            mChart4.setScaleEnabled(true);
            mChart4.setDrawGridBackground(false);
            mChart4.setPinchZoom(true);
            mChart4.setBackgroundColor(Color.BLACK);
            mChart4.moveViewToX(counter4.size() - 10);
            mChart4.setVisibleXRangeMaximum(10);
            mChart.resetViewPortOffsets() ;

        } else if (event.getUuid().equals("19b10003-e8f2-537e-4f6c-d104768a1214")) {

            z5 = event.getValue();
            counter5.add(z5);
            yValues5.add(new Entry(counter5.size(), z5));
            LineDataSet set1 = new LineDataSet(yValues5, "acceleration");
            set1.setFillAlpha(110);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(new LineDataSet(yValues5, "acceleration"));
            mChart5.setData(new LineData(dataSets));
            mChart5.notifyDataSetChanged(); // let the chart know it's data changed
            mChart5.invalidate(); // refresh chart
            mChart5.getDescription().setEnabled(true);
            mChart5.setTouchEnabled(true);
            mChart5.setDragEnabled(true);
            mChart5.setScaleEnabled(true);
            mChart5.setDrawGridBackground(false);
            mChart5.setPinchZoom(true);
            mChart5.setBackgroundColor(Color.BLACK);
            mChart5.moveViewToX(counter5.size() - 10);
            mChart5.setVisibleXRangeMaximum(10);
            mChart.resetViewPortOffsets() ;

        }*//*
 else if (event.getUuid().equals("19b10033-e8f2-537e-4f6c-d104768a1214")) {

            z6 = event.getValue();
            counter6.add(z6);
            yValues6.add(new Entry(counter6.size(), z6));
            LineDataSet set1 = new LineDataSet(yValues6, "acceleration");
            set1.setFillAlpha(110);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(new LineDataSet(yValues6, "acceleration"));
            mChart6.setData(new LineData(dataSets));
            mChart6.notifyDataSetChanged(); // let the chart know it's data changed
            mChart6.invalidate(); // refresh chart
            mChart6.getDescription().setEnabled(true);
            mChart6.setTouchEnabled(true);
            mChart6.setDragEnabled(true);
            mChart6.setScaleEnabled(true);
            mChart6.setDrawGridBackground(false);
            mChart6.setPinchZoom(true);
            mChart6.setBackgroundColor(Color.BLACK);
            mChart6.moveViewToX(counter6.size() - 10);
            mChart6.setVisibleXRangeMaximum(10);
            mChart.resetViewPortOffsets() ;

        } else if (event.getUuid().equals("19b10033-e8f2-537e-4f6c-d104768a1214")) {

            z7 = event.getValue();
            counter7.add(z7);
            yValues7.add(new Entry(counter7.size(), z7));
            LineDataSet set1 = new LineDataSet(yValues7, "acceleration");
            set1.setFillAlpha(110);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(new LineDataSet(yValues7, "acceleration"));
            mChart7.setData(new LineData(dataSets));
            mChart7.notifyDataSetChanged(); // let the chart know it's data changed
            mChart7.invalidate(); // refresh chart
            mChart7.getDescription().setEnabled(true);
            mChart7.setTouchEnabled(true);
            mChart7.setDragEnabled(true);
            mChart7.setScaleEnabled(true);
            mChart7.setDrawGridBackground(false);
            mChart7.setPinchZoom(true);
            mChart7.setBackgroundColor(Color.BLACK);
            mChart7.moveViewToX(counter7.size() - 10);
            mChart7.setVisibleXRangeMaximum(10);
            mChart.resetViewPortOffsets() ;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temporal_spatial);
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);


    }


    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();

    }

    private static final String TAG = BleMainActivity.class.getSimpleName();
    public static final String EXTRA_TEXT1 = "com.example.imu.ble.EXTRA_TEXT1";
    public String x;
    public ArrayList<Entry> yValues = new ArrayList<>();
    public ArrayList<Float> counter = new ArrayList<Float>();

   */
/* public ArrayList<Entry> yValues1 = new ArrayList<>();
    public ArrayList<Float> counter1 = new ArrayList<Float>();

    public ArrayList<Entry> yValues2 = new ArrayList<>();
    public ArrayList<Float> counter2 = new ArrayList<Float>();

    public ArrayList<Entry> yValues3 = new ArrayList<>();
    public ArrayList<Float> counter3 = new ArrayList<Float>();

    public ArrayList<Entry> yValues4 = new ArrayList<>();
    public ArrayList<Float> counter4 = new ArrayList<Float>();

    public ArrayList<Entry> yValues5 = new ArrayList<>();
    public ArrayList<Float> counter5 = new ArrayList<Float>();*//*


    public ArrayList<Entry> yValues6 = new ArrayList<>();
    public ArrayList<Float> counter6 = new ArrayList<Float>();

    public ArrayList<Entry> yValues7 = new ArrayList<>();
    public ArrayList<Float> counter7 = new ArrayList<Float>();
    public LineChart mChart;
    */
/*    public LineChart mChart1;
        public LineChart mChart2;
        public LineChart mChart3;
        public LineChart mChart4;
        public LineChart mChart5;*//*

    public LineChart mChart6;
    public LineChart mChart7;


    public float z;
}
*/
