# TP de physique en 3D — Pr Hicham MESBAH

Application Android (WebView) des TP de physique de la FS Agadir :
fenêtres SV2·STU2 et SMP2·SMC2·SMIA2, câblage par l'étudiant, relations du polycopié,
compte rendu à remplir, vérifier et enregistrer.

## Ouvrir et lancer
1. Android Studio (Koala ou plus récent) → **File › Open** → choisir le dossier `PrHichamMesbah`.
2. Laisser la synchronisation Gradle se terminer (Internet nécessaire la première fois).
3. Brancher un téléphone (débogage USB activé) ou lancer un émulateur → bouton **Run ▶**.

Au premier build, la tâche `downloadThree` télécharge three.js r128 dans
`app/src/main/assets/libs/`. Ensuite l'application fonctionne **sans Internet**.

## Générer l'APK à distribuer aux étudiants
**Build › Generate Signed Bundle / APK › APK** → créer une clé → `release`.
Le fichier se trouve dans `app/release/app-release.apk`.
(Pour un essai rapide : **Build › Build APK(s)**, fichier `app/build/outputs/apk/debug/app-debug.apk`.)

## Modifier la simulation
Toute la simulation est dans `app/src/main/assets/index.html`.

## Comptes rendus
Le bouton « Télécharger » enregistre le compte rendu (.html) dans
`Téléchargements/Pr_Hicham_MESBAH` et ouvre le menu de partage (WhatsApp, Gmail, Drive…).
