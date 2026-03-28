package com.example.vibrana;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

public class PianoActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private Map<Integer, Integer> soundMap = new HashMap<>();
    private Map<Integer, Integer> streamMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piano);

        // Configuration du SoundPool pour une faible latence
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        // Chargement des sons
        loadSounds();

        // Attribution des événements tactiles (Touch)
        setupKeys();

        findViewById(R.id.btn_home).setOnClickListener(v -> finish());
    }

    private void loadSounds() {
        soundMap.put(R.id.key_white_1, soundPool.load(this, R.raw.c4, 1));
        soundMap.put(R.id.key_white_2, soundPool.load(this, R.raw.d4, 1));
        soundMap.put(R.id.key_white_3, soundPool.load(this, R.raw.e4, 1));
        soundMap.put(R.id.key_white_4, soundPool.load(this, R.raw.f4, 1));
        soundMap.put(R.id.key_white_5, soundPool.load(this, R.raw.g4, 1));
        soundMap.put(R.id.key_white_6, soundPool.load(this, R.raw.a4, 1));
        soundMap.put(R.id.key_white_7, soundPool.load(this, R.raw.b4, 1));

        soundMap.put(R.id.key_black_1, soundPool.load(this, R.raw.cs4, 1));
        soundMap.put(R.id.key_black_2, soundPool.load(this, R.raw.ds4, 1));
        soundMap.put(R.id.key_black_3, soundPool.load(this, R.raw.fs4, 1));
        soundMap.put(R.id.key_black_4, soundPool.load(this, R.raw.gs4, 1));
        soundMap.put(R.id.key_black_5, soundPool.load(this, R.raw.as4, 1));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupKeys() {
        View.OnTouchListener keyTouchListener = (v, event) -> {
            Integer soundId = soundMap.get(v.getId());
            if (soundId == null) return false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // On arrête un éventuel stream précédent sur cette touche
                    Integer oldStreamId = streamMap.get(v.getId());
                    if (oldStreamId != null) {
                        soundPool.stop(oldStreamId);
                    }
                    // On joue le son en boucle (-1)
                    int streamId = soundPool.play(soundId, 0.8f, 0.8f, 1, -1, 1.0f);
                    streamMap.put(v.getId(), streamId);
                    v.setPressed(true);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // On arrête le son quand on relâche
                    Integer currentStreamId = streamMap.get(v.getId());
                    if (currentStreamId != null) {
                        soundPool.stop(currentStreamId);
                        streamMap.remove(v.getId());
                    }
                    v.setPressed(false);
                    return true;
            }
            return false;
        };

        // Touches blanches
        findViewById(R.id.key_white_1).setOnTouchListener(keyTouchListener);
        findViewById(R.id.key_white_2).setOnTouchListener(keyTouchListener);
        findViewById(R.id.key_white_3).setOnTouchListener(keyTouchListener);
        findViewById(R.id.key_white_4).setOnTouchListener(keyTouchListener);
        findViewById(R.id.key_white_5).setOnTouchListener(keyTouchListener);
        findViewById(R.id.key_white_6).setOnTouchListener(keyTouchListener);
        findViewById(R.id.key_white_7).setOnTouchListener(keyTouchListener);

        // Touches noires
        findViewById(R.id.key_black_1).setOnTouchListener(keyTouchListener);
        findViewById(R.id.key_black_2).setOnTouchListener(keyTouchListener);
        findViewById(R.id.key_black_3).setOnTouchListener(keyTouchListener);
        findViewById(R.id.key_black_4).setOnTouchListener(keyTouchListener);
        findViewById(R.id.key_black_5).setOnTouchListener(keyTouchListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}