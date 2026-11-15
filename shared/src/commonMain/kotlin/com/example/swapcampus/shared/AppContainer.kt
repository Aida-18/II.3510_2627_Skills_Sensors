package com.example.swapcampus.shared

/**
 * Conteneur de dépendances de l'application (injection « à la main »).
 * C'est un singleton de processus : les données en mémoire survivent à la
 * recréation de l'activité Android (rotation, changement de langue).
 */
object AppContainer {
    val authRepository: AuthRepository by lazy { AuthRepository() }

    val listingRepository: ListingRepository by lazy {
        ListingRepositoryImpl(ListingCatalog(DemoData.listings))
    }

    val moderationRepository: ModerationRepository by lazy {
        ModerationRepository(listingRepository)
    }
}
