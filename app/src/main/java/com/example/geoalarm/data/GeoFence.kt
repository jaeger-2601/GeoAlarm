package com.example.geoalarm.data

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.android.libraries.maps.model.LatLng
import java.util.*

@Entity(
    tableName = "geofences",
    indices = [
        Index("id", unique = true)
    ]
)
@Immutable
data class GeoFence (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "location") val location: LatLng,
    @ColumnInfo(name = "radius") val radius: Int,
    @ColumnInfo(name = "type") val type: AlarmType,
    @ColumnInfo(name = "created_at") val created_at: Date
)