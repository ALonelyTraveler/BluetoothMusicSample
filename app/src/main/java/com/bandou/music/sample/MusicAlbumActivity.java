package com.bandou.music.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.OnItemClick;
import com.bandou.music.model.AudioInfo;
import com.bandou.music.sample.adapter.ItemAlbumAdapter;
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
 * @ClassName: MusicAlbumActivity
 * @Description: 音乐专辑列表
 * @author: chenwei
 * @version: V1.0
 * @Date: 16/5/5 下午4:20
 */
public class MusicAlbumActivity extends BaseActivity {
    @BindView(R.id.lvAlbum)
    ListView lvAlbum;
    private ItemAlbumAdapter adapter;

    private Subscription albumSubscription;

    @Override
    public void initView() {
        adapter = new ItemAlbumAdapter(mContext, new ArrayList<AudioInfo>());
        lvAlbum.setAdapter(adapter);
        lvAlbum.addHeaderView(new View(mContext),null,false);
        lvAlbum.addFooterView(new View(mContext),null,false);
        Single<List<AudioInfo>> musicSingle = Single.fromCallable(new Callable<List<AudioInfo>>() {
            @Override
            public List<AudioInfo> call() throws Exception {
                return MusicLoader.getInstance().loadAlbum(mContext);
            }
        });
        albumSubscription = musicSingle.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<List<AudioInfo>>() {
                    @Override
                    public void onSuccess(List<AudioInfo> value) {
                        adapter.getObjects().clear();
                        if (value != null) {
                            adapter.getObjects().addAll(value);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (albumSubscription != null && !albumSubscription.isUnsubscribed()) {
            albumSubscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_music_album;
    }

    @OnItemClick({R.id.lvAlbum})
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AudioInfo item = adapter.getItem(position-1);
        Bundle bundle = new Bundle();
        bundle.putLong(MusicListActivity.LONG_MUSIC_ALBUM_ID, item.getAlbumId());
        Intent intent = new Intent(mContext, MusicListActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
