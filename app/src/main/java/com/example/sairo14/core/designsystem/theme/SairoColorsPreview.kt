package com.example.sairo14.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sairo14.core.designsystem.token.SairoColor

private data class ColorPreviewItem(
    val name: String,
    val color: Color,
)

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun SairoColorsPreview() {
    SairoTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    Text(
                        text = "Sairo Colors",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                item {
                    ColorPreviewSection(
                        title = "Gray palette",
                        colors = listOf(
                            ColorPreviewItem("Gray 50", SairoColor.Gray50),
                            ColorPreviewItem("Gray 100", SairoColor.Gray100),
                            ColorPreviewItem("Gray 300", SairoColor.Gray300),
                            ColorPreviewItem("Gray 500", SairoColor.Gray500),
                            ColorPreviewItem("Gray 700", SairoColor.Gray700),
                            ColorPreviewItem("Gray 900", SairoColor.Gray900),
                        ),
                    )
                }
                item {
                    ColorPreviewSection(
                        title = "Green and lime palette",
                        colors = listOf(
                            ColorPreviewItem("Green 50", SairoColor.Green50),
                            ColorPreviewItem("Green 500", SairoColor.Green500),
                            ColorPreviewItem("Green 900", SairoColor.Green900),
                            ColorPreviewItem("Lime 100", SairoColor.Lime100),
                            ColorPreviewItem("Lime 300", SairoColor.Lime300),
                            ColorPreviewItem("Lime 900", SairoColor.Lime900),
                        ),
                    )
                }
                item {
                    val colors = SairoTheme.colors
                    ColorPreviewSection(
                        title = "Sairo semantic colors",
                        colors = listOf(
                            ColorPreviewItem("Background canvas", colors.backgroundCanvas),
                            ColorPreviewItem("Surface sunken", colors.surfaceSunken),
                            ColorPreviewItem("Text primary", colors.textPrimary),
                            ColorPreviewItem("Action default", colors.actionDefault),
                            ColorPreviewItem("Chip lime background", colors.chipLimeBackground),
                            ColorPreviewItem("Chip lime text", colors.chipLimeText),
                            ColorPreviewItem("Warning base", colors.warningBase),
                            ColorPreviewItem("Selection ring", colors.selectionRing),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorPreviewSection(
    title: String,
    colors: List<ColorPreviewItem>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        colors.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(item.color),
                )
                Column {
                    Text(text = item.name, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = item.color.toArgbHex(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun Color.toArgbHex(): String = "#%08X".format(toArgb().toLong() and 0xFFFFFFFFL)
