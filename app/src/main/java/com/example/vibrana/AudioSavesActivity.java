package com.example.vibrana;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;

public class AudioSavesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecordingsAdapter adapter;
    private ArrayList<File> audioList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_saves);

        // Configuration de la liste (RecyclerView)
        recyclerView = findViewById(R.id.recyclerRecordings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Chargement et affichage des fichiers
        loadAudioFiles();
        adapter = new RecordingsAdapter(audioList, this);
        recyclerView.setAdapter(adapter);

        // Retour à l'accueil
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(AudioSavesActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    // Récupère les fichiers .m4a du dossier "Audio"
    private void loadAudioFiles() {
        File dir = new File(getExternalFilesDir(null), "Audio");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".m4a")) {
                        audioList.add(f);
                    }
                }
            }
        }
    }
}
