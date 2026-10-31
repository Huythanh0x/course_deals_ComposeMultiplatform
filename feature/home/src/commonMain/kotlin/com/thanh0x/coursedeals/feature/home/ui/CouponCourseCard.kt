package com.thanh0x.coursedeals.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanh0x.coursedeals.core.common.platformCurrentTimeMillis
import com.thanh0x.coursedeals.core.ui.components.CountdownTicker
import com.thanh0x.coursedeals.core.ui.components.NetworkImage
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.painterResource
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import com.thanh0x.coursedeals.core.ui.theme.RatingAmber
import com.thanh0x.coursedeals.core.ui.util.MapperToView
import com.thanh0x.coursedeals.domain.coupons.Coupon

@Composable
fun CouponCourseCard(
    coupon: Coupon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            CouponCardImageHeader(
                previewImage = coupon.previewImage,
                title = coupon.title ?: "",
                expiredTime = coupon.expiredTime,
            )

            CouponCardInfoSection(coupon = coupon)
        }
    }
}

@Composable
private fun CouponCardImageHeader(
    previewImage: String?,
    title: String,
    expiredTime: Long?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.78f)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        NetworkImage(
            url = previewImage,
            error = painterResource(Res.drawable.error_loading_image),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )

        CountdownTicker(
            expiredDate = expiredTime,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 4.dp),
        )
    }
}

@Composable
private fun CouponCardInfoSection(
    coupon: Coupon,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = coupon.title ?: "",
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = coupon.author ?: "",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = coupon.category ?: "",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(2.dp))

        CouponCardRatingBar(rating = coupon.rating ?: 0.0)

        Spacer(modifier = Modifier.height(2.dp))

        CouponCardMetricsRow(
            students = coupon.students ?: 0,
            reviews = coupon.reviews ?: 0,
        )
    }
}

@Composable
private fun CouponCardRatingBar(
    rating: Double,
    modifier: Modifier = Modifier,
) {
    val mapper = remember { MapperToView() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = mapper.mapRatingValue(rating),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RatingAmber,
        )

        Spacer(modifier = Modifier.width(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            for (i in 1..5) {
                val starTint = if (rating >= i - 0.25) {
                    RatingAmber
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = starTint,
                    modifier = Modifier.size(11.dp),
                )
            }
        }
    }
}

@Composable
private fun CouponCardMetricsRow(
    students: Int,
    reviews: Int,
    modifier: Modifier = Modifier,
) {
    val mapper = remember { MapperToView() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(bottom = 6.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp),
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = mapper.mapNumberOfStudent(students),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.width(10.dp))

        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp),
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = mapper.mapNumberOfReview(reviews),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CouponCourseCardPreview() {
    CourseDealsTheme {
        CouponCourseCard(
            coupon = Coupon(
                courseId = 123,
                author = "John Doe",
                category = "Development",
                contentLength = 120,
                couponCode = "FREE",
                couponUrl = "https://udemy.com",
                description = "Master Compose Multiplatform",
                expiredTime = platformCurrentTimeMillis() + 86400000L * 3,
                heading = "Great course",
                language = "English",
                level = "All Levels",
                previewImage = null,
                previewVideo = null,
                rating = 4.8,
                reviews = 1240,
                students = 15420,
                subCategory = "Mobile Development",
                title = "Android Jetpack Compose Multiplatform Masterclass",
                usesRemaining = 100,
                isNew = true,
            ),
            onClick = {},
        )
    }
}
