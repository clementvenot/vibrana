package com.example.vibrana;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200; // Code de demande de permission
    private String fileName = null;
    private SoundVisualizerView visualizerView;
    private ImageButton recordButton;
    private TextView timerText;
    private EditText titleInput;
    private MediaRecorder recorder = null;
    private boolean isRecording = false;
    private final Handler handler = new Handler(Looper.getMainLooper()); // Pour mettre à jour le timer
    private long startTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_activity); // Layout de l'activité

        // Configuration de la toolbar
        Toolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(Color.parseColor("#D56769")); // Couleur de la toolbar
            setSupportActionBar(toolbar); // Activation de la toolbar
        }

        // Récupération des vues
        visualizerView = findViewById(R.id.visualizer);
        recordButton = findViewById(R.id.recordButton);
        timerText = findViewById(R.id.timerText);
        titleInput = findViewById(R.id.titleInput);

        // Gestion du bouton de recording
        recordButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            } else {
                requestPermissions();
            }
        });

        // Gestion du bouton Home dans la toolbar
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            finish(); // Ferme cette activité pour revenir à l'écran précédent
        });

        // Gestion du bouton Save dans la toolbar
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            Intent intent = new Intent(RecordActivity.this, AudioSavesActivity.class);
            startActivity(intent);
        });
    }

    private void startRecording() {
        // On efface le graphique précédent au début d'un nouvel enregistrement
        visualizerView.clear();

        // Création du dossier d'enregistrement
        File storageDir = new File(getExternalFilesDir(null), "Audio");
        if (!storageDir.exists()) storageDir.mkdirs(); // Si le dossier n'existe pas, on le crée

        // Récupérer le titre de l'EditText
        String userTitle = titleInput.getText().toString().trim();
        if (userTitle.isEmpty()) {
            userTitle = "Record"; // Si vide, on donne un titre par défaut
        }
        // Nettoyer le titre pour éviter les caractères interdits dans les noms de fichiers
        userTitle = userTitle.replaceAll("[\\\\/:*?\"<>|]", "_");

        // Création du fichier d'enregistrement
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File audioFile = new File(storageDir, userTitle + "_" + timeStamp + ".m4a");
        fileName = audioFile.getAbsolutePath();

        // Enregistrement
        recorder = new MediaRecorder();
        try {
            // Configuration de l'enregistrement
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setOutputFile(fileName);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(44100); 
            recorder.setAudioEncodingBitRate(128000);

            // Lancement de l'enregistrement
            recorder.prepare();
            recorder.start();

            // Mise à jour du timer
            isRecording = true;
            startTime = SystemClock.elapsedRealtime();
            recordButton.setImageResource(R.drawable.ic_stop_record);
            updateUI();
            
        } catch (Exception e) { // Gestion des erreurs
            Log.e("RecordActivity", "Erreur startRecording", e);
            Toast.makeText(this, "Erreur lors du lancement", Toast.LENGTH_SHORT).show();
            if (recorder != null) {
                recorder.release();
                recorder = null;
            }
        }
    }

    private void stopRecording() {
        // Arrêt de l'enregistrement
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception e) { // Gestion des erreurs
                Log.e("RecordActivity", "Erreur stop", e);
            }
            recorder.release();
            recorder = null;
        }
        isRecording = false;
        // Mise à jour du bouton
        recordButton.setImageResource(R.drawable.ic_record);
        handler.removeCallbacksAndMessages(null); // On arrête la mise à jour du timer

        // notification
        Toast.makeText(this, "Saved : " + new File(fileName).getName(), Toast.LENGTH_LONG).show();
    }

    private void updateUI() {
        // Mise à jour du timer
        if (isRecording && recorder != null) {
            long elapsed = SystemClock.elapsedRealtime() - startTime; // Temps écoulé depuis le début de l'enregistrement

            // Conversion en minutes et secondes
            int seconds = (int) (elapsed / 1000);
            int minutes = seconds / 60;
            seconds %= 60;
            timerText.setText(String.format("%02d'%02d", minutes, seconds));

            try { // Récupération de l'amplitude maximale
                int maxAmplitude = recorder.getMaxAmplitude();
                visualizerView.addAmplitude(maxAmplitude);
            } catch (Exception e) { // Gestion des erreurs
                Log.e("RecordActivity", "Erreur updateUI", e);
            }
            // Mise à jour récursive
            handler.postDelayed(this::updateUI, 100);
        }
    }

    private boolean checkPermissions() {
        // Vérification des permissions
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        // Demande de permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Gestion de la réponse de la demande de permission
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        }
    }

    @Override
    protected void onStop() {
        // Arrêt de l'enregistrement lorsque l'activité est détruite
        super.onStop();
        if (isRecording) stopRecording();
    }
}