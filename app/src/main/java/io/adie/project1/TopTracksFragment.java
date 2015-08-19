package io.adie.project1;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
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
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class TopTracksFragment extends Fragment {
    public final static String SONG_RESULTS = "io.adie.project1.SONG_RESULTS";
    public final static String SONG_INDEX = "io.adie.project1.SONG_INDEX";
    static final String TAG = TopTracksFragment.class.getSimpleName();
    public static List<Track> tracks;
    final Runnable failedSearch = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), getString(R.string.no_songs_found), Toast.LENGTH_SHORT).show();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        Bundle b = getArguments();
        artistName = b.getString(ArtistSearchFragment.ARTIST_NAME);
        artistId = b.getString(ArtistSearchFragment.ARTIST_ID);

        songListView = (ListView) v.findViewById(R.id.song_list);
        tracks = new ArrayList<Track>();
        if (savedInstanceState != null) {
            adapter = savedInstanceState.getParcelable(SONG_RESULTS);
        } else {
            adapter = new TrackListAdapter(getActivity(), tracks);

            spotify.getArtistTopTrack(artistId, "US", new Callback<Tracks>() {
                @Override
                public void success(Tracks resultingTracks, Response response) {
                    List<Track> results = resultingTracks.tracks;
                    if (results.size() > 0) {
                        tracks.addAll(results);
                        getActivity().runOnUiThread(updateResult);
                    } else {
                        getActivity().runOnUiThread(failedSearch);
                    }
                    getActivity().runOnUiThread(updateResult);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, error.getLocalizedMessage());
                    getActivity().runOnUiThread(failedSearch);
                }
            });
        }

        songListView.setAdapter(adapter);

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long unused) {
                if(ArtistSearchActivity.isMasterDetails) {
                    PlayerFragment f = new PlayerFragment();
                    Bundle b = new Bundle();
                    b.putInt(TopTracksFragment.SONG_INDEX, position);

                    f.setArguments(b);

                    FragmentManager manager = getFragmentManager();

                    f.show(manager, TAG);
                }
                else {
                    Intent intent = new Intent(getActivity(), PlayerActivity.class);
                    intent.putExtra(SONG_INDEX, position);
                    startActivity(intent);
                }

            }
        });

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);

        // Note: getValues() is a method in your ArrayAdaptor subclass
        savedState.putParcelable(SONG_RESULTS, adapter);

    }
}
