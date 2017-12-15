package deplink.com.smartwirelessrelay.homegenius.manager.connect.local.tcp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import deplink.com.smartwirelessrelay.homegenius.Protocol.json.QueryOptions;
import deplink.com.smartwirelessrelay.homegenius.Protocol.json.device.lock.alertreport.Info;
import deplink.com.smartwirelessrelay.homegenius.Protocol.json.device.lock.alertreport.ReportAlertRecord;
import deplink.com.smartwirelessrelay.homegenius.Protocol.packet.GeneralPacket;
import deplink.com.smartwirelessrelay.homegenius.constant.AppConstant;
import deplink.com.smartwirelessrelay.homegenius.constant.ComandID;
import deplink.com.smartwirelessrelay.homegenius.manager.connect.ConnectionMonitor;
import deplink.com.smartwirelessrelay.homegenius.manager.connect.local.udp.UdpManager;
import deplink.com.smartwirelessrelay.homegenius.manager.connect.local.udp.interfaces.UdpManagerGetIPLintener;
import deplink.com.smartwirelessrelay.homegenius.util.DataExchange;
import deplink.com.smartwirelessrelay.homegenius.util.NetStatusUtil;
import deplink.com.smartwirelessrelay.homegenius.util.NetUtil;

/**
 * Created by Administrator on 2017/11/7.
 * 本地连接管理
 * 1.监听网络状态NetStatuChangeReceiver.onNetStatuschangeListener,只有wifi状态才能建立连接
 * 2.建立长连接后需要启动模拟心跳的线程.
 * 3.本地连接需要在udp广播后获取到连接ip地址后才能运行
 * 使用：
 * mLocalConnectmanager=LocalConnectmanager.getInstance();
 * mLocalConnectmanager.InitLocalConnectManager(this);
 * mLocalConnectmanager.addLocalConnectListener(this);
 */
public class LocalConnectmanager extends Binder implements UdpManagerGetIPLintener {

    private static final String TAG = "LocalConnectmanager";
    /**
     * 这个类设计成单例
     */
    private static LocalConnectmanager instance;
    private List<LocalConnecteListener> mLocalConnecteListener;
    private Context mContext;
    /**
     * 连接线程
     */
    private Thread connectThread;
    /**
     * 心跳包线程
     */
    private Thread monitorThread;
    /**
     * 模拟发送心跳包
     */
    private ConnectionMonitor connectionMonitor;
    /**
     * sslsocket套接字
     */
    private SSLSocket sslSocket;
    private UdpManager mUdpmanager;
    private InetSocketAddress address;

    /**
     * sslsocket握手成功
     */
    private boolean handshakeCompleted;

    public boolean isHandshakeCompleted() {
        return handshakeCompleted;
    }

    public SSLSocket getSslSocket() {
        return sslSocket;
    }

    private LocalConnectmanager() {
    }

    public static synchronized LocalConnectmanager getInstance() {
        if (instance == null) {
            instance = new LocalConnectmanager();
        }
        return instance;
    }

    private String appAuth;

    /**
     * 初始化本地连接管理器
     */
    public int InitLocalConnectManager(Context context, String bindAppAuth) {
        this.mContext = context;
        this.appAuth = bindAppAuth;
        this.mLocalConnecteListener = new ArrayList<>();
        if (mUdpmanager == null) {
            mUdpmanager = UdpManager.getInstance();
            mUdpmanager.InitUdpConnect(context, this);
        }
        registerNetBroadcast(context);
        return 0;
    }

    public void removeLocalConnectListener(LocalConnecteListener listener) {
        if (listener != null && mLocalConnecteListener.contains(listener)) {
            Log.i(TAG, "removeLocalConnectListener=" + listener.toString());
            this.mLocalConnecteListener.remove(listener);
        }

    }

    public void addLocalConnectListener(LocalConnecteListener listener) {
        if (listener != null && !mLocalConnecteListener.contains(listener)) {
            Log.i(TAG, "addLocalConnectListener=" + listener.toString());
            this.mLocalConnecteListener.add(listener);
        }
    }


