package com.example.swapcampus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swapcampus.shared.Listing
import com.example.swapcampus.shared.ListingStatus
import com.example.swapcampus.shared.SellerStats
import com.example.swapcampus.shared.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Annonces du vendeur connecté (y compris réservées/vendues) + compteurs (§5.5, §5.7). */
@Composable
fun MyListingsScreen(
    viewModel: MyListingsViewModel,
    onOpen: (Int) -> Unit,
    onEdit: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var toDelete by remember { mutableStateOf<Listing?>(null) }
    var toReserve by remember { mutableStateOf<Listing?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag(TestTags.MY_LISTINGS_LIST),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item { StatsRow(state.stats) }

        if (state.listings.isEmpty()) {
            item {
                Text(
                    stringResource(Res.string.my_listings_empty),
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }

        items(state.listings, key = { it.id }) { listing ->
            ListingCard(listing, onClick = { onOpen(listing.id) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // On ne propose que les transitions autorisées (§5.5).
                    when (listing.status) {
                        ListingStatus.Available -> {
                            item { Action(Res.string.action_edit) { onEdit(listing.id) } }
                            item { Action(Res.string.action_reserve) { toReserve = listing } }
                        }
                        is ListingStatus.Reserved -> {
                            item { Action(Res.string.action_edit) { onEdit(listing.id) } }
                            item { Action(Res.string.action_mark_sold) { viewModel.markAsSold(listing) } }
                            item {
                                Action(Res.string.action_cancel_reservation, secondary = true) {
                                    viewModel.putBackOnSale(listing)
                                }
                            }
                        }
                        // §5.4 : une annonce vendue ne se modifie pas directement.
                        is ListingStatus.Sold -> item {
                            Action(Res.string.action_make_available) { viewModel.putBackOnSale(listing) }
                        }
                    }
                    item { Action(Res.string.action_delete, secondary = true) { toDelete = listing } }
                }
            }
        }
    }

    toDelete?.let { listing ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text(stringResource(Res.string.delete_confirm_title)) },
            text = { Text(stringResource(Res.string.delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(listing)
                    toDelete = null
                }) { Text(stringResource(Res.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text(stringResource(Res.string.action_cancel)) }
            }
        )
    }

    toReserve?.let { listing ->
        var buyer by remember(listing.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { toReserve = null },
            title = { Text(stringResource(Res.string.reserve_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = buyer,
                    onValueChange = { buyer = it },
                    label = { Text(stringResource(Res.string.reserve_buyer)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = buyer.isNotBlank(),
                    onClick = {
                        viewModel.reserve(listing, buyer)
                        toReserve = null
                    }
                ) { Text(stringResource(Res.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { toReserve = null }) { Text(stringResource(Res.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun Action(label: StringResource, secondary: Boolean = false, onClick: () -> Unit) {
    if (secondary) {
        OutlinedButton(onClick = onClick) { Text(stringResource(label)) }
    } else {
        FilledTonalButton(onClick = onClick) { Text(stringResource(label)) }
    }
}

@Composable
private fun StatsRow(stats: SellerStats) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatTile(Res.string.stats_posted, stats.posted, Modifier.weight(1f))
        StatTile(Res.string.stats_available, stats.available, Modifier.weight(1f))
        StatTile(Res.string.stats_reserved, stats.reserved, Modifier.weight(1f))
        StatTile(Res.string.stats_sold, stats.sold, Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(label: StringResource, value: Int, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(stringResource(label), style = MaterialTheme.typography.labelMedium)
        }
    }
}
