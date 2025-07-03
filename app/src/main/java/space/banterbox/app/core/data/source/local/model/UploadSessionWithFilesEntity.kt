package space.banterbox.app.core.data.source.local.model

import androidx.room.Embedded
import androidx.room.Relation
import space.banterbox.app.core.data.source.local.entity.UploadFilesEntity
import space.banterbox.app.core.data.source.local.entity.UploadFilesTable
import space.banterbox.app.core.data.source.local.entity.UploadSessionEntity
import space.banterbox.app.core.data.source.local.entity.UploadSessionTable
import space.banterbox.app.core.data.source.local.entity.toUploadFile
import space.banterbox.app.core.data.source.local.entity.toUploadSession
import space.banterbox.app.core.domain.model.UploadSessionWithFiles

data class UploadSessionWithFilesEntity(
    @Embedded val uploadSessionEntity: UploadSessionEntity,
    @Relation(
        parentColumn = UploadSessionTable.Columns.ID,
        entityColumn = UploadFilesTable.Columns.SESSION_ID
    )
    val uploadFilesEntity: List<UploadFilesEntity>
)

fun UploadSessionWithFilesEntity.toUploadSessionWithFiles(): UploadSessionWithFiles {
    return UploadSessionWithFiles(
        uploadSession = uploadSessionEntity.toUploadSession(),
        uploadFiles = uploadFilesEntity.map { it.toUploadFile() }
    )
}