package deplink.com.smartwirelessrelay.homegenius.activity;import android.app.Activity;import android.app.AlertDialog;import android.app.Fragment;import android.app.FragmentTransaction;import android.content.BroadcastReceiver;import android.content.Context;import android.content.Intent;import android.content.IntentFilter;import android.os.Bundle;import android.os.Handler;import android.util.Log;import deplink.com.smartwirelessrelay.homegenius.Devices.ConnectManager;import deplink.com.smartwirelessrelay.homegenius.Devices.SDK_Data_Listener;import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;import deplink.com.smartwirelessrelay.homegenius.util.IPV4Util;import deplink.com.smartwirelessrelay.homegenius.util.PublicMethod;public class MainActivity extends Activity implements SDK_Data_Listener {    private static final String TAG = "MainActivity";    private ConnectManager ellESDK;    private HomeFragment mHomeFragment;    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_main);        mHomeFragment = new HomeFragment();        commitFragment(R.id.main_fragment, mHomeFragment);        broadCast=new NetBroadCast();        IntentFilter filter=new IntentFilter();        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");        registerReceiver(broadCast,filter);    }    protected void commitFragment(int layout, Fragment newFragment) {        FragmentTransaction transaction = getFragmentManager().beginTransaction();        transaction.replace(layout, newFragment);        transaction.commit();    }    @Override    protected void onRestart() {        super.onRestart();    }    @Override    protected void onResume() {        super.onResume();        ellESDK = ConnectManager.getInstance();        ellESDK.InitConnectManager(this, this);        Log.i(TAG, "mainactivity  本机IP地址="+ PublicMethod.getIPAddress(this));    }    @Override    protected void onDestroy() {        super.onDestroy();        unregisterReceiver(broadCast);    }    @Override    public void onRecvDeviceIp(final byte[] packet) {        Log.i(TAG, "mainactivity 已探测到中继器设备 本机IP地址="+ PublicMethod.getIPAddress(this));        mHandler.post(new Runnable() {            @Override            public void run() {                ConnectManager.getInstance().InitTcpIpConnect(IPV4Util.trans2IpV4Str(packet));                new AlertDialog                        .Builder(MainActivity.this)                        .setTitle("已探测到中继器设备")                        .setNegativeButton("确定", null)                        .setIcon(android.R.drawable.ic_menu_agenda)                        .setMessage("设备IP:" + IPV4Util.trans2IpV4Str(packet))                        .show();            }        });    }    @Override    public void onConnectDeviceFail(final String ipaddress) {        mHandler.post(new Runnable() {            @Override            public void run() {                new AlertDialog                        .Builder(MainActivity.this)                        .setTitle("连接失败")                        .setNegativeButton("确定", null)                        .setIcon(android.R.drawable.ic_menu_agenda)                        .setMessage("设备IP:" + ipaddress)                        .show();            }        });    }    private Handler mHandler = new Handler();    private NetBroadCast broadCast = null;    /**     * 网络切换时立即检查是否需要重连     */    class NetBroadCast extends BroadcastReceiver {        public final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";        @Override        public void onReceive(Context context, Intent intent) {            if (ACTION.equals(intent.getAction())) {                //                Log.i(TAG,"网络切换时立即检查是否需要重连");            }        }    }}