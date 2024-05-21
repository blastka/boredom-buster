package eu.maxkim.boredombuster.activity.ui.favorite

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.view.KeyEventDispatcher.Component
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import eu.maxkim.boredombuster.activity.model.Activity
import eu.maxkim.boredombuster.activity.ui.newactivity.CoroutinesTestRules
import eu.maxkim.boredombuster.activity.ui.newactivity.activity1
import eu.maxkim.boredombuster.activity.ui.newactivity.activity2
import eu.maxkim.boredombuster.activity.usecase.DeleteActivity
import eu.maxkim.boredombuster.activity.usecase.GetFavoriteActivities
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class FavoritesViewModelTest {

    @get:Rule
    val instant = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineRule = CoroutinesTestRules()

    private val mockGetFavoriteActivities: GetFavoriteActivities = mock()
    private val mockDeleteActivity: DeleteActivity = mock()


    private val activityObserver: Observer<FavoritesUiState> = mock()

    @Captor
    private lateinit var activityListCaptor: ArgumentCaptor<FavoritesUiState>
    //используется вместе с обсервером, механизм захвата результата

    @Test
    fun `the view model maps list of activities to list ui state`() {
        val livedataReturn = MutableLiveData<List<Activity>>()
        livedataReturn.value = listOf(activity1, activity2)
        val expectedList = listOf(activity1, activity2)

        whenever(mockGetFavoriteActivities.invoke()).doReturn(livedataReturn)
        //при вызове метода invoke у объекта должно возвращаться livedataReturn

        val viewModel = FavoritesViewModel(mockGetFavoriteActivities, mockDeleteActivity)
        viewModel.uiStateLiveData.observeForever(activityObserver)

        verify(activityObserver, times(1)).onChanged(activityListCaptor.capture())
        //отлов изменения livedata

        assert(activityListCaptor.value is FavoritesUiState.List)

        val actualList = (activityListCaptor.value as FavoritesUiState.List).activityList
        assertEquals(expectedList, actualList)
    }

    @Test
    fun `the view model maps empty list of activities to empty ui state`() {
        // Arrange
        val livedataReturn = MutableLiveData<List<Activity>>()
        livedataReturn.value = listOf()

        whenever(mockGetFavoriteActivities.invoke()).doReturn(livedataReturn)
        //при вызове метода invoke у объекта должно возвращаться livedataReturn

        val viewModel = FavoritesViewModel(mockGetFavoriteActivities, mockDeleteActivity)
        viewModel.uiStateLiveData.observeForever(activityObserver)

        verify(activityObserver, times(1)).onChanged(activityListCaptor.capture())
        //отлов изменения livedata, проверка взаимодействия

        assert(activityListCaptor.value is FavoritesUiState.Empty)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `calling deleteActivity() interacts with the correct use case`() {
        runTest {  // потому что есть вызов саспенд метода invoke
            val viewModel = FavoritesViewModel(
                mockGetFavoriteActivities,
                mockDeleteActivity
            )
            // Act
            viewModel.deleteActivity(activity1)
            advanceUntilIdle()//ждем завершения корутин
            verify(mockDeleteActivity, times(1)).invoke(activity1)
            //проверяем вообще был ли вызов deleteActivity.invoke
        }
    }
}

class MockTest() {
    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockComponent: Component
}