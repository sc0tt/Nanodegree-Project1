package io.adie.project1;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class TopTracksActivity extends AppCompatActivity {
    static final String TAG = TopTracksActivity.class.getSimpleName();
    String artistName;
    String artistId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        Intent intent = getIntent();
        artistName = intent.getStringExtra(ArtistSearchFragment.ARTIST_NAME);
        artistId = intent.getStringExtra(ArtistSearchFragment.ARTIST_ID);

        actionBarSetup(artistName);

        TopTracksFragment f = new TopTracksFragment();

        Bundle b = new Bundle();
        b.putString(ArtistSearchFragment.ARTIST_NAME, artistName);
        b.putString(ArtistSearchFragment.ARTIST_ID, artistId);

        f.setArguments(b);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.top_tracks_container, f).addToBackStack(TAG).commit();


    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void actionBarSetup(String subtitle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }
}
