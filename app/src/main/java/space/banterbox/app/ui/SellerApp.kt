package space.banterbox.app.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import space.banterbox.app.R
import space.banterbox.app.SharedViewModel
import space.banterbox.app.core.designsystem.LocalWindowSizeClass
import space.banterbox.app.core.designsystem.ShopsSellerIcons
import space.banterbox.app.core.designsystem.component.DefaultNavigationDrawer
import space.banterbox.app.core.designsystem.component.ShopsBackground
import space.banterbox.app.core.designsystem.component.ShopsGradientBackground
import space.banterbox.app.core.designsystem.component.ShopsNavigationBar
import space.banterbox.app.core.designsystem.component.ShopsNavigationBarItem
import space.banterbox.app.core.designsystem.component.ShopsTopAppBar
import space.banterbox.app.core.domain.model.ShopData
import space.banterbox.app.core.util.NetworkMonitor
import space.banterbox.app.navigation.NavigationDrawerDestination
import space.banterbox.app.navigation.ShopsNavHost
import space.banterbox.app.navigation.TopLevelDestination
import space.banterbox.app.ui.theme.GradientColors
import space.banterbox.app.ui.theme.LocalGradientColors
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class,
)
@Composable
fun SellerApp(
    windowSizeClass: WindowSizeClass,
    networkMonitor: NetworkMonitor,
    sharedViewModel: SharedViewModel,
    appState: SellerAppState = rememberSellerAppState(
        windowSizeClass = windowSizeClass,
        networkMonitor = networkMonitor,
    ),
    startGraph: String,
    startDestination: String,
) {
    Timber.d("NavHost: startDestination=$startDestination")
    val shouldShowGradientBackground = false
    /*appState.currentTopLevelDestination == TopLevelDestination.HOME*/

    var showSettingsDialog by rememberSaveable {
        mutableStateOf(false)
    }

    CompositionLocalProvider(
        LocalWindowSizeClass provides appState.windowSizeClass
    ) {
        ShopsBackground {
            ShopsGradientBackground(
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

                if (showSettingsDialog) {
                    // TODO: show settings dialog
                }

                val shopData by sharedViewModel.shopData.collectAsStateWithLifecycle()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var drawerWidth by remember { mutableFloatStateOf(drawerState.offset.value) }
                Timber.d("NavigationDrawer: offset=${drawerState.offset}")

                // As soon the user move the drawer, the content must move in sync.
                // So here we're creating a derived state of the drawer state
                // to update the content position.
                val contentOffset = remember {
                    derivedStateOf {
                        drawerState.offset.value
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier
                        ) {
                            /* Drawer Content */
                            ShopsNavigationDrawer(
                                shopData = shopData,
                                destinations = appState.navigationDrawerDestinations,
                                onNavigateToDestination = { destination ->
                                    appState.navigateToDrawerDestination(destination)
                                    scope.launch {
                                        drawerState.apply {
                                            if (isOpen) close()
                                        }
                                    }
                                },
                                currentDestination = appState.currentDestination,
                                modifier = Modifier.testTag("ShopsNavigationDrawer")
                            )
                        }
                    },
                    gesturesEnabled = drawerState.isOpen
                ) {
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
                                ShopsBottomBar(
                                    destinations = appState.topLevelDestinations,
                                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                                    currentDestination = appState.currentDestination,
                                    modifier = Modifier.testTag("ShopsBottomBar"),
                                )
                            }
                        },
                    ) { padding ->
                        val xPos = (abs(drawerWidth) - abs(contentOffset.value))
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
                                ShopsNavRail(
                                    destinations = appState.topLevelDestinations,
                                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                                    currentDestination = appState.currentDestination,
                                    modifier = Modifier
                                        .testTag("ShopsNavRail")
                                        .safeDrawingPadding(),
                                )
                            }

                            Column(Modifier.fillMaxSize()) {
                                // Show the top app bar on top level destinations.
                                val destination = appState.currentTopLevelDestination
                                if (destination != null) {
                                    ShopsTopAppBar(
                                        modifier = Modifier
                                            .shadow(4.dp),
                                        title = @Composable {
                                            Text(
                                                text = shopData.name,
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
                                                    painter = painterResource(id = ShopsSellerIcons.Id_Breadcrumbs),
                                                    contentDescription = "Open Drawer",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                        },
                                        actions = {
                                            IconButton(onClick = {}) {
                                                Icon(
                                                    imageVector = ShopsSellerIcons.MoreVert,
                                                    contentDescription = "Options",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                        }
                                    )
                                }

                                ShopsNavHost(
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

                SideEffect {
                    if (drawerWidth == 0f) {
                        drawerWidth = drawerState.offset.value
                    }
                }

                BackHandler(
                    enabled = drawerState.isOpen
                ) {
                    scope.launch {
                        drawerState.apply {
                            if (isOpen) {
                                close()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Deprecated("Not yet implemented")
@Composable
private fun ShopsNavRail(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    /*ShopsNavigationRail(modifier) {
        destinations.forEach { destination ->
            val hasUnread = true
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            ShopsNavigationRailItem(
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
private fun ShopsBottomBar(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    ShopsNavigationBar(
        modifier = modifier,
    ) {
        destinations.forEach { destination ->
            val hasUnread = false
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)

            if (destination.titleTextId == null) {
                ShopsNavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = {
                        Icon(
                            painter = painterResource(id = destination.unselectedIcon),
                            contentDescription = null,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            painter = painterResource(id = destination.selectedIcon),
                            contentDescription = null,
                        )
                    },
                    modifier = if (hasUnread) Modifier.notificationDot() else modifier,
                )
            } else {
                ShopsNavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = {
                        Icon(
                            painter = painterResource(id = destination.unselectedIcon),
                            contentDescription = null,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            painter = painterResource(id = destination.selectedIcon),
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(destination.iconTextId)) },
                    modifier = if (hasUnread) Modifier.notificationDot() else modifier,
                )
            }
        }
    }
}

@Composable
private fun ShopsNavigationDrawer(
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