package com.deplink.sdk.android.sdk.interfaces;

import android.graphics.Bitmap;

import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.bean.User;
import com.deplink.sdk.android.sdk.bean.UserSession;

/**
 * Created by billy on 2016/8/8.
 */
public interface SDKCoordinator {
    void afterLogin();
    void afterLogout();
    void afterDeviceBinding();
    void afterDeviceUnbinding();
    UserSession getUserSession();
    User getUserInfo();
    void MQTTConnectionLost(Throwable cause);
    void MQTTReconnectionFailed();
    void MQTTConnected();
    void notifySuccess(SDKAction action);
    void notifyGetImageSuccess(SDKAction action, Bitmap bm);
    void notifyBindSuccess(SDKAction action,String deviceKey);
    void notifyFailure(SDKAction action, String errMsg);
    void notifyFailure(SDKAction action, Throwable exception);
    void notifyDeviceDataUpdate(String deviceKey, int msgType);
    void notifyDeviceUpgrade(String productKey);
    void notifyDeviceOpSuccess(String op, String deviceKey);
    void notifyDeviceOpFailure(String op, String deviceKey, Throwable exception);
}