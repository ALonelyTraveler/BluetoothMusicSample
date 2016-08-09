package com.bandou.music.sample;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import butterknife.OnClick;
import com.bandou.bluetooth.a2dp.A2dpManager;
import com.bandou.bluetooth.a2dp.event.A2dpStatusEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class A2DPActivity extends BaseActivity {

    @Override
    public void initView() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_a2_dp;
    }

    @OnClick({R.id.btnClose, R.id.btnOpen,R.id.btnMusic})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnOpen:
                if (!A2dpManager.getInstance().isConnect()) {
                    A2dpManager.getInstance().connect(mContext,getIntent().getStringExtra("mac"));
                    EventBus.getDefault().register(this);
                }
                else{
                    Toast.makeText(mContext, "已连接A2DP!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnClose:
                if (A2dpManager.getInstance().isConnect()) {
                    A2dpManager.getInstance().destroy();

                }
                else{
                    Toast.makeText(mContext, "已断开A2DP!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnMusic:
                if (A2dpManager.getInstance().isConnect()) {
                    Intent intent = new Intent(mContext, MusicActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(mContext, "请先连接A2dp", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

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
}
