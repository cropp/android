package io.homeassistant.companion.android.common.phoneautomation

import kotlinx.coroutines.flow.Flow

/**
 * Provides CRUD access to user-defined [PhoneAutomation] rules.
 *
 * The repository is the single source of truth for automations stored on the device.
 */
interface PhoneAutomationRepository {

    /** Emits the full list of automations, updating whenever any automation changes. */
    fun getAllFlow(): Flow<List<PhoneAutomation>>

    /** Returns all currently enabled automations. */
    suspend fun getEnabled(): List<PhoneAutomation>

    /** Returns the automation with the given [id], or null if not found. */
    suspend fun getById(id: Long): PhoneAutomation?

    /**
     * Persists a new automation.
     *
     * @return The generated ID for the newly created automation.
     */
    suspend fun insert(automation: PhoneAutomation): Long

    /** Replaces the stored automation that shares the same [PhoneAutomation.id]. */
    suspend fun update(automation: PhoneAutomation)

    /** Permanently removes the given automation. */
    suspend fun delete(id: Long)
}
