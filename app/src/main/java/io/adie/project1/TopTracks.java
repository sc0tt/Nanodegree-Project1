package io.adie.project1;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class TopTracks extends AppCompatActivity {
    static final String TAG = ArtistSearch.class.getSimpleName();
    final Runnable failedSearch = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), getString(R.string.no_songs_found), Toast.LENGTH_SHORT).show();
        }
    };
    String artistName;
    String artistId;
    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify = api.getService();
    List<Track> tracks;
    ListView songListView;
    TrackListAdapter adapter;
    final Runnable updateResult = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        artistName = intent.getStringExtra(ArtistSearch.ARTIST_NAME);
        artistId = intent.getStringExtra(ArtistSearch.ARTIST_ID);

        actionBarSetup(artistName);

        songListView = (ListView) findViewById(R.id.song_list);
        tracks = new ArrayList<Track>();
        adapter = new TrackListAdapter(this, tracks);
        songListView.setAdapter(adapter);

        TopTenSongTask task = new TopTenSongTask();
        task.execute(artistId);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void actionBarSetup(String subtitle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class TrackListAdapter extends BaseAdapter {

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

    private class TopTenSongTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Map<String, Object> httpOptions = new HashMap<String, Object>();
            httpOptions.put("country", "US");
            spotify.getArtistTopTrack(params[0], httpOptions, new Callback<Tracks>() {
                @Override
                public void success(Tracks resultingTracks, Response response) {
                    List<Track> results = resultingTracks.tracks;
                    if (results.size() > 0) {
                        tracks.addAll(results);
                        runOnUiThread(updateResult);
                    } else {
                        runOnUiThread(failedSearch);
                    }
                    runOnUiThread(updateResult);
                }

                @Override
                public void failure(RetrofitError error) {
                    runOnUiThread(failedSearch);
                }
            });
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
