package xyx.com.lab9336;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Lab05Activity extends Activity {

    private TextView msg_tv;

    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver bluetoothBroadcastReceiver;

    private BluetoothDeviceAdapter bluetoothDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab05);
        msg_tv = findViewById(R.id.msg_tv);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        onCheck(null);
    }

    public void onCheck(View view) {
        if (bluetoothAdapter == null) {
            msg_tv.setText(Html.fromHtml("Sorry, it seems your device does not support <b>Bluetooth</b>..."));
            findViewById(R.id.btn).setEnabled(false);
        } else {
            findViewById(R.id.btn).setEnabled(true);
            if (bluetoothAdapter.isEnabled()) {
                msg_tv.setText("Bluetooth on your device is ENABLE.");
            } else {
                msg_tv.setText("Bluetooth on your device is DISABLE.");
            }
            ListView listView = findViewById(R.id.listView);
            bluetoothDeviceAdapter = new BluetoothDeviceAdapter();
            listView.setAdapter(bluetoothDeviceAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final BluetoothDevice bluetoothDevice = bluetoothDeviceAdapter.bluetoothDevices.get(i);
                    new AlertDialog.Builder(Lab05Activity.this)
                            .setMessage(Html.fromHtml(String.format("Do you wan to connect to <b>%1$s</b>?",
                                    TextUtils.isEmpty(bluetoothDevice.getName()) ?
                                            bluetoothDevice.getAddress() :
                                            bluetoothDevice.getName())))
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    connect(bluetoothDevice);
                                }
                            })
                            .show();
                }
            });
        }
    }

    private Boolean connect(BluetoothDevice bdDevice) {
        Boolean bool = false;
        try {
            Class cls = Class.forName("android.bluetooth.BluetoothDevice");
            Class[] par = {};
            Method method = cls.getMethod("createBond", par);
            Object[] args = {};
            bool = (Boolean) method.invoke(bdDevice);//, args);// this invoke creates the detected devices paired.
            //Log.i("Log", "This is: "+bool.booleanValue());
            //Log.i("Log", "devicesss: "+bdDevice.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool.booleanValue();
    }

    public boolean removeBond(BluetoothDevice btDevice) throws Exception {
        Class cls = Class.forName("android.bluetooth.BluetoothDevice");
        Method removeBondMethod = cls.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }


    public boolean createBond(BluetoothDevice btDevice) throws Exception {
        Class cls = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = cls.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    public void onScan(View view) {
        if (bluetoothBroadcastReceiver == null) {
            bluetoothBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        bluetoothDeviceAdapter.addData(device);
                    }
                }
            };
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(bluetoothBroadcastReceiver, filter);
        }
        bluetoothDeviceAdapter.clear();
        bluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothBroadcastReceiver != null) {
            unregisterReceiver(bluetoothBroadcastReceiver);
        }
    }

    private class BluetoothDeviceAdapter extends BaseAdapter {

        private List<BluetoothDevice> bluetoothDevices;

        public BluetoothDeviceAdapter() {
            bluetoothDevices = new ArrayList<>();
        }

        public void addData(BluetoothDevice bluetoothDevice) {
            bluetoothDevices.add(bluetoothDevice);
            notifyDataSetChanged();
        }

        public void clear() {
            bluetoothDevices.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return bluetoothDevices.size();
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
                view = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_2, null);
            }
            TextView tv1 = view.findViewById(android.R.id.text1);
            TextView tv2 = view.findViewById(android.R.id.text2);
            BluetoothDevice bluetoothDevice = bluetoothDevices.get(i);
            tv1.setText(bluetoothDevice.getName());
            tv2.setText(bluetoothDevice.getAddress());
            return view;
        }
    }
}