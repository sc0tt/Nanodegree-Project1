package io.adie.project1;

import android.app.Fragment;
import android.app.FragmentManager;
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
    public final static String SONG_DATA = "io.adie.project1.SONG_DATA";

    public final static String ARTISTS = "io.adie.project1.ARTISTS";
    public final static String URLS = "io.adie.project1.URLS";
    public final static String TITLES = "io.adie.project1.TITLES";
    public final static String ALBUMS = "io.adie.project1.ALBUMS";
    public final static String ARTWORK = "io.adie.project1.ARTWORK";

    static final String TAG = TopTracksFragment.class.getSimpleName();
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
    ArrayList<String> previewURLs = new ArrayList<>();
    ArrayList<String> artists = new ArrayList<>();
    ArrayList<String> albums = new ArrayList<>();
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> artwork = new ArrayList<>();
    private List<Track> tracks;

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
                        for (Track t : tracks) {
                            previewURLs.add(t.preview_url);
                            artists.add(t.artists.get(0).name);
                            albums.add(t.album.name);
                            titles.add(t.name);
                            artwork.add(t.album.images.get(0).url);
                        }
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
                Bundle b = new Bundle();
                b.putInt(TopTracksFragment.SONG_INDEX, position);
                b.putStringArrayList(URLS, previewURLs);
                b.putStringArrayList(ARTISTS, artists);
                b.putStringArrayList(ALBUMS, albums);
                b.putStringArrayList(TITLES, titles);
                b.putStringArrayList(ARTWORK, artwork);

                if (ArtistSearchActivity.isMasterDetails) {
                    PlayerFragment f = new PlayerFragment();

                    f.setArguments(b);

                    FragmentManager manager = getFragmentManager();

                    f.show(manager, TAG);
                } else {
                    Intent intent = new Intent(getActivity(), PlayerActivity.class);
                    intent.putExtra(SONG_DATA, b);
                    startActivity(intent);
                }

            }
        });

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);

        savedState.putParcelable(SONG_RESULTS, adapter);

    }
}
