package io.adie.project1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.client.Response;


public class ArtistSearchActivity extends AppCompatActivity {
    public final static String ARTIST_ID = "io.adie.project1.ARTIST_ID";
    public final static String ARTIST_NAME = "io.adie.project1.ARTIST_NAME";
    public final static String ARTIST_RESULTS = "io.adie.project1.ARTIST_RESULTS";
    static final String TAG = ArtistSearchActivity.class.getSimpleName();
    final Runnable failedSearch = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), getString(R.string.no_artists), Toast.LENGTH_SHORT).show();
        }
    };
    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify = api.getService();
    List<Artist> artists;
    ListView artistListView;
    SearchView searchTermView;
    ArtistListAdapter adapter;
    final Runnable updateResult = new Runnable() {
        @Override
        public void run() {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);

        // Note: getValues() is a method in your ArrayAdaptor subclass
        savedState.putParcelable(ARTIST_RESULTS, adapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_artist_search);
        artistListView = (ListView) findViewById(R.id.artist_list);
        searchTermView = (SearchView) findViewById(R.id.artist_search);
        artists = new ArrayList<Artist>();

        if (savedInstanceState != null) {
            adapter = savedInstanceState.getParcelable(ARTIST_RESULTS);
        } else {
            adapter = new ArtistListAdapter(this, artists);
        }

        artistListView.setAdapter(adapter);

        artistListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                Artist chosenArtist = (Artist) adapter.getItem(position);
                Intent intent = new Intent(getApplicationContext(), TopTracksActivity.class);
                intent.putExtra(ARTIST_ID, chosenArtist.id);
                intent.putExtra(ARTIST_NAME, chosenArtist.name);
                startActivity(intent);
            }
        });

        searchTermView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Searching for " + query);
                artists.clear();
                adapter.notifyDataSetChanged();
                spotify.searchArtists(query, new SpotifyCallback<ArtistsPager>() {
                    @Override
                    public void failure(SpotifyError spotifyError) {
                        runOnUiThread(failedSearch);
                    }

                    @Override
                    public void success(ArtistsPager artistsPager, Response response) {
                        List<Artist> results = artistsPager.artists.items;
                        if (results.size() > 0) {
                            artists.addAll(results);
                            runOnUiThread(updateResult);
                        } else {
                            runOnUiThread(failedSearch);
                        }

                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist_search, menu);
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
