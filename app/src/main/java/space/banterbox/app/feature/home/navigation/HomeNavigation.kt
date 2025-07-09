package space.banterbox.app.feature.home.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import space.banterbox.app.Constant
import space.banterbox.app.SharedViewModel
import space.banterbox.app.feature.home.presentation.create.CreateRoute
import space.banterbox.app.feature.home.presentation.create.WritePostRoute
import space.banterbox.app.feature.home.presentation.landing.HomeRoute
import space.banterbox.app.feature.home.presentation.notification.NotificationRoute
import space.banterbox.app.feature.home.presentation.post.PostDetailRoute
import space.banterbox.app.feature.home.presentation.profile.ProfileRoute
import space.banterbox.app.feature.home.presentation.search.SearchRoute
import space.banterbox.app.feature.home.presentation.settings.SettingsRoute
import space.banterbox.app.feature.home.presentation.webview.WebPageRoute
import space.banterbox.app.sharedViewModel

const val HOME_GRAPH_ROUTE_PATTERN = "home_graph"
const val SETTINGS_GRAPH_ROUTE_PATTERN = "settings_graph"
const val PROFILE_GRAPH_ROUTE_PATTERN = "profile_graph"
const val SEARCH_GRAPH_ROUTE_PATTERN = "search_graph"

const val FIRST_LOG_IN = "firstLogin"

const val homeNavigationRoute = "home_route?${FIRST_LOG_IN}={${FIRST_LOG_IN}}"
const val createNavigationRoute = "create_route"
const val USER_ID_ARG = "userId"
const val profileNavigationRoute = "profile_route/{$USER_ID_ARG}"
const val writePostNavigationRoute = "write_post_route"
const val POST_ID_ARG = "postId"
const val postDetailNavigationRoute = "post_detail_route/{$POST_ID_ARG}"
const val searchNavigationRoute = "search_route"
const val notificationsNavigationRoute = "notification_route"

const val webPageNavigationRoute = "web_page_route?url={url}"
const val settingsNavigationRoute = "settings_route"

const val maintenanceNavigationRoute = "maintenance_route"

private const val DEEP_LINK_URI_PATTERN =
    "https://www.shopsseller.cc/home"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(homeNavigationRoute, navOptions)
}

fun NavController.navigateToProfile(
    profileId: String = "",
    navOptions: NavOptions? = null
) {
    this.navigate(
        profileNavigationRoute.replace("{$USER_ID_ARG}", profileId),
        navOptions
    )
}

fun NavController.navigateToPostDetail(
    postId: String,
    navOptions: NavOptions? = null
) {
    this.navigate(
        postDetailNavigationRoute.replace("{$POST_ID_ARG}", postId),
        navOptions
    )
}

fun NavController.navigateToSearch(
    navOptions: NavOptions? = null
) {
    this.navigate(searchNavigationRoute, navOptions)
}

fun NavController.navigateToNotifications(
    navOptions: NavOptions? = null
) {
    this.navigate(notificationsNavigationRoute, navOptions)
}

fun NavController.navigateToWebPage(url: String, navOptions: NavOptions? = null) {
    this.navigate(webPageNavigationRoute.replace("{url}", url), navOptions)
}

fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    this.navigate(settingsNavigationRoute, navOptions)
}

fun NavController.navigateToHomeGraph(navOptions: NavOptions? = null) {
    this.navigate(HOME_GRAPH_ROUTE_PATTERN, navOptions)
}

fun NavController.navigateToSettingsGraph(navOptions: NavOptions? = null) {
    this.navigate(SETTINGS_GRAPH_ROUTE_PATTERN, navOptions)
}

fun NavController.navigateToProfileGraph(navOptions: NavOptions? = null) {
    this.navigate(PROFILE_GRAPH_ROUTE_PATTERN, navOptions)
}

fun NavController.navigateToWritePost(navOptions: NavOptions? = null) {
    this.navigate(writePostNavigationRoute, navOptions)
}

fun NavGraphBuilder.homeScreen(
    navController: NavHostController
) {
    composable(
        route = homeNavigationRoute,
        deepLinks = listOf(
            navDeepLink { uriPattern = DEEP_LINK_URI_PATTERN },
        ),
        arguments = listOf(
            navArgument(FIRST_LOG_IN) { defaultValue = "0" }
        ),
    ) {
        val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
        HomeRoute(
            sharedViewModel = sharedViewModel,
            onWritePostRequest = {
                navController.navigateToWritePost()
            },
            onNavigateToProfile = { userId ->
                navController.navigateToProfile(userId)
            },
            onNavigateToPost = { postId ->
                navController.navigateToPostDetail(postId)
            },
            onNavigateToNotifications = {
                navController.navigateToNotifications()
            }
        )
    }
}

fun NavGraphBuilder.createScreen() {
    composable(
        route = createNavigationRoute,
    ) {
        CreateRoute()
    }
}

