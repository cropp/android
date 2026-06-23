package io.homeassistant.companion.android.database.phoneautomation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phone_automations")
data class PhoneAutomationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "is_enabled", defaultValue = "1")
    val isEnabled: Boolean,
    /** JSON representation of a [io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationTrigger]. */
    @ColumnInfo(name = "trigger_json")
    val triggerJson: String,
    /** JSON representation of a [io.homeassistant.companion.android.common.phoneautomation.PhoneAutomationAction]. */
    @ColumnInfo(name = "action_json")
    val actionJson: String,
)
