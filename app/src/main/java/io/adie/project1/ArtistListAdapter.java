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

import kaaes.spotify.webapi.android.models.Artist;

public class ArtistListAdapter extends BaseAdapter implements Parcelable {

    Context ctx;
    List<Artist> artists;

    ArtistListAdapter(Context ctx, List<Artist> artists) {
        this.ctx = ctx;
        this.artists = artists;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public int describeContents() {
        return 0;
    }

    @Override
    public int getCount() {
        return artists.size();
    }

    @Override
    public Object getItem(int pos) {
        return artists.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return artists.indexOf(getItem(pos));
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        ArtistHolder holder = null;

        LayoutInflater mInflater = (LayoutInflater) ctx.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.artist, null);
            holder = new ArtistHolder();
            holder.tvArtist = (TextView) convertView.findViewById(R.id.artist_name);
            holder.ivArtist = (ImageView) convertView.findViewById(R.id.artist_image);

            convertView.setTag(holder);
        } else {
            holder = (ArtistHolder) convertView.getTag();
        }


        Artist artist = artists.get(pos);

        holder.tvArtist.setText(artist.name);
        if (artist.images.size() > 0) {
            Picasso.with(ctx)
                    .load(artist.images.get(0).url)
                    .into(holder.ivArtist);
        }

        return convertView;
    }

    public class ArtistHolder {
        ImageView ivArtist;
        TextView tvArtist;
    }
}