fun NavGraphBuilder.writePostScreen(
    navController: NavHostController
) {
    composable(
        route = writePostNavigationRoute,
    ) {
        val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
        WritePostRoute(
            sharedViewModel = sharedViewModel,
            onNavUp = { navController.popBackStack() }
        )
    }
}

fun NavGraphBuilder.webPageScreen(onBackClick: () -> Unit) {
    composable(
        route = webPageNavigationRoute,
        arguments = listOf(
            navArgument("url") {
                type = NavType.StringType
                defaultValue = Constant.LANDING_URL
            }
        ),
    ) { backStackEntry ->
        val url = backStackEntry.arguments?.getString("url") ?: Constant.LANDING_URL
        WebPageRoute(
            url = url,
            onNavUp = onBackClick,
        )
    }
}

fun NavGraphBuilder.settingsScreen(
    onBackClick: () -> Unit,
    openWebPage: (url: String) -> Unit,
) {
    composable(
        route = settingsNavigationRoute,
    ) {
        SettingsRoute(
            onNavUp = onBackClick,
            onOpenWebPage = openWebPage,
        )
    }
}

fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    onBackClick: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit = {},
) {
    navigation(
        route = HOME_GRAPH_ROUTE_PATTERN,
        startDestination = homeNavigationRoute,
    ) {
        composable(
            route = homeNavigationRoute,
            /* TODO: add deep links and other args here */
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
            HomeRoute(
                sharedViewModel = sharedViewModel,
                onWritePostRequest = {
                    navController.navigateToWritePost()
                },
                onNavigateToProfile = { userId ->
                    navController.navigateToProfile(userId)
                },
                onNavigateToPost = { postId ->
                    navController.navigateToPostDetail(postId)
                },
                onNavigateToNotifications = {
                    navController.navigateToNotifications()
                }
            )
        }
        composable(
            route = writePostNavigationRoute,
            /* TODO: add deep links and other args here */
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
            WritePostRoute(
                sharedViewModel = sharedViewModel,
                onNavUp = { navController.popBackStack() }
            )
        }

        composable(
            route = postDetailNavigationRoute,
            /* TODO: add deep links and other args here */
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
            PostDetailRoute(
                sharedViewModel = sharedViewModel,
                onNavUp = { navController.popBackStack() },
                onNavigateToProfile = { userId ->
                    navController.navigateToProfile(userId)
                }
            )
        }

        composable(
            route = notificationsNavigationRoute,
            /* TODO: add deep links and other args here */
        ) {
            NotificationRoute(
                onNavigateToPost = { postId ->
                    navController.navigateToPostDetail(postId)
                },
                onNavigateToProfile = { userId ->
                    navController.navigateToProfile(userId)
                },
                onNavUp = { navController.popBackStack() },
            )

        }
        nestedGraphs()
    }
}

fun NavGraphBuilder.profileGraph(
    startDestination: String = profileNavigationRoute,
    navController: NavController,
    onBackClick: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit = {},
) {
    navigation(
        route = PROFILE_GRAPH_ROUTE_PATTERN,
        startDestination = startDestination,
    ) {
        composable(
            route = profileNavigationRoute,
            arguments = listOf(
                navArgument(USER_ID_ARG) { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "seller://profile/{$USER_ID_ARG}"
            })
        ) {
            ProfileRoute(
                onOptionSettingsRequest = {
                    navController.navigateToSettings()
                }
            )
        }

        nestedGraphs()
    }
}

fun NavGraphBuilder.searchGraph(
    startDestination: String = searchNavigationRoute,
    navController: NavHostController,
    onBackClick: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit = {},
) {
    navigation(
        route = SEARCH_GRAPH_ROUTE_PATTERN,
        startDestination = startDestination,
    ) {
        composable(
            route = searchNavigationRoute,
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
            SearchRoute(
                sharedViewModel = sharedViewModel,
                onNavUp = onBackClick,
                onNavigateToProfile = { userId ->
                    navController.navigateToProfile(userId)
                },
                onNavigateToPostDetail = { postId ->
                    navController.navigateToPostDetail(postId)
                }
            )
        }

        nestedGraphs()
    }
}

fun NavGraphBuilder.settingsGraph(
    onBackClick: () -> Unit,
    onOpenWebPage: (url: String) -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit = {},
) {
    navigation(
        route = SETTINGS_GRAPH_ROUTE_PATTERN,
        startDestination = settingsNavigationRoute,
    ) {
        composable(
            route = settingsNavigationRoute,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }
        ) {
            SettingsRoute(
                onNavUp = onBackClick,
                onOpenWebPage = onOpenWebPage,
            )
        }
        nestedGraphs()
    }
}

fun NavController.navigateToSampleScreen(title: String, navOptions: NavOptions? = null) {
    this.navigate("sample?title={title}".replace("{title}", title), navOptions)
}
