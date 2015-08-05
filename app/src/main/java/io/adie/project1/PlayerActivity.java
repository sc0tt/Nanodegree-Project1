package io.adie.project1;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import kaaes.spotify.webapi.android.models.Track;

public class PlayerActivity extends AppCompatActivity {
    static final String TAG = PlayerActivity.class.getSimpleName();
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

    MediaPlayer player;

    private boolean playing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();
        currentSongIndex = intent.getIntExtra(TopTracksActivity.SONG_INDEX, 0);

        songName = (TextView) findViewById(R.id.player_song);
        artistName = (TextView) findViewById(R.id.player_artist);
        albumName = (TextView) findViewById(R.id.player_album);
        totalTime = (TextView) findViewById(R.id.play_total_time);
        albumArt = (ImageView) findViewById(R.id.player_art);
        prevTrack = (ImageButton) findViewById(R.id.prevTrack);
        playPauseTrack = (ImageButton) findViewById(R.id.playPause);
        nextTrack = (ImageButton) findViewById(R.id.nextTrack);

        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        Track selectedTrack = TopTracksActivity.tracks.get(currentSongIndex);
        playSong(selectedTrack);

        prevTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSongIndex == 0) {
                    currentSongIndex = (TopTracksActivity.tracks.size() - 1);
                } else {
                    currentSongIndex--;
                }

                Track selectedTrack = TopTracksActivity.tracks.get(currentSongIndex);
                playSong(selectedTrack);
            }
        });

        playPauseTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playing) {
                    player.pause();
                } else {
                    player.start();
                }
                updatePlayPauseIcon();
                playing = !playing;
            }
        });

        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSongIndex == (TopTracksActivity.tracks.size() - 1)) {
                    currentSongIndex = 0;
                } else {
                    currentSongIndex++;
                }
                Track selectedTrack = TopTracksActivity.tracks.get(currentSongIndex);
                playSong(selectedTrack);
            }
        });


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
            playPauseTrack.setImageResource(android.R.drawable.ic_media_play);
        } else {
            playPauseTrack.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void playSong(Track t) {
        playing = true;
        updatePlayPauseIcon();

        songName.setText(t.name);
        artistName.setText(t.artists.get(0).name);
        albumName.setText(t.album.name);
        Picasso.with(PlayerActivity.this).load(t.album.images.get(0).url).into(albumArt);

        try {
            player.setDataSource(t.preview_url);
            player.prepare();
            player.start();
            int minutes = (int) ((player.getDuration() / 1000) / 60);
            int seconds = (int) ((player.getDuration() / 1000) % 60);
            totalTime.setText(minutes + ":" + seconds);

        } catch (IOException e) {
            // todo
        }
    }
}
