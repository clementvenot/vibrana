package com.example.vibrana;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class SoundVisualizerView extends View {
    private final  float maxAmplitude = 32767f; // Amplitude maximale possible
    private final List<Float> amplitudes = new ArrayList<>(); // Liste des amplitudes
    private final Paint paint = new Paint(); // Paint pour dessiner les barres
    private static final int LINE_WIDTH = 10; // Epaisseur Barres


    public SoundVisualizerView(Context c, AttributeSet a) {
        super(c, a);
        paint.setColor(Color.RED); // Couleur des barres
        paint.setStrokeWidth(6f); // Epaisseur des barres
        paint.setStrokeCap(Paint.Cap.ROUND); // Arrondi des arêtes des barres
    }

    public void addAmplitude(int amp) {
        // Méthode pour ajouter une amplitude au graphique
        // On calcule une valeur entre 0.1 et 1.0 de la hauteur du graphique

        float height = getHeight();
        if (height == 0) height = 500; // Valeur par défaut si pas encore dessiné

        // Normalisation de l'amplitude pour qu'elle soit entre 0 et 1
        float normalizedAmp = (amp / maxAmplitude) * (height * 0.8f);

        // hauteur minimale = 10
        if (normalizedAmp < 10) normalizedAmp = 10;

        amplitudes.add(normalizedAmp);

        // On limite le nombre d'éléments dans la liste à la largeur du graphique
        int width = getWidth();
        if (width > 0) {
            int maxLines = width / LINE_WIDTH;
            while (amplitudes.size() > maxLines) { // On supprime les premiers éléments de la liste
                amplitudes.remove(0);
            }
        }
        invalidate(); // On demande un redessin de la vue
    }

    public void clear() {
        // Méthode pour effacer le graphique
        amplitudes.clear();
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // Méthode pour dessiner le graphique

        super.onDraw(canvas); // Appel de la méthode de la classe parente

        float height = getHeight();
        float width = getWidth();
        float centerY = height / 2f;
        
        // On dessine de droite à gauche
        float x = width - LINE_WIDTH;

        // On parcourt la liste des amplitudes dans l'ordre inverse
        for (int i = amplitudes.size() - 1; i >= 0; i--) {
            float amp = amplitudes.get(i);
            canvas.drawLine(x, centerY - amp/2, x, centerY + amp/2, paint); // Dessin de la barre
            x -= LINE_WIDTH; // Décalage de la position de dessin de la barre
            if (x < 0) break; // On s'arrête si on dépasse la largeur du graphique
        }
    }
}