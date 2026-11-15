package com.example.swapcampus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.swapcampus.shared.Listing
import com.example.swapcampus.shared.Category
import com.example.swapcampus.shared.ListingStatus
import com.example.swapcampus.shared.hasPhoto
import com.example.swapcampus.shared.resources.*
import org.jetbrains.compose.resources.stringResource

/** Rangée de puces de catégories. `null` = « Toutes » (si [withAll]). */
@Composable
fun CategoryChips(
    selection: Category?,
    onSelection: (Category?) -> Unit,
    withAll: Boolean,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (withAll) {
            item {
                FilterChip(
                    selected = selection == null,
                    onClick = { onSelection(null) },
                    label = { Text(stringResource(Res.string.listings_all_categories)) }
                )
            }
        }
        items(Category.entries) { category ->
            FilterChip(
                selected = selection == category,
                onClick = { onSelection(category) },
                label = { Text(stringResource(category.label)) },
                modifier = Modifier.testTag(TestTags.category(category))
            )
        }
    }
}

/** Photo de l'annonce, ou bandeau « Pas de photo » si absente ou impossible à charger. */
@Composable
fun ListingImage(listing: Listing, height: Dp, modifier: Modifier = Modifier) {
    var loadFailed by remember(listing.photoUrl) { mutableStateOf(false) }
    if (listing.hasPhoto() && !loadFailed) {
        AsyncImage(
            model = listing.photoUrl,
            contentDescription = listing.title,
            contentScale = ContentScale.Crop,
            onError = { loadFailed = true },
            modifier = modifier.fillMaxWidth().height(height)
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(Res.string.listings_no_photo),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Carte riche d'une annonce : image, titre, prix, catégorie, statut (§4 item 3). */
@Composable
fun ListingCard(
    listing: Listing,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    distanceKm: Double? = null,
    actions: @Composable () -> Unit = {}
) {
    val content: @Composable () -> Unit = {
        Column {
            ListingImage(listing, height = 160.dp)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    formatPrice(listing.price),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${stringResource(listing.category.label)} · ${listing.status.label()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (listing.status is ListingStatus.Available)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.tertiary
                )
                distanceKm?.let {
                    Text(
                        "📍 " + stringResource(Res.string.distance_away, formatDistance(it)),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                actions()
            }
        }
    }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth()) { content() }
    } else {
        Card(modifier = modifier.fillMaxWidth()) { content() }
    }
}
