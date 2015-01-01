package com.knowthissong;

/**
 * Created by niravnagda on 12/26/2014.
 */
import java.util.HashSet;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;

final class PlaySound {

    //    @Override
//    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
//        // TODO Auto-generated method stub
//    }
//
//    @Override
//    public void surfaceCreated(SurfaceHolder arg0) {
//        try {
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer.setDisplay(vidHolder);
//            mediaPlayer.setDataSource(vidAddress);
//            mediaPlayer.prepare();
//            mediaPlayer.setScreenOnWhilePlaying(true);
//            mediaPlayer.setOnPreparedListener(this);
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            int seekTo = 0;
//            if(getDuration() > 60000)
//                seekTo = getDuration()/2 - 30000;
//            seekTo(seekTo);
//            mediaPlayer.start();
//            mController = new MediaController(this);
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder arg0) {
//        // TODO Auto-generated method stub
//        mediaPlayer.stop();
//    }
//    @Override
//    public void onPrepared(MediaPlayer mp) {
//        mController.setMediaPlayer(this);
//        mController.setAnchorView(findViewById(R.id.surfView));
//        handler.post(new Runnable() {
//            public void run() {
//                mController.setEnabled(true);
//                mController.show();
//            }
//        });
//    }
//
//    @Override
//    public void start() {
//        mediaPlayer.start();
//    }
//
//    @Override
//    public void pause() {
//        mediaPlayer.pause();
//    }
//
//    @Override
//    public int getDuration() {
//        return mediaPlayer.getDuration();
//    }
//
//    @Override
//    public int getCurrentPosition() {
//        return mediaPlayer.getCurrentPosition();
//    }
//
//    @Override
//    public void seekTo(int pos) {
//        mediaPlayer.seekTo(pos);
//    }
//
//    @Override
//    public boolean isPlaying() {
//        return mediaPlayer.isPlaying();
//    }
//
//    @Override
//    public int getBufferPercentage() {
//        return 0;
//    }
//
//    @Override
//    public boolean canPause() {
//        return false;
//    }
//
//    @Override
//    public boolean canSeekBackward() {
//        return false;
//    }
//
//    @Override
//    public boolean canSeekForward() {
//        return true;
//    }
//
//    @Override
//    public int getAudioSessionId() {
//        return 0;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        /*
//         * the MediaController will hide after 3 seconds - tap the screen to
//         * make it appear again
//         */
//        mController.show();
//        return false;
//    }
//

}