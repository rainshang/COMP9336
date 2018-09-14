package xyx.com.lab9336;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class Lab07_1Activity extends Activity implements SensorEventListener {

    private TextView textView;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEvent accelerometerSensorEvent;
    private Sensor linearAccelerometerSensor;
    private SensorEvent linearAccelerometerSensorEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textView = new TextView(this);
        textView.setGravity(Gravity.CENTER);
        setContentView(textView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Lab07_2Activity.class));
            }
        });
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        linearAccelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, linearAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == accelerometerSensor) {
            accelerometerSensorEvent = sensorEvent;
        } else if (sensorEvent.sensor == linearAccelerometerSensor) {
            linearAccelerometerSensorEvent = sensorEvent;
        }
        textView.setText((accelerometerSensorEvent != null ? getDisplay(true, accelerometerSensorEvent) : "")
                + "\n\n\n" +
                (linearAccelerometerSensorEvent != null ? getDisplay(false, linearAccelerometerSensorEvent) : ""));
    }

    private String getDisplay(boolean withGravity, SensorEvent sensorEvent) {
        return String.format("Acceleration force %1$s gravity:\nX: %2$f\nY: %3$f\nZ: %4$f",
                withGravity ? "including" : "without",
                sensorEvent.values[0],
                sensorEvent.values[1],
                sensorEvent.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
