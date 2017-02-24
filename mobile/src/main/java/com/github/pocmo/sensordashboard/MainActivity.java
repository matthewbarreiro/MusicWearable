package com.github.pocmo.sensordashboard;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
//import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.sensordashboard.data.Sensor;
import com.github.pocmo.sensordashboard.events.BusProvider;
import com.github.pocmo.sensordashboard.events.NewSensorEvent;
import com.github.pocmo.sensordashboard.events.SensorUpdatedEvent;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.squareup.otto.Subscribe;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import com.github.pocmo.sensordashboard.MusicService.MusicBinder;
import android.widget.MediaController.MediaPlayerControl;

import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RefreshView, MediaPlayerControl
{

    //wear vars

    private RemoteSensorManager remoteSensorManager;
    Toolbar mToolbar;
    private View emptyState;
    private NavigationView mNavigationView;
    private Menu mNavigationViewMenu;
    private List<Node> mNodes;

    private MusicController controller;


    //music player vars
    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;


    private static final String TAG = "MainActivity";
    private int debug=1;
    private boolean paused=false, playbackPaused=false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        remoteSensorManager = RemoteSensorManager.getInstance(this);

        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
        getSongList();

        setController();

        if(debug==1)
            Log.d(TAG,"Create");

    }

    public void refresh(ArrayList<Song> songs)
    {
        if(debug==1)
            Log.d(TAG,"Refresh Ran");
        //sort
        Collections.sort(songs, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        SongAdapter songAdt = new SongAdapter(this, songs);
        songView.setAdapter(songAdt);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setTitle(R.string.app_name);
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    return true;
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(paused)
        {
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() 
    {
        controller.hide();
        super.onStop();
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem pMenuItem) {
        Toast.makeText(this, "Device: " + pMenuItem.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }


    private void notifyUSerForNewSensor(Sensor sensor) {
        Toast.makeText(this, "New Sensor!\n" + sensor.getName(), Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy()
    {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_shuffle:
  				musicSrv.setShuffle();
  				break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void songPicked(View view)
    {
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();   
  		if(playbackPaused)
  		{
    		setController();
    		playbackPaused=false;
  		}
  		controller.show(0);
    }

    public void getSongList()
    {
        //get the music

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst())
        {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do
            {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);

                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }



    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            if(debug==1)
                Log.d(TAG, "Service Connected");
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            musicSrv.setRefreshView(MainActivity.this);
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            musicBound = false;
        }
    };

    @Override
    protected void onStart()
    {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            startService(playIntent);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void    start() 
    {
    	musicSrv.go();
    }

    @Override
    public void    pause() 
    {
  		playbackPaused=true;
  		musicSrv.pausePlayer();
    }

    @Override
	public int getDuration() 
	{
  		if(musicSrv!=null &amp;&amp; musicBound &amp;&amp; musicSrv.isPng())
    		return musicSrv.getDur();
  		else return 0;
	}

	@Override
	public int getCurrentPosition()
	{
  		if(musicSrv!=null &amp;&amp; musicBound &amp;&amp; musicSrv.isPng())
    		return musicSrv.getPosn();
  		else return 0;
	}

    @Override
    public void seekTo(int pos) 
    {
    	musicSrv.seek(pos);
    }

	@Override
	public boolean isPlaying() 
	{
  		if(musicSrv!=null &amp;&amp; musicBound)
    		return musicSrv.isPng();
  		return false;
	}

    @Override
    public int     getBufferPercentage() 
    {
        return 0;
    }

    @Override
    public boolean canPause() 
    {
        return true;
    }

    @Override
    public boolean canSeekBackward() 
    {
        return true;
    }

    @Override
    public boolean canSeekForward() 
    {
        return true;
    }

    @Override
    public int     getAudioSessionId() 
    {
        return 0;
    }

    private void setController()
    {
        controller = new MusicController(this);

        //forward and back buttons
        controller.setPrevNextListeners(new View.OnClickListener() 
        {

  			@Override
  			public void onClick(View v) 
  			{
    		playNext();
  			}
  		
		}, 
		new View.OnClickListener() 
		{
  			@Override
  			public void onClick(View v) 
  			{
    			playPrev();
  			}
		});

		controller.setMediaPlayer(this);
		controller.setAnchorView(findViewById(R.id.song_list)); //MIGHT NEED TO UPDATE THIS WITH REFRESH
		controller.setEnabled(true);

    }

    //play next
	private void playNext()
	{
  		musicSrv.playNext();
  		if(playbackPaused)
  		{
    		setController();
    		playbackPaused=false;
  		}
  		controller.show(0);
	}
 
//play previous
	private void playPrev(
	{
  		musicSrv.playPrev();
  		if(playbackPaused)
  		{
    		setController();
    		playbackPaused=false;
  		}
  		controller.show(0);
	}
}

