package com.itbenevides.genesys21.screenshot

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import org.junit.Rule
import org.junit.Test

class EditorComparisonSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    @Test
    fun testEditorComparisonMockupResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            Column(Modifier.padding(16.dp)) {
                Text("Editor Comparison Showcase")
                Spacer(Modifier.height(16.dp))

                ComparisonRow("Header Component")
                Spacer(Modifier.height(16.dp))
                ComparisonRow("Text Component")
                Spacer(Modifier.height(16.dp))
                ComparisonRow("Button Component")
            }
        }
    }
}

@Composable
private fun ComparisonRow(label: String) {
    Column {
        Text(label)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("EDITOR UI")
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("RESULT")
            }
        }
    }
}
