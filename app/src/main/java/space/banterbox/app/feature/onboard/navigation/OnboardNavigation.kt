package space.banterbox.app.feature.onboard.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import space.banterbox.app.feature.home.navigation.HOME_GRAPH_ROUTE_PATTERN
import space.banterbox.app.feature.onboard.OnboardSharedViewModel
import space.banterbox.app.feature.onboard.presentation.boarding.AddBankRoute
import space.banterbox.app.feature.onboard.presentation.boarding.AddProductRoute
import space.banterbox.app.feature.onboard.presentation.boarding.AddStoreRoute
import space.banterbox.app.feature.onboard.presentation.boarding.LaunchStoreRoute
import space.banterbox.app.feature.onboard.presentation.login.LoginRoute
import space.banterbox.app.feature.onboard.presentation.login.OtpRoute
import space.banterbox.app.sharedViewModel

const val AUTH_GRAPH_ROUTE_PATTERN = "auth_graph"
const val ONBOARD_GRAPH_ROUTE_PATTERN = "onboard_graph"

const val loginNavigationRoute = "login_route"
const val otpNavigationRoute = "otp_route"
const val onboardSuccessNavigationRoute = "onboard_success_route"

/* Boarding routes */
const val addStoreRoute = "add_store_route"
const val addProductRoute = "add_product_route"
const val addBankAccountRoute = "add_bank_route"
const val launchStoreRoute = "launch_store_route"
/* END - Boarding routes */

fun NavController.navigateToLogin(navOptions: NavOptions? = null) {
    this.navigate(loginNavigationRoute, navOptions)
}

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onBackClick: () -> Unit,
    onShowSnackBar: suspend (String, String?) -> Boolean,
    onOpenWebPage: (url: String) -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit,
) {
    navigation(
        route = AUTH_GRAPH_ROUTE_PATTERN,
        startDestination = loginNavigationRoute,
    ) {
        composable(
            route = loginNavigationRoute,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
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
        ) { entry ->
            val sharedViewModel = entry.sharedViewModel<OnboardSharedViewModel>(navController)
            LoginRoute(
                onNextPage = {
                    navController.navigate(otpNavigationRoute)
                },
                onboardSharedViewModel = sharedViewModel,
                onOpenWebPage = onOpenWebPage
            )
        }
        composable(
            route = otpNavigationRoute,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Left
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        200, easing = LinearEasing
                    )
                )
            }
        ) { entry ->
            val sharedViewModel = entry.sharedViewModel<OnboardSharedViewModel>(navController)
            OtpRoute(
                args = entry.arguments,
                onboardSharedViewModel = sharedViewModel,
                onNavUp = navController::popBackStack
            )
        }
        nestedGraphs()
    }
}

@OptIn(ExperimentalLayoutApi::class)
fun NavGraphBuilder.onboardGraph(
    navController: NavHostController,
    startDestination: String = addStoreRoute,
    nestedGraphs: NavGraphBuilder.() -> Unit,
) {
    navigation(
        route = ONBOARD_GRAPH_ROUTE_PATTERN,
        startDestination = startDestination.ifBlank { addStoreRoute },
    ) {
        composable(
            route = addStoreRoute,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
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
            AddStoreRoute(
                onNextPage = {
                    navController.navigate(addProductRoute) {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(
            route = addProductRoute,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Left
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
            AddProductRoute(
                onNextPage = {
                    navController.navigate(addBankAccountRoute) {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(
            route = addBankAccountRoute,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Left
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
            AddBankRoute(
                onNextPage = {
                    navController.navigate(launchStoreRoute) {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(
            route = launchStoreRoute,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Left
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        200, easing = LinearEasing
                    )
                )
            }
        ) {
            LaunchStoreRoute(
                onNextPage = {
                    navController.navigate(HOME_GRAPH_ROUTE_PATTERN) {
                        popUpTo(0)
                    }
                }
            )
        }
        nestedGraphs()
    }
}



