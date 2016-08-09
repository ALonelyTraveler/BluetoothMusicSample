package com.bandou.music.sample;

import android.app.Activity;
import android.os.Bundle;
import com.bandou.music.MusicPlayer;

/**
 * 用于点击状态栏图标跳转
 * @ClassName: EmptyActivity
 * @Description: say something
 * @author: chenwei
 * @version: V1.0
 * @Date: 16/3/15 下午5:41
 */
public class EmptyActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!MusicPlayer.getInstance().isPrepared()) {
        }
        finish();
    }
}