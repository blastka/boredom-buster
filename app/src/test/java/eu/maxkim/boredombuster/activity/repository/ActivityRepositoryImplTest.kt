package eu.maxkim.boredombuster.activity.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eu.maxkim.boredombuster.activity.model.Activity
import eu.maxkim.boredombuster.activity.ui.newactivity.activity1
import eu.maxkim.boredombuster.model.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityRepositoryImplTest{

    @Test
    fun `getNewActivity() returns a result after switching the context`() = runTest {
        // Arrange
        val activityRepository = ActivityRepositoryImpl(
            appScope = this,
            ioDispatcher = StandardTestDispatcher(testScheduler),
            remoteDataSource = FakeActivityRemoteDataSource(),
            localDataSource = FakeActivityLocalDataSource()
        )

        val expectedActivity = activity1

        // Act
        val result = activityRepository.getNewActivity()

        // Assert
        assert(result is Result.Success)
        assertEquals((result as Result.Success).data, expectedActivity)
    }

    @Test
    fun `getNewActivityInANewCoroutine correctly calls remote data source`() = runTest {
        // Arrange
        val fakeRemoteRepository = FakeActivityRemoteDataSource()
        val activityRepository = ActivityRepositoryImpl(
            appScope = this,
            ioDispatcher = StandardTestDispatcher(testScheduler),
            remoteDataSource = fakeRemoteRepository,
            localDataSource = FakeActivityLocalDataSource()
        )

        // Act
        activityRepository.getNewActivityInANewCoroutine()
        advanceUntilIdle()//будет увеличивать виртуальное время до тех пор, пока больше не останется задач.

        // Assert
        assert(fakeRemoteRepository.getActivityWasCalled)
    }



}

class FakeActivityLocalDataSource : ActivityLocalDataSource {

    override suspend fun saveActivity(activity: Activity) {
        // Save
    }

    override suspend fun deleteActivity(activity: Activity) {
        // Delete
    }

    override suspend fun isActivitySaved(key: String): Boolean {
        return false
    }

    override fun getActivityListLiveData(): LiveData<List<Activity>> {
        return MutableLiveData()
    }
}

class FakeActivityRemoteDataSource : ActivityRemoteDataSource {

    var getActivityWasCalled = false
        private set

    override suspend fun getActivity(): Result<Activity> {
        getActivityWasCalled = true
        return Result.Success(activity1)
    }
}

