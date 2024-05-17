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
import java.lang.RuntimeException
@OptIn(ExperimentalCoroutinesApi::class)
class NewActivityViewModelTest{

    @get:Rule
    val coroutinesTestRules = CoroutinesTestRules()
    @Test
    fun `creating a viewmodel exposes loading ui state`() {
        // Arrange
        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(),
            FakeSaveActivity(),
            FakeDeleteActivity(),
            FakeIsActivitySaved()
        )
         assert(viewModel.uiState.value is NewActivityUiState.Loading)
    }

}

class FakeGetRandomActivity(): GetRandomActivity{

    val isSuccessful = true
    override suspend fun invoke(): Result<Activity> {
        return if (isSuccessful)
            Result.Success(activity1)
        else
            Result.Error(RuntimeException("error"))
    }
}

class FakeIsActivitySaved(): IsActivitySaved {
    val isActivitySaved = false
    override suspend fun invoke(key: String): Boolean {
        return isActivitySaved
    }

}

class FakeSaveActivity : SaveActivity {

    override suspend fun invoke(activity: Activity) {
        TODO("Not yet implemented")
    }
}

class FakeDeleteActivity : DeleteActivity {

    override suspend fun invoke(activity: Activity) {
        TODO("Not yet implemented")
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
