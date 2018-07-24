package com.bandou.bluetooth.a2dp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.bandou.bluetooth.a2dp.event.A2dpConnectedDevicesEvent;
import com.bandou.bluetooth.a2dp.event.A2dpStatusEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * ClassName: A2dpManager
 * Description: A2dp管理器
 * Creator: chenwei
 * Date: 16/8/9 上午9:48
 * Version: 1.0
 */
public class A2dpManager {
    private static A2dpManager instance;

    /**
     * 是否连接
     */
    private boolean a2dpConnect;

    private AutoConnect mAutoConnect;

    private Handler connectHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == AutoConnect.MSG_CONNECT_SUCCESS || msg.what == AutoConnect.MSG_CONNECT_FAIL) {
                try {
                    a2dpConnect = (msg.what == AutoConnect.MSG_CONNECT_SUCCESS && (mAutoConnect != null && mAutoConnect.isA2dpConnected()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new A2dpStatusEvent(a2dpConnect));
            } else if (msg.what == AutoConnect.MSG_QUERY_CONNECTED_DEVICES) {
                Object devicesObj = msg.obj;
                if (devicesObj == null) {
                    EventBus.getDefault().post(new A2dpConnectedDevicesEvent(null));
                } else {
                    EventBus.getDefault().post(new A2dpConnectedDevicesEvent((List<BluetoothDevice>) devicesObj));
                }

            }
        }
    };


    private A2dpManager() {
    }

    public static A2dpManager getInstance() {
        if (instance == null) {
            instance = new A2dpManager();
        }
        return instance;
    }

    /**
     * 连接
     *
     * @param context the context
     * @param mac     the mac
     */
    public void connect(Context context, String mac) {
        if (TextUtils.isEmpty(mac)) {
            throw new RuntimeException("The address is null!");
        }
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            throw new RuntimeException("bluetooth device unSupport!");
        }
        if (mAutoConnect == null) {
            mAutoConnect = new AutoConnect(context.getApplicationContext(), connectHandler);
        }
        mAutoConnect.startConnect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac));
    }

    /**
     * 查询所有连接设备
     */
    public void queryConnectedDevices(Context context) {
        if (mAutoConnect == null) {
            mAutoConnect = new AutoConnect(context.getApplicationContext(), connectHandler);
        }
        mAutoConnect.startQueryDevices();
    }

    /**
     * 关闭连接
     */
    public void destroy() {
        a2dpConnect = false;
        if (mAutoConnect != null) {
            try {
                mAutoConnect.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mAutoConnect = null;
        }
        connectHandler.removeCallbacksAndMessages(null);
    }

    public void connected(BluetoothDevice bluetoothDevice) {
        a2dpConnect = true;
        mAutoConnect.setBluetoothDevice(bluetoothDevice);
    }


    public boolean isConnect() {
        return a2dpConnect;
    }
}
