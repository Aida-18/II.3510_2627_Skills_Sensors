package com.example.swapcampus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.swapcampus.shared.UiState
import com.example.swapcampus.shared.distanceFrom
import com.example.swapcampus.shared.resources.*
import org.jetbrains.compose.resources.stringResource

private val RADIUS_OPTIONS_KM = listOf(1, 5, 10)

/** Parcourir et filtrer les annonces disponibles — accessible sans connexion (§5.2, §5.3). */
@Composable
fun ListingsScreen(viewModel: ListingsViewModel, onOpen: (Int) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val position by viewModel.position.collectAsState()
    val locationProvider = rememberLocationProvider()
    var isLocating by remember { mutableStateOf(false) }
    var locationUnavailable by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Nouveau tri/filtre → on revient en haut pour voir le premier résultat (le plus proche).
    LaunchedEffect(filters, position) { listState.scrollToItem(0) }

    // Dès que le tri par proximité est demandé et autorisé, on interroge le capteur.
    LaunchedEffect(filters.sortByProximity, locationProvider.permission) {
        when {
            !filters.sortByProximity -> Unit
            locationProvider.permission == PermissionState.DENIED -> viewModel.onSortByProximity(false)
            locationProvider.permission == PermissionState.GRANTED -> {
                isLocating = true
                val found = locationProvider.currentPosition()
                isLocating = false
                locationUnavailable = found == null
                if (found != null) viewModel.onPosition(found) else viewModel.onSortByProximity(false)
            }
            else -> Unit
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = filters.keyword,
                onValueChange = viewModel::onKeyword,
                label = { Text(stringResource(Res.string.listings_search)) },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag(TestTags.LISTINGS_SEARCH)
            )
            OutlinedTextField(
                value = filters.maxPrice,
                onValueChange = viewModel::onMaxPrice,
                label = { Text(stringResource(Res.string.listings_max_price)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(120.dp)
            )
        }

        CategoryChips(
            selection = filters.category,
            onSelection = viewModel::onCategory,
            withAll = true,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Proximité : tri + rayon (§4 item 10, §5.3 « location/distance »)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = filters.sortByProximity,
                    onClick = {
                        locationUnavailable = false
                        val enable = !filters.sortByProximity
                        if (enable && locationProvider.permission != PermissionState.GRANTED) {
                            locationProvider.requestPermission()
                        }
                        viewModel.onSortByProximity(enable)
                    },
                    leadingIcon = {
                        if (isLocating) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, Modifier.size(18.dp))
                        }
                    },
                    label = { Text(stringResource(Res.string.sort_nearest)) }
                )
            }
            if (filters.sortByProximity && position != null) {
                items(RADIUS_OPTIONS_KM) { km ->
                    FilterChip(
                        selected = filters.radiusKm == km,
                        onClick = { viewModel.onRadius(km) },
                        label = { Text(stringResource(Res.string.radius_km, km)) }
                    )
                }
            }
        }

        val locationMessage = when {
            isLocating -> Res.string.location_loading
            locationProvider.permission == PermissionState.DENIED -> Res.string.location_denied
            locationUnavailable -> Res.string.location_unavailable
            else -> null
        }
        locationMessage?.let {
            Text(
                stringResource(it),
                style = MaterialTheme.typography.bodySmall,
                color = if (it == Res.string.location_loading) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.error
            )
        }

        when (val state = uiState) {
            UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)

            is UiState.Success -> if (state.data.isEmpty()) {
                Text(
                    stringResource(Res.string.listings_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 24.dp).align(Alignment.CenterHorizontally)
                        .testTag(TestTags.LISTINGS_EMPTY)
                )
            } else {
                val shownPosition = position.takeIf { filters.sortByProximity }
                LazyColumn(
                    modifier = Modifier.testTag(TestTags.LISTINGS_LIST),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    items(state.data, key = { it.id }) { listing ->
                        ListingCard(
                            listing,
                            onClick = { onOpen(listing.id) },
                            distanceKm = listing.distanceFrom(shownPosition)
                        )
                    }
                }
            }
        }
    }
}
