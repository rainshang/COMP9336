package xyx.com.lab9336;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Ethan on 14/8/17.
 */

public class WifiUtil {

    private final static String FORMAT_QUOTE = "\"%1$s\"";

    private static Context mContext;
    private static WifiManager mWifiManager;

    public static void init(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
    }

    public static boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public static void enableWifi() {
        if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    public static void scanWifi() {
        mWifiManager.startScan();
        Toast.makeText(mContext, "Scanning wifi ...", Toast.LENGTH_LONG).show();
    }

    public static void registerWifiScanReceiver(Context context, BroadcastReceiver broadcastReceiver) {
        context.registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public static void registerWifiChangeReceiver(Context context, BroadcastReceiver broadcastReceiver) {
        context.registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
    }

    public static WifiInfo getConnectionInfo() {
        return mWifiManager.getConnectionInfo();
    }

    public static List<ScanResult> getScanResults() {
        List<ScanResult> result = mWifiManager.getScanResults();
        Collections.sort(result, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult t0, ScanResult t1) {
                int nameSort = t0.SSID.compareTo(t1.SSID);//sort by name
                if (nameSort == 0) {
                    return t1.level - t0.level;//sort by signal strength
                }
                return nameSort;
            }
        });
        //remove empty ssid wifi
        Iterator<ScanResult> iterator = result.iterator();
        while (iterator.hasNext()) {
            ScanResult scanResult = iterator.next();
            if (TextUtils.isEmpty(scanResult.SSID)) {
                iterator.remove();
            }
        }
        return result;
    }

