@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.redforge.app.ui.screens.splits

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.redforge.app.data.local.entities.Exercise
import com.redforge.app.data.local.entities.toLibraryItems
import com.redforge.app.ui.components.EmberEmptyState
import com.redforge.app.ui.components.ForgeCard
import com.redforge.app.ui.components.ForgeSectionHeader
import com.redforge.app.viewmodel.ExerciseLibraryViewModel
import com.redforge.app.viewmodel.redForgeViewModel

private val equipmentOptions = listOf(
    "All equipment", "Barbell", "Dumbbell", "Cable", "Machine", "Bodyweight", "EZ Bar", "Kettlebell", "Trap Bar", "Ab Wheel"
)

private val movementOptions = listOf(
    "All movements", "Horizontal Push", "Vertical Push", "Horizontal Pull", "Vertical Pull", "Squat", "Hinge", "Lunge", "Isolation", "Core", "Carry", "Calf Raise", "Full Body"
)

@Composable
fun ExerciseLibraryScreen(
    pickerMode: Boolean,
    onPick: (Exercise) -> Unit,
    onEdit: (Long) -> Unit,
    onOpenDetails: (Long) -> Unit,
    onCreateNew: () -> Unit
) {
    val vm: ExerciseLibraryViewModel = redForgeViewModel { app ->
        ExerciseLibraryViewModel(app.exerciseRepository)
    }
    val exercises by vm.exercises.collectAsState()

    var query by remember { mutableStateOf("") }
    var muscleFilter by remember { mutableStateOf("All muscles") }
    var equipmentFilter by remember { mutableStateOf("All equipment") }
    var movementFilter by remember { mutableStateOf("All movements") }
    var libraryFilter by remember { mutableStateOf("All") }

    val muscleOptions = remember(exercises) {
        listOf("All muscles") + exercises.map { it.muscleGroup }.distinct().sorted()
    }

    val filtered = remember(
        exercises,
        query,
        muscleFilter,
        equipmentFilter,
        movementFilter,
        libraryFilter
    ) {
        val q = query.trim().lowercase()
        exercises.filter { exercise ->
            val searchable = buildString {
                append(exercise.name)
                append(' ')
                append(exercise.aliases)
                append(' ')
                append(exercise.primaryMuscles)
                append(' ')
                append(exercise.secondaryMuscles)
                append(' ')
                append(exercise.equipment)
                append(' ')
                append(exercise.movementPattern)
            }.lowercase()

            val queryMatches = q.isBlank() || searchable.contains(q)
            val muscleMatches = muscleFilter == "All muscles" || exercise.muscleGroup == muscleFilter
            val equipmentMatches = equipmentFilter == "All equipment" || exercise.equipment == equipmentFilter
            val movementMatches = movementFilter == "All movements" || exercise.movementPattern == movementFilter
            val typeMatches = when (libraryFilter) {
                "Built-in" -> !exercise.isCustom
                "My exercises" -> exercise.isCustom
                else -> true
            }

            queryMatches && muscleMatches && equipmentMatches && movementMatches && typeMatches
        }.sortedBy { it.name }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNew) {
                Icon(Icons.Filled.Add, contentDescription = "New exercise")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            ForgeSectionHeader(
                title = if (pickerMode) "Choose an exercise" else "Exercise Library",
                subtitle = "${filtered.size} shown · ${exercises.size} total"
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("Search exercises or aliases") }
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Built-in", "My exercises").forEach { option ->
                    FilterChip(
                        selected = libraryFilter == option,
                        onClick = { libraryFilter = option },
                        label = { Text(option) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterDropDown(
                    label = muscleFilter,
                    options = muscleOptions,
                    onSelect = { muscleFilter = it }
                )
                FilterDropDown(
                    label = equipmentFilter,
                    options = equipmentOptions,
                    onSelect = { equipmentFilter = it }
                )
                FilterDropDown(
                    label = movementFilter,
                    options = movementOptions,
                    onSelect = { movementFilter = it }
                )
            }

            Spacer(Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmberEmptyState(
                        title = "No exercises found",
                        message = "Try another search or clear a filter. You can also create a custom exercise."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { exercise ->
                        ExerciseLibraryCard(
                            exercise = exercise,
                            pickerMode = pickerMode,
                            onPick = { onPick(exercise) },
                            onEdit = { onEdit(exercise.id) },
                            onOpenDetails = { onOpenDetails(exercise.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDropDown(
    label: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = !label.startsWith("All"),
            onClick = { expanded = true },
            label = { Text(label) }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ExerciseLibraryCard(
    exercise: Exercise,
    pickerMode: Boolean,
    onPick: () -> Unit,
    onEdit: () -> Unit,
    onOpenDetails: () -> Unit
) {
    ForgeCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (pickerMode) onPick else onOpenDetails
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (exercise.imageUri != null) {
                AsyncImage(
                    model = exercise.imageUri,
                    contentDescription = exercise.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Surface(
                    modifier = Modifier.size(58.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.FitnessCenter, contentDescription = null)
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    listOfNotNull(
                        exercise.equipment.takeIf { it.isNotBlank() },
                        exercise.movementPattern.takeIf { it.isNotBlank() }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    exercise.primaryMuscles.toLibraryItems().take(3).joinToString(" · ").ifBlank { exercise.muscleGroup },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!pickerMode) {
                IconButton(onClick = onOpenDetails) {
                    Icon(Icons.Filled.Info, contentDescription = "Exercise details")
                }
                if (exercise.isCustom) {
                    TextButton(onClick = onEdit) { Text("Edit") }
                }
            }
        }
    }
}
