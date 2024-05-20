package eu.maxkim.boredombuster.activity.ui.newactivity

import eu.maxkim.boredombuster.activity.model.Activity
import eu.maxkim.boredombuster.activity.usecase.DeleteActivity
import eu.maxkim.boredombuster.activity.usecase.GetRandomActivity
import eu.maxkim.boredombuster.activity.usecase.IsActivitySaved
import eu.maxkim.boredombuster.activity.usecase.SaveActivity
import eu.maxkim.boredombuster.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.Exception
import java.lang.RuntimeException

@OptIn(ExperimentalCoroutinesApi::class)
class NewActivityViewModelTest {

    @get:Rule
    val coroutinesTestRules = CoroutinesTestRules()

    @Test
    fun `после создания viewModel начальное значение uiState равно NewActivityUiState Loading`() {
        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(),
            FakeSaveActivity(),
            FakeDeleteActivity(),
            FakeIsActivitySaved()
        )
        assert(viewModel.uiState.value is NewActivityUiState.Loading)
    }

    @Test
    fun `после создания экземпляра NewActivityViewModel состояние ui равно NewActivityUiState Success`() {
        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(),
            FakeSaveActivity(),
            FakeDeleteActivity(),
            FakeIsActivitySaved()
        )
        val expectedUiState = NewActivityUiState.Success(activity1, false)
        coroutinesTestRules.testDispatcher.scheduler.runCurrent() //завершение всех сопрограмм
        val currentState = viewModel.uiState.value
        assertEquals(expectedUiState, currentState)
    }

    @Test
    fun `после создания viewModel состояние ui корректно обновляется до NewActivityUiState Error`() {
        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(false),
            FakeSaveActivity(),
            FakeDeleteActivity(),
            FakeIsActivitySaved()
        )
        coroutinesTestRules.testDispatcher.scheduler.runCurrent()//завершение всех сопрограмм

        val currentState = viewModel.uiState.value
        assert(currentState is NewActivityUiState.Error)
    }

    @Test
    fun `если действие уже сохранено, для isFavorite флага состояния пользовательского интерфейса установлено значение true`() {
        // Arrange
        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(), // our fake will return an error
            FakeSaveActivity(),
            FakeDeleteActivity(),
            FakeIsActivitySaved(isActivitySaved = true)
        )

        val expectedUiState = NewActivityUiState.Success(activity1, true)

        // Act
        coroutinesTestRules.testDispatcher.scheduler.runCurrent()//завершение всех сопрограмм

        // Assert
        val actualState = viewModel.uiState.value
        assertEquals(actualState, expectedUiState)
    }

    @Test
    fun `calling loadNewActivity() updates ui state with a new activity`() {
        // Arrange
        val fakeGetRandomActivity = FakeGetRandomActivity()
        val viewModel = NewActivityViewModel(
            fakeGetRandomActivity,
            FakeSaveActivity(),
            FakeDeleteActivity(),
            FakeIsActivitySaved()
        )
        coroutinesTestRules.testDispatcher.scheduler.runCurrent()//завершение всех сопрограмм

        val expectedResult = NewActivityUiState.Success(activity2, false)
        fakeGetRandomActivity.activity = activity2

        viewModel.loadNewActivity()
        coroutinesTestRules.testDispatcher.scheduler.runCurrent()//завершение всех сопрограмм

        val actualResult = viewModel.uiState.value
        assertEquals(actualResult, expectedResult)
    }

    @Test
    fun `calling setIsFavorite(true) triggers SaveActivity use case`() {
        val fakeSaveActivity = FakeSaveActivity()
        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(),
            fakeSaveActivity,
            FakeDeleteActivity(),
            FakeIsActivitySaved()
        )

        viewModel.setIsFavorite(activity1, true)
        coroutinesTestRules.testDispatcher.scheduler.runCurrent()//завершение всех сопрограмм

        assert(fakeSaveActivity.wasCalled)
    }

    @Test
    fun `calling setIsFavorite(false) triggers DeleteActivity use case`() {
        // Arrange
        val fakeDeleteActivity = FakeDeleteActivity()
        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(),
            FakeSaveActivity(),
            fakeDeleteActivity,
            FakeIsActivitySaved()
        )

        viewModel.setIsFavorite(activity1, false)

        coroutinesTestRules.testDispatcher.scheduler.runCurrent()//завершение всех сопрограмм

        assert(fakeDeleteActivity.wasCalled)
    }
}

class FakeGetRandomActivity(val isSuccessful: Boolean = true) : GetRandomActivity {

    var activity: Activity? = null
    override suspend fun invoke(): Result<Activity> {
        return if (isSuccessful)
            Result.Success(activity ?: activity1)
        else
            Result.Error(RuntimeException("error"))
    }
}

class FakeIsActivitySaved(val isActivitySaved: Boolean = false) : IsActivitySaved {

    override suspend fun invoke(key: String): Boolean {
        return isActivitySaved
    }

}

class FakeSaveActivity : SaveActivity {

    var wasCalled = false
        private set

    override suspend fun invoke(activity: Activity) {
        wasCalled = true
    }
}

class FakeDeleteActivity : DeleteActivity {
    var wasCalled = false
        private set
    override suspend fun invoke(activity: Activity) {
        wasCalled = true
    }
}


val activity1 = Activity(
    name = "Learn to dance",
    type = Activity.Type.Recreational,
    participantCount = 2,
    price = 0.1f,
    accessibility = 0.2f,
    key = "112233",
    link = "www.dance.com"
)

val activity2 = Activity(
    name = "Pet a dog",
    type = Activity.Type.Relaxation,
    participantCount = 1,
    price = 0.0f,
    accessibility = 0.1f,
    key = "223344",
    link = "www.dog.com"
)
