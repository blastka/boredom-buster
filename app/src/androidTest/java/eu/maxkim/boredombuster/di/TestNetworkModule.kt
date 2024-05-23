package eu.maxkim.boredombuster.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import eu.maxkim.boredombuster.activity.framework.api.ActivityApiClient
import eu.maxkim.boredombuster.activity.framework.api.ActivityTypeAdapter
import eu.maxkim.boredombuster.di.annotation.BaseUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)

class TestNetworkModule {

    @Provides
    @Singleton
    @BaseUrl
    fun provideBaseUrl(mockWebServer: MockWebServer): String {
        var url = ""
        val thread = Thread {
            url = mockWebServer.url("/").toString()//сетевая операция, для хилта нужно переключение потоков:
        }
        thread.start()
        thread.join()
        return url
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient{
        return OkHttpClient.Builder().build()
    }

    @Provides
    @Singleton
    fun provideMockServer(): MockWebServer{
        return MockWebServer()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, @BaseUrl baseUrl: String): Retrofit {
        val moshi: Moshi = Moshi.Builder().add(ActivityTypeAdapter()).build()
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .baseUrl(baseUrl)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiClient(retrofit: Retrofit): ActivityApiClient {
        return retrofit.create(ActivityApiClient::class.java)
    }
}