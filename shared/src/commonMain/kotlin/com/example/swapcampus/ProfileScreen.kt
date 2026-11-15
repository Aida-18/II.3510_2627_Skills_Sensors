package com.example.swapcampus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swapcampus.shared.ModerationAction
import com.example.swapcampus.shared.ModerationTarget
import com.example.swapcampus.shared.UserAccount
import com.example.swapcampus.shared.UserRole
import com.example.swapcampus.shared.platform
import com.example.swapcampus.shared.resources.*
import org.jetbrains.compose.resources.stringResource

/** Profil ; pour un administrateur, affiche aussi le journal de modération (§5.6, §7.6). */
@Composable
fun ProfileScreen(user: UserAccount, actionLog: List<ModerationAction>, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(user.name, style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(Res.string.profile_email, user.email))
        Text(stringResource(Res.string.profile_role, stringResource(user.role.label)))
        Text(
            stringResource(Res.string.profile_platform, platform()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Text(stringResource(Res.string.action_logout))
        }

        if (user.role == UserRole.ADMINISTRATOR) {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(stringResource(Res.string.mod_log_title), style = MaterialTheme.typography.titleMedium)
            if (actionLog.isEmpty()) {
                Text(stringResource(Res.string.mod_log_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            actionLog.forEach { action ->
                val target = (action.target as? ModerationTarget.ListingTarget)?.listingId ?: return@forEach
                Text(
                    stringResource(Res.string.mod_log_entry, target, formatDate(action.date.date), action.reason),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
