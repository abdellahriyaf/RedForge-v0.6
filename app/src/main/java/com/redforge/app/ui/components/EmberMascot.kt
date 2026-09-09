package com.redforge.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.redforge.app.ui.theme.ForgeGold
import com.redforge.app.ui.theme.ForgeRed
import com.redforge.app.ui.theme.ForgeRedDark

/**
 * "Ember" — RedForge's small mascot character. A friendly stylized flame
 * with a subtle idle bounce/glow animation, used in onboarding and to
 * deliver short tips/encouragement elsewhere in the app.
 */
@Composable
fun EmberMascot(size: androidx.compose.ui.unit.Dp = 72.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "ember_idle")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .offset(y = bounce.dp)
            .size(size)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.radialGradient(
                    listOf(ForgeRed.copy(alpha = glow), ForgeRedDark.copy(alpha = glow * 0.6f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.FitnessCenter,
            contentDescription = "Ember, your RedForge mascot",
            tint = ForgeGold,
            modifier = Modifier.size(size / 2.2f)
        )
    }
}

/** A speech-bubble style card for Ember's tips, used throughout onboarding. */
@Composable
fun EmberSpeechBubble(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EmberMascot(size = 56.dp)
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(14.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
