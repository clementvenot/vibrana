# Vibrana 🎸

**Vibrana** est une application Android complète pour les musiciens, offrant un accordeur de guitare de haute précision, un piano interactif et un module d'enregistrement audio.

## ✨ Fonctionnalités

### 1. Accordeur de Guitare (Tuner)
L'accordeur utilise le traitement du signal en temps réel pour une précision professionnelle.
*   **Accordage Standard** : Support complet des 6 cordes (Mi, La, Ré, Sol, Si, Mi).
*   **Mode Automatique** : Détecte intelligemment la corde que vous jouez sans intervention manuelle.
*   **Indicateur Visuel Dynamique** : Une interface intuitive avec une aiguille mobile basée sur le `horizontalBias` pour visualiser l'écart de fréquence.
*   **Précision Chirurgicale** : L'interface passe au **vert** lorsque vous êtes à moins de **1 Hz** de la cible.
*   **Algorithme d'Autocorrélation** : Analyse du pitch robuste pour filtrer les bruits ambiants.

### 2. Piano Virtuel
*   Un piano intégré pour vérifier des notes, accorder d'autres instruments à l'oreille ou composer.

### 3. Enregistreur & Archives
*   Enregistrez vos répétitions ou idées de riffs instantanément.
*   Interface dédiée pour gérer, écouter et organiser vos fichiers audio sauvegardés.

## 🛠 Stack Technique

*   **Langage** : Java
*   **Traitement Audio** : `AudioRecord` (échantillonnage à 44100Hz) avec algorithme d'autocorrélation personnalisé pour la détection de fréquence.
*   **UI/UX** : XML, Material Design, ConstraintLayout dynamique.
*   **Gestion des Permissions** : Système de gestion dynamique pour l'accès au microphone.

