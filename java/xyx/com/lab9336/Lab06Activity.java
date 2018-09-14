package xyx.com.lab9336;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class Lab06Activity extends Activity implements View.OnClickListener, LocationListener {

    private Button btn0, btn1;
    private TextView txt;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab06);
        btn0 = findViewById(R.id.btn0);
        btn0.setOnClickListener(this);
        btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(this);
        txt = findViewById(R.id.txt);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn0: {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    @SuppressWarnings("MissingPermission")
                    Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    txt.setText(String.format("GPS is enabled\n\nDate/Time: %1$s\nProvider: %2$s\nAccuracy: %3$f\nAltitude: %4$f\nLongitude: %5$f\nLatitude: %6$f\nSpeed: %7$f",
                            new Date(location.getTime()).toLocaleString(),
                            location.getProvider(),
                            location.getAccuracy(),
                            location.getAltitude(),
                            location.getLongitude(),
                            location.getLatitude(),
                            location.getSpeed()));
                    btn1.setVisibility(View.VISIBLE);
                } else {
                    txt.setText("GPS is disabled");
                    btn1.setVisibility(View.GONE);
                    new AlertDialog.Builder(this)
                            .setMessage("GPS is disabled\nDo you want to goto 'Setting' to enable it?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                            .show();
                }
            }
            break;
            case R.id.btn1:
                //noinspection MissingPermission
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        txt.setText(String.format("My current location information:\n\nDate/Time: %1$s\nProvider: %2$s\nAccuracy: %3$f\nAltitude: %4$f\nLongitude: %5$f\nLatitude: %6$f\nSpeed: %7$f",
                new Date(location.getTime()).toLocaleString(),
                location.getProvider(),
                location.getAccuracy(),
                location.getAltitude(),
                location.getLongitude(),
                location.getLatitude(),
                location.getSpeed()));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
