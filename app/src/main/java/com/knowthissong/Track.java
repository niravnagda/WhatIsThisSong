package com.knowthissong;

/**
 * Created by niravnagda on 12/27/2014.
 */
// Our model for the metadata for a track that we care about
class Track {
    public String key;
    public String trackName;
    public String artistName;
    public String albumName;
    public String albumArt;

    public Track(String k, String name, String artist, String album, String uri) {
        key = k;
        trackName = name;
        artistName = artist;
        albumName = album;
        albumArt = uri;
    }
}