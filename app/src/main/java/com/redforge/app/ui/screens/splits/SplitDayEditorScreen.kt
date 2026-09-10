@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.redforge.app.ui.screens.splits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.redforge.app.ui.components.EmberEmptyState
import com.redforge.app.ui.components.ForgeCard
import com.redforge.app.viewmodel.DayExerciseRow
import com.redforge.app.viewmodel.SplitDayEditorViewModel
import com.redforge.app.viewmodel.redForgeViewModel

private data class RepPreset(val label: String, val sets: Int, val repsLow: Int, val repsHigh: Int, val restSeconds: Int)
private val REP_PRESETS = listOf(RepPreset("Hypertrophy", 3, 8, 12, 75), RepPreset("Strength", 4, 3, 6, 150), RepPreset("Endurance", 3, 15, 20, 40))

@Composable
fun SplitDayEditorScreen(dayId: Long, pickedExerciseId: Long?, onPickedConsumed: () -> Unit, onPickExercise: () -> Unit, onBack: () -> Unit) {
    val vm: SplitDayEditorViewModel = redForgeViewModel { app -> SplitDayEditorViewModel(app.splitRepository, app.exerciseRepository, dayId) }
    val state by vm.uiState.collectAsState()
    val day by vm.dayName.collectAsState()
    var editingRow by remember { mutableStateOf<DayExerciseRow?>(null) }
    LaunchedEffect(pickedExerciseId) { if (pickedExerciseId != null) { vm.addExerciseById(pickedExerciseId); onPickedConsumed() } }
    Scaffold(topBar = { TopAppBar(title = { Text(day?.name ?: "Day") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }) }, floatingActionButton = { FloatingActionButton(onClick = onPickExercise) { Icon(Icons.Filled.Add, contentDescription = "Add exercise") } }) { padding ->
        if (state.rows.isEmpty()) Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) { EmberEmptyState(title = "No exercises yet", message = "Tap + to add one from your library.") }
        else {
            val sortedRows = state.rows.sortedBy { it.entry.orderIndex }
            LazyColumn(modifier = Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(sortedRows, key = { it.entry.id }) { row ->
                    val index = sortedRows.indexOfFirst { it.entry.id == row.entry.id }
                    val nextEntry = sortedRows.getOrNull(index + 1)?.entry
                    val linkedWithNext = row.entry.supersetGroup != null && row.entry.supersetGroup == nextEntry?.supersetGroup
                    ForgeCard(modifier = Modifier.fillMaxWidth(), onClick = { editingRow = row }) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) { Text(row.exercise.name, style = MaterialTheme.typography.titleMedium); if (row.entry.supersetGroup != null) { Spacer(Modifier.width(6.dp)); AssistChip(onClick = {}, label = { Text("Superset") }) } }
                                Text("${row.entry.targetSets} sets · ${row.entry.targetRepsLow}-${row.entry.targetRepsHigh} reps · " + if (row.entry.targetRestSeconds <= 0) "default rest" else "${row.entry.targetRestSeconds}s rest", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { vm.moveExercise(row.entry, -1) }) { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up") }
                            IconButton(onClick = { vm.moveExercise(row.entry, 1) }) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down") }
                            IconButton(onClick = { vm.removeExercise(row.entry) }) { Icon(Icons.Filled.Delete, contentDescription = "Remove") }
                        }
                        if (nextEntry != null) TextButton(onClick = { vm.toggleSupersetWithNext(row.entry) }) { Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text(if (linkedWithNext) "Unlink from next" else "Group with next (superset)") }
                    }
                }
            }
        }
    }
    editingRow?.let { row -> PrescriptionDialog(row = row, onDismiss = { editingRow = null }, onSave = { sets, low, high, rest -> vm.updatePrescription(row.entry, sets, low, high, rest); editingRow = null }) }
}

@Composable
private fun PrescriptionDialog(row: DayExerciseRow, onDismiss: () -> Unit, onSave: (Int, Int, Int, Int) -> Unit) {
    var sets by remember { mutableStateOf(row.entry.targetSets.toString()) }
    var repsLow by remember { mutableStateOf(row.entry.targetRepsLow.toString()) }
    var repsHigh by remember { mutableStateOf(row.entry.targetRepsHigh.toString()) }
    var rest by remember { mutableStateOf(if (row.entry.targetRestSeconds > 0) row.entry.targetRestSeconds.toString() else "") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(row.exercise.name) }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Quick presets", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { REP_PRESETS.forEach { preset -> AssistChip(onClick = { sets = preset.sets.toString(); repsLow = preset.repsLow.toString(); repsHigh = preset.repsHigh.toString(); rest = preset.restSeconds.toString() }, label = { Text(preset.label) }) } }
        OutlinedTextField(value = sets, onValueChange = { sets = it }, label = { Text("Target sets") }, singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value = repsLow, onValueChange = { repsLow = it }, label = { Text("Reps low") }, singleLine = true, modifier = Modifier.weight(1f)); OutlinedTextField(value = repsHigh, onValueChange = { repsHigh = it }, label = { Text("Reps high") }, singleLine = true, modifier = Modifier.weight(1f)) }
        OutlinedTextField(value = rest, onValueChange = { rest = it }, label = { Text("Rest (seconds, blank = default)") }, singleLine = true)
    } }, confirmButton = { TextButton(onClick = { onSave(sets.toIntOrNull() ?: row.entry.targetSets, repsLow.toIntOrNull() ?: row.entry.targetRepsLow, repsHigh.toIntOrNull() ?: row.entry.targetRepsHigh, rest.toIntOrNull() ?: 0) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
