package com.bandou.music.sample;

import android.content.Intent;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import butterknife.BindView;
import butterknife.OnClick;
import com.bandou.library.util.PreferencesUtils;
import com.bandou.music.EventBusCallback;
import com.bandou.music.MusicPlayer;
import com.bandou.music.event.MediaEvent;
import com.bandou.music.model.AudioInfo;
import com.bandou.music.model.PlayMode;
import com.bandou.music.model.SongArray;
import com.bandou.music.sample.utils.TimeUtils;
import com.bandou.music.utils.MusicLoader;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.concurrent.Callable;

/**
 * @ClassName: MusicActivity
 * @Description: 音乐播放页面
 * @author: chenwei
 * @version: V1.0
 * @Date: 16/5/5 下午4:18
 */
public class MusicActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    @BindView(R.id.tvMusicName)
    AppCompatTextView tvMusicName;
    @BindView(R.id.ivPlayOrPause)
    ImageView ivPlayOrPause;
    @BindView(R.id.tvBegin)
    AppCompatTextView tvBegin;
    @BindView(R.id.barMusicProgress)
    SeekBar barMusicProgress;
    @BindView(R.id.tvEnd)
    AppCompatTextView tvEnd;
    @BindView(R.id.ivLoop)
    ImageView ivLoop;
    @BindView(R.id.ivRandom)
    ImageView ivRandom;

    /**
     * 音乐进度条是否处于触摸状态
     */
    private boolean isTouch = false;

    Subscription musicSubscription;

    /**
     * ============================
     * start 获取和保存上一次播放的信息
     * ============================
     */

    public static final String KEY_INT_LOOP_MODE = "loop_mode";

    /**
     * 未循环播放
     */
    public static final int NONE_LOOP_MODE = 0;
    /**
     * 循环播放
     */
    public static final int REPEAT_LOOP_MODE = 1;
    /**
     * 随机播放
     */
    public static final int RANDOM_LOOP_MODE = 2;
    /**
     * 循环随机播放
     */
    public static final int BOTH_LOOP_MODE = 3;

    public static final String KEY_LONG_ALBUM_ID = "album_id";

    public static final String KEY_LONG_SONG_ID = "song_id";

    public static void setLoopMode(int mode) {
        PreferencesUtils.put(ApplicationContext.mContext, KEY_INT_LOOP_MODE, mode);
    }

    public static int getLoopMode() {
        return (int) PreferencesUtils.get(ApplicationContext.mContext, KEY_INT_LOOP_MODE, NONE_LOOP_MODE);
    }

    public static boolean needLoop() {
        int mode = getLoopMode();
        return mode == REPEAT_LOOP_MODE || mode == BOTH_LOOP_MODE;
    }

    public static boolean needRandom() {
        int mode = getLoopMode();
        return mode == RANDOM_LOOP_MODE || mode == BOTH_LOOP_MODE;
    }

    public static void setPlayMusicAlbum(long playAlbumId) {
        PreferencesUtils.put(ApplicationContext.mContext, KEY_LONG_ALBUM_ID, playAlbumId);
    }

    public static void setPlayMusicSong(long songId) {
        PreferencesUtils.put(ApplicationContext.mContext, KEY_LONG_SONG_ID, songId);
    }

    public static long getPlayMusicAlbum() {
        return (long) PreferencesUtils.get(ApplicationContext.mContext, KEY_LONG_ALBUM_ID, AudioInfo.INVALID_ID_INDEX);
    }

    public static long getPlayMusicSong() {
        return (long) PreferencesUtils.get(ApplicationContext.mContext, KEY_LONG_SONG_ID, AudioInfo.INVALID_ID_INDEX);
    }

    /**
     * ============================
     * end 获取和保存上一次播放的信息
     * ============================
     */
    @Override
    public void initView() {
        ivRandom.setSelected(needRandom());
        ivLoop.setSelected(needLoop());
        if (!MusicPlayer.getInstance().isPrepared()) {
            Single<SongArray> musicSingle = Single.fromCallable(new Callable<SongArray>() {
                @Override
                public SongArray call() throws Exception {
                    long albumId = MusicActivity.getPlayMusicAlbum();
                    long songId = MusicActivity.getPlayMusicSong();
                    SongArray songArray;
                    if (albumId != AudioInfo.INVALID_ID_INDEX) {
                        songArray = MusicLoader.getInstance().loadMusicByAlbumId(ApplicationContext.mContext, albumId,songId);
                    } else {
                        songArray = MusicLoader.getInstance().loadAllMusic(ApplicationContext.mContext,songId);
                    }
                    return songArray;
                }
            });
            musicSubscription = musicSingle.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<SongArray>() {
                        @Override
                        public void onSuccess(SongArray value) {
                            if (value != null && value.getSongs().size() > 0) {
                                int randomFlag = MusicActivity.needRandom() ? PlayMode.RANDOM : PlayMode.ORDER;
                                int loopFlag = MusicActivity.needLoop() ? PlayMode.LOOP : PlayMode.DEFAULT;
                                MusicPlayer.getInstance().getProvider().setAudios(value.getSongs(), value.getSongIndex()==-1?0:value.getSongIndex());
                                MusicPlayer.getInstance().getProvider().updatePlayMode(randomFlag|loopFlag);
                                updateMusicView(MusicPlayer.getInstance().getProvider().get());
                            }

                            if (musicSubscription != null && !musicSubscription.isUnsubscribed()) {
                                musicSubscription.unsubscribe();
                            }
                        }

                        @Override
                        public void onError(Throwable error) {

                        }
                    });
        }
        else{
            updateMusicView(MusicPlayer.getInstance().getProvider().get());
        }
        barMusicProgress.setOnSeekBarChangeListener(this);
        EventBus.getDefault().register(this);
        MusicPlayer.getInstance().registerTimer();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        MusicPlayer.getInstance().unregisterTimer();
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        MusicPlayer.getInstance().shutdown();
    }

    private void updateMusicView(AudioInfo audioInfo) {
        if (audioInfo == null) {
            tvMusicName.setText("");
            ivPlayOrPause.setSelected(false);
            barMusicProgress.setProgress(0);
            tvBegin.setText(TimeUtils.millis2HourSynxMinSec(0));
            tvEnd.setText(tvBegin.getText());
        } else {
            tvMusicName.setText(audioInfo.getName());
            ivPlayOrPause.setSelected(MusicPlayer.getInstance().isPlay());
            tvBegin.setText(TimeUtils.millis2HourSynxMinSec(MusicPlayer.getInstance().getProgress()));
            barMusicProgress.setProgress(MusicPlayer.getInstance().getProgress());
            barMusicProgress.setMax((int) audioInfo.getDuration());
            tvEnd.setText(TimeUtils.millis2HourSynxMinSec(audioInfo.getDuration()));
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_music;
    }


    @OnClick({R.id.lltMusicName, R.id.ivForward, R.id.ivBackward, R.id.ivPlayOrPause, R.id.ivLoop, R.id.ivRandom})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lltMusicName:
                Intent intent = new Intent(mContext, MusicTypeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case R.id.ivForward:
                MusicPlayer.getInstance().playPrevious();
                break;
            case R.id.ivBackward:
                MusicPlayer.getInstance().playNext();
                break;
            case R.id.ivPlayOrPause:
                if (MusicPlayer.getInstance().isPrepared()) {
                    if (MusicPlayer.getInstance().isPlay()) {
                        MusicPlayer.getInstance().pause();
                    } else {
                        MusicPlayer.getInstance().resume();
                    }
                }
                else{
                    if (MusicPlayer.getInstance().getProvider() != null && MusicPlayer.getInstance().getProvider().get() != null) {
                        MusicPlayer.getInstance().startup(new EventBusCallback());
                        MusicPlayer.getInstance().play(MusicPlayer.getInstance().getProvider().get().getFileUri().getPath());

                    }
                }
                break;
            case R.id.ivLoop: {
                ivLoop.setSelected(!ivLoop.isSelected());
                setLoopMode(ivLoop.isSelected() ? (ivRandom.isSelected() ? BOTH_LOOP_MODE : REPEAT_LOOP_MODE) : (ivRandom.isSelected() ? RANDOM_LOOP_MODE : NONE_LOOP_MODE));
                int randomFlag = MusicActivity.needRandom() ? PlayMode.RANDOM : PlayMode.ORDER;
                int loopFlag = MusicActivity.needLoop() ? PlayMode.LOOP : PlayMode.DEFAULT;
                MusicPlayer.getInstance().getProvider().updatePlayMode(randomFlag|loopFlag);
            }
                //MusicManager.getController().setLoop(ivLoop.isSelected());
                break;
            case R.id.ivRandom: {
                ivRandom.setSelected(!ivRandom.isSelected());
                setLoopMode(ivLoop.isSelected() ? (ivRandom.isSelected() ? BOTH_LOOP_MODE : REPEAT_LOOP_MODE) : (ivRandom.isSelected() ? RANDOM_LOOP_MODE : NONE_LOOP_MODE));
                int randomFlag = MusicActivity.needRandom() ? PlayMode.RANDOM : 0;
                int loopFlag = MusicActivity.needLoop() ? PlayMode.LOOP : 0;
                MusicPlayer.getInstance().getProvider().updatePlayMode(randomFlag|loopFlag);
            }
                //MusicManager.getController().setLoop(ivRandom.isSelected());
                //MusicManager.getController().updateMusicList();
                break;

        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (seekBar.getId() == R.id.barMusicProgress) {
                seekBar.setProgress(progress);
                tvBegin.setText(TimeUtils.millis2HourSynxMinSec(progress));
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (seekBar.getId() == R.id.barMusicProgress) {
            isTouch = true;
            MusicPlayer.getInstance().pauseTimer();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (seekBar.getId() == R.id.barMusicProgress) {
//            if (!A2dpManager.getInstance().isConnect()) {
//                return;
//            }
            isTouch = false;
            MusicPlayer.getInstance().seekTo(progress);
            tvBegin.setText(TimeUtils.millis2HourSynxMinSec(progress));
            MusicPlayer.getInstance().resumeTimer();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaEvent(MediaEvent event) {
//        public static final int ACTION_PLAY = 1;
//        public static final int ACTION_PAUSE = 2;
//        public static final int ACTION_ERROR = 3;
//        public static final int ACTION_PROGRESS = 4;
//        public static final int ACTION_SINGLE_COMPLETE = 5;
//        public static final int ACTION_ALL_COMPLETE = 6;

        if (event.getAction() == MediaEvent.ACTION_PROGRESS) {
            if (!isTouch) {
                barMusicProgress.setProgress(event.getProgress());
                tvBegin.setText(TimeUtils.millis2HourSynxMinSec(event.getProgress()));
            }
        }
        else if (event.getAction() == MediaEvent.ACTION_ALL_COMPLETE) {
            barMusicProgress.setProgress(0);
            tvBegin.setText(TimeUtils.millis2HourSynxMinSec(0));
            ivPlayOrPause.setSelected(false);
        }
        else if (event.getAction() == MediaEvent.ACTION_PLAY) {
            AudioInfo info = MusicPlayer.getInstance().getProvider().get();
            if (info != null) {
//                setPlayMusicAlbum(info.getAlbumId());
                setPlayMusicSong(info.getSongId());
            }
            updateMusicView(info);
        }
        else
        {
            updateMusicView(MusicPlayer.getInstance().getProvider().get());
        }

    }

}
