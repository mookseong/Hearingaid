package mookseong.project.hearingaid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private AudioStreamer audioStreamer;
    private AudioManager manager;
    private boolean btn = false;
    TextView text;
    TextView btu_text;
    CardView btn_play;
    View view;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_play = findViewById(R.id.startAudioStreamer);
        text = findViewById(R.id.itext);
        btu_text = findViewById(R.id.buttext);
        view =getWindow().getDecorView();
        //btn_stop = findViewById(R.id.stopAudioStreamer);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view != null) {
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }else
            getWindow().setStatusBarColor(Color.parseColor("#f2f2f2"));


        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},0);

        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {//현재 하드웨어 정보를 가져옴
            btn_play.setEnabled(true);

        }
        else {
            btn_play.setEnabled(false);
            Toast.makeText(getApplicationContext(), "마이크를 찾지 못했습니다.", Toast.LENGTH_LONG).show();
        }
        manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);


        btn_play.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (!btn) {
	                audioStreamer = new AudioStreamer();
	                audioStreamer.setAudioManager(manager);
	                setting(true,"스탑 버튼을 누르면\n마이크 출력이 중단됩니다.","Stop",255,91,91);
                    audioStreamer.start();
                } else {
                    setting(false,"시작 버튼을 누르면\n스피커로 마이크가 출력됩니다.","Start",25,150,247);
                    audioStreamer.audiopause();
                }
            }
        });
    }

    private void setting(boolean btn, String TEXT, String BTU_TEXT, int red, int green, int blue){
        btn_play.setCardBackgroundColor(Color.rgb(red, green, blue));
        text.setText(TEXT);
        btu_text.setText(BTU_TEXT);
        this.btn = btn;
    }
}

class AudioStreamer extends Thread {

    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int sampleRate = 44100;
    private int streamType = AudioManager.STREAM_MUSIC;
    private int mode = AudioTrack.MODE_STREAM;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private int channelConfigIn = AudioFormat.CHANNEL_IN_MONO;
    private int channelConfigOut = AudioFormat.CHANNEL_OUT_MONO;

    private int recordSize;
    private int trackSize;

    private AudioTrack track;
    private AudioRecord recorder;

    AudioStreamer() {
        recordSize = AudioRecord.getMinBufferSize(sampleRate, channelConfigIn, audioFormat);
        trackSize = AudioTrack.getMinBufferSize(sampleRate, channelConfigOut, audioFormat);
        recorder = new AudioRecord(audioSource, sampleRate, channelConfigIn, audioFormat, recordSize);

        if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            track = new AudioTrack(streamType, sampleRate, channelConfigOut, audioFormat, trackSize, mode);

        }
    }

    public void run() {
        recorder.startRecording();
        track.play();

        short[] buffer = new short[recordSize];
        int audioLenght = 0;

        while (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING
                && track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioLenght = recorder.read(buffer, 0, recordSize);
            track.write(buffer, 0, audioLenght);
        }
    }

    public void setAudioManager(AudioManager manager) {
        manager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        manager.setBluetoothScoOn(true);
        manager.setSpeakerphoneOn(true);

        if (manager.isBluetoothA2dpOn()) {
            manager.startBluetoothSco();
        }
    }

    void audiopause() {
        if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            recorder.stop();
        }
        if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            track.pause();
            track.flush();
        }
    }
}