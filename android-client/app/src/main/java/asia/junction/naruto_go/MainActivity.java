package asia.junction.naruto_go;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends Activity {
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 10013;

    private SensorManager sensorManager;
    private boolean accelerometerPresent;
    private Sensor accelerometerSensor;
    private BufferedWriter bw;

    private TextView textX, textY, textZ, cusorX, cusorY, trueX, trueY, inD;
    private Button btn;
    private EditText label;

    boolean startTraining = false;
    boolean controlFile = false;

    private int trainingDataSetCounter = 0;
    private int trainingDataCounter = 0;

    private int index = -2;
    private int mouseControl = 0;

    private List<List<Point>> allTrainingDataSet;
    private List<Point> currentTrainingDataSet;
    private Socket socket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionsIfDenied();
        findViews();
        initButtonListener();
        initSensorManager();
        socketInit();

        allTrainingDataSet = new ArrayList<>();
    }

    private void requestPermissionsIfDenied() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_CODE);
            }
        }
    }

    private void findViews() {
        textX = (TextView) findViewById(R.id.textx);
        textY = (TextView) findViewById(R.id.texty);
        textZ = (TextView) findViewById(R.id.textz);
        cusorX = (TextView) findViewById(R.id.cusorX);
        cusorY = (TextView) findViewById(R.id.cusorY);
        trueX = (TextView) findViewById(R.id.trueX);
        trueY = (TextView) findViewById(R.id.trueY);
        inD = (TextView) findViewById(R.id.inD);

        btn = (Button) findViewById(R.id.btn);
        label = (EditText)findViewById(R.id.label);
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
                            File dir = Environment.getExternalStorageDirectory();
                            dir = new File(dir, "naruto_go");
                            File file = new File(dir, "data_" + allTrainingDataSet.size() + ".text");
                            if (dir.exists() == false) {
                                dir.mkdirs();
                            }

                            String labelText = label.getText().toString();
                            if (labelText.length() == 0)
                                labelText = "0";

                            FileOutputStream fos = new FileOutputStream(file);
                            bw = new BufferedWriter(new OutputStreamWriter(fos));
                            bw.write(Utils.trainingDataToString(currentTrainingDataSet, labelText));
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

    private void socketInit() {
        try {
            socket = IO.socket("http://192.168.43.83:1337");
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.d("[socket.io]", "EVENT_CONNECT");
                    socket.emit("foo", "hi");
//                    socket.disconnect();
                }

            }).on("event", new Emitter.Listener() {

                @Override
                public void call(Object... args) {}

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {}

            });
            socket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
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

            if (socket.connected()) {
                socket.emit("onSensorChanged", String.format("%f,%f,%f",x,y,z));
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