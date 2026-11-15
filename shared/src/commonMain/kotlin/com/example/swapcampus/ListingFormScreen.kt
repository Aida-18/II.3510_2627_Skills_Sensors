package com.example.swapcampus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.swapcampus.shared.ListingError
import com.example.swapcampus.shared.resources.*
import org.jetbrains.compose.resources.stringResource

/** Formulaire commun à « Publier » (§5.1) et « Modifier » (§5.4). */
@Composable
fun ListingFormScreen(viewModel: ListingFormViewModel, isEdit: Boolean, onSaved: () -> Unit) {
    if (!viewModel.isAuthorized) {
        Text(
            stringResource(Res.string.edit_forbidden),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(24.dp)
        )
        return
    }

    val form by viewModel.form.collectAsState()
    val locationProvider = rememberLocationProvider()
    val scope = rememberCoroutineScope()
    var isLocating by remember { mutableStateOf(false) }
    var locationFailed by remember { mutableStateOf(false) }
    var awaitingPermission by remember { mutableStateOf(false) }

    fun capturePosition() {
        scope.launch {
            isLocating = true
            val position = locationProvider.currentPosition()
            isLocating = false
            locationFailed = position == null
            if (position != null) viewModel.onChange { it.copy(position = position) }
        }
    }

    // L'utilisateur vient d'accorder la permission : on capture la position.
    LaunchedEffect(locationProvider.permission) {
        if (awaitingPermission && locationProvider.permission == PermissionState.GRANTED) {
            awaitingPermission = false
            capturePosition()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(if (isEdit) Res.string.form_title_edit else Res.string.form_title_post),
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = form.title,
            onValueChange = { v -> viewModel.onChange { it.copy(title = v) } },
            label = { Text(stringResource(Res.string.form_title)) },
            isError = form.error == ListingError.EMPTY_TITLE,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(TestTags.FORM_TITLE)
        )

        OutlinedTextField(
            value = form.description,
            onValueChange = { v -> viewModel.onChange { it.copy(description = v) } },
            label = { Text(stringResource(Res.string.form_description)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.price,
            onValueChange = { v -> viewModel.onChange { it.copy(price = v) } },
            label = { Text(stringResource(Res.string.form_price)) },
            suffix = { Text("€") },
            isError = form.error == ListingError.INVALID_PRICE,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().testTag(TestTags.FORM_PRICE)
        )

        Text(stringResource(Res.string.form_category), style = MaterialTheme.typography.labelLarge)
        CategoryChips(
            selection = form.category,
            onSelection = { c -> c?.let { viewModel.onChange { f -> f.copy(category = c) } } },
            withAll = false
        )

        OutlinedTextField(
            value = form.photoUrl,
            onValueChange = { v -> viewModel.onChange { it.copy(photoUrl = v) } },
            label = { Text(stringResource(Res.string.form_photo)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth()
        )

        // Lieu de remise de l'objet, utilisé pour le tri par proximité.
        OutlinedButton(
            onClick = {
                locationFailed = false
                if (locationProvider.permission == PermissionState.GRANTED) {
                    capturePosition()
                } else {
                    awaitingPermission = true
                    locationProvider.requestPermission()
                }
            },
            enabled = !isLocating,
            colors = if (form.position != null)
                ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
            else ButtonDefaults.outlinedButtonColors(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLocating) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.LocationOn, contentDescription = null, Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(
                    if (form.position != null) Res.string.form_location_added else Res.string.form_location_add
                )
            )
        }
        when {
            locationProvider.permission == PermissionState.DENIED -> Res.string.location_denied
            locationFailed -> Res.string.location_unavailable
            else -> null
        }?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }

        form.error?.let { error ->
            Text(
                stringResource(
                    when (error) {
                        ListingError.EMPTY_TITLE -> Res.string.error_title_empty
                        ListingError.INVALID_PRICE -> Res.string.error_price_invalid
                    }
                ),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag(TestTags.FORM_ERROR)
            )
        }

        Button(
            onClick = { if (viewModel.save()) onSaved() },
            modifier = Modifier.fillMaxWidth().testTag(TestTags.FORM_SUBMIT)
        ) {
            Text(stringResource(if (isEdit) Res.string.action_save else Res.string.action_publish))
        }
    }
}
