package io.adie.project1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.client.Response;


public class ArtistSearch extends AppCompatActivity {
    public final static String ARTIST_ID = "com.mycompany.myfirstapp.ARTIST_ID";
    public final static String ARTIST_NAME = "com.mycompany.myfirstapp.ARTIST_NAME";
    public final static String ARTIST_RESULTS = "com.mycompany.myfirstapp.RESULTS";
    static final String TAG = ArtistSearch.class.getSimpleName();
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
    EditText searchTermView;
    ArtistListAdapter adapter;

    /*@Override
    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);

        // Note: getValues() is a method in your ArrayAdaptor subclass
        savedState.putSerializable(ARTIST_RESULTS, adapter);

    }*/
    final Runnable updateResult = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_artist_search);
        artistListView = (ListView) findViewById(R.id.artist_list);
        searchTermView = (EditText) findViewById(R.id.artist_search);
        artists = new ArrayList<Artist>();

        adapter = new ArtistListAdapter(this, artists);


        artistListView.setAdapter(adapter);

        artistListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                Artist chosenArtist = (Artist) adapter.getItem(position);
                Intent intent = new Intent(getApplicationContext(), TopTracks.class);
                intent.putExtra(ARTIST_ID, chosenArtist.id);
                intent.putExtra(ARTIST_NAME, chosenArtist.name);
                startActivity(intent);
            }
        });

        searchTermView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.d(TAG, "Searching for " + v.getText());
                    artists.clear();
                    adapter.notifyDataSetChanged();
                    ArtistSearchTask runner = new ArtistSearchTask();
                    runner.execute(v.getText().toString());
                }
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class ArtistListAdapter extends BaseAdapter {

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

    private class ArtistSearchTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            spotify.searchArtists(params[0], new SpotifyCallback<ArtistsPager>() {
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
