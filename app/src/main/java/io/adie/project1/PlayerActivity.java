package io.adie.project1;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {
    static final String TAG = PlayerActivity.class.getSimpleName();

    int currentSongIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            currentSongIndex = intent.getIntExtra(TopTracksFragment.SONG_INDEX, 0);

            PlayerFragment f = new PlayerFragment();
            Bundle b = new Bundle();
            b.putInt(TopTracksFragment.SONG_INDEX, currentSongIndex);

            f.setArguments(b);

            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.player_container, f).addToBackStack(TAG).commit();
        }
    }
}
