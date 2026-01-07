package jnu.kulipai.exam.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jnu.kulipai.exam.R

@Composable
fun FolderCard(
    name: String = "[object Object]",
    onFolderClick: (String) -> Unit // 修改为回调函数
) {
    Card(
        onClick = { onFolderClick(name) }, // 调用回调
        shape = CircleShape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 6.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp, 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.folder_24px),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
            Text(
                name,
                modifier = Modifier.padding(12.dp, 0.dp),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
