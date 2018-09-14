package xyx.com.lab9336;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Lab09Activity extends Activity {

    private final static long ONE_MINUTE = 1000l * 60;
    private final static long MONITOR_DURATION = ONE_MINUTE * 10;

    private Button btn0, btn1;
    private TextView msg_tv0, msg_tv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab09);
        btn0 = findViewById(R.id.btn0);
        btn1 = findViewById(R.id.btn1);
        msg_tv0 = findViewById(R.id.msg_tv0);
        msg_tv1 = findViewById(R.id.msg_tv1);

        btn1.setText(String.format("Start to monitor battery in %1$d minute(s)", MONITOR_DURATION / ONE_MINUTE));

        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float batteryPct = getCurrentBatteryPercentage();
                StringBuilder stringBuilder = new StringBuilder(String.format("Current level of battery is %1$.1f%%", batteryPct));

                Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                switch (chargePlug) {
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        stringBuilder.append("\nMobile is charging via AC");
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        stringBuilder.append("\nMobile is charging via USB");
                        break;
                    case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                        stringBuilder.append("\nMobile is charging via Wireless");
                        break;
                }
                msg_tv0.setText(stringBuilder);
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {

            private float startPercentage;

            @Override
            public void onClick(View v) {
                btn1.setEnabled(false);
                startPercentage = getCurrentBatteryPercentage();
                new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        float endPercentage = getCurrentBatteryPercentage();
                        msg_tv1.setText(String.format("Within last %1$d minute(s):\nInitial level of battery: %2$.1f%%\nFinal level of battery: %3$.1f%%\nConsumed battery: %4$.1f%%\n",
                                MONITOR_DURATION / ONE_MINUTE,
                                startPercentage,
                                endPercentage,
                                startPercentage - endPercentage));
                        btn1.setEnabled(true);
                    }
                }.sendEmptyMessageDelayed(0, MONITOR_DURATION);
            }
        });
    }

    private float getCurrentBatteryPercentage() {
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return level * 100f / scale;
    }
}