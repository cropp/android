package io.homeassistant.companion.android.database.phoneautomation

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhoneAutomationDao {

    @Query("SELECT * FROM phone_automations ORDER BY id ASC")
    fun getAllFlow(): Flow<List<PhoneAutomationEntity>>

    @Query("SELECT * FROM phone_automations WHERE is_enabled = 1 ORDER BY id ASC")
    suspend fun getEnabled(): List<PhoneAutomationEntity>

    @Query("SELECT * FROM phone_automations WHERE id = :id")
    suspend fun getById(id: Long): PhoneAutomationEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PhoneAutomationEntity): Long

    @Update
    suspend fun update(entity: PhoneAutomationEntity)

    @Delete
    suspend fun delete(entity: PhoneAutomationEntity)
}
