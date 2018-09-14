package xyx.com.lab9336;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collection;

public class Lab04Activity extends Activity {

    private TextView msg_tv;

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private WifiAdapter wifiAdapter;

    private BroadcastReceiver broadcastReceiver;
    private WifiP2pDevice wifiP2pDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab04);
        msg_tv = findViewById(R.id.msg_tv);

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (wifiP2pManager != null) {
            channel = wifiP2pManager.initialize(getApplicationContext(), getMainLooper(), null);
        } else {
            msg_tv.setText(Html.fromHtml("Sorry, your device seems not supporting <b>Wi-Fi Direct</b>..."));
            findViewById(R.id.btn).setEnabled(false);
        }

        ListView listView = findViewById(R.id.listView);
        wifiAdapter = new WifiAdapter();
        listView.setAdapter(wifiAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                wifiP2pDevice = wifiAdapter.wifiP2pDevices[i];
                new AlertDialog.Builder(Lab04Activity.this)
                        .setMessage("Do you want to connect to:\n" + wifiP2pDevice.deviceName)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, final int i) {
                                WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                                wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress;
                                wifiP2pManager.connect(channel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Snackbar.make(msg_tv, "Connection request has been sent, please check the message on " + wifiP2pDevice.deviceName, Snackbar.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onFailure(int i) {
                                        wifiP2pDevice = null;
                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (wifiP2pDevice != null) {
                    Snackbar.make(msg_tv, String.format("You've connected %1$s to successfully!", wifiP2pDevice.deviceName), Snackbar.LENGTH_LONG).show();
                }
            }
        };

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        registerReceiver(broadcastReceiver, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    public void onScan(final View view) {
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {

                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                        wifiAdapter.refreshWithData(wifiP2pDeviceList.getDeviceList());
                        if (!wifiP2pDeviceList.getDeviceList().isEmpty()) {
                            msg_tv.setText(Html.fromHtml("Detected these devices supporting <b>Wi-Fi Direct</b>:"));
                        } else {
                            msg_tv.setText(Html.fromHtml("Opps, it seems there is no available <b>Wi-Fi Direct</b> device."));
                        }
                    }
                });
            }

            @Override
            public void onFailure(int i) {
                Snackbar.make(msg_tv, "Sorry, something wrong happened...", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private class WifiAdapter extends BaseAdapter {

        private WifiP2pDevice[] wifiP2pDevices;

        public void refreshWithData(Collection<WifiP2pDevice> wifiP2pDevices) {
            this.wifiP2pDevices = wifiP2pDevices.toArray(new WifiP2pDevice[]{});
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return wifiP2pDevices != null ? wifiP2pDevices.length : 0;
        }

        @Override
        public Object getItem(int i) {
            return wifiP2pDevices[i];
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
            tv.setText(wifiP2pDevices[i].toString());
            return view;
        }
    }
}
