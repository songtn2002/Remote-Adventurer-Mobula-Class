package tech.experiment.songtn.remoteadventurermobulaclass;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static tech.experiment.songtn.remoteadventurermobulaclass.ConnectActivity.WifiCipherType.WIFICIPHER_NOPASS;
import static tech.experiment.songtn.remoteadventurermobulaclass.ConnectActivity.WifiCipherType.WIFICIPHER_WEP;
import static tech.experiment.songtn.remoteadventurermobulaclass.ConnectActivity.WifiCipherType.WIFICIPHER_WPA;

public class ConnectActivity extends AppCompatActivity {

    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    private ProgressDialog progressDialog;
    private  ProgressDialog progressConnect;

    private ListView connectList;
    private ImageButton connectButton;

    private WifiManager wifiManager;
    private IntentFilter wifiScanIF;
    private List<ScanResult> scanResults = null;
    private WifiScanReceiver wifiScanReceiver;
    private List<Hotspot> hotspotList = new ArrayList<>();

    private Hotspot selected;

    {
        wifiScanIF = new IntentFilter();
        wifiScanIF.addAction("android.net.wifi.SCAN_RESULTS");
        wifiScanReceiver = new WifiScanReceiver();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_layout);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        wifiManager= (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
        enableWifiScan();
        registerReceiver(wifiScanReceiver, wifiScanIF);
        connectList = (ListView) findViewById(R.id.connect_list);
        connectList.setVisibility(GONE);
        progressDialog = new ProgressDialog(ConnectActivity.this);
        progressDialog.setTitle("Scanning for Available Hotspots");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressConnect = new ProgressDialog(ConnectActivity.this);
        progressConnect.setTitle("Connecting to Hotspot");
        progressConnect.setMessage("Please wait...");
        progressConnect.setCancelable(false);
        connectButton = (ImageButton) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiManager.disconnect();
                if(selected==null){
                    Toast.makeText(ConnectActivity.this, "No hotspot selected",Toast.LENGTH_SHORT).show();
                }else{
                    connect2Hotspot(selected);
                    progressConnect.show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void connect2Hotspot(Hotspot selected) {
        WifiConfiguration wc = isExsits(selected.getName());
        if (wc!=null){
            connect(wc);
        }else{
            wc = createWifiConfig(selected.getName(), selected.getPassword(), selected.getType());
            connect(wc);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiScanReceiver);
    }

    private void enableWifiScan(){
        if (!wifiManager.isWifiEnabled()) {
            //开启wifi
            wifiManager.setWifiEnabled(true);
        }else{
            wifiManager.disconnect();
        }
        wifiManager.startScan();
    }

    class WifiScanReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                // wifi已成功扫描到可用wifi。
                scanResults = wifiManager.getScanResults();
                Toast.makeText(ConnectActivity.this, "scanned"+ String.valueOf(scanResults.size()),Toast.LENGTH_SHORT).show();
                for (ScanResult sr: scanResults){
                    WifiCipherType wc = null;
                    String capabilities = sr.capabilities;
                    wc = WIFICIPHER_WPA;
                    if (!TextUtils.isEmpty(capabilities)) {
                        if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                            wc = WIFICIPHER_WPA;
                        } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                            wc = WIFICIPHER_WEP;
                        } else {
                            wc = WIFICIPHER_NOPASS;
                        }
                    }
                    if (sr.SSID.contains("submarine")) hotspotList.add(new Hotspot(sr.SSID,wc));
                }
                HotspotAdapter adapter = new HotspotAdapter(ConnectActivity.this, R.layout.hotspot_item, hotspotList);
                connectList.setAdapter(adapter);
                connectList.setVisibility(View.VISIBLE);
                connectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Hotspot hotspot = hotspotList.get(position);
                        selected = hotspot;
                        view.setBackgroundColor(Color.YELLOW);
                        Toast.makeText(ConnectActivity.this, "Hotspot: "+hotspot.getName()+" selected",Toast.LENGTH_SHORT).show();
                    }
                });
                progressDialog.dismiss();
            }else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String SSID = wifiInfo.getSSID();
                    if (SSID.contains("Adventurer")||SSID.contains("Mobula")){
                        progressConnect.dismiss();
                        Intent intent_control = new Intent(ConnectActivity.this, ControlActivity.class);
                        startActivity(intent_control);
                    }
                }
            }

        }
    }

    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    private void connect(WifiConfiguration config) {
        int wcgID = wifiManager.addNetwork(config);
        wifiManager.enableNetwork(wcgID, true);
    }

    private WifiConfiguration createWifiConfig(String ssid, String password, WifiCipherType type) {
        //初始化WifiConfiguration
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        //指定对应的SSID
        config.SSID = "\"" + ssid + "\"";

        //不需要密码的场景
        if(type == WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //以WEP加密的场景
        } else if(type == WIFICIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0]= "\""+password+"\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            //以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
        } else if(type == WIFICIPHER_WPA) {
            config.preSharedKey = "\""+password+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }
}
