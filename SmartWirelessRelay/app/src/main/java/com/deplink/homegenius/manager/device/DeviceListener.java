package com.deplink.homegenius.manager.device;

import com.deplink.homegenius.Protocol.json.device.lock.SSIDList;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.homegenius.Deviceprops;

import java.util.List;

/**
 * Created by Administrator on 2017/11/9.
 */
public interface DeviceListener {
    /**
     *返回智能锁查询结果
     */
    void responseQueryResult(String result);
    /**
     *返回绑定结果
     */
    void responseBindDeviceResult(String result);
    /**
     *返回wifi列表
     */
    void responseWifiListResult(List<SSIDList>wifiList);
    void responseSetWifirelayResult(int result);
    /**
     */
    void responseAddDeviceHttpResult(String uid);
    void responseDeleteDeviceHttpResult(DeviceOperationResponse result);
    void responseAlertDeviceHttpResult(DeviceOperationResponse result);
    void responseGetDeviceInfoHttpResult(String result);
    /**
     */
    void responseQueryHttpResult(List<Deviceprops>devices);
}
