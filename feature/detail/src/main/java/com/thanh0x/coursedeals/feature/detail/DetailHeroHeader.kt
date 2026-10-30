package com.thanh0x.coursedeals.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thanh0x.coursedeals.core.ui.R as CoreR

@Composable
fun DetailHeroHeader(
    previewImage: String?,
    title: String,
    timeLeftText: String?,
    isBookmarked: Boolean,
    onBackClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.78f)
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(previewImage)
                .crossfade(true)
                .build(),
            error = painterResource(CoreR.drawable.error_loading_image),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        DetailHeroTopActions(
            onBackClick = onBackClick,
            isBookmarked = isBookmarked,
            onBookmarkToggle = onBookmarkToggle,
            onShareClick = onShareClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        timeLeftText?.takeIf { it.isNotBlank() }?.let { timeText ->
            ExpiryBadge(timeText = timeText, modifier = Modifier.align(Alignment.BottomEnd))
        }
    }
}

@Composable
private fun DetailHeroTopActions(
    onBackClick: () -> Unit,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonColors = IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        IconButton(
            onClick = onBackClick,
            colors = buttonColors,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(CircleShape),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
            )
        }
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            IconButton(
                onClick = onBookmarkToggle,
                colors = buttonColors,
                modifier = Modifier
                    .padding(end = 48.dp)
                    .clip(CircleShape),
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(
                onClick = onShareClick,
                colors = buttonColors,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                )
            }
        }
    }
}

@Composable
private fun ExpiryBadge(
    timeText: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(12.dp)
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = timeText,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
