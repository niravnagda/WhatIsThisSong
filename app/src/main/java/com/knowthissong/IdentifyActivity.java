package com.knowthissong;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.rdio.android.api.Rdio;
import com.rdio.android.api.RdioApiCallback;
import com.rdio.android.api.RdioListener;
import com.rdio.android.api.OAuth1WebViewActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class IdentifyActivity extends ActionBarActivity implements RdioListener {

    private MediaPlayer player;
    private Queue<Track> trackQueue;

    private static Rdio rdio;
    private static final String TAG = "WhatIsThisSong";

    private ImageView albumArt;
    private ImageView playPause;

    private static String accessToken = null;
    private static String accessTokenSecret = null;

    private static final String appKey = "dbzgvdm6t99cghz7fckwvqye";
    private static final String appSecret = "XcKbWWF4q9";

    private static final String PREF_ACCESSTOKEN = "prefs.accesstoken";
    private static final String PREF_ACCESSTOKENSECRET = "prefs.accesstokensecret";

    private static String collectionKey = null;
    private static String queryString = "love";
    private static String[] easyStrings = {"love", "yeah", "hell","die", "like", "Moon", "Die", "it", "Maroon", "Ketty"};
    private static String[] mediumStrings = {"Get", "Thang", "like","pop", "ya", "fire", "don't", "Rock","disco", "Dancing"};
    private static String[] hardStrings = {"Woman", "Baby", "Twist", "Little", "Christmas", "Penny", "Polka", "Swing", "Sing", "Uncle"};

    private DialogFragment getHeavyRotationDialog;
    private DialogFragment getUserDialog;
    private AlertDialog alert;
    private boolean finish = false;
    private AlertDialog.Builder scoresDialog;
    public Track track;
    public Bitmap artwork;
    TextView time;

    private static int currentInd = 0;
    private static int currentSong = 0;
    private String[] names;
    String level = "Easy";
    Timer T = new Timer();
    int count = 30;

    private HashMap<String, Integer> scores;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);
        //Display the rules
        displayRules();

        //Set the difficulty level and names of all the participants
        Intent intent = getIntent();
        int difficultLevel = intent.getIntExtra("difficultyLevel", 2);
        Random rn = new Random();
        if(difficultLevel == 2) {
            level = "Hard";
            int temp = (rn.nextInt() % hardStrings.length);
            if(temp < 0)
                temp = 0;
            queryString = hardStrings[0];
        }
        else if(difficultLevel == 1) {
            level = "Medium";
            int temp = (rn.nextInt() % mediumStrings.length);
            if(temp < 0)
                temp = 0;
            queryString = mediumStrings[temp];
        }
        else {
            level = "Easy";
            int temp = (rn.nextInt() % easyStrings.length);
            if(temp < 0)
                temp = 0;
            queryString = easyStrings[temp];
        }
        names = intent.getStringArrayExtra("names");
        TextView name = (TextView) findViewById(R.id.name);
        name.setText("Name: " + names[0]);

        TextView difficulty = (TextView) findViewById(R.id.level);
        difficulty.setText(level);

        // Set the timer to 30
        time = (TextView) findViewById(R.id.timer);

        if(player != null)
            player.stop();

        trackQueue = new LinkedList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Initialize our Rdio object.  If we have cached access credentials, then use them - otherwise
                // Initialize w/ null values and the user will be prompted (if the Rdio app is installed), or
                // we'll fallback to 30s samples.
                if (rdio == null) {
                    SharedPreferences settings = getPreferences(MODE_PRIVATE);
                    accessToken = settings.getString(PREF_ACCESSTOKEN, null);
                    accessTokenSecret = settings.getString(PREF_ACCESSTOKENSECRET, null);

                    rdio = new Rdio(appKey, appSecret, accessToken, accessTokenSecret, getApplicationContext(), IdentifyActivity.this);
                    rdio.prepareForPlayback();
                }
            }
        }).start();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdView layout = (AdView) findViewById(R.id.ad);
                final AdView mAdView = new AdView(getApplicationContext());
                mAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
                mAdView.setAdUnitId(getString(R.string.adUnitId));
                mAdView.setAdListener(new adListener());
                layout.addView(mAdView);

                AdRequest.Builder mAdRequest = new AdRequest.Builder();
                mAdRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
                mAdRequest.addKeyword("games");
                mAdView.loadAd(mAdRequest.build());
            }
        });

        playPause = (ImageView)findViewById(R.id.playPause);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });
        albumArt = (ImageView)findViewById(R.id.albumArt);

        ImageButton back = (ImageButton) findViewById(R.id.Back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish = true;
                showScores();
                player = null;
            }
        });

        scores = new HashMap<String, Integer>(10);
        for(int i=0;i<10;i++) {
            if(!names[i].equals("")) {
                scores.put(names[i],0);
            }
            else
                continue;
        }

        ImageButton wrong = (ImageButton) findViewById(R.id.Wrong);
        wrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateScore(0);
            }
        });
        ImageButton right = (ImageButton) findViewById(R.id.Right);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateScore(1);
            }
        });

        TextView viewScores = (TextView) findViewById(R.id.viewScore);
        viewScores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showScores();
            }
        });

        // Check if Internet present
        if (!haveNetworkConnection()) {
            // Internet Connection is not present
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please connect to internet for app to work")
                    .setCancelable(false)
                    .setPositiveButton("Connect to WIFI", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    })
                    .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if(player != null)
                                player = null;
                            if(T != null)
                                T.cancel();
                            finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();
            return;
        }
    }

    public void displayRules() {
        //load some kind of a view
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.rules, (ViewGroup) findViewById(R.id.rules));
        AlertDialog rulesDialog = new AlertDialog.Builder(this)
                .setTitle("Rules")
                .setIcon(android.R.drawable.ic_menu_info_details)
                .setView(view)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(player != null)
                            player.stop();
                        resetTimer();

                    }
                }).create();
        rulesDialog.setCanceledOnTouchOutside(false);
        rulesDialog.show();
    }

    public void updateScore(int score) {
        if(player != null) {
            player.stop();
            player = null;
        }
        TextView name = (TextView) findViewById(R.id.name);
        String playerName = names[currentSong % scores.size()];
        scores.put(playerName, scores.get(playerName) + score);
        currentSong++;
        playerName = names[currentSong % scores.size()];
        name.setText("Name: " + playerName);
        resetTimer();

        if(currentSong != 0 && currentSong % 20 == 0) {
            showScores();
        }
    }

    boolean visible = false;
    public void resetTimer() {
        count = 30;
        T.cancel();
        setTimer(30);
        final TextView songName = (TextView) findViewById(R.id.songName);
        final TextView artistName = (TextView) findViewById(R.id.artistName);
        if(track != null) {
            artistName.setText("By: " + track.artistName);
            songName.setText("Song was: " + track.trackName);
            if (artwork != null) {
                albumArt.setImageBitmap(artwork);
            } else
                albumArt.setImageResource(R.drawable.blank_album_art);
        }
        ImageButton wrong = (ImageButton) findViewById(R.id.Wrong);
        wrong.setVisibility(View.INVISIBLE);
        ImageButton right = (ImageButton) findViewById(R.id.Right);
        right.setVisibility(View.INVISIBLE);
        visible = false;
        TextView wait = (TextView) findViewById(R.id.timer);
        wait.setText("wait...");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                artistName.setText("By:");
                songName.setText("Song:");
                TextView wait = (TextView) findViewById(R.id.timer);
                wait.setText("30");
                next(true);
            }
        }, 4000);
    }

    public void setTimer(int text) {
        try {
            time.setText("" + text);

            if(text <= 25 && !visible) {
                ImageButton wrong = (ImageButton) findViewById(R.id.Wrong);
                wrong.setVisibility(View.VISIBLE);
                ImageButton right = (ImageButton) findViewById(R.id.Right);
                right.setVisibility(View.VISIBLE);
                visible = true;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    //show scores of users
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void showScores() {
        if(player != null) {
            player.stop();
        }
        if(T != null) {
            T.cancel();
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.scoreslayout, (ViewGroup) findViewById(R.id.scoresview));
        scoresDialog = new AlertDialog.Builder(IdentifyActivity.this);
        scoresDialog.setIcon(R.drawable.ic_launcher);
        scoresDialog.setTitle("Scores");
        //get scores of each user and put it in a table row
        Iterator it = scores.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String val = (String) pairs.getKey();
            TextView playerName = new TextView(this);
            Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Italic.ttf");
            playerName.setTypeface(type);
            if(val.length() > 30) {
                val = val.substring(0, 27);
                val +="...";
            }
            playerName.setText(val);
            playerName.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT,
                    1
            ));
            playerName.setGravity(Gravity.LEFT);
            playerName.setPadding(60,0,15,0);
            int score = (int) pairs.getValue();
            TextView scores = new TextView(this);
            scores.setTypeface(type);
            scores.setText("" + score);
            scores.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT,
                    1
            ));
            scores.setGravity(Gravity.RIGHT);
            scores.setPadding(15,0,50,0);
            TableLayout scoreLayout = new TableLayout(getApplicationContext());

            // add each text view to table view row
            TableRow scoreRow = new TableRow(getApplicationContext());
            GradientDrawable gd = new GradientDrawable();
            gd.setStroke(0, Color.BLACK);
            scoreRow.addView(playerName);
            scoreRow.addView(scores);
            scoreRow.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1));
            scoreRow.setPadding(5, 5, 5, 5);
            //add score row to the table
            scoreLayout.addView(scoreRow);
            scoreLayout.setBackgroundDrawable(gd);
            scoreLayout.setColumnStretchable(5, true);
            ((LinearLayout) layout).addView(scoreLayout);
        }
        scoresDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alert.dismiss();

                if(finish) {
                    T.cancel();
                    if(player != null) {
                        player.stop();
                        player = null;
                    }
                    finish();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    resetTimer();
                }
            }
        });
        scoresDialog.setView(layout);
        alert = scoresDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void playPause() {
        if (player != null) {
            if (player.isPlaying()) {
                //player.pause();
                //updatePlayPause(false);
                Toast.makeText(getApplicationContext(), "You cannot pause the song!!!", Toast.LENGTH_SHORT).show();
            } else {
                player.start();
                updatePlayPause(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_identify, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.backtohome:
                if(player != null) {
                    player.stop();
                    player = null;
                }
                if(T != null)
                    T.cancel();
                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                return true;
            case R.id.viewrules:
                if(player != null)
                    player.stop();
                displayRules();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "Cleaning up..");
        /*
        // Make sure to call the cleanup method on the API object
        rdio.cleanup();
        currentInd = 0;
        queryString = "love";

        // If we allocated a player, then cleanup after it
        if (player != null) {
            player.release();
            player.reset();
            player = null;
        }
        */
        super.onDestroy();
    }

    /**
     * Get the current user, and load their collection to start playback with.
     * Requires authorization and the Rdio app to be installed.
     */
    private void doSomething() {
        LoadMoreTracks();
    }

    /*************************
     * Dialog helpers
     *************************/

    public void LoadMoreTracks() {

        showGetHeavyRotationDialog();
        List<NameValuePair> args = new LinkedList<NameValuePair>();
        args.add(new BasicNameValuePair("type", "albums"));
        rdio.apiCall("getHeavyRotation", args, new RdioApiCallback() {
            @Override
            public void onApiSuccess(JSONObject result) {
                try {
                    List<NameValuePair> args = new LinkedList<NameValuePair>();
                    Random rn = new Random();
                    if(level.equals("Hard")) {
                        int temp = (rn.nextInt() % hardStrings.length);
                        if(temp < 0)
                            temp = 0;
                        queryString = hardStrings[0];
                    }
                    else if(level.equals("Medium")) {
                        int temp = (rn.nextInt() % mediumStrings.length);
                        if(temp < 0)
                            temp = 0;
                        queryString = mediumStrings[temp];
                    }
                    else {
                        int temp = (rn.nextInt() % easyStrings.length);
                        if(temp < 0)
                            temp = 0;
                        queryString = easyStrings[temp];
                    }
                    args.add(new BasicNameValuePair("query", queryString));
                    args.add(new BasicNameValuePair("types", "Track"));
                    args.add(new BasicNameValuePair("count", "10"));
                    currentInd++;
                    String temp = "" + (10*currentInd);
                    Log.i(TAG + " request", args.toString());
                    args.add(new BasicNameValuePair("start",temp));

                    rdio.apiCall("search", args, new RdioApiCallback() {
                        @Override
                        public void onApiFailure(String methodName, Exception e) {
                            Log.e(TAG, methodName + " failed: ", e);
                        }

                        @Override
                        public void onApiSuccess(JSONObject result) {
                            try {
                                Log.d(TAG + " :api response", result.toString());
                                result = result.getJSONObject("result");
                                List<Track> trackKeys = new LinkedList<Track>();
                                JSONArray tracks = result.getJSONArray("results");
                                for (int i = 0; i < tracks.length(); i++) {
                                    JSONObject trackObject = tracks.getJSONObject(i);
                                    String key = trackObject.getString("key");
                                    String name = trackObject.getString("name");
                                    String artist = trackObject.getString("artist");
                                    String album = trackObject.getString("album");
                                    String albumArt = trackObject.getString("icon");
                                    Log.d(TAG, "Found track: " + key + " => " + trackObject.getString("name"));
                                    trackKeys.add(new Track(key, name, artist, album, albumArt));
                                }
                                if (trackKeys.size() >= 1)
                                    trackQueue.addAll(trackKeys);
                                next(true);

                            } catch (Exception e) {
                                Log.e(TAG, "Failed to handle JSONObject: ", e);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Failed to handle JSONObject: ", e);
                } finally {
                    dismissGetHeavyRotationDialog();
                }
            }
            @Override
            public void onApiFailure(String methodName, Exception e) {
                dismissGetHeavyRotationDialog();
                Log.e(TAG, "getHeavyRotation failed. ", e);
            }
        });
    }

    private void next(final boolean manualPlay) {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }

        track = trackQueue.poll();
        if (trackQueue == null || trackQueue.size() < 3) {
            Log.i(TAG, "Track queue depleted, loading more tracks");
            LoadMoreTracks();
            return;
        }
        if (track == null) {
            Log.e(TAG, "Track is null!  Size of queue: " + trackQueue.size());
            return;
        }

        // Load the next track in the background and prep the player (to start buffering)
        // Do this in a bkg thread so it doesn't block the main thread in .prepare()
        AsyncTask<Track, Void, Track> task = new AsyncTask<Track, Void, Track>() {
            @Override
            protected Track doInBackground(Track... params) {
                Track track = params[0];
                if(track == null) {
                    return null;
                }
                if(player != null) {
                    player.stop();
                    player = null;
                }
                else {
                    try {
                        player = rdio.getPlayerForTrack(track.key, null, manualPlay);
                    } catch (Exception e) {
                        Log.e(TAG, "Encountered exception " + Log.getStackTraceString(e));
                        return null;
                    }
                    try {
                        player.prepare();
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                updateScore(0);
                            }
                        });
                    } catch(Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Exception " + Log.getStackTraceString(e));
                    }
                    try {
                        if(player != null) {
                            player.start();
                            T = new Timer();
                            T.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable()
                                    {
                                        public void run() {
                                            if(count >= 0)
                                                setTimer(count--);
                                        }
                                    });
                                }
                            }, 1000, 1000);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Encountered exception " + Log.getStackTraceString(e));
                    }
                }
                return track;
        }

            @Override
            protected void onPostExecute(Track track) {
                if(track != null) {
                    updatePlayPause(true);
                }
            }
        };
        task.execute(track);

        // Fetch album art in the background and then update the UI on the main thread
        final AsyncTask<Track, Void, Bitmap> artworkTask = new AsyncTask<Track, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Track... params) {
                Track track = params[0];
                try {
                    String artworkUrl = track.albumArt.replace("square-200", "square-600");
                    Log.i(TAG, "Downloading album art: " + artworkUrl);
                    Bitmap bm = null;
                    try {
                        URL aURL = new URL(artworkUrl);
                        URLConnection conn = aURL.openConnection();
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        BufferedInputStream bis = new BufferedInputStream(is);
                        bm = BitmapFactory.decodeStream(bis);
                        bis.close();
                        is.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error getting bitmap", e);
                    }
                    return bm;
                } catch (Exception e) {
                    Log.e(TAG, "Error downloading artwork", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap albunCover) {
                artwork = albunCover;
            }
        };
        artworkTask.execute(track);
    }

    private void updatePlayPause(boolean playing) {
        if (playing) {
            return;
        } else {
            playPause.setImageResource(R.drawable.play);
        }
    }

    @Override
    public void onRdioReadyForPlayback() {
        Log.i(TAG, "Rdio SDK is ready for playback");

        if (accessToken != null && accessTokenSecret != null) {
            doSomething();
        } else {
            Log.d(TAG, "Key and secret not identified");
        }
    }

    @Override
    public void onRdioUserPlayingElsewhere() {
        Log.w(TAG, "Tell the user that playback is stopping.");
    }

    /*
     * Dispatched by the Rdio object once the setTokenAndSecret call has finished, and the credentials are
     * ready to be used to make API calls.  The token & token secret are passed in so that you can
     * save/cache them for future re-use.
     * @see com.rdio.android.api.RdioListener#onRdioAuthorised(java.lang.String, java.lang.String)
     */
    @Override
    public void onRdioAuthorised(String accessToken, String accessTokenSecret) {
        Log.i(TAG, "Application authorised, saving access token & secret.");
        Log.d(TAG, "Access token: " + accessToken);
        Log.d(TAG, "Access token secret: " + accessTokenSecret);

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCESSTOKEN, accessToken);
        editor.putString(PREF_ACCESSTOKENSECRET, accessTokenSecret);
        editor.commit();
    }

    /*************************
     * Activity overrides
     *************************/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, "Login success");
                if (data != null) {
                    accessToken = data.getStringExtra("token");
                    accessTokenSecret = data.getStringExtra("tokenSecret");
                    onRdioAuthorised(accessToken, accessTokenSecret);
                    rdio.setTokenAndSecret(accessToken, accessTokenSecret);
                }
            } else if (resultCode == RESULT_CANCELED) {
                if (data != null) {
                    String errorCode = data.getStringExtra(OAuth1WebViewActivity.EXTRA_ERROR_CODE);
                    String errorDescription = data.getStringExtra(OAuth1WebViewActivity.EXTRA_ERROR_DESCRIPTION);
                    Log.v(TAG, "ERROR: " + errorCode + " - " + errorDescription);
                }
                accessToken = null;
                accessTokenSecret = null;
            }
            rdio.prepareForPlayback();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showGetHeavyRotationDialog() {
        if (getHeavyRotationDialog == null) {
            getHeavyRotationDialog = new RdioProgress();
        }

        if (getHeavyRotationDialog.isAdded()) {
            return;
        }

        Bundle args = new Bundle();
        args.putString("message", getResources().getString(R.string.getting_heavy_rotation));

        getHeavyRotationDialog.setArguments(args);
        getHeavyRotationDialog.show(getFragmentManager(), "getHeavyRotationDialog");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void dismissGetHeavyRotationDialog() {
        if (getHeavyRotationDialog != null) {
            getHeavyRotationDialog.dismiss();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish = true;
            if(player != null) {
                player.stop();
            }
            player = null;
            if(T != null)
                T.cancel();
            showScores();
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}