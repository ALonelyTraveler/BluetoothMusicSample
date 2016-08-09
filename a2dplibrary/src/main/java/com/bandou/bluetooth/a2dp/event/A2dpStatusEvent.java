package com.bandou.bluetooth.a2dp.event;

/**
 * ClassName: A2dpStatusEvent
 * Description: A2dp状态消息
 * Creator: chenwei
 * Date: 16/8/9 上午9:49
 * Version: 1.0
 */
public class A2dpStatusEvent {
    private final boolean success;

    public A2dpStatusEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
