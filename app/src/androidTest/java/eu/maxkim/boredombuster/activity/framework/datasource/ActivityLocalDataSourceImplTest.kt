package eu.maxkim.boredombuster.activity.framework.datasource

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import eu.maxkim.boredombuster.activity.framework.db.ActivityDao
import eu.maxkim.boredombuster.activity.model.Activity
import eu.maxkim.boredombuster.activity.ui.favorite.FavoritesUiState
import eu.maxkim.boredombuster.framework.AppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.io.IOException
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ActivityLocalDataSourceImplTest{
    private lateinit var activityDao: ActivityDao
    private lateinit var database: AppDatabase

    @get:Rule
    val instant = InstantTaskExecutorRule()//делает лайвдату синхронной

    private val activityObserver: Observer<List<Activity>> = mock()//тестовый обсервер

    @Captor
    private lateinit var activityListCaptor: ArgumentCaptor<List<Activity>> //используется вместе с обсервером, механизм захвата результата
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .build()
        activityDao = database.activityDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun canSaveActivityToTheDbAndReadIt() = runTest {
        val activityLocalDataSourceImpl = ActivityLocalDataSourceImpl(activityDao)
        activityLocalDataSourceImpl.saveActivity(androidActivity1)
        assert(activityLocalDataSourceImpl.isActivitySaved(androidActivity1.key))
    }

    @Test
    fun canDeleteActivityFromTheDb() = runTest {
        val activityLocalDataSourceImpl = ActivityLocalDataSourceImpl(activityDao)
        activityLocalDataSourceImpl.saveActivity(androidActivity1)
        activityLocalDataSourceImpl.deleteActivity(androidActivity1)
        assert(!activityLocalDataSourceImpl.isActivitySaved(androidActivity1.key))
    }

    @Test
    fun canSaveActivityToTheDbAndObserveTheLiveData() = runTest {
        val activityLocalDataSourceImpl = ActivityLocalDataSourceImpl(activityDao)
        val expectedList = listOf(androidActivity1, androidActivity2)

        activityLocalDataSourceImpl.getActivityListLiveData().observeForever(activityObserver)//на что реагируем, подписка один вызов
        activityLocalDataSourceImpl.saveActivity(androidActivity1)//второй
        activityLocalDataSourceImpl.saveActivity(androidActivity2)//третий

        verify(activityObserver, times(3)).onChanged(activityListCaptor.capture())//ожидается 3 раза вызова onChanged у обсервера
        //capture - отлов livedata в activityListCaptor

        assertEquals(activityListCaptor.value, expectedList)
    }
}

val androidActivity1 = Activity(
    name = "Learn to dance",
    type = Activity.Type.Recreational,
    participantCount = 2,
    price = 0.1f,
    accessibility = 0.2f,
    key = "112233",
    link = "www.dance.com"
)

val androidActivity2 = Activity(
    name = "Pet a dog",
    type = Activity.Type.Relaxation,
    participantCount = 1,
    price = 0.0f,
    accessibility = 0.1f,
    key = "223344",
    link = "www.dog.com"
)