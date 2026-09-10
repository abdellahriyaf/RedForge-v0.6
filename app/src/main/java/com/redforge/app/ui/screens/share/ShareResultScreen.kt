@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.redforge.app.ui.screens.share

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.redforge.app.ui.components.ForgeButton
import com.redforge.app.ui.theme.ForgeGold
import com.redforge.app.ui.theme.ForgeGradientEnd
import com.redforge.app.ui.theme.ForgeGradientStart
import com.redforge.app.util.ComposeCapture
import com.redforge.app.util.ShareImageUtil
import com.redforge.app.viewmodel.ShareScope
import com.redforge.app.viewmodel.ShareSummary
import com.redforge.app.viewmodel.ShareTemplate
import com.redforge.app.viewmodel.ShareViewModel
import com.redforge.app.viewmodel.redForgeViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun ShareResultScreen(initialScope: ShareScope) {
    val vm: ShareViewModel = redForgeViewModel { app -> ShareViewModel(app.workoutRepository, app.splitRepository) }
    val summary by vm.summary.collectAsState()
    var scope by remember { mutableStateOf(initialScope) }
    var template by remember { mutableStateOf(ShareTemplate.SUMMARY) }
    val context = LocalContext.current
    LaunchedEffect(scope) { vm.load(scope) }
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Share your result", style = MaterialTheme.typography.headlineMedium)
        Text("Pick a time range and a card style.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShareScope.values().forEach { s -> FilterChip(selected = scope == s, onClick = { scope = s }, label = { Text(s.name.lowercase().replaceFirstChar { it.uppercase() }) }) }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TemplateChip(ShareTemplate.SUMMARY, template) { template = it }
            TemplateChip(ShareTemplate.STREAK, template) { template = it }
            TemplateChip(ShareTemplate.VOLUME, template) { template = it }
        }
        Spacer(Modifier.height(18.dp))
        summary?.let { s -> ResultCard(s, template = template) }
        Spacer(Modifier.height(20.dp))
        ForgeButton(text = "Share as image", onClick = {
            val activity = context as? Activity ?: return@ForgeButton
            val current = summary ?: return@ForgeButton
            ComposeCapture.captureToBitmap(activity = activity, widthDp = 340.dp, heightDp = 420.dp, content = { ResultCard(current, template = template, forCapture = true) }) { bitmap ->
                ShareImageUtil.shareBitmap(context, bitmap, fileName = "redforge_${template.name.lowercase()}_${scope.name.lowercase()}.png")
            }
        }, modifier = Modifier.fillMaxWidth(0.82f))
    }
}

@Composable
private fun TemplateChip(value: ShareTemplate, selected: ShareTemplate, onSelect: (ShareTemplate) -> Unit) {
    FilterChip(selected = value == selected, onClick = { onSelect(value) }, leadingIcon = {
        Icon(when (value) { ShareTemplate.SUMMARY -> Icons.Filled.Share; ShareTemplate.STREAK -> Icons.Filled.LocalFireDepartment; ShareTemplate.VOLUME -> Icons.Filled.FitnessCenter }, contentDescription = null)
    }, label = { Text(when (value) { ShareTemplate.SUMMARY -> "Summary"; ShareTemplate.STREAK -> "Streak"; ShareTemplate.VOLUME -> "Volume" }) })
}

@Composable
private fun ResultCard(summary: ShareSummary, template: ShareTemplate = ShareTemplate.SUMMARY, forCapture: Boolean = false) {
    Box(modifier = Modifier.let { if (forCapture) it.width(340.dp).height(420.dp) else it.fillMaxWidth() }.clip(RoundedCornerShape(24.dp)).background(Brush.verticalGradient(listOf(ForgeGradientStart, ForgeGradientEnd))).padding(24.dp)) {
        when (template) { ShareTemplate.SUMMARY -> SummaryTemplate(summary); ShareTemplate.STREAK -> StreakTemplate(summary); ShareTemplate.VOLUME -> VolumeTemplate(summary) }
    }
}

@Composable
private fun SummaryTemplate(summary: ShareSummary) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Column { BrandHeader(); Text(scopeLabel(summary.scope), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground); Text(summary.splitName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Column { StatRow("Workouts completed", summary.workoutsCompleted.toString()); StatRow("Working sets", summary.totalSets.toString()); StatRow("Total volume", summary.totalVolume.toString()); StreakLine(summary.currentStreak) }
    }
}

@Composable
private fun StreakTemplate(summary: ShareSummary) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        BrandHeader(); Spacer(Modifier.height(28.dp)); Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = ForgeGold, modifier = Modifier.size(80.dp)); Spacer(Modifier.height(8.dp)); Text("${summary.currentStreak}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground); Text("DAY STREAK", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForgeGold); Spacer(Modifier.height(20.dp)); Text(scopeLabel(summary.scope), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun VolumeTemplate(summary: ShareSummary) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Column { BrandHeader(); Text("Volume forged", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground); Text(scopeLabel(summary.scope), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = ForgeGold, modifier = Modifier.size(42.dp)); Spacer(Modifier.height(6.dp)); Text(summary.totalVolume.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground); Text("TOTAL VOLUME", style = MaterialTheme.typography.labelLarge, color = ForgeGold); Spacer(Modifier.height(18.dp)); StatRow("Workouts", summary.workoutsCompleted.toString()); StatRow("Working sets", summary.totalSets.toString())
        }
    }
}

@Composable private fun BrandHeader() { Text("REDFORGE", color = ForgeGold, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium) }
@Composable private fun StreakLine(streak: Int) { Spacer(Modifier.height(10.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = ForgeGold); Spacer(Modifier.width(6.dp)); Text("$streak-day streak", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) } }
@Composable private fun StatRow(label: String, value: String) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) } }
private fun scopeLabel(scope: ShareScope) = when (scope) { ShareScope.DAY -> "Today's Grind"; ShareScope.MONTH -> "This Month"; ShareScope.YEAR -> "This Year" }
