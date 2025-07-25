package space.banterbox.app.core.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import space.banterbox.app.core.data.source.local.entity.UploadFilesEntity
import space.banterbox.app.core.data.source.local.entity.UploadFilesTable
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadFilesDao {

    @Query("SELECT * FROM ${UploadFilesTable.name} WHERE ${UploadFilesTable.Columns.SESSION_ID} = :sessionId")
    fun getAllUploadFiles(sessionId: Long): Flow<List<UploadFilesEntity>>

    @Query("SELECT * FROM ${UploadFilesTable.name} WHERE ${UploadFilesTable.Columns.SESSION_ID} = :sessionId")
    suspend fun getAllUploadFilesSync(sessionId: Long): List<UploadFilesEntity>

    @Query("SELECT * FROM ${UploadFilesTable.name} " +
            "WHERE ${UploadFilesTable.Columns.SESSION_ID} = :sessionId AND " +
            "${UploadFilesTable.Columns.LOCAL_URI_STRING} = :uriString")
    suspend fun getDeviceFileForUriSync(sessionId: Long, uriString: String): UploadFilesEntity?

    @Query("UPDATE ${UploadFilesTable.name} " +
            "SET ${UploadFilesTable.Columns.STATUS} = :status " +
            "WHERE ${UploadFilesTable.Columns.ID} = :id")
    suspend fun updateFileStatus(id: Long, status: Int): Int

    @Query("UPDATE ${UploadFilesTable.name} " +
            "SET ${UploadFilesTable.Columns.PROGRESS} = :progress " +
            "WHERE ${UploadFilesTable.Columns.ID} = :id")
    suspend fun updateFileUploadProgress(id: Long, progress: Int)

    @Query("UPDATE ${UploadFilesTable.name} " +
                "SET ${UploadFilesTable.Columns.UPLOADED_FILENAME} = :uploadedFileName, " +
                "${UploadFilesTable.Columns.UPLOADED_AT} = :uploadedAt " +
                "WHERE ${UploadFilesTable.Columns.ID} = :id")
    suspend fun updateUploadedFileName(id: Long, uploadedFileName: String, uploadedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(uploadFilesEntity: UploadFilesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(uploadFilesEntities: List<UploadFilesEntity>)

    @Query("DELETE FROM ${UploadFilesTable.name} WHERE ${UploadFilesTable.Columns.ID} = :id")
    suspend fun deleteFile(id: Long)

}