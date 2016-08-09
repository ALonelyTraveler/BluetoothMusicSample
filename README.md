# BluetoothMusicSample
在个人写的[音乐播放器API](https://github.com/ALonelyTraveler/MusicLibrarySample)和[蓝牙连接框架](https://github.com/ALonelyTraveler/BluetoothLibrarySample)的基础上添加蓝牙连接A2DP进行音乐播放的功能，源码部分并不多，主要是对蓝牙进行A2DP连接。

##当前版本(VERSION)
[![Maven Central](https://img.shields.io/badge/VERSION-1.0.0-orange.svg)](https://bintray.com/gcssloop/maven/sutil/view)

## gradle依赖

	compile 'com.bandou:a2dp:VERSION'
	
## 项目依赖

	compile 'org.greenrobot:eventbus:3.0.0'
	
##使用

	====================================
	基本方法说明
	====================================
	//判断A2DP是否连接
	if (A2dpManager.getInstance().isConnect()) {
		//连接A2DP
		A2dpManager.getInstance().connect(macAddress);
	}
	else{
		//断开连接
		A2dpManager.getInstance().destroy();
	}
	
	====================================
	在Activity或其它地方注册A2dpStatusEvent
	EventBus.getDefault().register(this);
	====================================
	
	@Subscribe(threadMode = ThreadMode.MAIN)
    public void onA2dpStatusEvent(A2dpStatusEvent event) {
        EventBus.getDefault().unregister(this);
        if (event.isSuccess()) {
            Toast.makeText(mContext, "连接a2dp成功", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(mContext, "连接a2dp失败", Toast.LENGTH_SHORT).show();
        }
    }

## 更新日志

>
>1.0.0 (2016-8-9)
>
>* 初始化版本
>
