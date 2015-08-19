package io.adie.project1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Track;

public class PlayerActivity extends AppCompatActivity {
    static final String TAG = PlayerActivity.class.getSimpleName();
    static final String NOW_PLAYING = "io.adie.project1.NOW_PLAYING";
    static final String IS_PLAYING = "io.adie.project1.IS_PLAYING";

    int currentSongIndex;

    TextView songName;
    TextView artistName;
    TextView albumName;
    TextView totalTime;
    TextView currentTime;
    ImageView albumArt;

    ImageButton playPauseTrack;
    ImageButton nextTrack;
    ImageButton prevTrack;

    SeekBar seekBar;

    PlayerService playerService;
    BroadcastReceiver mReceiver;

    private boolean playing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();
        currentSongIndex = intent.getIntExtra(TopTracksFragment.SONG_INDEX, 0);

        playerService = new PlayerService();

        songName = (TextView) findViewById(R.id.player_song);
        artistName = (TextView) findViewById(R.id.player_artist);
        albumName = (TextView) findViewById(R.id.player_album);
        totalTime = (TextView) findViewById(R.id.play_total_time);
        currentTime = (TextView) findViewById(R.id.play_current_time);
        albumArt = (ImageView) findViewById(R.id.player_art);
        prevTrack = (ImageButton) findViewById(R.id.prevTrack);
        playPauseTrack = (ImageButton) findViewById(R.id.playPause);
        nextTrack = (ImageButton) findViewById(R.id.nextTrack);
        seekBar = (SeekBar) findViewById(R.id.song_seek_bar);

        Track selectedTrack = TopTracksFragment.tracks.get(currentSongIndex);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
                if (fromUser) {
                    Intent requestSeek = new Intent();
                    requestSeek.setAction(PlayerService.SET_POSITION);
                    requestSeek.putExtra(PlayerService.SET_POSITION, position);
                    sendBroadcast(requestSeek);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });

        prevTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSongIndex == 0) {
                    currentSongIndex = (TopTracksFragment.tracks.size() - 1);
                } else {
                    currentSongIndex--;
                }

                Track selectedTrack = TopTracksFragment.tracks.get(currentSongIndex);
                playSong(selectedTrack);
            }
        });

        playPauseTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playIntent = new Intent(PlayerActivity.this, PlayerService.class);
                if (playing) {
                    playIntent.setAction(PlayerService.ACTION_PAUSE);
                } else {
                    playIntent.setAction(PlayerService.ACTION_RESUME);
                }
                playing = !playing;
                updatePlayPauseIcon();
                startService(playIntent);
            }
        });

        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSongIndex == (TopTracksFragment.tracks.size() - 1)) {
                    currentSongIndex = 0;
                } else {
                    currentSongIndex++;
                }
                Track selectedTrack = TopTracksFragment.tracks.get(currentSongIndex);
                playSong(selectedTrack);
            }
        });

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(PlayerService.SEND_LEN)) {
                    int songLen = intent.getIntExtra(PlayerService.SEND_LEN, 0);
                    int minutes = ((songLen / 1000) / 60);
                    int seconds = ((songLen / 1000) % 60);
                    String out = String.format("%d:%02d", minutes, seconds);
                    totalTime.setText(out);
                    seekBar.setMax(songLen);
                } else if (action.equals(PlayerService.SEND_POSITION)) {
                    int currTime = intent.getIntExtra(PlayerService.SEND_POSITION, 0);
                    int minutes = ((currTime / 1000) / 60);
                    int seconds = ((currTime / 1000) % 60);
                    String out = String.format("%d:%02d", minutes, seconds);
                    currentTime.setText(out);
                    seekBar.setProgress(currTime);
                }
            }
        };
        IntentFilter mActionFilter = new IntentFilter();
        mActionFilter.addAction(PlayerService.SEND_LEN);
        mActionFilter.addAction(PlayerService.SEND_POSITION);
        registerReceiver(mReceiver, mActionFilter);

        if (savedInstanceState != null) {
            currentSongIndex = savedInstanceState.getInt(NOW_PLAYING, 0);
            playing = savedInstanceState.getBoolean(IS_PLAYING, false);

            Intent requestSongLen = new Intent();
            requestSongLen.setAction(PlayerService.REQUEST_LEN);
            sendBroadcast(requestSongLen);

            Intent requestSongCurr = new Intent();
            requestSongCurr.setAction(PlayerService.REQUEST_POSITION);
            sendBroadcast(requestSongCurr);

            Track currentTrack = TopTracksFragment.tracks.get(currentSongIndex);
            updateUIComponents(currentTrack);
        } else {
            playSong(selectedTrack);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(NOW_PLAYING, currentSongIndex);
        savedInstanceState.putBoolean(IS_PLAYING, playing);
    }

    @Override
    public void onDestroy() {
        try {
            PlayerActivity.this.unregisterReceiver(mReceiver);
        } catch ( IllegalArgumentException e ) {
            Log.e(TAG, e.getMessage());
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
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

    private void updatePlayPauseIcon() {
        if (playing) {
            playPauseTrack.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            playPauseTrack.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void playSong(Track t) {
        playing = true;
        updateUIComponents(t);
        Intent playIntent = new Intent(PlayerActivity.this, PlayerService.class);
        playIntent.putExtra(PlayerService.STREAM_URL, t.preview_url);
        playIntent.setAction(PlayerService.ACTION_PLAY);
        startService(playIntent);
    }

    private void updateUIComponents(Track t) {
        updatePlayPauseIcon();

        songName.setText(t.name);
        artistName.setText(t.artists.get(0).name);
        albumName.setText(t.album.name);
        Picasso.with(PlayerActivity.this).load(t.album.images.get(0).url).into(albumArt);
    }


}
