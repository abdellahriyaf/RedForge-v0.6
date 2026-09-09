@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.redforge.app.ui.screens.splits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.redforge.app.data.local.entities.SplitDay
import com.redforge.app.ui.components.ForgeCard
import com.redforge.app.viewmodel.SplitEditorViewModel
import com.redforge.app.viewmodel.redForgeViewModel

@Composable
fun SplitEditorScreen(
    splitId: Long,
    onOpenDay: (Long) -> Unit,
    onBack: () -> Unit
) {
    val vm: SplitEditorViewModel = redForgeViewModel { app -> SplitEditorViewModel(app.splitRepository, splitId) }
    val state by vm.uiState.collectAsState()
    var showAddDayDialog by remember { mutableStateOf(false) }
    var showRenameSplitDialog by remember { mutableStateOf(false) }
    var showDeleteSplitDialog by remember { mutableStateOf(false) }
    var renamingDay by remember { mutableStateOf<SplitDay?>(null) }
    var topMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.split?.name ?: "Split") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    Box {
                        IconButton(onClick = { topMenuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(expanded = topMenuExpanded, onDismissRequest = { topMenuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Rename split") },
                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                onClick = { topMenuExpanded = false; showRenameSplitDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete split") },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                onClick = { topMenuExpanded = false; showDeleteSplitDialog = true }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDayDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add day")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp)) {
            Text(
                "Days repeat in this order — use the arrows to reorder, or add a rest day to pace your cycle.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            state.split?.let { split ->
                ForgeCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("Deload week", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Scales displayed target sets down ~40% during your next cycle through this split.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = split.isDeloadCycle, onCheckedChange = { vm.setDeloadCycle(it) })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.days.isEmpty()) {
                com.redforge.app.ui.components.EmberEmptyState(
                    title = "No days yet",
                    message = "Tap + to add your first training day (or a rest day) to this split."
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.days, key = { it.id }) { day ->
                        DayRow(
                            day = day,
                            onOpen = { if (!day.isRestDay) onOpenDay(day.id) },
                            onMoveUp = { vm.moveDay(day, -1) },
                            onMoveDown = { vm.moveDay(day, 1) },
                            onRename = { renamingDay = day },
                            onDelete = { vm.deleteDay(day) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDayDialog) {
        AddDayDialog(
            onDismiss = { showAddDayDialog = false },
            onAddTrainingDay = { name -> showAddDayDialog = false; vm.addDay(name) },
            onAddRestDay = { showAddDayDialog = false; vm.addRestDay() }
        )
    }

    if (showRenameSplitDialog && state.split != null) {
        RenameDialog(
            title = "Rename split",
            currentName = state.split!!.name,
            onDismiss = { showRenameSplitDialog = false },
            onConfirm = { newName -> vm.renameSplit(newName); showRenameSplitDialog = false }
        )
    }

    if (showDeleteSplitDialog && state.split != null) {
        AlertDialog(
            onDismissRequest = { showDeleteSplitDialog = false },
            title = { Text("Delete \"${state.split!!.name}\"?") },
            text = { Text("This deletes the split and its days/exercise assignments. Your logged workout history is not affected. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteSplitDialog = false
                    vm.deleteSplit()
                    onBack()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteSplitDialog = false }) { Text("Cancel") } }
        )
    }

    renamingDay?.let { day ->
        RenameDialog(
            title = "Rename day",
            currentName = day.name,
            onDismiss = { renamingDay = null },
            onConfirm = { newName -> vm.renameDay(day, newName); renamingDay = null }
        )
    }
}

@Composable
private fun DayRow(
    day: SplitDay,
    onOpen: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ForgeCard(modifier = Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(
                if (day.isRestDay) Icons.Filled.Bedtime else Icons.Filled.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(day.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Day ${day.dayOrder}" + if (day.isRestDay) " · Rest" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onMoveUp) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
            }
            IconButton(onClick = onMoveDown) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { menuExpanded = false; onRename() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddDayDialog(onDismiss: () -> Unit, onAddTrainingDay: (String) -> Unit, onAddRestDay: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a day") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Day name") },
                    placeholder = { Text("e.g. Push Day") },
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = onAddRestDay) { Text("Or just add a rest day instead") }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onAddTrainingDay(name.trim()) }, enabled = name.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RenameDialog(title: String, currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name.trim()) }, enabled = name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
