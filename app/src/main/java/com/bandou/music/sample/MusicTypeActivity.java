package com.bandou.music.sample;

import android.content.Intent;
import android.view.View;
import butterknife.OnClick;

/**
 * @ClassName: MusicTypeActivity
 * @Description: 音乐类别（专辑 or 全部)
 * @author: chenwei
 * @version: V1.0
 * @Date: 16/5/5 下午4:28
 */
public class MusicTypeActivity extends BaseActivity {
    @Override
    public void initView() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_music_type;
    }

    @OnClick({R.id.lltAlbum, R.id.lltAllMusic})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lltAlbum: {
                Intent intent = new Intent(mContext, MusicAlbumActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
                break;
            case R.id.lltAllMusic: {
                Intent intent = new Intent(mContext, MusicListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
                break;
        }
    }
}
