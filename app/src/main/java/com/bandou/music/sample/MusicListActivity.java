package com.bandou.music.sample;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnItemClick;
import com.bandou.music.controller.ControllerResponser;
import com.bandou.music.MusicPlayer;
import com.bandou.music.model.AudioInfo;
import com.bandou.music.model.PlayMode;
import com.bandou.music.sample.adapter.ItemSongAdapter;
import com.bandou.music.utils.MusicLoader;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @ClassName: MusicListActivity
 * @Description: 音乐列表
 * @author: chenwei
 * @version: V1.0
 * @Date: 16/5/5 下午4:21
 */
public class MusicListActivity extends BaseActivity {

    public static final String LONG_MUSIC_ALBUM_ID = "album_id";

    @BindView(R.id.lvMusic)
    ListView lvMusic;

    private ItemSongAdapter adapter;

    private long albumId = AudioInfo.INVALID_ID_INDEX;

    private Subscription musicSubscription;

    @Override
    public void initView() {
        albumId = getIntent().getExtras()!=null?getIntent().getExtras().getLong(LONG_MUSIC_ALBUM_ID, AudioInfo.INVALID_ID_INDEX): AudioInfo.INVALID_ID_INDEX;
        adapter = new ItemSongAdapter(mContext, new ArrayList<AudioInfo>());
        lvMusic.setAdapter(adapter);
        lvMusic.addHeaderView(new View(mContext),null,false);
        lvMusic.addFooterView(new View(mContext),null,false);
        Single<List<AudioInfo>> musicSingle = Single.fromCallable(new Callable<List<AudioInfo>>() {
            @Override
            public List<AudioInfo> call() throws Exception {
                return albumId != AudioInfo.INVALID_ID_INDEX ? MusicLoader.getInstance().loadMusicByAlbumId(mContext, albumId) : MusicLoader.getInstance().loadAllMusic(mContext);
            }
        });
        musicSubscription = musicSingle.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<List<AudioInfo>>() {
                    @Override
                    public void onSuccess(List<AudioInfo> value) {
                        adapter.getObjects().clear();
                        if (value != null) {
                            adapter.getObjects().addAll(value);
                        }
                        adapter.notifyDataSetChanged();
                        if (musicSubscription != null && !musicSubscription.isUnsubscribed()) {
                            musicSubscription.unsubscribe();
                        }
                    }

                    @Override
                    public void onError(Throwable error) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (musicSubscription != null && !musicSubscription.isUnsubscribed()) {
            musicSubscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_music_list;
    }

    @OnItemClick({R.id.lvMusic})
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        AudioInfo info = adapter.getItem(position-1);
        MusicActivity.setPlayMusicAlbum(albumId);
        int error = MusicPlayer.getInstance().play(info.getFileUri().getPath());
        if (error == ControllerResponser.NORMAL) {
            int randomFlag = MusicActivity.needRandom() ? PlayMode.RANDOM : PlayMode.ORDER;
            int loopFlag = MusicActivity.needLoop() ? PlayMode.LOOP : PlayMode.DEFAULT;
            MusicPlayer.getInstance().getProvider().setAudios(adapter.getObjects(), position - 1);
            MusicPlayer.getInstance().getProvider().updatePlayMode(randomFlag|loopFlag);

            Intent intent = new Intent(mContext, MusicActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else if (error==ControllerResponser.ERROR_DATA){
            Toast.makeText(mContext, "文件路径不存在", Toast.LENGTH_SHORT).show();
        }
    }

}
