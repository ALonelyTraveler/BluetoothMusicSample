
package com.bandou.bluetooth.a2dp;

import android.bluetooth.*;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.*;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class AutoConnect {

    private static final String TAG = AutoConnect.class.getSimpleName();

    public static final int MSG_CONNECT_SUCCESS = 1000;

    public static final int MSG_CONNECT_FAIL = 10001;

    public static final int MSG_DELAY = 500;

    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private IBluetoothA2dp mIBluetoothA2dp = null;

    private BluetoothA2dp mBluetoothA2dp = null;

    private BluetoothDevice mBluetoothDevice = null;

    private Context mContext = null;

    private WorkerThreadHandler mWorkerThreadHandler;

    private HandlerThread mWorkerThread;

    private Handler mMainHandler;

    private static final String BLUETOOTHDEVICE_CREATE_BOND = "createBond";

    private static final int CHECK_BLUETOOTH_PAIR_INTERVAL = 200;

    private static final int CHECK_BLUETOOTH_PAIR_TIMEOUT = 7000;

    private static final int CHECK_A2DP_IS_BIND_INTERVAL = 200;

    private static final int CHECK_A2DP_IS_BIND_TIMEOUT = 5000;

    private class WorkerThreadHandler extends Handler {

        WorkerThreadHandler(final Looper looper) {
            super(looper);
        }

    }

    private final Runnable mAutoConnectRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                doBTConnect();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    };

    private final ServiceConnection mA2dpServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            Log.v(TAG, "onServiceDisconnected, unBind A2dpService");
            mIBluetoothA2dp = null;
        }

        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            Log.v(TAG, "onServiceConnected, bind A2dpService success");
            mIBluetoothA2dp = IBluetoothA2dp.Stub.asInterface(service);
        }
    };

    private final ServiceListener mA2dpServiceListener = new ServiceListener() {
        @Override
        public void onServiceDisconnected(final int profile) {
            Log.v(TAG, "onServiceDisconnected, unBind A2dpService");
            mBluetoothA2dp = null;
        }

        @Override
        public void onServiceConnected(final int profile, final BluetoothProfile proxy) {
            Log.v(TAG, "onServiceConnected, bind A2dpService success");
            try {
                mBluetoothA2dp = (BluetoothA2dp) proxy;
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    };

    public AutoConnect(final Context context, final Handler handler) {
        mContext = context;
        mMainHandler = handler;
        onInit();
    }

    private boolean isAboveJBMR1() {
        return (Build.VERSION.SDK_INT >= 17);
    }

    @SuppressWarnings("MissingPermission")
    private void doBTConnect() throws Exception {
        Log.v(TAG, "-----------------------------doBTConnect-----------");
        if (isDevicePaired(mBluetoothAdapter.getBondedDevices())) {
            if (isA2dpConnected()) {
                mMainHandler.sendEmptyMessageDelayed(MSG_CONNECT_SUCCESS,MSG_DELAY);
            } else {
                connectToA2dp(mBluetoothDevice);
            }
        } else {
            pairToDevice();
            if (isA2dpConnected()) {
                mMainHandler.sendEmptyMessageDelayed(MSG_CONNECT_SUCCESS,MSG_DELAY);
            } else {
                connectToA2dp(mBluetoothDevice);
            }
        }
    }

    private void bindToA2dpService() throws SecurityException {
        if (isAboveJBMR1()) {
            final Intent a2dpIntent = new Intent(IBluetoothA2dp.class.getName());
            Intent eintent = new Intent(getExplicitIntent(mContext, a2dpIntent));
            mContext.bindService(eintent, mA2dpServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            mBluetoothAdapter.getProfileProxy(mContext, mA2dpServiceListener, BluetoothProfile.A2DP);
        }
    }
    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
    private void unBindToA2dpService() {
        if (isAboveJBMR1()) {
            mContext.unbindService(mA2dpServiceConnection);
        } else {
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, mBluetoothA2dp);
        }
    }

    private void onInitWorker() {
        if (mWorkerThread == null) {
            mWorkerThread = new HandlerThread("WorkerThread");
            mWorkerThread.start();
        }
        if (mWorkerThreadHandler == null)
            mWorkerThreadHandler = new WorkerThreadHandler(mWorkerThread.getLooper());
    }

    public void onDispatchWorker() {
        if (mWorkerThreadHandler != null) {
            mWorkerThreadHandler = null;
        }
        if (mWorkerThread != null) {
            mWorkerThread.getLooper().quit();
            mWorkerThread = null;
        }
    }

    public void startConnect(BluetoothDevice aBluetoothDevice) {
        mBluetoothDevice = aBluetoothDevice;
        checkA2dpServiceIsReady();
    }


    private void checkA2dpServiceIsReady() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final long time = System.currentTimeMillis();
                while (true) {
                    if (System.currentTimeMillis() - time < CHECK_A2DP_IS_BIND_TIMEOUT) {
                        if (isAboveJBMR1()) {
                            if (mIBluetoothA2dp == null) {
                                try {
                                    Thread.sleep(CHECK_A2DP_IS_BIND_INTERVAL);
                                } catch (final InterruptedException e) {
                                    e.printStackTrace();
                                    break;
                                }
                            } else {
                                if (mWorkerThreadHandler != null)
                                    mWorkerThreadHandler.post(mAutoConnectRunnable);
                                break;
                            }
                        } else {
                            if (mBluetoothA2dp == null) {
                                try {
                                    Thread.sleep(CHECK_A2DP_IS_BIND_INTERVAL);
                                } catch (final InterruptedException e) {
                                    e.printStackTrace();
                                    break;
                                }
                            } else {
                                if (mWorkerThreadHandler != null)
                                    mWorkerThreadHandler.post(mAutoConnectRunnable);
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
        }).start();
    }
    @SuppressWarnings("MissingPermission")
    private boolean isDevicePaired(final Set<BluetoothDevice> list) {
        for (final BluetoothDevice device : list) {
            if (device.getName().equalsIgnoreCase(mBluetoothDevice.getName())) {
                return true;
            }
        }
        Log.w(TAG, "device not paired!");
        return false;
    }

    private boolean createBond(final Class invokedClass, final BluetoothDevice bluetoothDevice) throws Exception {
        final Method createBondMethod = invokedClass.getMethod(BLUETOOTHDEVICE_CREATE_BOND);
        final Boolean returnValue = (Boolean) createBondMethod.invoke(bluetoothDevice);
        return returnValue.booleanValue();
    }
    @SuppressWarnings("MissingPermission")
    private void pairToDevice() throws Exception {
        createBond(mBluetoothDevice.getClass(), mBluetoothDevice);
        Log.v(TAG, "pairToDevice");
        try {
            final long time = System.currentTimeMillis();
            while (!isDevicePaired(mBluetoothAdapter.getBondedDevices())) {
                if ((System.currentTimeMillis() - time) > CHECK_BLUETOOTH_PAIR_TIMEOUT)
                    break;
                Thread.sleep(CHECK_BLUETOOTH_PAIR_INTERVAL);
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void connectToA2dp(final BluetoothDevice device) throws Exception {
        final boolean isSuccess;
        if (isAboveJBMR1()) {
            isSuccess = mIBluetoothA2dp.connect(device);
        } else {
            isSuccess = (Boolean) mBluetoothA2dp.getClass().getMethod("connect", BluetoothDevice.class).invoke(mBluetoothA2dp, device);
        }
        Log.v(TAG, "connectToA2dp, isSuccess = " + isSuccess);
        if (isSuccess) {
            mMainHandler.sendEmptyMessageDelayed(MSG_CONNECT_SUCCESS,MSG_DELAY);
        } else {
            mMainHandler.sendEmptyMessageDelayed(MSG_CONNECT_FAIL,MSG_DELAY);
        }
    }

    public boolean isA2dpConnected() throws Exception {
        if (isAboveJBMR1()) {
            return isA2dpConnectAboveJBMR1();
        } else {
            return isA2dpConnectBelowJBMR1();
        }
    }

    private boolean isA2dpConnectAboveJBMR1() throws RemoteException {
        final int state = mIBluetoothA2dp.getConnectionState(mBluetoothDevice);
        Log.v(TAG, "A2dp state = " + state);
        if (state == BluetoothProfile.STATE_CONNECTED)
            return true;
        return false;
    }
    @SuppressWarnings("MissingPermission")
    private boolean isA2dpConnectBelowJBMR1() throws RemoteException {
        final int state = mBluetoothA2dp.getConnectionState(mBluetoothDevice);
        Log.v(TAG, "A2dp state = " + state);
        if (state == BluetoothProfile.STATE_CONNECTED)
            return true;
        return false;
    }

    private void onInit() throws SecurityException {
        Log.v(TAG, "onInit");
        onInitWorker();
        bindToA2dpService();
    }

    public void destroy() throws Exception {
        Log.v(TAG, "destroy---");
        onDispatchWorker();
        mMainHandler = null;
        if (mContext != null) {
            unBindToA2dpService();
            mContext = null;
        }
    }

}
