package eu.maxkim.boredombuster.activity.framework.datasource

import com.squareup.moshi.Moshi
import eu.maxkim.boredombuster.activity.framework.api.ActivityApiClient
import eu.maxkim.boredombuster.activity.framework.api.ActivityTypeAdapter
import eu.maxkim.boredombuster.activity.model.Activity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import eu.maxkim.boredombuster.model.Result
import retrofit2.HttpException

@ExperimentalCoroutinesApi
class ActivityRemoteDataSourceImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiClient: ActivityApiClient

    private val client = OkHttpClient.Builder().build()

    private val moshi: Moshi = Moshi.Builder()
        .add(ActivityTypeAdapter())
        .build()

    @Before
    fun createServer() {
        mockWebServer = MockWebServer()

        apiClient = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/")) // setting a dummy url
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
            .create(ActivityApiClient::class.java)
    }

    @After
    fun shutdownServer() {
        mockWebServer.shutdown()
    }

    @Test
    fun `correct response is parsed into success result`() = runTest {
        // Arrange
        val response = MockResponse()
            .setBody(successfulResponse)//xml
            .setResponseCode(200)//ответ

        mockWebServer.enqueue(response)//добавление ответа в очередь

        val activityRemoteDataSource = ActivityRemoteDataSourceImpl(apiClient)
        val expectedActivity = responseActivity//что ожидаем в итоге какой распарсенный класс

        // Act
        val result = activityRemoteDataSource.getActivity()//запрос

        // Assert
        assert(result is Result.Success)
        assertEquals((result as Result.Success).data, expectedActivity)
    }

    @Test
    fun `error response returns http error result`() = runTest {
        // Arrange
        val response = MockResponse()
            .setBody(errorResponse)
            .setResponseCode(400)

        mockWebServer.enqueue(response)

        val activityRemoteDataSource = ActivityRemoteDataSourceImpl(apiClient)

        // Act
        val result = activityRemoteDataSource.getActivity()

        // Assert
        assert(result is Result.Error)
        assert((result as Result.Error).error is HttpException)
    }


}


val responseActivity = Activity(
    name = "Go to a music festival with some friends",
    type = Activity.Type.Social,
    participantCount = 4,
    price = 0.4f,
    accessibility = 0.2f,
    key = "6482790",
    link = ""
)

val successfulResponse = """
    {
      "activity": "Go to a music festival with some friends",
      "type": "social",
      "participants": 4,
      "price": 0.4,
      "link": "",
      "key": "6482790",
      "accessibility": 0.2
    }
""".trimIndent()

val errorResponse = "I am not a json :o"