package com.example.vibrana;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

public class TuneGuitarActivity extends AppCompatActivity {

    private static final String TAG = "TuneGuitarActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 8192; // Taille memoire pour l'enregistrement

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;

    // Vues UI
    private TextView tvCurrentNote;
    private TextView tvHz;
    private View pointerContainer; // Conteneur de la flèche qui bouge
    private SwitchCompat switchAuto;

    // Données de fréquences
    private final Map<String, Double> noteFrequencies = new HashMap<>();
    private String selectedNote = "G";
    private double targetFrequency = 196.00; // Fréquence cible (ex: Sol/G = 196Hz)
    private double lastFrequency = -1;
    private static final double SMOOTHING_FACTOR = 0.4; // Lissage pour éviter que l'aiguille tremble trop

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tune_guitar);

        initFrequencies(); // Charge les fréquences standards
        initUI();          // Initialise les boutons et textes

        // Vérification des permissions micro
        if (checkPermissions()) {
            startPitchDetection();
        } else {
            requestPermissions();
        }
    }

    private void initFrequencies() {
        //Définit les fréquences cibles pour chaque corde d'une guitare standard.
        noteFrequencies.put("E_HIGH", 329.63); // Mi aigu
        noteFrequencies.put("B", 246.94);      // Si
        noteFrequencies.put("G", 196.00);      // Sol
        noteFrequencies.put("D", 146.83);      // Ré
        noteFrequencies.put("A", 110.00);      // La
        noteFrequencies.put("E_LOW", 82.41);   // Mi grave
    }

    private void initUI() {
        // Relie les éléments XML au code Java et configure les clics.
        Toolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.green_primary));
        }

        tvCurrentNote = findViewById(R.id.tv_current_note);
        tvHz = findViewById(R.id.tv_hz);
        pointerContainer = findViewById(R.id.pointer_container);
        switchAuto = findViewById(R.id.switch_auto);

        // Configuration des clics sur chaque corde
        findViewById(R.id.row_e_high).setOnClickListener(v -> selectNote("E_HIGH", "E"));
        findViewById(R.id.row_b).setOnClickListener(v -> selectNote("B", "B"));
        findViewById(R.id.row_g).setOnClickListener(v -> selectNote("G", "G"));
        findViewById(R.id.row_d).setOnClickListener(v -> selectNote("D", "D"));
        findViewById(R.id.row_a).setOnClickListener(v -> selectNote("A", "A"));
        findViewById(R.id.row_e_low).setOnClickListener(v -> selectNote("E_LOW", "E"));

        // Boutons de navigation
        findViewById(R.id.btnHome).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> startActivity(new Intent(this, AudioSavesActivity.class)));
        findViewById(R.id.btnMusic).setOnClickListener(v -> startActivity(new Intent(this, PianoActivity.class)));
        
        selectNote("G", "G"); // Note sélectionnée par défaut
    }

    private void selectNote(String noteKey, String displayNote) {
        // Change la note cible quand on clique sur une corde.
        selectedNote = noteKey;
        targetFrequency = noteFrequencies.get(noteKey);
        tvCurrentNote.setText(displayNote);
        lastFrequency = -1; // Reset du lissage
        updateStringSelectionUI(noteKey); // Met à jour l'affichage visuel des cordes
    }

    private void updateStringSelectionUI(String activeNoteKey) {
        // Met en rouge la corde sélectionnée et affiche un point plein.
        resetStringsUI(); // Remet tout en noir d'abord
        int dotId, lineId;
        switch (activeNoteKey) {
            case "E_HIGH": dotId = R.id.dot_e_high; lineId = R.id.line_e_high; break;
            case "B": dotId = R.id.dot_b; lineId = R.id.line_b; break;
            case "G": dotId = R.id.dot_g; lineId = R.id.line_g; break;
            case "D": dotId = R.id.dot_d; lineId = R.id.line_d; break;
            case "A": dotId = R.id.dot_a; lineId = R.id.line_a; break;
            case "E_LOW": dotId = R.id.dot_e_low; lineId = R.id.line_e_low; break;
            default: return;
        }
        ((ImageView)findViewById(dotId)).setImageResource(R.drawable.dot_selected);
        findViewById(lineId).setBackgroundColor(ContextCompat.getColor(this, R.color.string_red));
    }

    private void resetStringsUI() {
        // Remet toutes les cordes en noir et désélectionne les points.
        int[] dots = {R.id.dot_e_high, R.id.dot_b, R.id.dot_g, R.id.dot_d, R.id.dot_a, R.id.dot_e_low};
        int[] lines = {R.id.line_e_high, R.id.line_b, R.id.line_g, R.id.line_d, R.id.line_a, R.id.line_e_low};
        for (int dot : dots) ((ImageView) findViewById(dot)).setImageResource(R.drawable.dot_unselected);
        for (int line : lines) findViewById(line).setBackgroundColor(Color.BLACK);
    }

    private void startPitchDetection() {
        // Lance l'enregistrement en arrière-plan pour écouter la guitare.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return;

        try {
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            int actualBufferSize = Math.max(BUFFER_SIZE, minBufferSize);
            
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, actualBufferSize);
            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord could not be initialized");
                return;
            }

            isRecording = true;
            audioRecord.startRecording();

            // Thread séparé pour ne pas bloquer l'écran
            recordingThread = new Thread(() -> {
                short[] buffer = new short[actualBufferSize / 2];
                while (isRecording) {
                    int read = audioRecord.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        double frequency = calculateFrequency(buffer, read);
                        if (frequency > 0) {
                            // On repasse sur le thread principal pour modifier l'interface (UI)
                            mainHandler.post(() -> updateTuningUI(frequency));
                        }
                    }
                }
            });
            recordingThread.start();
        } catch (Exception e) {
            Log.e(TAG, "Error in startPitchDetection", e);
        }
    }

    private double calculateFrequency(short[] buffer, int readSize) {
        // Calcule la fréquence de la note jouée.
        // Utilise l'autocorrélation pour trouver la période du signal.
        long rms = 0;
        for (int i = 0; i < readSize; i++) rms += Math.abs(buffer[i]);
        long avgVolume = rms / readSize;

        // Si le son est trop faible, on ignore (évite que l'aiguille bouge avec le bruit)
        if (avgVolume < 35) return -1; 

        int bestLag = -1;
        float maxCorrelation = -1;
        float firstCorrelation = 0; 
        
        int minLag = SAMPLE_RATE / 500; // Limite haute (500Hz)
        int maxLag = SAMPLE_RATE / 60;  // Limite basse (60Hz)
        int analysisSize = Math.min(readSize, 4096);

        // Énergie du signal
        for (int i = 0; i < analysisSize - minLag; i += 4) {
            firstCorrelation += (float) buffer[i] * buffer[i];
        }

        // Recherche du pic de corrélation
        for (int lag = minLag; lag <= maxLag; lag++) {
            float correlation = 0;
            for (int i = 0; i < analysisSize - lag; i += 4) { 
                correlation += (float) buffer[i] * buffer[i + lag];
            }
            if (correlation > maxCorrelation) {
                maxCorrelation = correlation;
                bestLag = lag;
            }
        }
        
        // On vérifie que la note est assez "claire" (pas juste du bruit)
        if (maxCorrelation < firstCorrelation * 0.5) return -1;
        
        return (bestLag != -1) ? (double) SAMPLE_RATE / bestLag : -1;
    }

    private void updateTuningUI(double frequency) {
        // Met à jour la position de la flèche et le texte Hz.
        // Lissage de la valeur pour plus de stabilité
        if (lastFrequency == -1) lastFrequency = frequency;
        else lastFrequency = lastFrequency + SMOOTHING_FACTOR * (frequency - lastFrequency);

        // Si mode Auto, change de corde automatiquement
        if (switchAuto.isChecked()) findClosestNote(lastFrequency);

        double diff = lastFrequency - targetFrequency; // Écart entre note jouée et note cible

        String sign = (diff >= 0) ? "+" : "";
        tvHz.setText(String.format("%s%.1f Hz", sign, diff));
        
        // Calcul du placement de la flèche (0.5 = milieu)
        float maxRange = 10.0f; // On affiche +/- 10Hz sur la barre
        float bias = (float) (diff / (maxRange * 2)) + 0.5f; 
        
        if (bias < 0) bias = 0; 
        if (bias > 1) bias = 1;

        // Déplacement réel de la flèche dans le layout
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) pointerContainer.getLayoutParams();
        params.horizontalBias = bias;
        pointerContainer.setLayoutParams(params);
        pointerContainer.requestLayout(); 
        
        // Si l'écart est < 1Hz, on passe en vert (c'est accordé !)
        int color = (Math.abs(diff) < 1.0) ? Color.parseColor("#2E7D32") : Color.BLACK;
        tvHz.setTextColor(color);
        tvCurrentNote.setTextColor(color);
    }

    private void findClosestNote(double frequency) {
        //Mode Auto : cherche quelle corde est la plus proche de la fréquence entendue.
        double minDiff = Double.MAX_VALUE;
        String closestKey = selectedNote;
        for (Map.Entry<String, Double> entry : noteFrequencies.entrySet()) {
            double diff = Math.abs(frequency - entry.getValue());
            if (diff < minDiff) {
                minDiff = diff;
                closestKey = entry.getKey();
            }
        }
        // Change de corde si on est vraiment proche d'une autre (seuil de 15Hz)
        if (!closestKey.equals(selectedNote) && minDiff < 15) {
            String display = closestKey.startsWith("E") ? "E" : closestKey;
            selectNote(closestKey, display);
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRecording = false; // Arrête l'écoute quand on quitte l'appli
        if (audioRecord != null) {
            try { 
                if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop();
                }
            } catch (Exception ignored) {}
            audioRecord.release();
            audioRecord = null;
        }
    }
}