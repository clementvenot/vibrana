package com.example.vibrana;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.ViewHolder> {

    private final ArrayList<File> audioFiles;
    private final Context context;
    private MediaPlayer mediaPlayer = null;
    private int currentPlaying = -1;
    private ViewHolder currentHolder = null;
    private final Handler handler = new Handler();

    public RecordingsAdapter(ArrayList<File> audioFiles, Context context) {
        // Constructeur de l'adaptateur
        this.audioFiles = audioFiles;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Création de la vue pour chaque élément de la liste
        View view = LayoutInflater.from(context).inflate(R.layout.audio_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = audioFiles.get(position);

        // Afficher le nom du fichier
        holder.title.setText(file.getName().replace(".m4a", ""));

        if (currentPlaying == position) {
            // Réinitialiser l'apparence si ce n'est pas l'élément en cours de lecture
            currentHolder = holder;
            holder.btnPlay.setImageResource(mediaPlayer != null && mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        } else {
            // Sinon, afficher le bouton de lecture
            holder.btnPlay.setImageResource(R.drawable.ic_play);
            holder.seekBar.setProgress(0);
        }

        // Gestion des clics sur les boutons play pause
        holder.btnPlay.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return; // Vérification de la position

            if (currentPlaying == currentPos) {
                // Si le bouton est déjà en cours de lecture, on met en pause ou on relancera la lecture
                togglePlayPause(holder);
            } else {
                // Sinon, on lance la lecture du fichier
                playAudio(audioFiles.get(currentPos), holder, currentPos);
            }
        });

        // Gestion des clics sur le bouton de suppression
        holder.btnDelete.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return; // Vérification de la position

            if (currentPlaying == currentPos) {
                // Si le fichier en cours de lecture est supprimé, on arrête la lecture
                stopCurrent();
            }

            // Supprimer le fichier
            File fileToDelete = audioFiles.get(currentPos);
            if (fileToDelete.delete()) {
                // Supprimer le fichier de la liste
                audioFiles.remove(currentPos);
                notifyItemRemoved(currentPos); // Mise à jour de la vue
                notifyItemRangeChanged(currentPos, getItemCount());
            }
        });

        // Gestion de la seekBar
        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Mettre à jour la position du curseur de lecture
                int currentPos = holder.getBindingAdapterPosition();
                if (fromUser && currentPos != RecyclerView.NO_POSITION && currentPlaying == currentPos && mediaPlayer != null) {
                    // Vérification de la position et de l'état de lecture
                    mediaPlayer.seekTo(progress); // Déplacer le curseur
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {} // Ne rien faire lors du déplacement du curseur
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {} // Ne rien faire lors du déplacement du curseur
        });
    }

    private void togglePlayPause(ViewHolder holder) {
        // Mettre en pause ou reprendre la lecture
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                // Mettre en pause si en cours de lecture
                mediaPlayer.pause();
                holder.btnPlay.setImageResource(R.drawable.ic_play);
                handler.removeCallbacks(updateSeekBarTask);
            } else {
                // Reprendre la lecture si en pause
                mediaPlayer.start();
                holder.btnPlay.setImageResource(R.drawable.ic_pause);
                handler.post(updateSeekBarTask);
            }
        }
    }

    private void playAudio(File file, ViewHolder holder, int position) {
        // Jouer le fichier audio
        if (!file.exists() || file.length() == 0) {
            // Si le fichier n'existe pas ou est vide, afficher un message
            Toast.makeText(context, "Le fichier est vide ou corrompu", Toast.LENGTH_SHORT).show();
            return;
        }

        stopCurrent(); // Arrêter la lecture précédente si elle existe

        mediaPlayer = new MediaPlayer(); // Création du lecteur audio
        try {
            // Configuration audio pour utiliser le flux de musique (haut-parleur)
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            // Mettre à jour l'affichage
            currentPlaying = position;
            currentHolder = holder;
            holder.btnPlay.setImageResource(R.drawable.ic_pause);
            holder.seekBar.setMax(mediaPlayer.getDuration());

            // Mettre à jour la seekBar
            handler.post(updateSeekBarTask);

            // Gestion des erreurs
            mediaPlayer.setOnCompletionListener(mp -> {
                stopCurrent();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(context, "Erreur de lecture audio", Toast.LENGTH_SHORT).show();
                stopCurrent();
                return true;
            });

        } catch (IOException e) {
            e.printStackTrace(); // Afficher les détails de l'erreur
            Toast.makeText(context, "Impossible de lire le fichier", Toast.LENGTH_SHORT).show();
        }
    }

    private final Runnable updateSeekBarTask = new Runnable() {
        // Mettre à jour la seekBar
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying() && currentHolder != null) {
                // Mettre à jour la position de la seekBar
                currentHolder.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 100); // Mettre à jour récursivement
            }
        }
    };

    private void stopCurrent() {
        // Arrêter la lecture du fichier en cours
        if (mediaPlayer != null) {
            try {
                // Arrêter et libérer la ressource
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (Exception e) {
                // Gestion des erreurs
                e.printStackTrace();
            }
            mediaPlayer.release(); // Libérer la ressource
            mediaPlayer = null; // Réinitialiser la variable
        }
        handler.removeCallbacks(updateSeekBarTask); // Arrêter la mise à jour de la seekBar
        if (currentHolder != null) {
            // Réinitialiser l'apparence
            currentHolder.btnPlay.setImageResource(R.drawable.ic_play);
            currentHolder.seekBar.setProgress(0);
        }
        currentPlaying = -1;
        currentHolder = null;
    }

    @Override
    public int getItemCount() {
        // Retourner le nombre d'éléments dans la liste
        return audioFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Vue pour chaque élément de la liste
        TextView title;
        ImageButton btnPlay;
        ImageView btnDelete;
        SeekBar seekBar;

        public ViewHolder(@NonNull View itemView) {
            // Constructeur de la vue
            super(itemView);
            title = itemView.findViewById(R.id.txtTitle);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            seekBar = itemView.findViewById(R.id.seekBar);
        }
    }
}