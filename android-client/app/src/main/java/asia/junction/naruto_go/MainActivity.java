package asia.junction.naruto_go;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.BufferedWriter;
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

    private File dir, file;
    private TextView textX, textY, textZ, inD;
    private Button learnBtn, predictBtn, restartBtn;
    private EditText label;

    boolean startTraining = false;

    private int index = -2;

    private List<Point> currentDataSet;
    private Socket socket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionsIfDenied();
        createOutputFile("train");
        findViews();
        initButtonListener();
        initSensorManager();
        socketInit();
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

    private void createOutputFile(String prefix) {
        if (null == dir) {
            dir = new File(Environment.getExternalStorageDirectory(), "naruto_go");
        }
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyyMMddhhmmss");
        file = new File(dir, prefix + dateFmt.format(new Date()) + ".data");
        if (dir.exists() == false) {
            dir.mkdirs();
        }
    }


    private void writeToFile(String output, File outputFile) {
        writeToFile(output, outputFile, false);
    }

    private void writeToFile(String output, File outputFile, boolean append) {
        try {
            FileOutputStream fos = new FileOutputStream(outputFile, append);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(output);
            bw.newLine();
            bw.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findViews() {
        textX = (TextView) findViewById(R.id.textx);
        textY = (TextView) findViewById(R.id.texty);
        textZ = (TextView) findViewById(R.id.textz);
        inD = (TextView) findViewById(R.id.inD);

        learnBtn = (Button) findViewById(R.id.learn);
        predictBtn = (Button) findViewById(R.id.predict);
        restartBtn = (Button) findViewById(R.id.restart);
        label = (EditText)findViewById(R.id.label);
    }

    private void initButtonListener() {
        learnBtn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startTraining = true;
                        currentDataSet = new ArrayList<>();
                        break;
                    case MotionEvent.ACTION_UP:
                        currentDataSet = Utils.normalize(currentDataSet, 50);

                        String labelText = label.getText().toString();
                        if (labelText.length() == 0)
                            labelText = "0";
                        writeToFile(Utils.dataToString(currentDataSet, labelText), file, true);
                }
                // TODO Auto-generated method stub
                return false;
            }
        });

        predictBtn.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startTraining = true;
                        currentDataSet = new ArrayList<>();
                        break;
                    case MotionEvent.ACTION_UP:
                        currentDataSet = Utils.normalize(currentDataSet, 50);

                        File predict = new File(dir, "predict.data");
                        writeToFile(Utils.dataToString(currentDataSet, "0"), predict);

                        File model = new File(dir, "train.data.model");
                        File output = new File(dir, "result.data");
                        try {
                            int result = svm_predict.main(new String[]{
                                    predict.toString(),
                                    model.toString(),
                                    output.toString()
                            });
                            label.setText(String.valueOf(result));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return false;
            }
        });

        restartBtn.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                createOutputFile("train");
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
                    socket.emit("foo", "hi");
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
                currentDataSet.add(new Point(x, y, z));
            }
            preX = x;
            preY = y;
            preZ = z;
        }
    };
}