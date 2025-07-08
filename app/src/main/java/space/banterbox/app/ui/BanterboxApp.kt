package space.banterbox.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import space.banterbox.app.R
import space.banterbox.app.SharedViewModel
import space.banterbox.app.core.designsystem.LocalWindowSizeClass
import space.banterbox.app.core.designsystem.component.DefaultNavigationDrawer
import space.banterbox.app.core.designsystem.component.BanterboxBackground
import space.banterbox.app.core.designsystem.component.BanterboxGradientBackground
import space.banterbox.app.core.domain.model.ShopData
import space.banterbox.app.core.util.NetworkMonitor
import space.banterbox.app.navigation.NavigationDrawerDestination
import space.banterbox.app.navigation.BanterboxNavHost
import space.banterbox.app.navigation.TopLevelDestination
import space.banterbox.app.ui.theme.GradientColors
import space.banterbox.app.ui.theme.LocalGradientColors
import timber.log.Timber

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class,
)
@Composable
fun BanterboxApp(
    windowSizeClass: WindowSizeClass,
    networkMonitor: NetworkMonitor,
    sharedViewModel: SharedViewModel,
    appState: BanterboxAppState = rememberSellerAppState(
        windowSizeClass = windowSizeClass,
        networkMonitor = networkMonitor,
    ),
    startGraph: String,
    startDestination: String,
) {
    Timber.d("NavHost: startDestination=$startDestination")
    val shouldShowGradientBackground = true
    /*appState.currentTopLevelDestination == TopLevelDestination.HOME*/

    CompositionLocalProvider(
        LocalWindowSizeClass provides appState.windowSizeClass
    ) {
        BanterboxBackground {
            BanterboxGradientBackground(
                gradientColors = if (shouldShowGradientBackground) {
                    LocalGradientColors.current
                } else {
                    GradientColors()
                },
            ) {
                val snackbarHostState = remember { SnackbarHostState() }

                val isOffline by appState.isOffline.collectAsStateWithLifecycle()

                // If user is not connected to the internet show a snack bar to inform them.
                val notConnectedMessage = stringResource(R.string.you_are_not_connected_to_the_internet)
                /*LaunchedEffect(isOffline) {
                    if (isOffline) {
                        snackbarHostState.showSnackbar(
                            message = notConnectedMessage,
                            duration = SnackbarDuration.Indefinite,
                        )
                    }
                }*/

                Scaffold(
                    modifier = Modifier
                        // .navigationBarsPadding()
                        .semantics {
                            testTagsAsResourceId = true
                        }
                    ,
                    // containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = appState.shouldShowBottomBar,
                            enter = slideInVertically(
                                initialOffsetY = { height ->
                                    height / 2
                                }
                            ),
                            exit = slideOutVertically(
                                targetOffsetY = { height -> height },
                                animationSpec = tween(
                                    durationMillis = 175,
                                    easing = FastOutLinearInEasing
                                )
                            ),
                        ) {
                            BanterboxBottomBar(
                                destinations = appState.topLevelDestinations,
                                onNavigateToDestination = appState::navigateToTopLevelDestination,
                                currentDestination = appState.currentDestination,
                                modifier = Modifier.testTag("BanterboxBottomBar"),
                            )
                        }
                    },
                ) { padding ->
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .consumeWindowInsets(padding)
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(
                                    WindowInsetsSides.Horizontal,
                                ),
                            )
                        /*.offset(
                            x = with (LocalDensity.current) {
                                max(0.dp, xPos.toDp() - 56.dp)
                            }
                        )*/,
                    ) {
                        if (appState.shouldShowNavRail) {
                            BanterboxNavRail(
                                destinations = appState.topLevelDestinations,
                                onNavigateToDestination = appState::navigateToTopLevelDestination,
                                currentDestination = appState.currentDestination,
                                modifier = Modifier
                                    .testTag("BanterboxNavRail")
                                    .safeDrawingPadding(),
                            )
                        }

                        Column(Modifier.fillMaxSize()) {
                            // Show the top app bar on top level destinations.
                            val destination = appState.currentTopLevelDestination
                            if (destination != null) {
                                /*BanterboxTopAppBar(
                                    modifier = Modifier
                                        .shadow(4.dp),
                                    title = @Composable {
                                        Text(
                                            text = "Name",
                                            style = MaterialTheme.typography.titleMedium
                                                .copy(fontWeight = FontWeight.W700)
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            scope.launch {
                                                drawerState.apply {
                                                    if (isClosed) open() else close()
                                                }
                                            }
                                        }) {
                                            Icon(
                                                painter = painterResource(id = BanterboxSellerIcons.Id_Breadcrumbs),
                                                contentDescription = "Open Drawer",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    },
                                    actions = {
                                        IconButton(onClick = {}) {
                                            Icon(
                                                imageVector = BanterboxSellerIcons.MoreVert,
                                                contentDescription = "Options",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }
                                )*/
                            }

                            BanterboxNavHost(
                                modifier = Modifier.weight(1F),
                                appState = appState,
                                onShowSnackBar = { message, action ->
                                    snackbarHostState.showSnackbar(
                                        message = message,
                                        actionLabel = action,
                                        duration = SnackbarDuration.Short,
                                    ) == SnackbarResult.ActionPerformed
                                },
                                startGraph = startGraph,
                                startDestination = startDestination
                            )

                            AnimatedVisibility(
                                visible = isOffline,
                                enter = slideInVertically(
                                    animationSpec = tween(
                                        200,
                                    )
                                ) { fullHeight ->
                                    fullHeight / 3
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1F)
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Text(
                                        text = notConnectedMessage,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = insetMedium)
                                    )
                                }
                            }
                        }

                        // TODO: We may want to add padding or spacer when the snackbar is shown so that
                        //  content doesn't display behind it.
                    }
                }
            }
        }
    }
}

