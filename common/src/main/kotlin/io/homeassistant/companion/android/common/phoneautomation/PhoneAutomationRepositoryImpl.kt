package io.homeassistant.companion.android.common.phoneautomation

import io.homeassistant.companion.android.database.phoneautomation.PhoneAutomationDao
import io.homeassistant.companion.android.database.phoneautomation.PhoneAutomationEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import timber.log.Timber

private val phoneAutomationJson = Json { ignoreUnknownKeys = true }

internal class PhoneAutomationRepositoryImpl @Inject constructor(
    private val dao: PhoneAutomationDao,
) : PhoneAutomationRepository {

    override fun getAllFlow(): Flow<List<PhoneAutomation>> =
        dao.getAllFlow().map { entities -> entities.mapNotNull { it.toDomain() } }

    override suspend fun getEnabled(): List<PhoneAutomation> =
        dao.getEnabled().mapNotNull { it.toDomain() }

    override suspend fun getById(id: Long): PhoneAutomation? =
        dao.getById(id)?.toDomain()

    override suspend fun insert(automation: PhoneAutomation): Long =
        dao.insert(automation.toEntity())

    override suspend fun update(automation: PhoneAutomation) =
        dao.update(automation.toEntity())

    override suspend fun delete(id: Long) {
        val entity = dao.getById(id) ?: return
        dao.delete(entity)
    }

    private fun PhoneAutomationEntity.toDomain(): PhoneAutomation? = try {
        PhoneAutomation(
            id = id,
            name = name,
            isEnabled = isEnabled,
            trigger = phoneAutomationJson.decodeFromString<PhoneAutomationTrigger>(triggerJson),
            action = phoneAutomationJson.decodeFromString<PhoneAutomationAction>(actionJson),
        )
    } catch (e: Exception) {
        Timber.w(e, "Failed to deserialize phone automation entity id=$id")
        null
    }

    private fun PhoneAutomation.toEntity() = PhoneAutomationEntity(
        id = id,
        name = name,
        isEnabled = isEnabled,
        triggerJson = phoneAutomationJson.encodeToString<PhoneAutomationTrigger>(trigger),
        actionJson = phoneAutomationJson.encodeToString<PhoneAutomationAction>(action),
    )
}
