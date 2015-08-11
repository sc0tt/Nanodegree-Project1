package io.adie.project1;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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


public class TopTracksActivity extends AppCompatActivity {
    public final static String SONG_RESULTS = "io.adie.project1.SONG_RESULTS";
    public final static String SONG_INDEX = "io.adie.project1.SONG_INDEX";
    static final String TAG = ArtistSearchActivity.class.getSimpleName();
    public static List<Track> tracks;
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
    ListView songListView;
    TrackListAdapter adapter;
    final Runnable updateResult = new Runnable() {
        @Override
        public void run() {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        Intent intent = getIntent();
        artistName = intent.getStringExtra(ArtistSearchActivity.ARTIST_NAME);
        artistId = intent.getStringExtra(ArtistSearchActivity.ARTIST_ID);

        actionBarSetup(artistName);

        songListView = (ListView) findViewById(R.id.song_list);
        tracks = new ArrayList<Track>();
        if (savedInstanceState != null) {
            adapter = savedInstanceState.getParcelable(SONG_RESULTS);
        } else {
            adapter = new TrackListAdapter(this, tracks);
            Map<String, Object> httpOptions = new HashMap<String, Object>();
            httpOptions.put("country", "US");
            spotify.getArtistTopTrack(artistId, httpOptions, new Callback<Tracks>() {
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
        }

        songListView.setAdapter(adapter);

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long unused) {
                Intent intent = new Intent(TopTracksActivity.this, PlayerActivity.class);
                intent.putExtra(SONG_INDEX, position);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);

        // Note: getValues() is a method in your ArrayAdaptor subclass
        savedState.putParcelable(SONG_RESULTS, adapter);

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
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
