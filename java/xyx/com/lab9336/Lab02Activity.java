package xyx.com.lab9336;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Lab02Activity extends Activity {

    private WifiAdapter WifiAdapter;
    private List<ScanResult> wifis;

    private boolean isTask1OrTask2;
    private BroadcastReceiver wifiBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab02);

        wifis = new ArrayList<>();
        WifiAdapter = new WifiAdapter();
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(WifiAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final ScanResult scanResultToConnect = wifis.get(i);
                new AlertDialog.Builder(Lab02Activity.this)
                        .setMessage("Do you want to connect to Wi-Fi:\n" + scanResultToConnect.SSID)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                WifiUtil.connect2Wifi(Lab02Activity.this, scanResultToConnect);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
    }

    public void onTask1(View view) {
        scan(true);
    }

    public void onTask2(View view) {
        scan(false);
    }

    private void checkWifiReceiver() {
        WifiUtil.enableWifi();

        if (wifiBroadcastReceiver == null) {
            wifiBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                        List<ScanResult> result = WifiUtil.getScanResults();
                        if (isTask1OrTask2) {
                            wifis = result;
                        } else {
                            Iterator<ScanResult> iterator = result.iterator();
                            String sameSSID = "";
                            int sameCount = 0;
                            while (iterator.hasNext()) {
                                ScanResult scanResult = iterator.next();
                                if (sameSSID.equals(scanResult.SSID)) {
                                    if (sameCount > 3) {
                                        iterator.remove();
                                    }
                                } else {
                                    sameSSID = scanResult.SSID;
                                    sameCount = 0;
                                }
                                sameCount++;
                            }
                            wifis = result;
                        }
                        WifiAdapter.notifyDataSetChanged();
                    } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                        if (info != null && info.getState().equals(NetworkInfo.State.CONNECTED)) {
                            WifiInfo wifiInfo = WifiUtil.getConnectionInfo();
                            new AlertDialog.Builder(Lab02Activity.this)
                                    .setTitle(WifiUtil.removeQuote(wifiInfo.getSSID()) + "'s Info:")
                                    .setMessage(String.format("ESSID: %1$s\n       IP: %2$s",
                                            wifiInfo.getBSSID(),
                                            WifiUtil.getIpAddress(wifiInfo.getIpAddress())))
                                    .show();
                        }
                    }
                }
            };
            WifiUtil.registerWifiScanReceiver(this, wifiBroadcastReceiver);
            WifiUtil.registerWifiChangeReceiver(this, wifiBroadcastReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiBroadcastReceiver != null) {
            unregisterReceiver(wifiBroadcastReceiver);
        }
    }

    private void scan(boolean isTask1OrTask2) {
        checkWifiReceiver();
        this.isTask1OrTask2 = isTask1OrTask2;
        WifiUtil.scanWifi();
        wifis.clear();
        WifiAdapter.notifyDataSetChanged();
    }

    private class WifiAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return wifis.size();
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
            TextView tv = (TextView) view;
            tv.setText((i + 1) + ". " + wifis.get(i).SSID);
            return view;
        }
    }
}
