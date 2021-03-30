package me.pgb.a2021_03_17_radio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final String TAG = "MAIN__";
    private MediaPlayer mediaPlayer;
    public String text = "WUML 91.5 FM Lowell, MA";
    public String url = "http://s3.radio.co/s5e286e909/listen"; //"http://vprbbc.streamguys.net:80/vprbbc24.mp3";
    //private static final String url = "http://vprbbc.streamguys.net:80/vprbbc24.mp3";
    private Button internetRadioButton;
    private Button changestationButton;

    private TextView status;
    private TextView station;

    private boolean radioOn;
    private boolean radioWasOnBefore;

    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        Spinner spinner = findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.radio_stations, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        radioOn = false;
        radioWasOnBefore = false;

        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mediaPlayer = new MediaPlayer();

        SeekBar seekBar = (SeekBar) findViewById(R.id.volumeSeekBar);
        seekBar.setMax(maxVolume);
        seekBar.setProgress(currentVolume);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        internetRadioButton = findViewById(R.id.internet_radio_button);
        changestationButton = findViewById(R.id.change_station);
        status = (TextView)findViewById(R.id.isPlaying);
        station = (TextView)findViewById(R.id.current_station);

        changestationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    mediaPlayer.reset();
                    station.setText(text);
                    status.setText("RADIO IS NOT PLAYING");
                    if (!mediaPlayer.isPlaying()) {
                        if (radioWasOnBefore) {
                            mediaPlayer.release();
                            mediaPlayer = new MediaPlayer();
                        }
                        radioSetup(mediaPlayer);
                        mediaPlayer.prepareAsync();
                        station.setText(text);
                        radioOn = true;
                        internetRadioButton.setText("Turn radio OFF");
                    }
                }
        });

        internetRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (radioOn) { // ON so Turn OFF
                    radioOn = false;
                    internetRadioButton.setText("Turn radio ON");
                    if (mediaPlayer.isPlaying()) {
                        Log.i(TAG, "Radio is playing- turning off " );
                        radioWasOnBefore = true;
                    }
                    mediaPlayer.reset();
                    status.setText("RADIO IS NOT PLAYING");
                } else { // OFF so Turn ON
                    radioOn = true;
                    internetRadioButton.setText("Turn radio OFF");
                    if (!mediaPlayer.isPlaying()) {
                        if (radioWasOnBefore) {
                            mediaPlayer.release();
                            mediaPlayer = new MediaPlayer();
                        }
                        radioSetup(mediaPlayer);
                        mediaPlayer.prepareAsync();
                        station.setText(text);
                    }
                }
            }
        });
    }

    public void radioSetup(MediaPlayer mediaPlayer) {
        status.setText("PREPARING STATION");
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i(TAG, "onPrepared" );
                mediaPlayer.start();
                status.setText("RADIO IS PLAYING");
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.i(TAG, "onError: " + String.valueOf(what).toString());
                return false;
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(TAG, "onCompletion" );
                mediaPlayer.reset();
            }
        });

        try {
            mediaPlayer.setDataSource(url);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = null;
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

    private void setUpMediaPlayer() {
        Handler handler = null;

        HandlerThread handlerThread = new HandlerThread("media player") {
            @Override
            public void onLooperPrepared() {
                Log.i(TAG, "onLooperPrepared");

            }
        };

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        text = parent.getItemAtPosition(position).toString();
        url = getResources().getStringArray(R.array.radio_urls)[position];
        Toast.makeText(parent.getContext(), url, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}