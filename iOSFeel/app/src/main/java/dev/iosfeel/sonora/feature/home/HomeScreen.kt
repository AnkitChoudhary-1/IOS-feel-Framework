package dev.iosfeel.sonora.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.sonora.core.design.SonoraTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val colors = SonoraTheme.colors
    val typography = SonoraTheme.typography

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(54.dp))

            Text(
                text = "Listen Now",
                style = typography.largeTitle.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Recently Played Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "RECENTLY PLAYED",
                        style = typography.caption1.copy(
                            color = colors.textTertiary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your Offline Collection",
                        style = typography.title2.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Grant storage permission to index local tracks.",
                        style = typography.body.copy(
                            color = colors.textSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "Recently Added",
                style = typography.title2.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "No tracks scanned yet. Head over to Library to load your music.",
                    style = typography.subhead.copy(
                        color = colors.textSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
