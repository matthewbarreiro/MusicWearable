package com.github.pocmo.sensordashboard;

import android.os.IBinder;
import android.content.Intent;
import android.app.Service;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by Liam on 2017-02-02.
 */

import com.squareup.otto.Subscribe;
import com.github.pocmo.sensordashboard.data.Sensor;
import com.github.pocmo.sensordashboard.events.BusProvider;
import com.github.pocmo.sensordashboard.events.NewSensorEvent;
import com.github.pocmo.sensordashboard.events.SensorUpdatedEvent;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener
{

    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    private ArrayList<Song> filteredSongs;
    //current position
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();

    private float curHeartRate;
    private float curStepRate;

    private static final String TAG = "Music Service";
    private int debug=1;

    private RefreshView refreshView;


    @Override
    public IBinder onBind(Intent intent)
    {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        player.stop();
        player.release();
        return false;
    }

    public void setRefreshView(RefreshView refresher)
    {
        refreshView=refresher;
    }

    public void playSong()
    {
        //reset mediaplayer
        player.reset();

        //get song
        if(debug==1)
            Log.d(TAG,"Position = "+songPosn);

        Song playSong = filteredSongs.get(songPosn);
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try
        {
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e)
        {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    public void onCreate()
    {
        //create the service
        super.onCreate();
        //initialize position
        songPosn=0;
        //create player
        player = new MediaPlayer();
        initMusicPlayer();

    }

    public void initMusicPlayer()
    {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setSong(int songIndex)
    {
        songPosn=songIndex;
    }

    public void setList(ArrayList<Song> theSongs)
    {
        songs=theSongs;
        filteredSongs = new ArrayList<Song>();
        filterSongs();
        refreshView.refresh(filteredSongs);
    }

    public void filterSongs()
    {
        if(filteredSongs!=null)
            filteredSongs.clear();
        for(Song song:songs) {
            if (curHeartRate >= 80)
            {
                if (song.getTitle().charAt(0) < 'O')
                    filteredSongs.add(song);
            } else
            {
                if (song.getTitle().charAt(0) >= 'O')
                    filteredSongs.add(song);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer)
    {
        filterSongs();
        refreshView.refresh(filteredSongs);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        //start playback
        player.start();     //CHECK
    }

    public class MusicBinder extends Binder
    {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Subscribe
    public void onSensorUpdatedEvent(final SensorUpdatedEvent event)
    {
        if(event.getSensor().getId()==13)
        {
            curStepRate=event.getDataPoint().getValues()[0];
        }
        else
        {
            curHeartRate=event.getDataPoint().getValues()[0];
        }
        //TextView textView = (TextView) findViewById(R.id.empty_state);
        //textView.append(curHeartRate+", "+curStepRate+", "+"\n");
    }
}
