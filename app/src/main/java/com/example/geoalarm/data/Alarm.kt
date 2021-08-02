package com.example.geoalarm.data

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.android.libraries.maps.model.LatLng
import java.util.Date

enum class AlarmType{
    ON_ENTRY,
    ON_EXIT
}

@Entity(
    tableName = "alarms",
    indices = [
        Index("id", unique = true)
    ]
)
@Immutable
data class Alarm (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "location") val location: LatLng,
    @ColumnInfo(name = "radius") val radius: Int,
    @ColumnInfo(name = "type") val type: AlarmType,
    @ColumnInfo(name = "is_active") val is_active: Boolean,
    @ColumnInfo(name = "created_at") val created_at: Date
)