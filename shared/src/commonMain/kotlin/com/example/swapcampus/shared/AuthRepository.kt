package com.example.swapcampus.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Authentification simplifiée en mémoire (§8.2, TP1/TP2) : liste de comptes de démo,
 * pas de hachage. Sera remplacée par Firebase Authentication.
 */
class AuthRepository(
    private val accounts: List<UserAccount> = DemoData.accounts
) {

    private val _currentUser = MutableStateFlow<UserAccount?>(null)

    /** Compte connecté, ou `null` si personne n'est connecté. */
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    /**
     * Connecte l'utilisateur si l'email (insensible à la casse) et le mot de passe
     * correspondent à un compte actif.
     * @return le compte connecté, ou `null` en cas d'échec (l'utilisateur courant ne change pas).
     */
    fun authenticate(email: String, password: String): UserAccount? =
        accounts.find {
            it.email.equals(email.trim(), ignoreCase = true) &&
                it.passwordHash == password &&
                it.active
        }?.also { _currentUser.value = it }

    fun logout() {
        _currentUser.value = null
    }
}
