package com.knowthissong;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    protected String[] names = {"", "", "", "", "", "", "", "","", ""};
    protected int difficultyLevel = 0;
    protected Boolean goAhead = false;
    private AlertDialog.Builder alertDialog2;
    private static int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_launcher);

        final ImageButton reset = (ImageButton) findViewById(R.id.Reset);
        reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Reset the values in the EditText fields
                resetValues();
            }
        });

        final ImageButton add = (ImageButton) findViewById(R.id.addicon);
        add.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                if(count < 10) {
                    EditText name = new EditText(MainActivity.this);
                    LinearLayout ll = (LinearLayout) findViewById(R.id.edittext);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.topMargin = 20;
                    ll.addView(name, layoutParams);
                    name.requestFocus();
                    count++;
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(),"Cannot add anymore participants", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
        final ImageButton go = (ImageButton) findViewById(R.id.Go);
        go.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Reset the values in the EditText fields
                int index = 0;
                ViewGroup group = (ViewGroup)findViewById(R.id.edittext);
                for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                    View view = group.getChildAt(i);
                    if (view instanceof EditText) {
                        String name = ((EditText)view).getText().toString();
                        if(!name.equals("")) {
                            names[index] = name.trim();
                            index++;
                            goAhead = true;
                        }
                    }
                }
                if(index == 0){
                    Toast toast = Toast.makeText(getApplicationContext(), "No player name added",Toast.LENGTH_LONG);
                    toast.show();
                    goAhead = false;
                }

                if(goAhead) {
                    // Show the alert dialog with buttons
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.alertlayout, (ViewGroup) findViewById(R.id.alertlayout));
                    alertDialog2 = new AlertDialog.Builder(MainActivity.this);
                    // Setting Dialog Title
                    alertDialog2.setTitle("Select level of difficulty");
                    alertDialog2.setView(layout);
                    // Showing Alert Dialog
                    final AlertDialog alert = alertDialog2.create();
                    alert.show();
                    alertDialog2.setCancelable(true);
                    final Button easyButton = (Button) layout.findViewById(R.id.easy_button);
                    easyButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View layout) {
                            difficultyLevel = 0;
                            alert.dismiss();
                            startIdentifyActivity();
                        }
                    });

                    final Button mediumButton = (Button) layout.findViewById(R.id.medium_button);
                    mediumButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            difficultyLevel = 1;
                            alert.dismiss();
                            startIdentifyActivity();
                        }
                    });

                    final Button difficultButton = (Button) layout.findViewById(R.id.difficult_button);
                    difficultButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            difficultyLevel = 2;
                            alert.dismiss();
                            startIdentifyActivity();
                        }
                    });
                }
                resetValues();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void resetValues() {
        ViewGroup group = (ViewGroup)findViewById(R.id.edittext);
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText)view).setText("");
            }
        }
    }

    public void startIdentifyActivity() {
        Intent intent = new Intent(getApplicationContext(), IdentifyActivity.class);
        intent.putExtra("difficultyLevel", difficultyLevel);
        intent.putExtra("names", names);
        finish();
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            System.exit(0);
        }
        return super.onKeyDown(keyCode, event);
    }

}