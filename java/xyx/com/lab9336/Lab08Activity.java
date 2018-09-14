package xyx.com.lab9336;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static android.util.Half.EPSILON;

public class Lab08Activity extends Activity implements SensorEventListener {

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[3];

    private TextView msg_tv;

    private SensorManager sensorManager;
    private LocationManager locationManager;

    private Sensor gyroscopeSensor;
    private boolean displayAsRawOrAngleGyroscope;
    private float[] currentAngles = new float[3];
    private long lastTimestamp;

    private Sensor magneticSensor;
    private boolean displayAsRawOrAngleMagnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab08);
        msg_tv = findViewById(R.id.msg_tv);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    public void onShowRotationRaw(View view) {
        displayAsRawOrAngleGyroscope = true;
        initGyroscopeSensor();
    }

    private void initGyroscopeSensor() {
        if (gyroscopeSensor == null) {
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (magneticSensor != null) {
            sensorManager.unregisterListener(this, magneticSensor);
        }
    }

    public void onShowRotationAngle(View view) {
        displayAsRawOrAngleGyroscope = false;
        for (int i = 0; i < currentAngles.length; i++) {
            currentAngles[i] = 0;
        }
        lastTimestamp = 0;
        initGyroscopeSensor();
    }

    public void onMagnetometerRaw(View view) {
        displayAsRawOrAngleMagnetic = true;
        initMagneticSensor();
    }

    public void onShowDirectionAngle(View view) {
        displayAsRawOrAngleMagnetic = false;
        initMagneticSensor();
    }

    private void initMagneticSensor() {
        if (magneticSensor == null) {
            magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (gyroscopeSensor != null) {
            sensorManager.unregisterListener(this, gyroscopeSensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == gyroscopeSensor) {
            if (displayAsRawOrAngleGyroscope) {
                msg_tv.setText(String.format("The rate of rotation of axis\nX: %1$f\nY: %2$f\nZ: %3$f",
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]));
            } else {
//                final float deltaT = (sensorEvent.timestamp - lastTimestamp) * NS2S;
//                int maxAbsoluteValueIndex = 0;
//                float maxAbsoluteValue = 0;
//                for (int i = 0; i < currentAngles.length; i++) {
//                    currentAngles[i] += sensorEvent.values[i] * deltaT;
//                    if (maxAbsoluteValue < Math.abs(currentAngles[i])) {
//                        maxAbsoluteValue = Math.abs(currentAngles[i]);
//                        maxAbsoluteValueIndex = i;
//                    }
//                }

                final float dT = (sensorEvent.timestamp - lastTimestamp) * NS2S;
                float axisX = sensorEvent.values[0];
                float axisY = sensorEvent.values[1];
                float axisZ = sensorEvent.values[2];
                float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                // Normalize the rotation vector if it's big enough to get the axis
                // (that is, EPSILON should represent your maximum allowable margin of error)
                if (omegaMagnitude > EPSILON) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }

                // Integrate around this axis with the angular speed by the timestep
                // in order to get a delta rotation from this sample over the timestep
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                deltaRotationVector[0] = sinThetaOverTwo * axisX;
                deltaRotationVector[1] = sinThetaOverTwo * axisY;
                deltaRotationVector[2] = sinThetaOverTwo * axisZ;

                int maxAbsoluteValueIndex = 0;
                float maxAbsoluteValue = 0;
                for (int i = 0; i < deltaRotationVector.length; i++) {
                    deltaRotationVector[i] += deltaRotationVector[i] * dT;
                    if (maxAbsoluteValue < Math.abs(deltaRotationVector[i])) {
                        maxAbsoluteValue = Math.abs(deltaRotationVector[i]);
                        maxAbsoluteValueIndex = i;
                    }


                    if (lastTimestamp == 0) {
                        msg_tv.setText(String.format("Rotation:\n%1$f°", 0f));
                    } else {
                        msg_tv.setText(String.format("Rotation:\n%1$f°", deltaRotationVector[maxAbsoluteValueIndex]));
                    }
                    lastTimestamp = sensorEvent.timestamp;
                }


//
//
//                @SuppressWarnings("MissingPermission")
//                Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
//
//
//                GeomagneticField geomagneticField = new GeomagneticField(
//                        double2Float(location.getLatitude()),
//                        double2Float(location.getLongitude()),
//                        double2Float(location.getAltitude()),
//                        System.currentTimeMillis()
//                );
//                msg_tv.setText(
//                        geomagneticField.getDeclination() + "");
            }
        } else if (sensorEvent.sensor == magneticSensor) {
            if (displayAsRawOrAngleMagnetic) {
                msg_tv.setText(String.format("The geomagnetic field strength axis\nX: %1$f\nY: %2$f\nZ: %3$f",
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]));
            } else {
                float value = 0;
                if (sensorEvent.values[0] != 0) {
                    if (sensorEvent.values[0] > 0) {
                        value = (float) (270 + Math.atan(sensorEvent.values[1] / sensorEvent.values[0]));
                    } else {
                        value = (float) (90 + Math.atan(sensorEvent.values[1] / sensorEvent.values[0]));
                    }
                } else {
                    value = sensorEvent.values[1] > 0 ?
                            0 :
                            180;
                }
                msg_tv.setText(String.format("Exact Heading:\n%1$f\nRound Value: %2$d",
                        value,
                        (int) value));
            }
        }

    }

    private float double2Float(double d) {
        return Double.valueOf(d).floatValue();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
