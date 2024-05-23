package eu.maxkim.boredombuster.activity.ui.newactivity

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.test.core.app.ApplicationProvider
import eu.maxkim.boredombuster.R
import eu.maxkim.boredombuster.activity.framework.datasource.androidActivity1
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.text.Typography.times

class NewActivityScreenKtTest{
    @get:Rule
    val composeTestRule = createComposeRule()//для контента на экране

    @Test
    fun activityNameDisplayedOnACard() {
        composeTestRule.setContent {//отображаем
            NewActivityCard(
                modifier = Modifier.fillMaxWidth(),
                activity = androidActivity1,
                isFavorite = false,
                onFavoriteClick = { },
                onLinkClick = { }
            )
        }
        composeTestRule.onRoot().printToLog(tag = "SEMANTICS TREE")//выводит дерево семантики

        composeTestRule.onNodeWithText(androidActivity1.name).assertIsDisplayed()//проверка отображения на экране
    }

    @Test
    fun onFavoriteClickCallbackIsTriggered() {
        val onFavoriteClick: (isFavorite: Boolean) -> Unit = mock()//мокаем для отслеживания ее вызова
        val isFavorite = false

        composeTestRule.setContent {//отображаем
            NewActivityCard(
                modifier = Modifier.fillMaxWidth(),
                activity = androidActivity1,
                isFavorite = false,
                onFavoriteClick = onFavoriteClick,//описание содержимого
                onLinkClick = { }
            )
        }

        val contentDescription = ApplicationProvider.getApplicationContext<Context>().getString(R.string.cd_save_activity)//получили контекст и стрингу
        composeTestRule.onNodeWithContentDescription(contentDescription).performClick()//кликаем по элементу с этим содержимым, то есть строкой в contentDescription компоуза
        verify(onFavoriteClick, times(1)).invoke(!isFavorite)
    }
}