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
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;

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

    private String songTitle="";
    private static final int NOTIFY_ID=1;

    private boolean shuffle=false;
    private Random rand;



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

        songTitle=playSong.getTitle();

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
        rand=new Random();

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
        if(debug==1)
            Log.d(TAG,"Songs filtered with value: "+curHeartRate);
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
        if(player.getCurrentPosition()>0)
        {
            mediaPlayer.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) 
    {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        //start playback
        player.start();     //CHECK

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
        notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
        Notification.Builder builder = new Notification.Builder(this);
 
        builder.setContentIntent(pendInt)
        .setSmallIcon(R.drawable.play)
        .setTicker(songTitle)
        .setOngoing(true)
        .setContentTitle("Playing")
        .setContentText(songTitle);
        Notification not = builder.build();
 
        startForeground(NOTIFY_ID, not);
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
        if(debug==1)
            Log.d(TAG,"sensor event");
        if(event.getSensor().getId()==13)
        {
            curStepRate=event.getDataPoint().getValues()[0];
        }
        else
        {
            curHeartRate=event.getDataPoint().getValues()[0];
            if(debug==1)
                Log.d(TAG,"heart rate set to "+curHeartRate);
        }
        //TextView textView = (TextView) findViewById(R.id.empty_state);
        //textView.append(curHeartRate+", "+curStepRate+", "+"\n");
    }


    //playback functions
    public int getPosn()
    {
        return player.getCurrentPosition();
    }
 
    public int getDur()
    {
        return player.getDuration();
    }
 
    public boolean isPng()
    {
        return player.isPlaying();
    }
 
    public void pausePlayer()
    {
        player.pause();
    }
 
    public void seek(int posn)
    {
        player.seekTo(posn);
    }
 
    public void go()
    {
        player.start();
    }

    public void playPrev()
    {
        songPosn--;
        if(songPosn<0) songPosn=songs.size()-1;
        playSong();
    }

    public void playNext()
    {
        if(shuffle)
        {
            int newSong = songPosn;
            while(newSong==songPosn)
            {
                newSong=rand.nextInt(songs.size());
            }
        songPosn=newSong;
        }
        else
        {
            songPosn++;
            if(songPosn>=songs.size()) songPosn=0;
        }
        playSong();
    }

    @Override
    public void onDestroy()
    {
        stopForeground(true);
    }

    public void setShuffle()
    {
        if(shuffle) shuffle=false;
        else shuffle=true;
    }


}
