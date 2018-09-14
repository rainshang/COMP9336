package xyx.com.lab9336;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Lab10Activity extends Activity {

    private final static String KEY_INPUT_CONTENT = "KEY_INPUT_CONTENT";
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String AP_FILE;

    private EditText et;

    private WifiAdapter wifiAdapter;
    private BroadcastReceiver wifiBroadcastReceiver;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab10);
        AP_FILE = getApplicationContext().getExternalCacheDir() + "/aps.txt";

        et = findViewById(R.id.et);
        progressDialog = new ProgressDialog(this);

        wifiAdapter = new WifiAdapter();
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(wifiAdapter);


        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
        TextView msg_tv = findViewById(R.id.msg_tv);
        msg_tv.setText(Formatter.formatFileSize(getApplicationContext(), bytesAvailable));
    }

    public void onReadFromSP(View view) {
        et.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(KEY_INPUT_CONTENT, null));
        et.setSelection(et.getText().length());

    }

    public void onSave2SP(View view) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit()
                .putString(KEY_INPUT_CONTENT, et.getText().toString())
                .commit();
    }

    public void onReadFromFile(View view) {
        new AsyncTask<Void, Void, List<String>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
            }

            @Override
            protected List<String> doInBackground(Void... params) {
                try {
                    String string = new Scanner(new File(AP_FILE)).useDelimiter("\\Z").next();
                    String[] rawStrings = string.split("\n");
                    List<String> strings = new ArrayList<>();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < rawStrings.length; i++) {
                        if (i != 0 && i % 4 == 0) {
                            strings.add(stringBuilder.toString());
                            stringBuilder = new StringBuilder();
                        }
                        stringBuilder.append(rawStrings[i] + '\n');
                    }
                    return strings;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<String> strings) {
                super.onPostExecute(strings);
                progressDialog.dismiss();
                wifiAdapter.freshWithData(strings);
            }
        }.execute();
    }

    public void onSave2File(View view) {
        scan();
    }

    private void checkWifiReceiver() {
        WifiUtil.enableWifi();

        if (wifiBroadcastReceiver == null) {
            wifiBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                        List<ScanResult> result = WifiUtil.getScanResults();

                        new AsyncTask<List<ScanResult>, Void, Boolean>() {

                            @Override
                            protected Boolean doInBackground(List<ScanResult>... params) {
                                List<ScanResult> result = params[0];

                                @SuppressWarnings("MissingPermission")
                                Location location = ((LocationManager) getSystemService(LOCATION_SERVICE))
                                        .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                                StringBuilder toSaveResult = new StringBuilder();
                                for (ScanResult scanResult : result) {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append("1-Date: " + DATE_FORMAT.format(new Date(scanResult.timestamp)) + '\n');
                                    stringBuilder.append(String.format("2-Location: latitude:%1$f longitude:%2$f\n", location.getLatitude(), location.getLongitude()));
                                    stringBuilder.append("3-SSID: " + scanResult.SSID + '\n');
                                    stringBuilder.append(String.format("4-Signal strength: %1$d dBm\n", scanResult.level));
                                    toSaveResult.append(stringBuilder);
                                }

                                File file = new File(AP_FILE);
                                if (file.exists()) {
                                    file.delete();
                                }
                                try {
                                    file.createNewFile();
                                    PrintWriter printWriter = new PrintWriter(file);
                                    printWriter.print(toSaveResult);
                                    printWriter.close();
                                    return true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            }

                            @Override
                            protected void onPostExecute(Boolean aBoolean) {
                                super.onPostExecute(aBoolean);
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), aBoolean ?
                                        "AP info has been saved successfully!" :
                                        "AP info has been saved failed!", Toast.LENGTH_LONG).show();
                            }
                        }.execute(result);
                    }
                }
            };
            WifiUtil.registerWifiScanReceiver(this, wifiBroadcastReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiBroadcastReceiver != null) {
            unregisterReceiver(wifiBroadcastReceiver);
        }
    }

    private void scan() {
        progressDialog.show();
        checkWifiReceiver();
        WifiUtil.scanWifi();
        wifiAdapter.notifyDataSetChanged();
    }

    private class WifiAdapter extends BaseAdapter {

        private List<String> data;

        public void freshWithData(List<String> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
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
            tv.setText(data.get(i));
            return view;
        }
    }
}
