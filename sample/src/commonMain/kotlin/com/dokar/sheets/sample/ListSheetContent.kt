package com.dokar.sheets.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ListSheetContent(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Header(
            text = "List",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        LazyColumn {
            items(count = 20) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {}
                        .padding(16.dp),
                ) {
                    Text("Item $it")
                }
            }
        }
    }
}