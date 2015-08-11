package io.adie.project1;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener {
    static final String ACTION_PLAY = "io.adie.project1.PLAY";
    static final String ACTION_PAUSE = "io.adie.project1.PAUSE";
    static final String STREAM_URL = "io.adie.project1.URL";

    MediaPlayer mMediaPlayer = null;
    BroadcastReceiver mReceiver;

    public IBinder onBind(Intent intent){
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        if (intent.getAction().equals(ACTION_PLAY)) {
            String songUrl = intent.getStringExtra(STREAM_URL);
            try {
                mMediaPlayer.setDataSource(songUrl);
            } catch (IOException e) {
                return 1;
            }
            mMediaPlayer.prepareAsync();
        }

        return 0;
    }

    public void onPrepared(MediaPlayer player) {
        player.start();
//        int minutes = ((player.getDuration() / 1000) / 60);
//        int seconds = ((player.getDuration() / 1000) % 60);
//        totalTime.setText(minutes + ":" + seconds);
    }
}