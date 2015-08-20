package io.adie.project1;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {
    static final String TAG = PlayerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState == null) {
            Intent intent = getIntent();

            PlayerFragment f = new PlayerFragment();
            Bundle b = intent.getBundleExtra(TopTracksFragment.SONG_DATA);

            f.setArguments(b);

            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.player_container, f).addToBackStack(TAG).commit();
        }
    }
}
