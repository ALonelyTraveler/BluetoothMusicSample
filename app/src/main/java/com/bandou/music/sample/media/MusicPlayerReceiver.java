package com.bandou.music.sample.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import com.bandou.bluetooth.a2dp.A2dpManager;
import com.bandou.music.MusicPlayer;

/**
 * ClassName: MusicPlayerReceiver
 * Description: 监听蓝牙设备的MediaButton和a2dp判断
 * Creator: chenwei
 * Date: 16/8/9 上午10:43
 * Version: 1.0
 */
public class MusicPlayerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            if (intent.getExtras() == null) {
                return;
            }
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent == null) {
                return;
            }
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (MusicPlayer.getInstance().isPlay()) {
                        MusicPlayer.getInstance().pause();
                    } else {
                        if (MusicPlayer.getInstance().isPrepared()){
                            MusicPlayer.getInstance().resume();
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    if (MusicPlayer.getInstance().isPrepared()){
                        MusicPlayer.getInstance().resume();
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    if (MusicPlayer.getInstance().isPlay()){
                        MusicPlayer.getInstance().pause();
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    if (MusicPlayer.getInstance().isPrepared()){
                        MusicPlayer.getInstance().stop();
                    }
                    if (A2dpManager.getInstance().isConnect()) {
                        A2dpManager.getInstance().destroy();
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    if (MusicPlayer.getInstance().isPlay()){
                        MusicPlayer.getInstance().playNext();
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    if (MusicPlayer.getInstance().isPlay()){
                        MusicPlayer.getInstance().playPrevious();
                    }
                    break;
            }
        } else if(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())){
            if (MusicPlayer.getInstance().isPrepared()){
                MusicPlayer.getInstance().stop();
            }
            if (A2dpManager.getInstance().isConnect()) {
                A2dpManager.getInstance().destroy();
            }
        }
    }
}
