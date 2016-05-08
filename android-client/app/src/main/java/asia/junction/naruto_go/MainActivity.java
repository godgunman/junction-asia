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
    private static final int PERMISSION_REQUEST_CODE = 10013;
    private static final int NORMALIZE_SIZE = 30;

    private SensorManager sensorManager;
    private boolean accelerometerPresent;
    private Sensor accelerometerSensor;

    private File dir, file;
    private TextView textX, textY, textZ;
    private Button learnBtn, predictBtn, restartBtn;
    private EditText label, user;

    boolean startTraining = false;

    private List<Point> currentDataSet;
    private Socket socket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionsIfDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        createOutputFile("train");
        findViews();
        initButtonListener();
        initSensorManager();
        socketInit();
    }

    private void requestPermissionsIfDenied(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{ permission },
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void createOutputFile(String prefix) {
        if (null == dir) {
            dir = new File(Environment.getExternalStorageDirectory(), "naruto_go");
        }
        SimpleDateFormat dateFmt = new SimpleDateFormat("hhmmss");
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

        learnBtn = (Button) findViewById(R.id.learn);
        predictBtn = (Button) findViewById(R.id.predict);
        restartBtn = (Button) findViewById(R.id.restart);
        label = (EditText) findViewById(R.id.label);
        user = (EditText) findViewById(R.id.user);
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
                        currentDataSet = Utils.normalize(currentDataSet, NORMALIZE_SIZE);

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
                            Utils.sendResultToServer(result);
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
                String prefix = user.getText().toString();
                if (prefix.length() == 0)
                    prefix = "train";
                createOutputFile(prefix);
                return false;
            }
        });
    }

    private void initSensorManager() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager
                .getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);

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
                public void call(Object... args) {
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                }

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

        boolean isFirst = true;
        double threshold = 0.7;
        double preX, preY, preZ;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;

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
                maxX = x;
            } else if (Math.abs(preX - x) < threshold &&
                    Math.abs(preY - y) < threshold &&
                    Math.abs(preZ - z) < threshold) {
                return;
            }

            if (socket.connected()) {
                socket.emit("onSensorChanged", String.format("%f,%f,%f", x, y, z));
            }

            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);

            textX.setText(String.format("X:%+8.3f  MAX:%+8.3f  MIN:%+8.3f", x, maxX, minX));
            textY.setText(String.format("Y:%+8.3f  MAX:%+8.3f  MIN:%+8.3f", y, maxY, minY));
            textZ.setText(String.format("Z:%+8.3f  MAX:%+8.3f  MIN:%+8.3f", z, maxZ, minZ));
            if (startTraining) {
                currentDataSet.add(new Point(x, y, z));
            }
            preX = x;
            preY = y;
            preZ = z;
        }
    };
}