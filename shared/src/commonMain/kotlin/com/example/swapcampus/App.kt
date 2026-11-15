package com.example.swapcampus

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.padding as pad
import androidx.compose.ui.unit.dp
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.swapcampus.shared.AppContainer
import com.example.swapcampus.shared.UserAccount
import com.example.swapcampus.shared.UserRole
import com.example.swapcampus.shared.resources.*
import com.example.swapcampus.theme.SwapCampusTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Routes de navigation. */
private object Routes {
    const val LISTINGS = "listings"
    const val DETAIL = "detail/{id}"
    const val MY_LISTINGS = "myListings"
    const val POST = "post"
    const val EDIT = "edit/{id}"
    const val PROFILE = "profile"

    fun detail(id: Int) = "detail/$id"
    fun edit(id: Int) = "edit/$id"
}

private data class BottomTab(val route: String, val label: StringResource, val icon: ImageVector)

private val tabs = listOf(
    BottomTab(Routes.LISTINGS, Res.string.nav_listings, Icons.Filled.Home),
    BottomTab(Routes.MY_LISTINGS, Res.string.nav_my_listings, Icons.AutoMirrored.Filled.List),
    BottomTab(Routes.POST, Res.string.nav_post, Icons.Filled.Add),
    BottomTab(Routes.PROFILE, Res.string.nav_profile, Icons.Filled.Person)
)

/** Application SwapCampus partagée Android / iOS (Compose Multiplatform). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val auth = AppContainer.authRepository
    val repository = AppContainer.listingRepository
    val moderation = AppContainer.moderationRepository
    val currentUser by auth.currentUser.collectAsState()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTab = tabs.any { it.route == currentRoute }

    SwapCampusTheme {
        Scaffold(
            topBar = {
                if (!isTab && currentRoute != null) {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(Res.string.action_back)
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.navigateToTab(tab.route) },
                            modifier = Modifier.testTag(TestTags.nav(tab.route)),
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label)) }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.LISTINGS,
                modifier = Modifier.padding(padding)
            ) {
                // Public : parcourir sans connexion (§3.1)
                composable(Routes.LISTINGS) {
                    ListingsScreen(
                        viewModel = viewModel { ListingsViewModel(repository) },
                        onOpen = { navController.navigate(Routes.detail(it)) }
                    )
                }

                composable(
                    Routes.DETAIL,
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { entry ->
                    val id = entry.savedStateHandle.get<Int>("id") ?: -1
                    val vm = viewModel { ListingDetailViewModel(repository, moderation, id) }
                    val listing by vm.listing.collectAsState()
                    val admin = currentUser?.takeIf { it.role == UserRole.ADMINISTRATOR }
                    ListingDetailScreen(
                        listing = listing,
                        onModerate = admin?.let { a ->
                            { reason -> if (vm.remove(reason, a)) navController.popBackStack() }
                        }
                    )
                }

                // Protégé : nécessite d'être connecté
                composable(Routes.MY_LISTINGS) {
                    RequireLogin(currentUser) {
                        MyListingsScreen(
                            viewModel = viewModel { MyListingsViewModel(repository, auth) },
                            onOpen = { navController.navigate(Routes.detail(it)) },
                            onEdit = { navController.navigate(Routes.edit(it)) }
                        )
                    }
                }

                composable(Routes.POST) {
                    RequireLogin(currentUser) { user ->
                        // §7.1 : un administrateur ne possède pas d'annonces.
                        if (user.role == UserRole.ADMINISTRATOR) {
                            Text(
                                stringResource(Res.string.admin_cannot_post),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.pad(24.dp)
                            )
                            return@RequireLogin
                        }
                        ListingFormScreen(
                            viewModel = viewModel { ListingFormViewModel(repository, auth, null) },
                            isEdit = false,
                            onSaved = { navController.navigateToTab(Routes.MY_LISTINGS) }
                        )
                    }
                }

                composable(
                    Routes.EDIT,
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { entry ->
                    val id = entry.savedStateHandle.get<Int>("id") ?: -1
                    RequireLogin(currentUser) {
                        ListingFormScreen(
                            viewModel = viewModel { ListingFormViewModel(repository, auth, id) },
                            isEdit = true,
                            onSaved = { navController.popBackStack() }
                        )
                    }
                }

                composable(Routes.PROFILE) {
                    RequireLogin(currentUser) { user ->
                        val actionLog by moderation.actionLog.collectAsState()
                        ProfileScreen(
                            user = user,
                            actionLog = actionLog,
                            onLogout = {
                                auth.logout()
                                navController.navigateToTab(Routes.LISTINGS)
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Affiche [content] si un utilisateur est connecté, sinon l'écran de connexion. */
@Composable
private fun RequireLogin(user: UserAccount?, content: @Composable (UserAccount) -> Unit) {
    if (user == null) {
        LoginScreen { email, password ->
            AppContainer.authRepository.authenticate(email, password) != null
        }
    } else {
        content(user)
    }
}

/** Navigation entre onglets sans empiler les écrans. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
