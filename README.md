# SwapCampus : Géolocalisation (projet inachevé)

**II.3510 Mobile Development, présentation « Sensors » (§4 item 10)**
Maryam El Moutadayene & Aida Hamdane

Ce dépôt est un **projet inachevé** : à vous de le compléter !
SwapCampus est l'application de petites annonces du cours (Kotlin Multiplatform + Compose Multiplatform, Android et iOS).
Ici, tout fonctionne **sauf la géolocalisation**. Votre mission : permettre de **trier les annonces par proximité**
grâce au capteur de position du téléphone.

À la fin, le bouton **« Les plus proches »** demande l'autorisation de localisation, lit la position GPS, trie les
annonces de la plus proche à la plus lointaine, affiche la distance (« à 226 m ») et propose des filtres ≤ 1 / 5 / 10 km.

La présentation est dans [`presentation/`](presentation/).

---

## 0. Démarrer

1. Ouvrir le dossier dans **Android Studio** (File › Open), attendre la fin du « Gradle sync ».
2. Lancer la configuration **`app`** sur un émulateur (API 34 ou plus) : l'app tourne, mais le tri par proximité ne marche pas encore.
3. Lancer les tests unitaires : clic droit sur `shared/src/commonTest/kotlin` › *Run Tests*
   (ou `./gradlew :shared:testAndroidHostTest`).
   **3 tests échouent** dans `ProximityTest` : ils passeront au vert au fil des exercices.

Comptes de démo : `etudiant1@swapcampus.com` / `1234` · `admin@swapcampus.com` / `admin`

### Simuler une position GPS
Les annonces de démo sont placées autour du campus d'Issy-les-Moulineaux (48.8245, 2.2799).
- **Émulateur Android** : panneau `⋯` › **Location** › saisir la latitude/longitude › **Set Location**,
  ou en ligne de commande : `adb emu geo fix 2.2799 48.8245` (attention : longitude puis latitude).
- **Simulateur iOS** : menu *Features › Location › Custom Location…*

---

## Exercice 1 : Déclarer les permissions (Android)
📄 `app/src/main/AndroidManifest.xml`

Une application Android doit **déclarer** les permissions qu'elle utilise. Ajoutez les deux permissions de localisation :
position **approximative** (réseau Wi-Fi / antennes) et position **précise** (GPS).

> 💡 Cherchez `ACCESS_COARSE_LOCATION` et `ACCESS_FINE_LOCATION` dans la documentation Android.
> Depuis Android 12, l'utilisateur peut n'accorder que la position approximative : il faut donc demander les deux.

---

## Exercice 2 : Calculer une distance (formule de Haversine)
📄 `shared/src/commonMain/kotlin/com/example/swapcampus/shared/Position.kt` › `distanceKm`

La Terre étant (presque) une sphère, la distance entre deux points GPS ne se calcule pas avec Pythagore,
mais avec la **formule de Haversine** :

```
a = sin²(Δlat / 2) + cos(lat1) · cos(lat2) · sin²(Δlon / 2)
d = 2 · R · asin(√a)          R = 6371 km (rayon moyen de la Terre)
```

Les angles doivent être en **radians** (fonction `toRadians()` fournie).
Ce code est dans `commonMain` : il est **partagé** par Android et iOS.

✅ Test : `haversine_paris_london_about_344_km`

---

## Exercice 3 : Trier et filtrer par proximité
📄 même fichier, `Position.kt`

- **3a. `sortedByProximity`** : trier les annonces de la plus proche à la plus lointaine,
  les annonces **sans position** à la fin. *(indice : `sortedBy { }` + `distanceFrom(user)` + `?:`)*
- **3b. `isWithinRadius`** : garder les annonces à moins de `radiusKm` kilomètres.
  Les règles exactes sont dans le commentaire de la fonction.

✅ Tests : `sorted_nearest_first_without_position_last`, `filter_by_radius`
→ à ce stade, **les 22 tests unitaires sont verts**.

---

## Exercice 4 : Lire le capteur de position (Android)
📄 `shared/src/androidMain/kotlin/com/example/swapcampus/LocationProvider.android.kt`

La position se lit différemment sur chaque plateforme. Le code commun déclare une interface `LocationProvider`
(`expect`), et chaque plateforme fournit son implémentation (`actual`).

- **4a. `requestPermission()`** : afficher la demande d'autorisation système (le `launcher` est déjà prêt).
- **4b. `currentPosition()`** : lire la position avec le `LocationManager` et la convertir en `Position`.
  Les étapes sont détaillées dans le commentaire.

✅ Vérification manuelle : lancez l'app, cliquez sur **« Les plus proches »**, acceptez la permission :
les annonces se trient et la distance s'affiche. Testez aussi le **refus** de la permission : un message s'affiche.

---

## Bonus
- **iOS** : la version iPhone est déjà écrite (`shared/src/iosMain/.../LocationProvider.ios.kt`, avec `CLLocationManager`).
  Comparez-la avec votre version Android, puis lancez l'app dans Xcode (`iosApp/iosApp.xcodeproj`).
- Position **fraîche** sur Android avec `LocationManagerCompat.getCurrentLocation(...)` (voir l'étape 4b).
- Ajouter un rayon **≤ 20 km** (`RADIUS_OPTIONS_KM` dans `ListingsScreen.kt`).
- Afficher la distance dans l'**écran de détail** d'une annonce (`ListingDetailScreen.kt`).

---

## Où regarder (déjà fait pour vous)

| Fichier | Rôle |
|---|---|
| `shared/.../LocationProvider.kt` | Interface commune (`expect`) + état de la permission |
| `shared/.../ViewModels.kt` › `ListingsViewModel` | Combine annonces + filtres + position (`combine`) |
| `shared/.../ListingsScreen.kt` | Puce « Les plus proches », rayons, message si permission refusée |
| `shared/.../ListingFormScreen.kt` | Bouton « Ajouter ma position » à la publication |
| `iosApp/iosApp/Info.plist` + projet Xcode | Clé `NSLocationWhenInUseUsageDescription` (obligatoire sur iOS) |
