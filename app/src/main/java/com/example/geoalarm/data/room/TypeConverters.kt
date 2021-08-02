package com.example.geoalarm.data.room

import androidx.room.TypeConverter
import com.example.geoalarm.data.AlarmType
import com.google.android.libraries.maps.model.LatLng
import java.util.*

class TypeConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun latLngToString(location: LatLng?): String?{
        return "${location?.latitude},${location?.longitude}"
    }

    @TypeConverter
    fun fromLatLngString(string: String?): LatLng?{

        if (string == null) return null

        val latlong = string.split(",").toTypedArray()
        val latitude = latlong[0].toDouble()
        val longitude = latlong[1].toDouble()

        return LatLng(latitude, longitude)
    }

    @TypeConverter
    fun alarmTypeToInt(type:AlarmType?): Int? {
        return type?.ordinal
    }

    @TypeConverter
    fun fromOrdinal(num:Int?): AlarmType? {

        return num?.let{ AlarmType.values()[it] } ?: null
    }
}