@Deprecated("Not yet implemented")
@Composable
private fun BanterboxNavRail(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    /*BanterboxNavigationRail(modifier) {
        destinations.forEach { destination ->
            val hasUnread = true
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            BanterboxNavigationRailItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = destination.unselectedIcon,
                        contentDescription = null,
                    )
                },
                selectedIcon = {
                    Icon(
                        imageVector = destination.selectedIcon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.iconTextId)) },
                modifier = if (hasUnread) Modifier.notificationDot() else modifier,
            )
        }
    }*/
}

@Composable
private fun BanterboxBottomBar(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
    ) {
        destinations.forEach { destination ->
            val hasUnread = false
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)

            if (destination.titleTextId == null) {
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = {
                        if (selected) {
                            Icon(
                                painter = painterResource(id = destination.selectedIcon),
                                contentDescription = null,
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = destination.unselectedIcon),
                                contentDescription = null,
                            )
                        }
                    },
                    modifier = if (hasUnread) Modifier.notificationDot() else modifier,
                )
            } else {
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = {
                        if (selected) {
                            Icon(
                                painter = painterResource(id = destination.selectedIcon),
                                contentDescription = null,
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = destination.unselectedIcon),
                                contentDescription = null,
                            )
                        }
                    },
                    label = { Text(stringResource(destination.iconTextId)) },
                    modifier = if (hasUnread) Modifier.notificationDot() else modifier,
                )
            }
        }
    }
}

@Composable
private fun BanterboxNavigationDrawer(
    shopData: ShopData,
    destinations: List<NavigationDrawerDestination>,
    onNavigateToDestination: (NavigationDrawerDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    DefaultNavigationDrawer(
        shopData = shopData,
        destinations = destinations,
        onNavigateToDestination = onNavigateToDestination,
        currentDestination = currentDestination,
        modifier = modifier,
    )
}

private fun Modifier.notificationDot(): Modifier =
    composed {
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        drawWithContent {
            drawContent()
            drawCircle(
                tertiaryColor,
                radius = 5.dp.toPx(),
                // This is based on the dimensions of the NavigationBar's "indicator pill";
                // however, its parameters are private, so we must depend on them implicitly
                // (NavigationBarTokens.ActiveIndicatorWidth = 64.dp)
                center = center + Offset(
                    64.dp.toPx() * .45f,
                    32.dp.toPx() * -.45f - 6.dp.toPx(),
                ),
            )
        }
    }


private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination): Boolean {
    val stackString = this?.hierarchy?.map { it.route }?.joinToString("->") ?: ""
    Timber.tag("Navigation").d("Hierarchy stack $stackString")
    return this?.hierarchy
        ?.filterNot { it.route?.contains("_graph") == true }
        ?.any {
            it.route?.contains(destination.name, ignoreCase = true) ?: false
        } ?: false
}