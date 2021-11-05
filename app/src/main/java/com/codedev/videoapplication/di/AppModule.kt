package com.codedev.videoapplication.di

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.codedev.videoapplication.data.remote_api.VideoApi
import com.codedev.videoapplication.data.repository.VideoRepository
import com.codedev.videoapplication.data.repository.VideoRepositoryImpl
import com.codedev.videoapplication.data.utils.API_KEY
import com.codedev.videoapplication.data.utils.BASE_URL
import com.codedev.videoapplication.domain.usecases.GetVideo
import com.codedev.videoapplication.domain.usecases.SearchVideo
import com.codedev.videoapplication.domain.usecases.VideoUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import kotlin.reflect.KProperty

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofitInstance(): VideoApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request: Request =
                    chain.request().newBuilder().addHeader("Authorization", API_KEY).build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(VideoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(videoApi: VideoApi): VideoRepository {
        return VideoRepositoryImpl(videoApi)
    }

    @Provides
    @Singleton
    fun provideUseCases(videoRepository: VideoRepository): VideoUseCases {
        return VideoUseCases(
            searchVideo = SearchVideo(videoRepository),
            getVideo = GetVideo(videoRepository)
        )
    }
}