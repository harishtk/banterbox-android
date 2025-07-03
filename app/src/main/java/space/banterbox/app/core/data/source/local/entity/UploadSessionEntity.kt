package space.banterbox.app.core.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import space.banterbox.app.core.data.source.local.AppDatabase
import space.banterbox.app.core.domain.model.UploadSession

@Entity(tableName = UploadSessionTable.name)
data class UploadSessionEntity(
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "status")
    val status: Int,
    @ColumnInfo(name = "folder_name")
    val folderName: String,
    @ColumnInfo(name = "training_type")
    val trainingType: String
) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var _id: Long? = null
}

object UploadSessionTable {
    const val name: String = AppDatabase.TABLE_UPLOAD_SESSION

    object Columns {
        const val CREATED_AT            = "created_at"
        const val STATUS                = "status"
        const val ID                    = "id"
        const val FOLDER_NAME           = "folder_name"
        const val TRAINING_TYPE         = "training_type"
    }
}

fun UploadSessionEntity.toUploadSession(): UploadSession {
    return UploadSession(
        id = _id,
        createdAt = createdAt,
        status = UploadSessionStatus.fromRawValue(status),
        folderName = folderName,
        trainingType = trainingType,
    )
}

enum class UploadSessionStatus(val status: Int) {
    NOT_STARTED(0), PARTIALLY_DONE(1), UPLOAD_COMPLETE(2),
    FAILED(5), UNKNOWN(-1);

    companion object {
        fun fromRawValue(rawValue: Int): UploadSessionStatus {
            return when (rawValue) {
                0 -> NOT_STARTED
                1 -> PARTIALLY_DONE
                2 -> UPLOAD_COMPLETE
                5 -> FAILED
                else -> UNKNOWN
            }
        }
    }
}