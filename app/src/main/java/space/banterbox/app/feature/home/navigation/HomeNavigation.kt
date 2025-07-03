package space.banterbox.app.feature.home.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import space.banterbox.app.Constant
import space.banterbox.app.feature.home.presentation.admin.AdminRoute
import space.banterbox.app.feature.home.presentation.create.CreateRoute
import space.banterbox.app.feature.home.presentation.insights.InsightsRoute
import space.banterbox.app.feature.home.presentation.inventory.InventoryRoute
import space.banterbox.app.feature.home.presentation.landing.HomeRoute
import space.banterbox.app.feature.home.presentation.settings.SettingsRoute
import space.banterbox.app.feature.home.presentation.webview.WebPageRoute

const val HOME_GRAPH_ROUTE_PATTERN = "home_graph"
const val SETTINGS_GRAPH_ROUTE_PATTERN = "settings_graph"

const val FIRST_LOG_IN = "firstLogin"

const val homeNavigationRoute = "home_route?${FIRST_LOG_IN}={${FIRST_LOG_IN}}"
const val insightsNavigationRoute = "insights_route"
const val insightsOverviewNavigationRoute = "insights_overview_route"
const val createNavigationRoute = "create_route"
const val inventoryNavigationRoute = "inventory_route"
const val adminNavigationRoute = "admin_route"

const val webPageNavigationRoute = "web_page_route?url={url}"
const val settingsNavigationRoute = "settings_route"

const val maintenanceNavigationRoute = "maintenance_route"

private const val DEEP_LINK_URI_PATTERN =
    "https://www.shopsseller.cc/home"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(homeNavigationRoute, navOptions)
}

fun NavController.navigateToInsights(navOptions: NavOptions? = null) {
    this.navigate(insightsNavigationRoute, navOptions)
}

fun NavController.navigateToCreate(navOptions: NavOptions? = null) {
    this.navigate(createNavigationRoute, navOptions)
}

fun NavController.navigateToInventory(navOptions: NavOptions? = null) {
    this.navigate(inventoryNavigationRoute, navOptions)
}

fun NavController.navigateToAdmin(navOptions: NavOptions? = null) {
    this.navigate(adminNavigationRoute, navOptions)
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

fun NavGraphBuilder.homeScreen() {
    composable(
        route = homeNavigationRoute,
        deepLinks = listOf(
            navDeepLink { uriPattern = DEEP_LINK_URI_PATTERN },
        ),
        arguments = listOf(
            navArgument(FIRST_LOG_IN) { defaultValue = "0" }
        ),
    ) {
        HomeRoute()
    }
}

fun NavGraphBuilder.insightsScreen() {
    composable(
        route = insightsNavigationRoute,
    ) {
        InsightsRoute()
    }
}

fun NavGraphBuilder.createScreen() {
    composable(
        route = createNavigationRoute,
    ) {
        CreateRoute()
    }
}

fun NavGraphBuilder.inventoryScreen() {
    composable(
        route = inventoryNavigationRoute,
    ) {
        InventoryRoute()
    }
}

fun NavGraphBuilder.adminScreen() {
    composable(
        route = adminNavigationRoute,
    ) {
        AdminRoute()
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
    navController: NavController,
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
            HomeRoute()
        }
        composable(
            route = createNavigationRoute,
            /* TODO: add deep links and other args here */
        ) {
            CreateRoute()
        }
        composable(
            route = inventoryNavigationRoute,
        ) {
            InventoryRoute()
        }
        composable(
            route = adminNavigationRoute,
            /* TODO: add deep links and other args here */
        ) {
            AdminRoute()
        }
        nestedGraphs()
    }
}

fun NavGraphBuilder.insightsGraph(
    startDestination: String = insightsOverviewNavigationRoute,
    navController: NavController,
    onBackClick: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit = {},
) {
    navigation(
        route = insightsNavigationRoute,
        startDestination = startDestination,
    ) {
        composable(
            route = insightsOverviewNavigationRoute,
            deepLinks = listOf(navDeepLink { uriPattern = "seller://insights" })
        ) {
            InsightsRoute()
        }
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
