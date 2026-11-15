package com.example.swapcampus.shared

import kotlinx.datetime.LocalDate

/** Comptes et annonces de démonstration, pour ne pas démarrer sur une liste vide. */
object DemoData {

    private val creation = LocalDate(2026, 10, 1)

    val student1 = UserAccount(
        id = 1, name = "Étudiant 1", email = "etudiant1@swapcampus.com",
        passwordHash = "1234", creationDate = creation,
        role = UserRole.STUDENT, active = true
    )

    val student2 = UserAccount(
        id = 2, name = "Étudiant 2", email = "etudiant2@swapcampus.com",
        passwordHash = "1234", creationDate = creation,
        role = UserRole.STUDENT, active = true
    )

    val admin = UserAccount(
        id = 3, name = "Administrateur", email = "admin@swapcampus.com",
        passwordHash = "admin", creationDate = creation,
        role = UserRole.ADMINISTRATOR, active = true
    )

    val accounts = listOf(student1, student2, admin)

    /** Point de référence pour la démo : campus d'Issy-les-Moulineaux. */
    val campus = Position(48.8245, 2.2799)

    val listings = listOf(
        Listing(
            id = 1, title = "Kotlin in Action",
            description = "2e édition, très bon état, quelques annotations au crayon.",
            price = 25.0, category = Category.BOOKS,
            photoUrl = "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=600",
            author = student1, status = ListingStatus.Available,
            creationDate = LocalDate(2026, 10, 3),
            position = Position(48.8260, 2.2820)
        ),
        Listing(
            id = 2, title = "Calculatrice TI-83 Premium",
            description = "Mode examen, avec câble USB.",
            price = 40.0, category = Category.ELECTRONICS,
            photoUrl = "https://images.unsplash.com/photo-1611125832047-1d7ad1e8e48f?w=600",
            author = student2, status = ListingStatus.Available,
            creationDate = LocalDate(2026, 10, 5),
            position = Position(48.8380, 2.3010)
        ),
        Listing(
            id = 3, title = "Vélo de ville",
            description = "Taille M, freins révisés, antivol fourni.",
            price = 80.0, category = Category.SPORTS_LEISURE,
            photoUrl = "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=600",
            author = student2, status = ListingStatus.Available,
            creationDate = LocalDate(2026, 10, 8),
            position = Position(48.8566, 2.3522)
        ),
        Listing(
            id = 4, title = "Notes de cours – Algèbre linéaire",
            description = "Fiches complètes du semestre 1, tapées et imprimées.",
            price = 5.0, category = Category.COURSES, photoUrl = null,
            author = student1, status = ListingStatus.Reserved("etudiant2@swapcampus.com"),
            creationDate = LocalDate(2026, 10, 10),
            position = Position(48.8200, 2.2700)
        ),
        Listing(
            id = 5, title = "Bureau pliable",
            description = "Idéal pour une chambre étudiante, 80 × 50 cm.",
            price = 30.0, category = Category.FURNITURE, photoUrl = null,
            author = student1, status = ListingStatus.Sold(LocalDate(2026, 10, 20)),
            creationDate = LocalDate(2026, 10, 12)
        )
    )
}
