package jnu.kulipai.exam.ui.screens.about

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItem(
    modifier: Modifier = Modifier,
    title: String,
    painter: Painter? = null,
    onClick: () -> Unit = { },
    onLongClick: (() -> Unit)? = null,
    subtitle: String? = null,
    enabled: Boolean = true,
    action: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(32.dp)
) {
    val height = if (subtitle != null) 65.dp else 55.dp

    val titleStyle = MaterialTheme.typography.titleLarge.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 20.sp
    )
    val subtitleTextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
    )

    Box(
        modifier.padding(vertical = 4.dp, horizontal = 12.dp),
    )
    {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .clip(shape)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick,
                enabled = enabled
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (painter != null) {
            Icon(
                painter = painter,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(24.dp).clip(CircleShape),
                contentDescription = null,
            )
        }
        Column(
            Modifier
                .padding(horizontal = if (painter != null) 0.dp else 16.dp)
                .weight(1f),
        ) {
            Text(
                text = title,
                style = titleStyle,
                color = titleStyle.color.copy(alpha = if (enabled) 1f else 0.6f)
            )
            if (subtitle != null) {
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = subtitle,
                    style = subtitleTextStyle,
                    color = subtitleTextStyle.color.copy(alpha = 0.75f),
                )
            }
        }
        if (action != null) {
            Box(
                Modifier
                    .widthIn(min = 56.dp)
                    .padding(horizontal = 12.dp),
            ) {
                action()
            }
        }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyPersonItem(
    modifier: Modifier = Modifier,
    title: String,
    painter: Painter? = null,
    onClick: () -> Unit = { },
    onLongClick: (() -> Unit)? = null,
    subtitle: String? = null,
    enabled: Boolean = true,
    action: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(32.dp)
) {
    val height = if (subtitle != null) 65.dp else 55.dp

    val titleStyle = MaterialTheme.typography.titleLarge.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 20.sp
    )
    val subtitleTextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
    )

    Box(
        modifier.padding(vertical = 4.dp, horizontal = 12.dp),
    )
    {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = height)
                .clip(shape)
                .combinedClickable(
                    onLongClick = onLongClick,
                    onClick = onClick,
                    enabled = enabled
                )
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (painter != null) {
                Image(

                    painter = painter,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .size(32.dp).clip(CircleShape),
                    contentDescription = null,
                )
            }
            Column(
                Modifier
                    .padding(horizontal = if (painter != null) 0.dp else 16.dp)
                    .weight(1f),
            ) {
                Text(
                    text = title,
                    style = titleStyle,
                    color = titleStyle.color.copy(alpha = if (enabled) 1f else 0.6f)
                )
                if (subtitle != null) {
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = subtitle,
                        style = subtitleTextStyle,
                        color = subtitleTextStyle.color.copy(alpha = 0.75f),
                    )
                }
            }
            if (action != null) {
                Box(
                    Modifier
                        .widthIn(min = 56.dp)
                        .padding(horizontal = 12.dp),
                ) {
                    action()
                }
            }
        }
    }
}