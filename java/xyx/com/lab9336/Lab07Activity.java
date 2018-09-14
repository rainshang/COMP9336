package xyx.com.lab9336;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class Lab07Activity extends Activity {

    private SensorAdapter sensorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab07);
        ListView listView = findViewById(R.id.listView);
        sensorAdapter = new SensorAdapter();
        listView.setAdapter(sensorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Sensor sensor = sensorAdapter.sensors.get(i);
                if (Sensor.TYPE_ACCELEROMETER == sensor.getType()) {
                    startActivity(new Intent(getApplicationContext(), Lab07_1Activity.class));
                }
            }
        });
    }

    public void onShowAllSensors(View view) {
        sensorAdapter.refreshData();
    }

    private class SensorAdapter extends BaseAdapter {

        private List<Sensor> sensors;

        public void refreshData() {
            SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return sensors != null ? sensors.size() : 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_1, null);
            }
            TextView textView = (TextView) view;
            Sensor sensor = sensors.get(i);
            textView.setText(String.format("%1$d) Name: %2$s\nVendor: %3$s\nMaximumRange: %4$f\nMinDelay: %5$d",
                    i + 1,
                    sensor.getName(),
                    sensor.getVendor(),
                    sensor.getMaximumRange(),
                    sensor.getMinDelay()));
            return view;
        }
    }
}
