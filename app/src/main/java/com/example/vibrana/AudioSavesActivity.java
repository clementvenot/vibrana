package com.example.vibrana;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;

public class AudioSavesActivity extends AppCompatActivity {
    private RecyclerView recyclerView; // Vue de liste
    private RecordingsAdapter adapter; // Adaptateur pour la liste
    private ArrayList<File> audioList = new ArrayList<>(); // Liste des fichiers audio

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configuration de la vue
        setContentView(R.layout.audio_saves);

        // Récupération de la vue
        recyclerView = findViewById(R.id.recyclerRecordings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadAudioFiles(); // Chargement des fichiers audio

        // Configuration de l'adaptateur
        adapter = new RecordingsAdapter(audioList, this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnHome).setOnClickListener(v -> finish());
    }

    private void loadAudioFiles() {
        // Chargement des fichiers audio depuis le dossier
        File dir = new File(getExternalFilesDir(null), "Audio");

        // Parcours du dossier
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles(); // Récupération des fichiers
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".m4a")) { // Vérification de l'extension
                        audioList.add(f); // Ajout du fichier à la liste
                    }
                }
            }
        }
    }
}