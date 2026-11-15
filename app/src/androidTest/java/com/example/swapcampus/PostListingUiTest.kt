package com.example.swapcampus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swapcampus.shared.Category
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests UI automatisés (§8.8) : ils lancent la vraie application sur un émulateur
 * et la pilotent comme un utilisateur (clics, saisie), via les [TestTags]
 * (indépendants de la langue FR/EN).
 *
 * Lancer : `./gradlew :app:connectedDebugAndroidTest` (émulateur démarré).
 */
@RunWith(AndroidJUnit4::class)
class PostListingUiTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    /** Les données sont en mémoire et partagées entre les tests : on ne se connecte que si besoin. */
    private fun logInIfNeeded() {
        val onLoginScreen = compose.onAllNodes(hasTestTag(TestTags.LOGIN_EMAIL))
            .fetchSemanticsNodes().isNotEmpty()
        if (onLoginScreen) {
            compose.onNodeWithTag(TestTags.LOGIN_EMAIL).performTextInput("etudiant1@swapcampus.com")
            compose.onNodeWithTag(TestTags.LOGIN_PASSWORD).performTextInput("1234")
            compose.onNodeWithTag(TestTags.LOGIN_BUTTON).performClick()
        }
    }

    @Test
    fun posting_a_listing_shows_it_in_my_listings_and_the_public_list() {
        val title = "Casque audio test ${System.currentTimeMillis()}"

        // 1. Onglet « Publier » → connexion exigée (§3.1)
        compose.onNodeWithTag(TestTags.nav("post")).performClick()
        logInIfNeeded()

        // 2. Remplir le formulaire et publier
        compose.onNodeWithTag(TestTags.FORM_TITLE).performTextInput(title)
        compose.onNodeWithTag(TestTags.FORM_PRICE).performTextInput("15")
        compose.onNodeWithTag(TestTags.category(Category.ELECTRONICS)).performClick()
        compose.onNodeWithTag(TestTags.FORM_SUBMIT).performScrollTo().performClick()

        // 3. Redirection vers « Mes annonces » : la nouvelle annonce y figure (§5.1)
        compose.onNodeWithTag(TestTags.MY_LISTINGS_LIST).performScrollToNode(hasText(title))
        compose.onNode(hasText(title) and hasAnyAncestor(hasTestTag(TestTags.MY_LISTINGS_LIST)))
            .assertIsDisplayed()

        // 4. Elle est aussi visible publiquement, et retrouvable par la recherche (§5.2, §5.3)
        compose.onNodeWithTag(TestTags.nav("listings")).performClick()
        compose.onNodeWithTag(TestTags.LISTINGS_SEARCH).performTextInput(title)
        compose.onNode(hasText(title) and hasAnyAncestor(hasTestTag(TestTags.LISTINGS_LIST)))
            .assertIsDisplayed()
    }

    @Test
    fun posting_without_title_shows_an_error_and_stays_on_the_form() {
        compose.onNodeWithTag(TestTags.nav("post")).performClick()
        logInIfNeeded()

        compose.onNodeWithTag(TestTags.FORM_PRICE).performTextInput("10")
        compose.onNodeWithTag(TestTags.FORM_SUBMIT).performScrollTo().performClick()

        // Règle métier §5.1 : titre obligatoire → message d'erreur, pas de publication
        compose.onNodeWithTag(TestTags.FORM_ERROR).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag(TestTags.FORM_SUBMIT).assertIsDisplayed()
    }

    @Test
    fun search_without_results_shows_an_explicit_message() {
        // Parcourir ne demande pas de connexion (§3.1) ; §5.3 : pas d'écran vide silencieux
        compose.onNodeWithTag(TestTags.nav("listings")).performClick()
        compose.onNodeWithTag(TestTags.LISTINGS_SEARCH).performTextInput("zzz-aucune-annonce")
        compose.onNodeWithTag(TestTags.LISTINGS_EMPTY).assertIsDisplayed()
    }
}
