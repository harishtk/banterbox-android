package space.banterbox.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import space.banterbox.app.feature.home.navigation.HOME_GRAPH_ROUTE_PATTERN
import space.banterbox.app.feature.home.navigation.homeGraph
import space.banterbox.app.feature.home.navigation.insightsGraph
import space.banterbox.app.feature.home.navigation.navigateToWebPage
import space.banterbox.app.feature.home.navigation.settingsGraph
import space.banterbox.app.feature.home.navigation.webPageNavigationRoute
import space.banterbox.app.feature.home.navigation.webPageScreen
import space.banterbox.app.feature.home.presentation.miscellaneous.SampleRoute
import space.banterbox.app.feature.onboard.navigation.authGraph
import space.banterbox.app.feature.onboard.navigation.onboardGraph
import space.banterbox.app.ui.SellerAppState
import timber.log.Timber

@Composable
fun ShopsNavHost(
    appState: SellerAppState,
    onShowSnackBar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    startGraph: String = HOME_GRAPH_ROUTE_PATTERN,
    startDestination: String = "",
) {
    Timber.d("ShopsNavHost() called with: appState = [$appState], onShowSnackBar = [$onShowSnackBar], modifier = [$modifier], startDestination = [$startDestination]")
    val navController = appState.navController

    NavHost(
        navController = navController,
        startDestination = startGraph,
        modifier = modifier
    ) {
        // TODO: add navigation items
        homeGraph(
            navController = navController,
            onBackClick = navController::popBackStack,
        ) {
            settingsGraph(
                onBackClick = navController::popBackStack,
                onOpenWebPage = { url ->
                    navController.navigateToWebPage(url)
                },
            ) {
                // TODO: add nested graph items
                // TODO: 1. Add web page preview
                webPageScreen(
                    onBackClick = navController::popBackStack
                )
            }
        }

        insightsGraph(
            navController = navController,
            onBackClick = navController::popBackStack
        )

        authGraph(
            navController = navController,
            onBackClick = navController::popBackStack,
            onShowSnackBar = onShowSnackBar,
            onOpenWebPage = { url ->
                navController.navigate(
                    webPageNavigationRoute.replace("{url}", url)
                )
            }
        ) {
            // TODO: -done- add nested graph items
            webPageScreen(
                onBackClick = navController::popBackStack
            )
        }

        onboardGraph(
            navController = navController,
            startDestination = startDestination,
        ) {
            webPageScreen(
                onBackClick = navController::popBackStack
            )
        }

        // TODO: 1. Add maintenance graph
        // TODO: 2. Add force-update graph

        composable(
            route = "sample?title={title}",
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                    defaultValue = "Sample"
                }
            ),
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Sample"
            SampleRoute(title = title)
        }

    }
}