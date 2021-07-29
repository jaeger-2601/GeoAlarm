package com.example.geoalarm.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.geoalarm.data.Alarm

@Database(
    entities = [
        Alarm::class
    ],
    version = 1,
    exportSchema = false
)
@androidx.room.TypeConverters(TypeConverters::class)
abstract class GeoAlarmDatabase : RoomDatabase() {
    abstract fun alarmsDao() : AlarmsDao

    companion object {

        @Volatile
        private var INSTANCE: GeoAlarmDatabase? = null

        fun getInstance(context: Context): GeoAlarmDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        GeoAlarmDatabase::class.java,
                        "geo_alarm_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}