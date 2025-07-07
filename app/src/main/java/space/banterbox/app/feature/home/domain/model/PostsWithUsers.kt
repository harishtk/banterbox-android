package space.banterbox.app.feature.home.domain.model

data class PostsWithUsers(
    val posts: List<Post>,
    val users: List<UserSummary>,
    val nextPagingKey: Int?,
)
