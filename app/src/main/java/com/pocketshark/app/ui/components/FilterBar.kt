package com.pocketshark.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pocketshark.app.ui.theme.PocketSharkColors
import com.pocketshark.app.ui.theme.PocketSharkTypography

/**
 * Glass-styled search/filter input bar.
 */
@Composable
fun FilterBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Filter packets…"
) {
    val shape = RoundedCornerShape(14.dp)

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PocketSharkColors.GlassBg, shape)
            .border(1.dp, PocketSharkColors.GlassBorder, shape),
        placeholder = {
            Text(
                text = placeholder,
                style = PocketSharkTypography.bodyMedium,
                color = PocketSharkColors.TextTertiary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = PocketSharkColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        },
        singleLine = true,
        textStyle = PocketSharkTypography.bodyMedium.copy(color = PocketSharkColors.TextPrimary),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = PocketSharkColors.AccentEmerald,
        )
    )
}

/**
 * Selectable glass filter chip for protocol filtering.
 */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = PocketSharkColors.AccentEmerald
) {
    val shape = RoundedCornerShape(10.dp)
    val bgColor = if (selected) color.copy(alpha = 0.2f) else PocketSharkColors.GlassBgLight
    val borderColor = if (selected) color.copy(alpha = 0.4f) else PocketSharkColors.GlassBorder
    val textColor = if (selected) color else PocketSharkColors.TextSecondary

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor, shape)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = PocketSharkTypography.labelMedium,
            color = textColor
        )
    }
}
