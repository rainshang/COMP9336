package xyx.com.lab9336;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Lab07_2Activity extends Activity implements SensorEventListener {

    private final static String ORIENTATIONS_ON_TABLE = "On the table";
    private final static String ORIENTATIONS_DEFAULT = "Default";
    private final static String ORIENTATIONS_UPSIDE_DOWN = "Upside down";
    private final static String ORIENTATIONS_RIGHT = "Right";
    private final static String ORIENTATIONS_LEFT = "Left";

    private TextView textView;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        setContentView(linearLayout);
        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER);
        tv.setText("Orientation of the phone is:");
        linearLayout.addView(tv);
        textView = new TextView(this);
        textView.setTextSize(70);
        textView.setGravity(Gravity.CENTER);
        linearLayout.addView(textView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.values[2] > 7) {
            textView.setText(ORIENTATIONS_ON_TABLE);
        } else if (sensorEvent.values[1] > 7) {
            textView.setText(ORIENTATIONS_DEFAULT);
        } else if (sensorEvent.values[1] < -7) {
            textView.setText(ORIENTATIONS_UPSIDE_DOWN);
        } else if (sensorEvent.values[0] < -7) {
            textView.setText(ORIENTATIONS_RIGHT);
        } else if (sensorEvent.values[0] > 7) {
            textView.setText(ORIENTATIONS_LEFT);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
