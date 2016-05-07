package asia.junction.naruto_go;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private SensorManager sensorManager;
    private boolean accelerometerPresent;
    private Sensor accelerometerSensor;
    private BufferedWriter bw;

    private TextView textInfo, textX, textY, textZ, cusorX, cusorY, trueX, trueY, inD;
    private Button btn, btn2;

    boolean startTraining = false;
    boolean controlFile = false;

    private int trainingDataSetCounter = 0;
    private int trainingDataCounter = 0;

    private int index = -2;
    private int mouseControl = 0;

    private List<List<Point>> allTrainingDataSet;
    private List<Point> currentTrainingDataSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        initButtonListener();
        initSensorManager();

        allTrainingDataSet = new ArrayList<>();
    }

    private void findViews() {
        // textInfo = (TextView) findViewById(R.id.info);
        textX = (TextView) findViewById(R.id.textx);
        textY = (TextView) findViewById(R.id.texty);
        textZ = (TextView) findViewById(R.id.textz);
        cusorX = (TextView) findViewById(R.id.cusorX);
        cusorY = (TextView) findViewById(R.id.cusorY);
        trueX = (TextView) findViewById(R.id.trueX);
        trueY = (TextView) findViewById(R.id.trueY);
        inD = (TextView) findViewById(R.id.inD);

        btn = (Button) findViewById(R.id.btn);
        btn2 = (Button) findViewById(R.id.btn2);
    }

    private void initButtonListener() {
        btn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startTraining = true;
                        currentTrainingDataSet = new ArrayList<>();
                        break;
                    case MotionEvent.ACTION_UP:

                        currentTrainingDataSet = Utils.normalize(currentTrainingDataSet, 50);
                        allTrainingDataSet.add(currentTrainingDataSet);

                        try {
                            FileOutputStream fos = openFileOutput("data_" + allTrainingDataSet.size() + ".text", MODE_APPEND);
                            bw = new BufferedWriter(new OutputStreamWriter(fos));
                            bw.write(Utils.trainingDataToString(currentTrainingDataSet));
                            bw.flush();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (controlFile) {
                            try {
                                bw.newLine();
                                bw.flush();

                                String scaleResult = svm_scale.main(new String[]{
                                        "-r", "/sdcard/HCI/finalTrain.range.txt",
                                        "/sdcard/HCI/test" + trainingDataSetCounter + ".txt"});

                                FileWriter fw2 = new FileWriter("/sdcard/HCI/test"
                                        + trainingDataSetCounter + ".txt.scale", false);
                                BufferedWriter bw2 = new BufferedWriter(fw2); // 將BufferedWeiter與FileWrite物件做連結

                                bw2.write(scaleResult);
                                bw2.flush();

                                index = svm_predict
                                        .main(new String[]{
                                                "/sdcard/HCI/test" + trainingDataSetCounter
                                                        + ".txt.scale",
                                                "/sdcard/HCI/finalTrain.model.txt",
                                                "/sdcard/HCI/predict" + trainingDataSetCounter
                                                        + ".txt"});
                                // Log.d("index",""+index);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            startTraining = false;
                            break;
                        }
                }
                // TODO Auto-generated method stub
                return false;
            }
        });

        btn2.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        index = -2;
                        mouseControl = 1;

                        break;
                    case MotionEvent.ACTION_UP:

                        mouseControl = 0;
                }
                // TODO Auto-generated method stub
                return false;
            }
        });
    }

    private void initSensorManager() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager
                .getSensorList(Sensor.TYPE_ACCELEROMETER);

        if (sensorList.size() > 0) {
            accelerometerPresent = true;
            accelerometerSensor = sensorList.get(0);

            String strSensor = "Name: " + accelerometerSensor.getName()
                    + "\nVersion: "
                    + String.valueOf(accelerometerSensor.getVersion())
                    + "\nVendor: " + accelerometerSensor.getVendor()
                    + "\nType: "
                    + String.valueOf(accelerometerSensor.getType()) + "\nMax: "
                    + String.valueOf(accelerometerSensor.getMaximumRange())
                    + "\nResolution: "
                    + String.valueOf(accelerometerSensor.getResolution())
                    + "\nPower: "
                    + String.valueOf(accelerometerSensor.getPower())
                    + "\nClass: " + accelerometerSensor.getClass().toString();

            // textInfo.setText(strSensor);

        } else {
            accelerometerPresent = false;
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (accelerometerPresent) {
            sensorManager.registerListener(accelerometerListener,
                    accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
            Toast.makeText(this, "Register accelerometerListener",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();

        if (accelerometerPresent) {
            sensorManager.unregisterListener(accelerometerListener);
            Toast.makeText(this, "Unregister accelerometerListener",
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    private SensorEventListener accelerometerListener = new SensorEventListener() {

        boolean isFirst;
        double threshold = 1.3;
        double preX, preY, preZ;

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub

            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];

            if (isFirst) {
                isFirst = false;
            } else if (Math.abs(preX - x) < threshold &&
                    Math.abs(preY - y) < threshold &&
                    Math.abs(preZ - z) < threshold) {
                return;
            }

            textX.setText("X: " + String.valueOf(x));
            textY.setText("Y: " + String.valueOf(y));
            textZ.setText("Z: " + String.valueOf(z));
            inD.setText("ind: " + index);
            if (startTraining) {
                currentTrainingDataSet.add(new Point(x, y, z));
            }
            preX = x;
            preY = y;
            preZ = z;
        }
    };
}