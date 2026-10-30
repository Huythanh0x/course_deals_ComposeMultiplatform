package com.thanh0x.coursedeals.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thanh0x.coursedeals.core.ui.R as CoreR
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
    val context = LocalContext.current
    val mapper = remember(context) { MapperToView(context) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Preview Image + Time Left badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.78f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(coupon.previewImage)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(CoreR.drawable.progress_animation),
                    error = painterResource(CoreR.drawable.error_loading_image),
                    contentDescription = coupon.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )

                // Time left badge
                val timeLeftText = mapper.mapTimeLeft(coupon.expiredTime)
                if (timeLeftText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = timeLeftText,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Course Info Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                // Title (2 lines max)
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

                // Author (1 line max)
                Text(
                    text = coupon.author ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Category
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

                // Rating (Text + Stars)
                val ratingValue = coupon.rating ?: 0.0
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = mapper.mapRatingValue(ratingValue),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RatingAmber,
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // 5-star mini indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        for (i in 1..5) {
                            val starTint = if (ratingValue >= i - 0.25) {
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

                Spacer(modifier = Modifier.height(2.dp))

                // Students & Reviews count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp),
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = mapper.mapNumberOfStudent(coupon.students),
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
                        text = mapper.mapNumberOfReview(coupon.reviews),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
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
                expiredTime = System.currentTimeMillis() + 86400000L * 3,
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
