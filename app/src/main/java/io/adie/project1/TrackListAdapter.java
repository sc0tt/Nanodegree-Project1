package io.adie.project1;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class TrackListAdapter extends BaseAdapter implements Parcelable {

    Context ctx;
    List<Track> tracks;

    TrackListAdapter(Context ctx, List<Track> tracks) {
        this.ctx = ctx;
        this.tracks = tracks;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public int describeContents() {
        return 0;
    }

    @Override
    public int getCount() {
        return tracks.size();
    }

    @Override
    public Object getItem(int pos) {
        return tracks.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return tracks.indexOf(getItem(pos));
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        TrackHolder holder = null;

        LayoutInflater mInflater = (LayoutInflater) ctx.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.song, null);
            holder = new TrackHolder();
            holder.tvSongName = (TextView) convertView.findViewById(R.id.song_name);
            holder.tvSongAlbum = (TextView) convertView.findViewById(R.id.song_album);
            holder.ivAlbumArt = (ImageView) convertView.findViewById(R.id.song_image);

            convertView.setTag(holder);
        } else {
            holder = (TrackHolder) convertView.getTag();
        }


        Track track = tracks.get(pos);

        holder.tvSongName.setText(track.name);
        holder.tvSongAlbum.setText(track.album.name);
        if (track.album.images.size() > 0) {
            Picasso.with(ctx)
                    .load(track.album.images.get(0).url)
                    .into(holder.ivAlbumArt);
        }

        return convertView;
    }

    public class TrackHolder {
        ImageView ivAlbumArt;
        TextView tvSongName;
        TextView tvSongAlbum;
    }
}
