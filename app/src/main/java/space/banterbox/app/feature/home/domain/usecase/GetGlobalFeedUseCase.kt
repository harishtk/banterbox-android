package space.banterbox.app.feature.home.domain.usecase

import space.banterbox.app.common.util.paging.PagedRequest
import space.banterbox.app.feature.home.domain.model.PostsWithUsers
import space.banterbox.app.feature.home.domain.repository.PostRepository
import javax.inject.Inject

class GetGlobalFeedUseCase @Inject constructor(
    private val postRepository: PostRepository,
) {
    suspend operator fun invoke(pagedRequest: PagedRequest<Int>): PostsWithUsers {
        return PostsWithUsers(emptyList(), emptyList(), null)
    }
}