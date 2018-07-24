package com.bandou.bluetooth.a2dp.event;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * ClassName: A2dpConnectedDevicesEvent
 * Description: A2dp已连接的蓝牙设备
 * Creator: chenwei
 * Date: 16/8/9 上午9:49
 * Version: 1.0
 */
public class A2dpConnectedDevicesEvent {
    private final List<BluetoothDevice> mDevices;

    public A2dpConnectedDevicesEvent(List<BluetoothDevice> devices) {
        this.mDevices = devices;
    }

    public List<BluetoothDevice> getDevices() {
        return mDevices;
    }
}
