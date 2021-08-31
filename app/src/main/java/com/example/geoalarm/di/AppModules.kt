package com.example.geoalarm.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.example.geoalarm.GeoFenceBroadcastReceiver
import com.example.geoalarm.data.room.AlarmsDao
import com.example.geoalarm.data.room.GeoAlarmDatabase
import com.example.geoalarm.repository.AlarmsRepository
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesDB(@ApplicationContext appContext: Context): AlarmsDao {
        return Room.databaseBuilder(
            appContext.applicationContext,
            GeoAlarmDatabase::class.java,
            "geo_alarm_database"
        )
            .fallbackToDestructiveMigration()
            .build()
            .alarmsDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object GeoFencingModule {

    @Provides
    @Singleton
    fun provideGeoFencingIntent(@ApplicationContext appContext: Context) : PendingIntent {
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(appContext, GeoFenceBroadcastReceiver::class.java)

            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofences().

            PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        return geofencePendingIntent
    }

    @Provides
    @Singleton
    fun provideGeoFencingClient(@ApplicationContext appContext: Context) : GeofencingClient {
        return LocationServices.getGeofencingClient(appContext)
    }

}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRepository(alarmsDao: AlarmsDao) : AlarmsRepository {
        return AlarmsRepository(alarmsDao)
    }

}
