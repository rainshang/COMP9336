package xyx.com.lab9336;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class LabListActivity extends ListActivity {

    private final static int REQUEST_CODE_LOCATION_PERMISSION = 7;
    private final static int REQUEST_CODE_EXTERNAL_STORAGE_PERMISSION = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WifiUtil.init(getApplicationContext());
        setListAdapter(new LabAdapter());
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object cls = adapterView.getAdapter().getItem(i);
                if (cls != null) {
                    startActivity(new Intent(getApplicationContext(), (Class<?>) cls));
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED
                    != checkSelfPermission(Manifest.permission_group.LOCATION)) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_CODE_LOCATION_PERMISSION);
            }

            if (PackageManager.PERMISSION_GRANTED
                    != checkSelfPermission(Manifest.permission_group.STORAGE)) {
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, REQUEST_CODE_EXTERNAL_STORAGE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION
                || requestCode == REQUEST_CODE_EXTERNAL_STORAGE_PERMISSION) {
            for (int grantResult : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != grantResult) {
                    Snackbar.make(getListView(), "Cannot work properly without these permissions!", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    private class LabAdapter extends BaseAdapter {
        private final static int LAB_COUNT = 9;

        @Override
        public int getCount() {
            return LAB_COUNT;
        }

        @Override
        public Object getItem(int i) {
            try {
                return Class.forName(String.format("%1$s.Lab%2$02dActivity", getPackageName(), getItemId(i)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Snackbar.make(getListView(), e.toString(), Snackbar.LENGTH_LONG).show();
                return null;
            }
        }

        @Override
        public long getItemId(int i) {
            return i + 2;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_1, null);
            }
            TextView tv = (TextView) view;
            tv.setGravity(Gravity.CENTER);
            tv.setText(String.format("Lab %1$02d", getItemId(i)));
            return view;
        }
    }

}