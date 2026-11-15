package com.example.swapcampus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.swapcampus.shared.resources.*
import org.jetbrains.compose.resources.stringResource

private enum class LoginError { EMPTY, INVALID }

/**
 * Écran de connexion. [onLogin] renvoie `true` si les identifiants sont valides ;
 * l'écran protégé s'affiche alors automatiquement (l'utilisateur courant est observé).
 */
@Composable
fun LoginScreen(onLogin: (String, String) -> Boolean) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<LoginError?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(Res.string.login_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.login_required),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = { Text(stringResource(Res.string.login_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp).testTag(TestTags.LOGIN_EMAIL)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text(stringResource(Res.string.login_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).testTag(TestTags.LOGIN_PASSWORD)
        )

        Button(
            onClick = {
                error = when {
                    email.isBlank() || password.isBlank() -> LoginError.EMPTY
                    !onLogin(email.trim(), password) -> LoginError.INVALID
                    else -> null
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag(TestTags.LOGIN_BUTTON)
        ) {
            Text(stringResource(Res.string.action_login))
        }

        error?.let {
            Text(
                text = stringResource(
                    when (it) {
                        LoginError.EMPTY -> Res.string.login_error_empty_fields
                        LoginError.INVALID -> Res.string.login_error_invalid_credentials
                    }
                ),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Text(
            stringResource(Res.string.login_demo_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}