    /**
     * 初始化本地连接管理器
     */
    public int InitConnect(String ipAddress) {
        initConnectThread(ipAddress);
        initMonitorThread();
        return 0;
    }

    /**
     * 初始化sslsocket连接线程
     */
    private void initConnectThread(final String ipAddress) {
        if (connectThread != null) {
            connectThread.run();
        } else {
            connectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sslSocket(ipAddress);
                }
            });
            connectThread.start();
        }

    }

    /**
     * 初始化发送心跳包的任务
     */
    private void initMonitorThread() {
        if (null != monitorThread) {
            monitorThread.run();
        } else {
            monitorThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    connectionMonitor = new ConnectionMonitor(mContext);
                    connectionMonitor.startTimer();
                }
            });
            monitorThread.start();
        }
    }

    /**
     * 初始化sslsocket
     */
    public void sslSocket(String ipAddress) {
        resetSslSocket();
        try {
            // Loading CAs from an InputStream
            CertificateFactory cf;
            cf = CertificateFactory.getInstance("X.509");

            final X509Certificate server_ca;
            InputStream cert = mContext.getResources().openRawResource(R.raw.server);
            server_ca = (X509Certificate) cf.generateCertificate(cert);
            cert.close();

            // Creating a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca-certificate", server_ca);

            InputStream pkcs12in = mContext.getResources().openRawResource(R.raw.client);
            KeyStore pKeyStore = KeyStore.getInstance("PKCS12");
            pKeyStore.load(pkcs12in, AppConstant.PASSWORD_FOR_PKCS12.toCharArray());

            // Creating a TrustManager that trusts the CAs in our KeyStore.
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(pKeyStore, null);
            pkcs12in.close();

            // Creating an SSLSocketFactory that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            sslSocket = (SSLSocket) sslContext.getSocketFactory().createSocket();
            sslSocket.setSoTimeout(AppConstant.LOCAL_SERVER_SOCKET_TIMEOUT);
            sslSocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
                @Override
                public void handshakeCompleted(HandshakeCompletedEvent event) {
                    Log.i(TAG, "ssl握手成功回调");
                    handshakeCompleted = true;
                }
            });
            address = new InetSocketAddress(ipAddress, AppConstant.TCP_CONNECT_PORT);
            sslSocket.connect(address, AppConstant.SERVER_CONNECT_TIMEOUT);
            Log.e(TAG, "创建sslsocket success" + address.toString());
            if (appAuth == null) {
                if (mContext != null) {
                    Toast.makeText(mContext, "用户未绑定zgbee模块", Toast.LENGTH_SHORT).show();
                }
            } else {
                bindApp(appAuth);
            }
            while (sslSocket != null && !sslSocket.isClosed()) {
                getIn();
            }
        } catch (Exception e) {
            initSocketing = false;
            handshakeCompleted = false;
            e.printStackTrace();
        }

    }

    /**
     * 连接上后需要绑定app才能通讯
     */
    private void bindApp(String uid) {
        GeneralPacket packet = new GeneralPacket(mContext);
        QueryOptions queryCmd = new QueryOptions();
        queryCmd.setOP("SET");
        queryCmd.setMethod("BindApp");
        queryCmd.setTimestamp();
        queryCmd.setAuthId(uid);
        Gson gson = new Gson();
        String text = gson.toJson(queryCmd);
        packet.packBindUnbindAppPacket(uid, ComandID.CMD_BIND, text.getBytes());
        getOut(packet.data);
    }


    /**
     * tcp发送数据
     *
     * @param message
     * @return
     */
    public int getOut(byte[] message) {
        if (sslSocket == null) {
            Log.i(TAG, "socket==null cannot send tcp ip message");
            return -1;
        }
        if (sslSocket.isClosed()) {
            reConnectLoclNet();
        } else {
            try {
                Log.e(TAG, "getOut() send start: ");
                OutputStream out = sslSocket.getOutputStream();
                out.write(message);
                Log.e(TAG, "getOut() send cuccess: " + message.length);
                out.flush();
                out.close();
            } catch (IOException e) {
                reConnectLoclNet();
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    /**
     * 重新建立连接
     */
    private void reConnectLoclNet() {
        resetSslSocket();
        if (mContext != null && mLocalConnecteListener != null) {
            if (mUdpmanager == null) {
                mUdpmanager = UdpManager.getInstance();
            }
            mUdpmanager.InitUdpConnect(mContext, this);
        }
    }

    /**
     * 获取tcp/ip连接的数据
     */
    public String getIn() {
        if (sslSocket == null) {
            Log.i(TAG, "getIn() socket==null cannot receive message");

            return "";
        }
        if (sslSocket.isClosed()) {
            Log.i(TAG, "getIn() sslSocket.isClosed");
            return "sslSocket.isClosed";
        }
        String str = null;
        try {
            InputStream input = sslSocket.getInputStream();
            byte[] buf = new byte[10240];
            int len = input.read(buf);
            if (len != -1) {
                //读取cmd参数
                int cmd = DataExchange.bytesToInt(buf, 6, 1);
                str = new String(buf, 0, len);
                Log.i(TAG, "cmd=" + cmd + "length=" + len);
                //数据长度,如果携带数据，数据的长度占2byte
                byte[] lengthByte = new byte[2];
                //数据长度int表示
                int length;
                System.arraycopy(buf, AppConstant.PACKET_DATA_LENGTH_START_INDEX, lengthByte, 0, 2);
                length = DataExchange.bytesToInt(lengthByte, 0, 2);

                switch (cmd) {
                    case ComandID.HEARTBEAT_RESPONSE:

                        break;
                    case ComandID.ALARM_REPORT:
                        str = new String(buf, AppConstant.BASICLEGTH, length);
                        Log.i(TAG, "报警记录=" + str);
                        decodeAlarmRecord(str);
                        break;
                    case ComandID.CMD_BIND_APP_RESPONSE:
                        str = new String(buf, AppConstant.BASICLEGTH, length);
                        for (int i = 0; i < mLocalConnecteListener.size(); i++) {
                            Log.i(TAG, "绑定结果=" + str);
                            mLocalConnecteListener.get(i).OnBindAppResult(str);
                        }
                        break;
                    case ComandID.QUERY_DEV_RESPONSE:
                        System.out.println("received:" + "length=" + length + "received devlist:" + str);
                        str = new String(buf, AppConstant.BASICLEGTH, length);
                        for (int i = 0; i < mLocalConnecteListener.size(); i++) {
                            Log.i(TAG, "mLocalConnecteListener=" + mLocalConnecteListener.get(i).toString());
                            mLocalConnecteListener.get(i).OnGetQueryresult(str);
                        }
                        break;
                    case ComandID.SET_CMD_RESPONSE:
                        str = new String(buf, AppConstant.BASICLEGTH, length);
                        Log.i(TAG, "received 设置结果:" + str + "length=" + length);
                        for (int i = 0; i < mLocalConnecteListener.size(); i++) {
                            mLocalConnecteListener.get(i).OnGetSetresult(str);
                        }
                        break;
                    case ComandID.CMD_SEND_SMART_DEV_RESPONSE:
                        // 绑定网关（中继器） 回应:{ "OP": "REPORT", "Method": "SetDevList", "Result": 0 }
                        //绑定设备回应当前所有已绑定的设备，自己对有没有绑定上
                        //{ "OP": "REPORT", "Method": "DevList", "Device": [ { "Uid": "77685180654101946200316696479888", "Status": "lo" } ], "SmartDev": [ { "Uid": "00-12-4b-00-0b-26-c2-15", "Org": "ismart", "Type": "SMART_LOCK", "Ver": "1" } ] }
                        //删除智能回应:{ "OP": "REPORT", "Method": "DevList", "Device": [ { "Uid": "77685180654101946200316696479888", "Status": "lo" } ], "SmartDev": [ ] }
                        str = new String(buf, AppConstant.BASICLEGTH, length);
                        System.out.println("绑定智能回应:" + str);
                        for (int i = 0; i < mLocalConnecteListener.size(); i++) {
                            mLocalConnecteListener.get(i).OnGetBindresult(str);
                        }
                        break;
                    case ComandID.CMD_DEV_SCAN_WIFI_ACK:
                        str = new String(buf, AppConstant.BASICLEGTH, length);
                        System.out.println("查询wifi列表回应:" + str);
                        for (int i = 0; i < mLocalConnecteListener.size(); i++) {
                            mLocalConnecteListener.get(i).getWifiList(str);
                        }
                        break;
                    case ComandID.CMD_DEV_SET_WIFI_ACK:
                        str = new String(buf, AppConstant.BASICLEGTH, length);
                        System.out.println("设置中继上网返回:" + str);
                        //设置中继上网返回:{ "OP": "REPORT", "Method": "WIFI", "Result": 0 }
                        for (int i = 0; i < mLocalConnecteListener.size(); i++) {
                            mLocalConnecteListener.get(i).onSetWifiRelayResult(str);
                        }
                        break;
                }
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 解析报警记录
     *
     * @param str
     */
    private void decodeAlarmRecord(String str) {
        Gson gson = new Gson();
        ReportAlertRecord record = gson.fromJson(str, ReportAlertRecord.class);
        if (record != null) {
            //  String recode = record.getALARM_INFO().get(0).getINFO();
            //  Log.i(TAG, "recode=" + recode);
            // ReportAlertRecordReal mAlertRecordReal = gson.fromJson(recode, ReportAlertRecordReal.class);
            List<Info> alermList = record.getInfo();
            Log.i(TAG, "报警记录=" + alermList.size());
            for (int i = 0; i < mLocalConnecteListener.size(); i++) {
                mLocalConnecteListener.get(i).onGetalarmRecord(alermList);
            }
        }
    }


    public void registerNetBroadcast(Context conext) {
        //注册网络状态监听
        mUdpmanager.registerNetBroadcast(conext);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        conext.registerReceiver(broadCast, filter);
    }

    public void unRegisterNetBroadcast(Context conext) {
        mUdpmanager.unRegisterNetBroadcast(conext);
        conext.unregisterReceiver(broadCast);
    }

    /**
     * 当前的网络情况
     */
    private int currentNetStatu = 4;
    private int lastNetStatu;
    public static final int NET_TYPE_WIFI_CONNECTED = 0;
    /**
     * WIFI不可用
     */
    public static final int NET_TYPE_WIFI_DISCONNECTED = 4;
    private NetBroadCast broadCast = new NetBroadCast();

    class NetBroadCast extends BroadcastReceiver {
        public final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION.equals(intent.getAction())) {
                Log.i(TAG, "网络连接变化");
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeInfo = manager.getActiveNetworkInfo();
                if (activeInfo != null && NetUtil.isWiFiActive(context)) {
                    if (NetStatusUtil.isNetworkOnline()) {
                        currentNetStatu = NET_TYPE_WIFI_CONNECTED;
                    } else {
                        currentNetStatu = NET_TYPE_WIFI_DISCONNECTED;
                    }
                    if (currentNetStatu != lastNetStatu) {
                        lastNetStatu = currentNetStatu;
                        if (currentNetStatu == NET_TYPE_WIFI_CONNECTED) {
                            //重新连接
                            if (mContext != null && mLocalConnecteListener != null) {
                                if (mUdpmanager == null) {
                                    mUdpmanager = UdpManager.getInstance();
                                    mUdpmanager.InitUdpConnect(mContext, LocalConnectmanager.this);
                                }
                            }
                        } else if (currentNetStatu == NET_TYPE_WIFI_DISCONNECTED) {
                            //TODO wifi连接不可用
                            if (mContext != null) {
                                Toast.makeText(mContext, "wifi连接不可用，本地连接已断开", Toast.LENGTH_SHORT).show();
                            }
                            if (mUdpmanager != null) {
                                mUdpmanager = null;
                            }
                            resetSslSocket();
                        }
                    }

                }
            }
        }
    }

    /**
     * 重置sslsocket连接
     */
    private void resetSslSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (sslSocket != null) {
                    try {
                        initSocketing = false;
                        sslSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sslSocket = null;
                }
            }
        }).start();
    }
    private boolean initSocketing = false;
    @Override
    public void onGetLocalConnectIp(String ipAddress, String uid) {
        if (!initSocketing) {
            initSocketing = true;
            InitConnect(ipAddress);

        }
    }
}