    public static void connect2Wifi(Activity activity, final ScanResult targetScanResult) {
        WifiConfiguration wifiConfiguration = isExistingWifi(targetScanResult.SSID);
        if (wifiConfiguration != null) {
            mWifiManager.enableNetwork(wifiConfiguration.networkId, true);
            showConnectingToast(targetScanResult.SSID);
        } else {
            if (targetScanResult.capabilities.contains("EAP")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                LinearLayout linearLayout = new LinearLayout(activity);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final EditText usernameET = new EditText(activity);
                usernameET.setHint("Username");
                final EditText pwdET = new EditText(activity);
                pwdET.setHint("Password");
                pwdET.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                linearLayout.addView(usernameET);
                linearLayout.addView(pwdET);
                builder.setTitle("Please fulfil the form to continue");
                builder.setView(linearLayout);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        connect2PEAP(targetScanResult.SSID, usernameET.getText().toString(), pwdET.getText().toString());
                    }
                });
                builder.show();
            } else if (targetScanResult.capabilities.contains("WEP")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                final EditText pwdET = new EditText(activity);
                pwdET.setHint("Password");
                builder.setTitle("Please fulfil the form to continue");
                builder.setView(pwdET);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        connect2WEP(targetScanResult.SSID, pwdET.getText().toString());
                    }
                });
                builder.show();
            } else if (targetScanResult.capabilities.contains("WPA2")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                final EditText pwdET = new EditText(activity);
                pwdET.setHint("Password");
                builder.setTitle("Please fulfil the form to continue");
                builder.setView(pwdET);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        connect2WPA2(targetScanResult.SSID, pwdET.getText().toString());
                    }
                });
                builder.show();
            } else if (targetScanResult.capabilities.contains("WPA")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                final EditText pwdET = new EditText(activity);
                pwdET.setHint("Password");
                builder.setTitle("Please fulfil the form to continue");
                builder.setView(pwdET);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        connect2WPA(targetScanResult.SSID, pwdET.getText().toString());
                    }
                });
                builder.show();
            } else {
                connect2Open(targetScanResult.SSID);
            }
        }
    }

    private static void showConnectingToast(String ssid) {
        Toast.makeText(mContext, String.format("Connecting to %1$s ...", ssid), Toast.LENGTH_LONG).show();
    }

    private static WifiConfiguration getWifiConfiguration(String ssid) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.allowedAuthAlgorithms.clear();
        wifiConfiguration.allowedGroupCiphers.clear();
        wifiConfiguration.allowedKeyManagement.clear();
        wifiConfiguration.allowedPairwiseCiphers.clear();
        wifiConfiguration.allowedProtocols.clear();
        wifiConfiguration.SSID = toConfigFormat(ssid);
        return wifiConfiguration;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static void connect2PEAP(String ssid, String username, String password) {
        WifiConfiguration wifiConfiguration = getWifiConfiguration(ssid);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
        WifiEnterpriseConfig wifiEnterpriseConfig = new WifiEnterpriseConfig();
        wifiEnterpriseConfig.setIdentity(username);
        wifiEnterpriseConfig.setPassword(password);
        wifiEnterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
        wifiConfiguration.enterpriseConfig = wifiEnterpriseConfig;
        saveAndConnect(wifiConfiguration);
    }

    private static void saveAndConnect(WifiConfiguration wifiConfiguration) {
        wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
        int networkId = mWifiManager.addNetwork(wifiConfiguration);
        mWifiManager.enableNetwork(networkId, true);
        showConnectingToast(wifiConfiguration.SSID);
    }

    private static void connect2WEP(String ssid, String password) {
        WifiConfiguration wifiConfiguration = getWifiConfiguration(ssid);
        wifiConfiguration.hiddenSSID = true;
        wifiConfiguration.wepKeys[0] = toConfigFormat(password);
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiConfiguration.wepTxKeyIndex = 0;
        saveAndConnect(wifiConfiguration);
    }

    private static void connect2WPA2(String ssid, String password) {
        WifiConfiguration wifiConfiguration = getWifiConfiguration(ssid);
        wifiConfiguration.hiddenSSID = true;
        wifiConfiguration.preSharedKey = toConfigFormat(password);
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        saveAndConnect(wifiConfiguration);
    }

    private static void connect2WPA(String ssid, String password) {
        WifiConfiguration wifiConfiguration = getWifiConfiguration(ssid);
        wifiConfiguration.hiddenSSID = true;
        wifiConfiguration.preSharedKey = toConfigFormat(password);
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        saveAndConnect(wifiConfiguration);
    }

    private static void connect2Open(String ssid) {
        WifiConfiguration wifiConfiguration = getWifiConfiguration(ssid);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiConfiguration.wepTxKeyIndex = 0;
        saveAndConnect(wifiConfiguration);
    }

    private static WifiConfiguration isExistingWifi(String ssid) {
        String configSsid = toConfigFormat(ssid);
        List<WifiConfiguration> wifiConfigurations = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
            if (wifiConfiguration.SSID.equals(configSsid)) {
                return wifiConfiguration;
            }
        }
        return null;
    }

    private static String toConfigFormat(String value) {
        return String.format(FORMAT_QUOTE, value);
    }

    public static String removeQuote(String value) {
        return value.substring(1, value.length() - 1);
    }

    public static String getIpAddress(int ipAdress) {
        return (ipAdress & 0xFF) + "." +
                ((ipAdress >> 8) & 0xFF) + "." +
                ((ipAdress >> 16) & 0xFF) + "." +
                (ipAdress >> 24 & 0xFF);
    }

    public static boolean is5GSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mWifiManager.is5GHzBandSupported();
        } else {
            try {
                Class cls = Class.forName("android.net.wifi.WifiManager");
                Method method = cls.getMethod("isDualBandSupported");
                Object invoke = method.invoke(mWifiManager);
                boolean is5GhzSupported = (boolean) invoke;
                return is5GhzSupported;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean isWifiConnected() {
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    public static String get802_11Type(ScanResult scanResult) {
        Class cls = scanResult.getClass();
        try {
            Field field = cls.getField("ChannelMode");
            String channelMode = (String) field.get(scanResult);
            return "802." + channelMode.split("_")[0];
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getModulation(String _802_11Type) {
        switch (_802_11Type) {
            case "802.11-1997":
                return "DSSS, FHSS";
            case "802.11a":
            case "802.11g":
                return "OFDM";
            case "802.11b":
                return "DSSS";
            case "802.11n":
            case "802.11ac":
            case "802.11ax":
                return "MIMO-OFDM";
            case "802.11ad":
                return "OFDM, single carrier, low-power single carrier";
            case "802.11ay":
                return "OFDM, single carrier";
            case "802.11-2007":
            case "802.11-2012":
            case "802.11-2016":
                return "DSSS, OFDM";
            default:
                return null;
        }
    }

}
