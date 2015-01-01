package com.knowthissong;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;


import java.io.IOException;
import java.util.logging.Handler;

/**
 * Created by niravnagda on 12/25/2014.
 */
public class LoadMusic {

    public LoadMusic(MediaPlayer mp) throws IOException{
        mp.reset();
        mp.prepare();
    }

    public void startMusic(MediaPlayer mp, int id) throws Exception {
        mp.start();
    }
}