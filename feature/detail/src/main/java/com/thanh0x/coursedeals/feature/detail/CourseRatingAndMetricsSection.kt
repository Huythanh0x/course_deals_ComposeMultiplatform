package com.thanh0x.coursedeals.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanh0x.coursedeals.core.ui.theme.RatingAmber
import com.thanh0x.coursedeals.core.ui.util.MapperToView
import com.thanh0x.coursedeals.domain.coupons.Coupon

@Composable
fun CourseRatingAndMetricsSection(
    coupon: Coupon,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapper = remember(context) { MapperToView(context) }
    val rating = coupon.rating ?: 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        RatingStarsRow(rating = rating, mapper = mapper)
        Spacer(modifier = Modifier.height(10.dp))
        MetricsRow(coupon = coupon, mapper = mapper)
    }
}

@Composable
private fun RatingStarsRow(
    rating: Double,
    mapper: MapperToView,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = mapper.mapRatingValue(rating),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RatingAmber,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            for (i in 1..5) {
                val starTint = if (rating >= i - 0.25) RatingAmber else MaterialTheme.colorScheme.outlineVariant
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = starTint,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun MetricsRow(
    coupon: Coupon,
    mapper: MapperToView,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MetricItemCard(
            icon = Icons.Default.Group,
            value = mapper.mapNumberOfStudent(coupon.students),
            label = "Students",
            modifier = Modifier.weight(1f),
        )
        MetricItemCard(
            icon = Icons.Default.ChatBubbleOutline,
            value = mapper.mapNumberOfReview(coupon.reviews),
            label = "Reviews",
            modifier = Modifier.weight(1f),
        )
        coupon.contentLength?.let { length ->
            MetricItemCard(
                icon = Icons.Default.Schedule,
                value = mapper.mapContentLength(length),
                label = "Duration",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetricItemCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
