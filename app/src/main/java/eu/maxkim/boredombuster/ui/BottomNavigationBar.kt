package eu.maxkim.boredombuster.ui

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import eu.maxkim.boredombuster.activity.ui.newactivity.Tags
import eu.maxkim.boredombuster.model.Destination

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    currentDestination: Destination,
    onNavigate: (destination: Destination) -> Unit
) {
    BottomNavigation(
        modifier = modifier
    ) {
        listOf(
            Destination.Activity,
            Destination.Favorites
        ).forEach { destination ->
            val testTag = when (destination) {
                Destination.Activity -> Tags.ActivityTab
                Destination.Favorites -> Tags.FavoritesTab
                else -> ""
            }
            BottomNavigationItem(
                modifier = modifier.testTag(testTag),
                selected = currentDestination.path == destination.path,
                icon = {
                    destination.icon?.let { image ->
                        Icon(
                            imageVector = image,
                            contentDescription = destination.path
                        )
                    }
                },
                onClick = {
                    onNavigate(destination)
                },
                label = {
                    Text(text = destination.title)
                }
            )
        }
    }
}