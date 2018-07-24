// IBluetoothA2dp.aidl
package android.bluetooth;

// Declare any non-default types here with import statements

import android.bluetooth.BluetoothDevice;

/**
 * System private API for Bluetooth A2DP service
 *
 * {@hide}
 */
interface IBluetoothA2dp {
    // Public API
    boolean connect(in BluetoothDevice device);
    boolean disconnect(in BluetoothDevice device);
    List<BluetoothDevice> getConnectedDevices();
    List<BluetoothDevice> getDevicesMatchingConnectionStates(in int[] states);
    int getConnectionState(in BluetoothDevice device);
    boolean setPriority(in BluetoothDevice device, int priority);
    int getPriority(in BluetoothDevice device);
    boolean isAvrcpAbsoluteVolumeSupported();
    oneway void adjustAvrcpAbsoluteVolume(int direction);
    oneway void setAvrcpAbsoluteVolume(int volume);
    boolean isA2dpPlaying(in BluetoothDevice device);
}
