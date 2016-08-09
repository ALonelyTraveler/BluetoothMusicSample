package com.bandou.music.sample;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.bandou.music.sample.model.DeviceInfo;
import com.bdkj.ble.controller.BleController;
import com.bdkj.ble.controller.EventBusBroadcaster;
import com.bdkj.ble.event.ConnectAction;
import com.bdkj.ble.event.EventConstants;
import com.bdkj.ble.event.ServiceAction;
import com.bdkj.ble.scanner.BLEScanner;
import com.bdkj.ble.scanner.BaseScanner;
import com.bdkj.ble.scanner.ScanCallBack;
import com.bdkj.ble.secretary.BleSecretary;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BLEActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    SwipeRefreshLayout mSwipeLayout = null;
    private String serviceUUID = "14839AC4-7D7E-415C-9A42-167340CF2339";
    private String characteristicUUID = "8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3";
    private String notifyUUID = "0734594A-A8E7-4B1A-A6B1-CD5243059A57";

    private String battery_service = "0000180f-0000-1000-8000-00805f9b34fb";
    private String battery_character = "00002a19-0000-1000-8000-00805f9b34fb";
    List<DeviceInfo> list = new ArrayList<>();
    List<String> names = new ArrayList<>();
    List<String> addressAll = new ArrayList<>();
    ListView lvDevice = null;
    BaseScanner scanner = null;

    BleController<BleSecretary> mController;
    private Handler mHandler = new Handler();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        if (scanner != null) {
            scanner.stopScan();
        }
        if (mController != null) {
            mController.cancelConnect();
        }
    }

    @Override
    public void initView() {
        initRefresh();

        EventBus.getDefault().register(this);
        lvDevice = (ListView) findViewById(R.id.lvDevices);
        lvDevice.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names));
        lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                scanner.stopScan();
                mSwipeLayout.setRefreshing(false);
                mSwipeLayout.setEnabled(false);
                if (mController == null) {
                    mController = new BleController<BleSecretary>(BLEActivity.this.getApplicationContext(), new BleSecretary() {

                        @Override
                        public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BLEActivity.this, "读到数据-------", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                        @Override
                        public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {

                        }

                        @Override
                        public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {

                        }
                    });
                    mController.setBroadcaster(new EventBusBroadcaster());
                }
                DeviceInfo info = list.get(i);
                try {
                    mController.connect(info.address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        scanner = new BLEScanner(8000);
//        scanner.setBluetoothFilter(new NameMatcher("WL75"));
        scanner.setCallBack(new ScanCallBack() {
            @Override
            public void startScan() {
            }

            @Override
            public void finishScan() {
                mSwipeLayout.setRefreshing(false);
            }

            @Override
            public void foundSpeificDevice(String name, String address, int rssi) {
                if (!addressAll.contains(address)) {
                    DeviceInfo info = new DeviceInfo();
                    info.name = name;
                    info.address = address;
                    info.rssi = rssi;
                    list.add(info);
                    names.add(name);
                    addressAll.add(address);

                    ((ArrayAdapter) lvDevice.getAdapter()).notifyDataSetChanged();
                }
            }
        });


        findViewById(R.id.btnDisconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mController != null) {
                    if (mController.isConnect()) {
                        mController.disconnect();
                    } else {
                        mController.cancelConnect();
                    }
                }
                mSwipeLayout.setEnabled(true);
            }
        });

        findViewById(R.id.btnReadBattery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mController != null && mController.isConnect()) {
                    Toast.makeText(BLEActivity.this, "是否成功:" + mController.getBluetoothSecretary().readCharacteristic(battery_service, battery_character), Toast.LENGTH_SHORT).show();

                }
            }
        });

        findViewById(R.id.btnOpenA2dp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mController!=null&&mController.isConnect()) {
                    Intent intent = new Intent(mContext, A2DPActivity.class);
                    intent.putExtra("mac", mController.getConnectMac());
                    startActivity(intent);
                }
                else{
                    Toast.makeText(mContext, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }



    private void initRefresh() {
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_ly);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeLayout.setSize(SwipeRefreshLayout.DEFAULT);
//        mSwipeLayout.setProgressViewEndTarget(true, 100);
        mSwipeLayout.setEnabled(true);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeLayout.setRefreshing(true);
                onRefresh();
            }
        }, 1000);
    }


    @Override
    public void onRefresh() {
        list.clear();
        names.clear();
        addressAll.clear();
        ((ArrayAdapter) lvDevice.getAdapter()).notifyDataSetChanged();
        scanner.startScan();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectAction(ConnectAction action) {
        if (action.action.equals(EventConstants.SUCCESS)) {
            Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();


        } else if (action.action.equals(EventConstants.FAIL)) {
            Toast.makeText(this, "连接失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceAction(ServiceAction action) {
        Toast.makeText(this, "接收到服务", Toast.LENGTH_SHORT).show();
        mController.getBluetoothSecretary().setCharacteristicNotification(serviceUUID, notifyUUID, true);
    }


}
