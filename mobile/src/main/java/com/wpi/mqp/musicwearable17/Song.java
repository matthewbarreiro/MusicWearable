package com.wpi.mqp.musicwearable17;

/**
 * Created by Liam on 2017-01-27.
 * Using code from code.tutsplus.com
 */
public class Song
{
    private long id;
    private String title;
    private String artist;

    public Song(long songID, String songTitle, String songArtist)
    {
        id = songID;
        title = songTitle;
        artist = songArtist;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
}