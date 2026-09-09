package com.redforge.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.redforge.app.ui.components.EmberMascot
import com.redforge.app.ui.components.EmberSpeechBubble
import com.redforge.app.ui.components.ForgeButton

private data class TutorialStep(val title: String, val body: String)

private val steps = listOf(
    TutorialStep(
        "Hey, I'm Ember!",
        "I'll show you around RedForge in a few quick steps. Nothing here ever leaves your phone — it's just you, me, and your gains."
    ),
    TutorialStep(
        "Build your split",
        "Create a split like Push/Pull/Legs, add training days, then pick exercises for each — with images, links, sets, reps and rest time. Change it anytime."
    ),
    TutorialStep(
        "Log your workout",
        "Start today's workout and log each set as you go. Everything saves instantly, so even if your phone locks or the app closes, your progress is safe."
    ),
    TutorialStep(
        "Rest timer, your way",
        "Fire up the rest timer between sets — it keeps running in a notification even if you leave the app, with sound and vibration."
    ),
    TutorialStep(
        "Track it scientifically",
        "RedForge estimates your 1-rep max, tracks training volume, and shows trends — plus progress photos and body measurements over time."
    ),
    TutorialStep(
        "Keep your streak alive",
        "Stick to the plan you built and watch your streak grow. Share your day, month, or year as a clean result card whenever you're proud of it."
    )
)

@Composable
fun TutorialScreen(onDone: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val step = steps[index]
    val isLast = index == steps.lastIndex

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            steps.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .weight(1f)
                        .clip(CircleShape)
                        .background(
                            if (i <= index) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        Spacer(Modifier.height(40.dp))
        EmberMascot(size = 96.dp)
        Spacer(Modifier.height(28.dp))

        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(180)) },
            label = "tutorial_step"
        ) { current ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(current.title, style = MaterialTheme.typography.headlineMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(
                    current.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isLast) {
                OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) {
                    Text("Skip")
                }
            }
            ForgeButton(
                text = if (isLast) "Let's train" else "Next",
                onClick = {
                    if (isLast) onDone() else index++
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
