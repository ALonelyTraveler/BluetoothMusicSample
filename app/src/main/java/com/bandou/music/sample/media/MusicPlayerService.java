/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.bandou.music.sample.media;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RemoteControlClient;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.bandou.music.MusicPlayer;

/**
 * ClassName: MusicPlayerService
 * Description: 监听音乐播放的焦点和电话接入等事件
 * Creator: chenwei
 * Date: 16/8/9 上午10:45
 * Version: 1.0
 */
public class MusicPlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private RemoteControlClient remoteControlClient;
    private AudioManager audioManager;
    private PhoneStateListener phoneStateListener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 音乐上次是否播放
     */
    private boolean musicLastIsPlay = false;

    @Override
    public void onCreate() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        try {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        if (MusicPlayer.getInstance().isPlay()) {
                            musicLastIsPlay = true;
                            MusicPlayer.getInstance().pause();
                        }
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                        if (musicLastIsPlay) {
                            musicLastIsPlay = false;
                            MusicPlayer.getInstance().resume();
                        }
                    } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {

                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            };
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
        super.onCreate();
    }

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ComponentName remoteComponentName = new ComponentName(getApplicationContext(), MusicPlayerReceiver.class.getName());
        try {
            if (remoteControlClient == null) {
                audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                mediaButtonIntent.setComponent(remoteComponentName);
                PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
                remoteControlClient = new RemoteControlClient(mediaPendingIntent);
                audioManager.registerRemoteControlClient(remoteControlClient);
            }
            remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                    | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE | RemoteControlClient.FLAG_KEY_MEDIA_STOP
                    | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS | RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (remoteControlClient != null) {
            audioManager.unregisterRemoteControlClient(remoteControlClient);
            audioManager.abandonAudioFocus(this);
        }
        try {
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            if (MusicPlayer.getInstance().isPlay()) {
                musicLastIsPlay = true;
                MusicPlayer.getInstance().pause();
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            if (musicLastIsPlay) {
                musicLastIsPlay = false;
                MusicPlayer.getInstance().resume();
            }
        }
    }

}
