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

import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RefreshView
{

    //wear vars
    private RemoteSensorManager remoteSensorManager;
    Toolbar mToolbar;
    //private ViewPager pager;
    private View emptyState;
    private NavigationView mNavigationView;
    private Menu mNavigationViewMenu;
    private List<Node> mNodes;
    //private float curHeartRate;
    //private float curStepRate;


    //music player vars
    private ArrayList<Song> songList;
    //private ArrayList<Song> filteredSongList;
    private ListView songView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;

    private static final String TAG = "MainActivity";
    private int debug=1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        //emptyState = findViewById(R.id.empty_state);

        //mNavigationView = (NavigationView) findViewById(R.id.navView);
        //mNavigationView.setNavigationItemSelectedListener(this);
        //mNavigationViewMenu = mNavigationView.getMenu();

        //initToolbar();
//        initViewPager();

        remoteSensorManager = RemoteSensorManager.getInstance(this);

//        final EditText tagname = (EditText) findViewById(R.id.tagname);

//        findViewById(R.id.tag_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String tagnameText = "EMPTY";
//                if (!tagname.getText().toString().isEmpty()) {
//                    tagnameText = tagname.getText().toString();
//                }
//
//                RemoteSensorManager.getInstance(MainActivity.this).addTag(tagnameText);
//            }
//        });

//        tagname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//            @Override
//            public boolean onEditorAction(TextView v, int actionId,
//                                          KeyEvent event) {
//                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
//                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//
//                    in.hideSoftInputFromWindow(tagname
//                                    .getApplicationWindowToken(),
//                            InputMethodManager.HIDE_NOT_ALWAYS);
//
//
//                    return true;
//
//                }
//                return false;
//            }
//        });

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
        getSongList();

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
//                    switch (item.getItemId()) {
//                        case R.id.action_about:
//                            startActivity(new Intent(MainActivity.this, AboutActivity.class));
//                            return true;
//                        case R.id.action_export:
//                            startActivity(new Intent(MainActivity.this, ExportActivity.class));
//                            return true;
//                    }

                    return true;
                }
            });
        }
    }

//    private void initViewPager() {
//        pager = (ViewPager) findViewById(R.id.pager);
//
//        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int i, float v, int i2) {
//
//            }
//
//            @Override
//            public void onPageSelected(int id) {
//                ScreenSlidePagerAdapter adapter = (ScreenSlidePagerAdapter) pager.getAdapter();
//                if (adapter != null) {
//                    Sensor sensor = adapter.getItemObject(id);
//                    if (sensor != null) {
//                        remoteSensorManager.filterBySensorId((int) sensor.getId());
//                    }
//                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int i) {
//
//            }
//        });
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        List<Sensor> sensors = RemoteSensorManager.getInstance(this).getSensors();
        //pager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager(), sensors));

//        if (sensors.size() > 0) {
//            emptyState.setVisibility(View.VISIBLE);
//        }
//            emptyState.setVisibility(View.GONE);
//        } else {
//            emptyState.setVisibility(View.VISIBLE);
//        }

        remoteSensorManager.startMeasurement();

        /*mNavigationViewMenu.clear();
        remoteSensorManager.getNodes(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(final NodeApi.GetConnectedNodesResult pGetConnectedNodesResult) {
                mNodes = pGetConnectedNodesResult.getNodes();
                for (Node node : mNodes) {
                    SubMenu menu = mNavigationViewMenu.addSubMenu(node.getDisplayName());

                    MenuItem item = menu.add("15 sensors");
                    if (node.getDisplayName().startsWith("G")) {
                        item.setChecked(true);
                        item.setCheckable(true);
                    } else {
                        item.setChecked(false);
                        item.setCheckable(false);
                    }
                }
            }
        });*/

    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);

        remoteSensorManager.stopMeasurement();
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem pMenuItem) {
        Toast.makeText(this, "Device: " + pMenuItem.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }

//    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
//        private List<Sensor> sensors;
//
//        public ScreenSlidePagerAdapter(FragmentManager fm, List<Sensor> symbols) {
//            super(fm);
//            this.sensors = symbols;
//        }
//
//
//        public void addNewSensor(Sensor sensor) {
//            this.sensors.add(sensor);
//        }
//
//
//        private Sensor getItemObject(int position) {
//            return sensors.get(position);
//        }
//
//        @Override
//        public android.support.v4.app.Fragment getItem(int position) {
//            return SensorFragment.newInstance(sensors.get(position).getId());
//        }
//
//        @Override
//        public int getCount() {
//            return sensors.size();
//        }
//
//    }


    private void notifyUSerForNewSensor(Sensor sensor) {
        Toast.makeText(this, "New Sensor!\n" + sensor.getName(), Toast.LENGTH_SHORT).show();
    }


/*    @Subscribe
    public void onNewSensorEvent(final NewSensorEvent event) {
        //((ScreenSlidePagerAdapter) pager.getAdapter()).addNewSensor(event.getSensor());
        //pager.getAdapter().notifyDataSetChanged();
        emptyState.setVisibility(View.GONE);
        notifyUSerForNewSensor(event.getSensor());

    }*/
/*
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
        TextView textView = (TextView) findViewById(R.id.empty_state);
        textView.append(curHeartRate+", "+curStepRate+", "+"\n");
    }
*/



//    private void printData (View emptyState){
//
//
//
//       // EditText editMessage=(EditText)findViewById(R.id.edit_message);
//        TextView textView = (TextView) findViewById(R.id.empty_state);

//
//
//
//        //get text from edittext and convert it to string
//        //String messageString=editMessage.getText().toString();
//
//        //set string from edittext to textview
////        textView.setText(messageString);
////        textView.append(messageString + "\n");
//        textView.append("HARDCODE!"+ "\n");
//
//        //clear edittext after sending text to message
//       // editMessage.setText("");
//
//
//    }
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
            //case R.id.action_shuffle:
            //    //shuffle
            //    break;
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
//                if(curHeartRate>=80)
//                {
//                    if(thisTitle.charAt(0)<'O')
//                        filteredSongList.add(new Song(thisId, thisTitle, thisArtist));
//
//                }
//                else
//                {
//                    if(thisTitle.charAt(0)>='O')
//                        filteredSongList.add(new Song(thisId, thisTitle, thisArtist));
//                }
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
}

