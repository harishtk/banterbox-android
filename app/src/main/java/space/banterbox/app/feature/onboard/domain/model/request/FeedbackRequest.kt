package space.banterbox.app.feature.onboard.domain.model.request

data class FeedbackRequest(
    val rating: String,
    val tags: String,
    val comment: String
)