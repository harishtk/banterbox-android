package space.banterbox.app.feature.home.presentation.landing

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import space.banterbox.app.R
import space.banterbox.app.feature.home.domain.model.Post
import space.banterbox.app.feature.home.domain.model.UserSummary
import space.banterbox.app.feature.home.presentation.profile.FullScreenErrorLayout
import space.banterbox.app.ui.spacerSizeTiny
import space.banterbox.app.ui.theme.BanterboxTheme

@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val feedUiState by viewModel.feedUiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        feedUiState = feedUiState,
        uiAction = viewModel.accept
    )
}

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    feedUiState: FeedUiState = FeedUiState.Idle,
    uiAction: (HomeUiAction) -> Unit = {},
) {

    // This code should be called when UI is ready for use and relates to Time To Full Display.
    ReportDrawnWhen { true /* Add custom conditions here. eg. !isSyncing */ }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Vertical
                    )
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    // .verticalScroll(rememberScrollState())
                    .imePadding(),
            ) {
                when (feedUiState) {
                    FeedUiState.Idle -> {}
                    FeedUiState.Loading -> {
                        LoadingScreen()
                    }
                    is FeedUiState.Error -> {
                        FullScreenErrorLayout(
                            errorMessage = feedUiState.errorMessage,
                            onClick = { uiAction(HomeUiAction.Refresh) }
                        )
                    }
                    is FeedUiState.Success -> {
                        FeedContent(
                            feeds = feedUiState,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedContent(
    modifier: Modifier = Modifier,
    feeds: FeedUiState.Success,
) {
    val userMap = remember(feeds.users) {
        feeds.users.associateBy { it.id }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
    ) {
        Text(text = "Hoots", style = MaterialTheme.typography.headlineMedium)

        LazyColumn {
            items(feeds.posts) { post ->
                val author = userMap[post.authorId]!!

                PostCard(
                    post = post,
                    author = author,
                )
            }
            item { Spacer(Modifier.height(spacerSizeTiny)) }
        }
    }
}

@Composable
private fun PostCard(
    modifier: Modifier = Modifier,
    post: Post,
    author: UserSummary,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Info Header
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = CenterVertically) {
                    UserAvatar(
                        modifier = Modifier.size(36.dp),
                        profile = author
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = author.displayName, style = MaterialTheme.typography.titleSmall)
                        Text(text = "@${author.username}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                IconButton(onClick = { /* TODO: Handle more options click */ }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                }
            }

            // Post Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Actions (e.g., Like button) - Simplified
            Icon(Icons.Filled.FavoriteBorder, contentDescription = "Like")
        }
    }
}

@Composable
private fun UserAvatar(
    modifier: Modifier = Modifier,
    profile: UserSummary,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!profile.profilePictureId.isBlank()) {
            // In a real app, you'd use a library like Coil or Glide here
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background), // Placeholder
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val initials = profile.displayName.split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
            Text(
                text = initials.ifEmpty { "?" },
                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
            )
        }
    }
}

@Composable
private fun LoadingScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Preview
@Composable
private fun HomeDefaultPreview() {
    Box(
        Modifier.background(Color.White)
    ) {
        BanterboxTheme {
            HomeScreen(
                feedUiState = FeedUiState.Success(
                    posts = listOf(
                        Post(
                            id = "1",
                            authorId = "1",
                            content = "This is a sample post content.",
                            createdAt = "2023-09-10T12:00:00Z",
                            updatedAt = "2023-09-10T12:00:00Z",
                            likesCount = 10,
                            likedByCurrentUser = true
                        ),
                    ),
                    users = listOf(
                        UserSummary(
                            id = "1",
                            username = "john_doe",
                            displayName = "John Doe",
                            profilePictureId = ""
                        )
                    )
                )
            )
        }
    }
}

@Preview
@Composable
private fun FeedContentPreview() {
    BanterboxTheme {
        PostCard(
            post = Post(
                id = "1",
                authorId = "1",
                content = "This is a sample post content.",
                createdAt = "2023-09-10T12:00:00Z",
                updatedAt = "2023-09-10T12:00:00Z",
                likesCount = 10,
                likedByCurrentUser = true
            ),
            author = UserSummary(
                id = "1",
                username = "john_doe",
                displayName = "John Doe",
                profilePictureId = ""
            )
        )
    }
}

@Preview
@Composable
private fun LoadingScreenPreview() {
    BanterboxTheme {
        LoadingScreen()
    }
}