package com.turnzero.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.theme.ColorTheme

@Composable
fun ThemePreviewTile(
    theme: ColorTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderModifier = if (isSelected) {
        Modifier.border(2.dp, theme.primary, RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    Column(modifier = modifier.width(140.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .then(borderModifier)
                .clip(RoundedCornerShape(16.dp))
                .background(theme.surface)
                .clickable(onClick = onClick)
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                MiniTopBar(theme)
                Spacer(Modifier.height(6.dp))
                MiniTile(theme)
                Spacer(Modifier.weight(1f))
                MiniBottomBar(theme)
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(theme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = theme.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun MiniTopBar(theme: ColorTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(theme.surfaceContainer, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(theme.onSurface.copy(alpha = 0.4f), CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .background(theme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun MiniTile(theme: ColorTheme) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.surfaceContainer, RoundedCornerShape(6.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(theme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(theme.primary)
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(6.dp)
                .background(theme.onSurface.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(theme.primary.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun MiniBottomBar(theme: ColorTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .background(theme.surfaceContainer, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(theme.onSurface.copy(alpha = 0.3f))
            )
        }
    }
}
