package com.bandou.music.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bandou.music.model.AudioInfo;
import com.bandou.music.sample.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class ItemAlbumAdapter extends BaseAdapter {

    private List<AudioInfo> objects;

    private Context context;
    private LayoutInflater layoutInflater;

    public ItemAlbumAdapter(Context context, List<AudioInfo> objects) {
        this.context = context;
        this.objects = objects;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public AudioInfo getItem(int position) {
        return objects.get(position);
    }

    public List<AudioInfo> getObjects() {
        return objects;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_album, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.ivCover = (ImageView) convertView.findViewById(R.id.ivCover);
            viewHolder.tvAlbumName = (TextView) convertView.findViewById(R.id.tvName);
            viewHolder.tvArtist = (TextView) convertView.findViewById(R.id.tvArtist);

            convertView.setTag(viewHolder);
        }
        initializeViews((AudioInfo) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(AudioInfo object, ViewHolder holder) {
        //TODO implement
        holder.tvAlbumName.setText(object.getAlbum());
        holder.tvArtist.setText(object.getSinger());
        Glide.with(context)
                .load(object.getAlbumArtUri())
                .asBitmap()
                .placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)
                .into(holder.ivCover);

    }

    protected class ViewHolder {
        private ImageView ivCover;
        private TextView tvAlbumName;
        private TextView tvArtist;
    }
}
