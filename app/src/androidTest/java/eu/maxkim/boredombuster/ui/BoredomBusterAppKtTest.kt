package eu.maxkim.boredombuster.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eu.maxkim.boredombuster.R
import eu.maxkim.boredombuster.activity.model.Activity
import eu.maxkim.boredombuster.activity.ui.newactivity.Tags
import eu.maxkim.boredombuster.framework.AppDatabase
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class BoredomBusterAppKtTest{
    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var database: AppDatabase

    @Before
    fun init(){
        hiltRule.inject()
    }

    @After
    fun tearDown(){
        mockWebServer.shutdown()
        database.close()
    }

    @Test
    fun refreshingSavingAndDeletingWorksCorrectly() {
        enqueueActivityResponse(successfulAndroidResponse1)
        waitUntilVisibleWithText(responseAndroidActivity1.name)

        enqueueActivityResponse(successfulAndroidResponse2)
        refreshActivity()
        waitUntilVisibleWithText(responseAndroidActivity2.name)

        saveAsFavorite()
        navigateToFavorites()

        composeTestRule.onNodeWithText(responseAndroidActivity2.name).assertIsDisplayed()
        deleteFromFavorites()

        composeTestRule.onNodeWithText(responseAndroidActivity2.name).assertDoesNotExist()

        val noActivitiesMessage = ApplicationProvider.getApplicationContext<Context>().getString(R.string.message_empty_activity_list)

        composeTestRule.onNodeWithText(noActivitiesMessage).assertIsDisplayed()
    }

    private fun waitUntilVisibleWithText(text: String){//ждет пока хотя бы один узел с таким текстом не появится
        composeTestRule.waitUntil {
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().size == 1
        }
    }

    private fun clickOnNodeWithContentDescription(@StringRes cdRes: Int) {
        val contentDescription = ApplicationProvider.getApplicationContext<Context>()
            .getString(cdRes)

        composeTestRule.onNodeWithContentDescription(contentDescription)
            .performClick()
    }

    private fun saveAsFavorite() {
        clickOnNodeWithContentDescription(R.string.cd_save_activity)
    }

    private fun deleteFromFavorites() {
        clickOnNodeWithContentDescription(R.string.cd_delete_activity)
    }

    private fun refreshActivity() {
        clickOnNodeWithContentDescription(R.string.cd_refresh_activity)
    }

    private fun navigateToFavorites() {
        composeTestRule.onNodeWithTag(Tags.FavoritesTab)
            .performClick()
    }

    private fun navigateToActivity() {
        composeTestRule.onNodeWithTag(Tags.ActivityTab)
            .performClick()
    }

    private fun enqueueActivityResponse(activityJson: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(activityJson)
        )
    }

}

val successfulAndroidResponse1 = """
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

val successfulAndroidResponse2 = """
    {
      "activity": "Learn how to use a french press",
      "type": "recreational",
      "participants": 1,
      "price": 0.3,
      "link": "https://en.wikipedia.org/wiki/French_press",
      "key": "4522866",
      "accessibility": 0.3
    }
""".trimIndent()

val responseAndroidActivity1 = Activity(
    name = "Go to a music festival with some friends",
    type = Activity.Type.Social,
    participantCount = 4,
    price = 0.4f,
    accessibility = 0.2f,
    key = "6482790",
    link = ""
)

val responseAndroidActivity2 = Activity(
    name = "Learn how to use a french press",
    type = Activity.Type.Recreational,
    participantCount = 1,
    price = 0.3f,
    accessibility = 0.3f,
    key = "4522866",
    link = "https://en.wikipedia.org/wiki/French_press"
)