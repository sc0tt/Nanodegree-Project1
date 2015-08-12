package io.adie.project1;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;

import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener {
    static final String TAG = PlayerService.class.getSimpleName();
    static final String ACTION_PLAY = "io.adie.project1.PLAY";
    static final String ACTION_PAUSE = "io.adie.project1.PAUSE";
    static final String ACTION_RESUME = "io.adie.project1.RESUME";
    static final String SET_POSITION = "io.adie.project1.SET_POS";

    static final String SEND_POSITION = "io.adie.project1.SEND_POSITION";
    static final String REQUEST_POSITION = "io.adie.project1.REQUEST_POSITION";
    static final String SEND_LEN = "io.adie.project1.SET_LEN";
    static final String REQUEST_LEN = "io.adie.project1.REQUEST_LEN";
    static final String STREAM_URL = "io.adie.project1.URL";

    MediaPlayer mMediaPlayer = null;
    BroadcastReceiver mReceiver;
    private final Handler handler = new Handler();
    boolean paused = false;

    private Runnable updateCurrentTime = new Runnable() {
        public void run() {
            if (mMediaPlayer != null) {
                sendCurrentTime();
            }
            handler.postDelayed(this, 200); // 200 ms
        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_PLAY)) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                String songUrl = intent.getStringExtra(STREAM_URL);
                try {
                    mMediaPlayer.setDataSource(songUrl);
                } catch (IOException e) {
                    return 1;
                }
                mMediaPlayer.prepareAsync();
            } else if (intent.getAction().equals(ACTION_PAUSE)) {
                mMediaPlayer.pause();
                paused = true;
            } else if (intent.getAction().equals(ACTION_RESUME)) {
                mMediaPlayer.start();
                paused = false;
            }
        }

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(SET_POSITION)) {
                    int position = intent.getIntExtra(SET_POSITION, 0);
                    if (mMediaPlayer != null) {
                        mMediaPlayer.seekTo(position);
                    }
                } else if(action.equals(REQUEST_POSITION)) {
                    sendCurrentTime();
                } else if(action.equals(REQUEST_LEN)) {
                    sendSongLength();
                }
            }
        };
        IntentFilter mActionFilter = new IntentFilter();
        mActionFilter.addAction(SET_POSITION);
        mActionFilter.addAction(REQUEST_POSITION);
        mActionFilter.addAction(REQUEST_LEN);
        registerReceiver(mReceiver, mActionFilter);

        return 0;
    }

    public void onPrepared(MediaPlayer player) {
        player.start();
        paused = false;
        sendSongLength();
        handler.postDelayed(updateCurrentTime, 250);
    }

    public void sendCurrentTime() {
        Intent response = new Intent();
        response.setAction(SEND_POSITION);
        response.putExtra(SEND_POSITION, mMediaPlayer.getCurrentPosition());
        sendBroadcast(response);
    }

    public void sendSongLength() {
        Intent response = new Intent();
        response.setAction(SEND_LEN);
        response.putExtra(SEND_LEN, mMediaPlayer.getDuration());
        sendBroadcast(response);
    }
}