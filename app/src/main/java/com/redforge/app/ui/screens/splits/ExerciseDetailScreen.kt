@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.redforge.app.ui.screens.splits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
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
import com.redforge.app.viewmodel.ExerciseDetailViewModel
import com.redforge.app.viewmodel.redForgeViewModel

@Composable
fun ExerciseDetailScreen(
    exerciseId: Long,
    onBack: () -> Unit,
    onEdit: ((Long) -> Unit)? = null
) {
    val vm: ExerciseDetailViewModel = redForgeViewModel { app ->
        ExerciseDetailViewModel(app.exerciseRepository, exerciseId)
    }
    val exercise by vm.exercise.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val current = exercise
                    if (current?.isCustom == true && onEdit != null) {
                        TextButton(onClick = { onEdit(current.id) }) { Text("Edit") }
                    }
                }
            )
        }
    ) { padding ->
        val current = exercise
        if (current == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmberEmptyState(
                    title = "Exercise unavailable",
                    message = "This exercise may have been removed from the library."
                )
            }
        } else {
            ExerciseDetailContent(
                exercise = current,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}

@Composable
private fun ExerciseDetailContent(
    exercise: Exercise,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (exercise.imageUri != null) {
            AsyncImage(
                model = exercise.imageUri,
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        } else {
            ForgeCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.FitnessCenter, contentDescription = null, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Exercise library entry", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Strong Ember demonstration is planned for v0.7.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MetaChip(exercise.muscleGroup)
            if (exercise.equipment.isNotBlank()) MetaChip(exercise.equipment)
            if (exercise.movementPattern.isNotBlank()) MetaChip(exercise.movementPattern)
        }

        ForgeSectionHeader(
            title = "Muscles",
            subtitle = exercise.difficulty + " · " + exercise.defaultRepRange
        )

        DetailListCard("Primary", exercise.primaryMuscles.toLibraryItems())
        DetailListCard("Secondary", exercise.secondaryMuscles.toLibraryItems())

        if (exercise.instructions.isNotBlank()) {
            DetailTextCard("How to", exercise.instructions)
        }

        DetailListCard("Key cues", exercise.keyCues.toLibraryItems())
        DetailListCard("Common mistakes", exercise.commonMistakes.toLibraryItems())

        if (exercise.aliases.isNotBlank()) {
            DetailListCard("Also known as", exercise.aliases.toLibraryItems())
        }

        Text(
            "Demo media is intentionally not bundled in v0.6. Strong Ember exercise demonstrations are the v0.7 visual project.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MetaChip(text: String) {
    AssistChip(onClick = {}, label = { Text(text) })
}

@Composable
private fun DetailListCard(title: String, values: List<String>) {
    if (values.isEmpty()) return

    ForgeCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        values.forEach { value ->
            Text("• $value", modifier = Modifier.padding(vertical = 2.dp))
        }
    }
}

@Composable
private fun DetailTextCard(title: String, text: String) {
    ForgeCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(text)
    }
}
