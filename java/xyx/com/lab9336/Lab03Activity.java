package xyx.com.lab9336;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.widget.TextView;

public class Lab03Activity extends Activity {

    private TextView msg_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab03);
        msg_tv = findViewById(R.id.msg_tv);
        showCurrentConnectionInfo();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showCurrentConnectionInfo() {
        msg_tv.setText(msg_tv.getText() + "\n\n\n" + (
                WifiUtil.is5GSupported() ?
                        "Your device supports 5GHz Wi-Fi connection." :
                        "Your device does not support 5GHz Wi-Fi connection."
        ));

        if (WifiUtil.isWifiConnected()) {
            WifiInfo wifiInfo = WifiUtil.getConnectionInfo();
            msg_tv.setText(msg_tv.getText() + "\n\n\n" +
                    String.format("Your device is connecting to %1$s.\nIt's frequency is %2$.3f GHz.\nIt's bit rate is %3$d %4$s.",
                            WifiUtil.removeQuote(wifiInfo.getSSID()),
                            wifiInfo.getFrequency() / 1000f,
                            wifiInfo.getLinkSpeed(),
                            WifiInfo.LINK_SPEED_UNITS)

            );

            String ssid = WifiUtil.removeQuote(wifiInfo.getSSID());
            for (ScanResult scanResult : WifiUtil.getScanResults()) {
                if (scanResult.SSID.equals(ssid)) {
                    String _802_11Type = WifiUtil.get802_11Type(scanResult);
                    msg_tv.setText(msg_tv.getText() + "\n" +
                            String.format("It's 802.11 protocol is %1$s.\nIt's modulation is %2$s.",
                                    _802_11Type,
                                    WifiUtil.getModulation(_802_11Type))
                    );
                    break;
                }
            }


        } else {
            msg_tv.setText(msg_tv.getText().toString() + '\n' +
                    "Your device is not connecting to any Wi-Fi network now."
            );
        }
    }
}