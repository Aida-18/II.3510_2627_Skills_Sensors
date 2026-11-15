package com.example.swapcampus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swapcampus.shared.Listing
import com.example.swapcampus.shared.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Vue détaillée d'une annonce (§5.2).
 * @param onModerate non nul seulement pour un administrateur : retire l'annonce avec un motif (§5.6).
 */
@Composable
fun ListingDetailScreen(listing: Listing?, onModerate: ((reason: String) -> Unit)?) {
    if (listing == null) {
        Text(stringResource(Res.string.detail_not_found), Modifier.padding(24.dp))
        return
    }
    var showModerationDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ListingImage(listing, height = 260.dp)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(listing.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                formatPrice(listing.price),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text("${stringResource(listing.category.label)} · ${listing.status.label()}")
            if (listing.description.isNotBlank()) {
                Text(listing.description, style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                stringResource(Res.string.detail_seller, listing.author.name),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                stringResource(Res.string.detail_posted_on, formatDate(listing.creationDate)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (onModerate != null) {
                OutlinedButton(
                    onClick = { showModerationDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) { Text(stringResource(Res.string.mod_remove)) }
            }
        }
    }

    if (showModerationDialog && onModerate != null) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showModerationDialog = false },
            title = { Text(stringResource(Res.string.mod_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(stringResource(Res.string.mod_reason)) },
                    minLines = 2
                )
            },
            confirmButton = {
                TextButton(
                    enabled = reason.isNotBlank(),
                    onClick = {
                        showModerationDialog = false
                        onModerate(reason)
                    }
                ) { Text(stringResource(Res.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showModerationDialog = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            }
        )
    }
}
