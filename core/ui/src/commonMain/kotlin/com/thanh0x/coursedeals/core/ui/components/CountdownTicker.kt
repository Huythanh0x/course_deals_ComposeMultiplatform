package com.thanh0x.coursedeals.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanh0x.coursedeals.core.common.SystemTimeProvider
import com.thanh0x.coursedeals.core.common.TimeProvider
import kotlinx.coroutines.delay

/**
 * Animated coupon countdown ticker composable with urgency coloring and pulsing effects.
 *
 * Ticker automatically refreshes every minute (or every second when in high-urgency < 1h mode),
 * self-contained inside the composable to prevent unnecessary parent recompositions.
 *
 * - "> 24h": "X days left" (Normal / Calm theme)
 * - "< 24h": "XXh XXm remaining" (Warning state, pulses gently)
 * - "< 1h": "Expiring soon: XXm" (High-urgency badge with pulsing flame / warning icon)
 */
@Composable
fun CountdownTicker(
    expiredDate: Long?,
    modifier: Modifier = Modifier,
    timeProvider: TimeProvider = SystemTimeProvider,
    fontSize: TextUnit = 10.sp,
    showIcon: Boolean = true,
) {
    var countdownState by remember(expiredDate) {
        mutableStateOf(CountdownFormatter.formatExpiry(expiredDate, timeProvider))
    }

    LaunchedEffect(expiredDate, timeProvider) {
        while (true) {
            val updated = CountdownFormatter.formatExpiry(expiredDate, timeProvider)
            countdownState = updated
            if (updated.isExpired || updated.isUnknown) {
                break
            }
            // Update interval: every 1s when under 1 minute or under 1 hour for real-time smoothness,
            // or every 30s when under 24 hours, or every 60s when > 24 hours
            val delayInterval = when {
                updated.remainingMillis < 60_000L -> 1_000L
                updated.remainingMillis < 3_600_000L -> 10_000L
                updated.remainingMillis < 86_400_000L -> 30_000L
                else -> 60_000L
            }
            delay(delayInterval)
        }
    }

    if (countdownState.isUnknown) {
        return
    }

    UrgencyBadge(
        countdownState = countdownState,
        modifier = modifier,
        fontSize = fontSize,
        showIcon = showIcon,
    )
}

/**
 * Urgency badge visual presentation based on [CountdownState].
 */
@Composable
fun UrgencyBadge(
    countdownState: CountdownState,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 10.sp,
    showIcon: Boolean = true,
    shapeRadius: Dp = 8.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "UrgencyPulseTransition")

    // Pulsing alpha for warning & critical urgency
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = when (countdownState.urgency) {
            ExpiryUrgency.CRITICAL -> 0.70f
            ExpiryUrgency.WARNING -> 0.85f
            ExpiryUrgency.NORMAL -> 1.0f
        },
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (countdownState.urgency == ExpiryUrgency.CRITICAL) 800 else 1400,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "BadgePulseAlpha",
    )

    val (containerColor, contentColor) = when {
        countdownState.isExpired -> {
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        }
        countdownState.urgency == ExpiryUrgency.CRITICAL -> {
            MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        }
        countdownState.urgency == ExpiryUrgency.WARNING -> {
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        }
        else -> {
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        }
    }

    val iconVector = when {
        countdownState.isExpired -> Icons.Default.Schedule
        countdownState.urgency == ExpiryUrgency.CRITICAL -> Icons.Default.LocalFireDepartment
        countdownState.urgency == ExpiryUrgency.WARNING -> Icons.Default.Timer
        else -> Icons.Default.Schedule
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = containerColor,
        animationSpec = tween(durationMillis = 300),
        label = "ContainerColorAnim",
    )

    val iconSize = (fontSize.value + 2).dp

    Box(
        modifier = modifier
            .alpha(if (countdownState.urgency != ExpiryUrgency.NORMAL) pulseAlpha else 1.0f)
            .clip(RoundedCornerShape(shapeRadius))
            .background(color = animatedContainerColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showIcon) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize),
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = countdownState.displayText,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}
