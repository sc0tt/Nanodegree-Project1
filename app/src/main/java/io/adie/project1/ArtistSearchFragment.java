package io.adie.project1;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class ArtistSearchFragment extends Fragment {

    public final static String ARTIST_ID = "io.adie.project1.ARTIST_ID";
    public final static String ARTIST_NAME = "io.adie.project1.ARTIST_NAME";
    public final static String ARTIST_RESULTS = "io.adie.project1.ARTIST_RESULTS";
    public final static String ARTIST_DATA = "io.adie.project1.ARTIST_DATA";

    static final String TAG = ArtistSearchFragment.class.getSimpleName();
    final Runnable failedSearch = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), getString(R.string.no_artists), Toast.LENGTH_SHORT).show();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_artist_search, container, false);

        artistListView = (ListView) v.findViewById(R.id.artist_list);
        searchTermView = (SearchView) v.findViewById(R.id.artist_search);
        artists = new ArrayList<Artist>();

        if (savedInstanceState != null) {
            adapter = savedInstanceState.getParcelable(ARTIST_RESULTS);
        } else {
            adapter = new ArtistListAdapter(getActivity(), artists);
        }

        artistListView.setAdapter(adapter);

        artistListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                Artist chosenArtist = (Artist) adapter.getItem(position);
                Bundle b = new Bundle();
                b.putString(ArtistSearchFragment.ARTIST_NAME, chosenArtist.name);
                b.putString(ArtistSearchFragment.ARTIST_ID, chosenArtist.id);

                if(ArtistSearchActivity.isMasterDetails) {
                    TopTracksFragment f = new TopTracksFragment();

                    f.setArguments(b);

                    FragmentManager manager = getFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.top_tracks_container, f).addToBackStack(TAG).commit();
                }
                else {
                    Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                    intent.putExtra(ARTIST_DATA, b);
                    startActivity(intent);
                }
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
                        getActivity().runOnUiThread(failedSearch);
                    }

                    @Override
                    public void success(ArtistsPager artistsPager, Response response) {
                        List<Artist> results = artistsPager.artists.items;
                        if (results.size() > 0) {
                            artists.addAll(results);
                            getActivity().runOnUiThread(updateResult);
                        } else {
                            getActivity().runOnUiThread(failedSearch);
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
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);

        // Note: getValues() is a method in your ArrayAdaptor subclass
        savedState.putParcelable(ARTIST_RESULTS, adapter);

    }


}
