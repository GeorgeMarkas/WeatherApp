package io.github.georgemarkas.weatherapp.ui.weather.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ForecastRow(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit
) {
    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        content = content
    )
}

data class ForecastTab(
    val name: String,
    val content: @Composable () -> Unit
)

@Composable
fun ForecastBox(
    tabs: List<ForecastTab>,
    title: String
) {

    // TODO: REPLACE MANUAL DP VALUES
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // TODO: REPLACE MANUAL DP VALUES
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
            // TODO: REPLACE MANUAL DP VALUES
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp,)
            ) {
                tabs.forEachIndexed { index, tab ->
                    SegmentedButton(
                        icon = {},
                        selected = index == selectedIndex,
                        onClick = { selectedIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = tabs.size
                        ),
                        label = {
                            Text(
                                text = tab.name,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
            tabs.forEachIndexed { index, tab ->
                if (index == selectedIndex) {
                    tab.content()
                }
            }
        }
    }
}



