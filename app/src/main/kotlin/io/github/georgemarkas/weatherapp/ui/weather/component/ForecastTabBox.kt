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
import io.github.georgemarkas.weatherapp.ui.theme.dimens

@Composable
fun ForecastRow(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit
) {
    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.forecastRowContentsHArrangement),
        contentPadding = PaddingValues(horizontal = MaterialTheme.dimens.forecastRowContentsHPadding),
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

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.forecastBoxBordersHorizontal)
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.dimens.forecastBoxColumnPadding)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    start = MaterialTheme.dimens.spacing4,
                    top = MaterialTheme.dimens.spacing3,
                    bottom = MaterialTheme.dimens.spacing1)
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .padding(
                        horizontal = MaterialTheme.dimens.spacing3,
                        vertical = MaterialTheme.dimens.spacing2)
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



