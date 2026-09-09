@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.redforge.app.ui.screens.splits

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.redforge.app.ui.components.ForgeButton
import com.redforge.app.ui.components.ForgeSectionHeader
import com.redforge.app.viewmodel.ExerciseEditorViewModel
import com.redforge.app.viewmodel.redForgeViewModel

private val muscleGroups = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Full Body")
private val movements = listOf("Horizontal Push", "Vertical Push", "Horizontal Pull", "Vertical Pull", "Squat", "Hinge", "Lunge", "Isolation", "Core", "Carry", "Calf Raise", "Full Body")
private val difficulties = listOf("Beginner", "Intermediate", "Advanced")

@Composable
fun ExerciseEditorScreen(exerciseId: Long, onSaved: (Long) -> Unit, onBack: () -> Unit) {
    val vm: ExerciseEditorViewModel = redForgeViewModel { app ->
        ExerciseEditorViewModel(app.exerciseRepository, exerciseId)
    }
    val existing by vm.existing.collectAsState()

    var name by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf(muscleGroups.first()) }
    var equipment by remember { mutableStateOf("") }
    var aliases by remember { mutableStateOf("") }
    var primaryMuscles by remember { mutableStateOf("") }
    var secondaryMuscles by remember { mutableStateOf("") }
    var movementPattern by remember { mutableStateOf(movements.first()) }
    var difficulty by remember { mutableStateOf("Intermediate") }
    var defaultRepRange by remember { mutableStateOf("8-12") }
    var instructions by remember { mutableStateOf("") }
    var keyCues by remember { mutableStateOf("") }
    var commonMistakes by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var link by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(existing) {
        existing?.let {
            name = it.name
            muscleGroup = it.muscleGroup
            equipment = it.equipment
            aliases = it.aliases
            primaryMuscles = it.primaryMuscles
            secondaryMuscles = it.secondaryMuscles
            movementPattern = it.movementPattern.ifBlank { movements.first() }
            difficulty = it.difficulty
            defaultRepRange = it.defaultRepRange
            instructions = it.instructions
            keyCues = it.keyCues
            commonMistakes = it.commonMistakes
            imageUri = it.imageUri
            link = it.referenceLink ?: ""
            notes = it.notes
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUri = it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (exerciseId == 0L) "New exercise" else "Edit exercise") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    TextButton(onClick = { imagePicker.launch("image/*") }) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Add photo")
                    }
                }
            }
            if (imageUri != null) {
                TextButton(onClick = { imagePicker.launch("image/*") }) { Text("Change photo") }
            }

            ForgeSectionHeader("Identity", "The fields below power search and exercise detail pages.")
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Exercise name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = aliases, onValueChange = { aliases = it }, label = { Text("Aliases (separate with ;)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = equipment, onValueChange = { equipment = it }, label = { Text("Equipment") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            Text("Muscle group", style = MaterialTheme.typography.labelLarge)
            ChoiceRow(options = muscleGroups, selected = muscleGroup) { muscleGroup = it }

            Text("Movement pattern", style = MaterialTheme.typography.labelLarge)
            ChoiceRow(options = movements, selected = movementPattern) { movementPattern = it }

            Text("Difficulty", style = MaterialTheme.typography.labelLarge)
            ChoiceRow(options = difficulties, selected = difficulty) { difficulty = it }

            OutlinedTextField(value = primaryMuscles, onValueChange = { primaryMuscles = it }, label = { Text("Primary muscles (separate with ;)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = secondaryMuscles, onValueChange = { secondaryMuscles = it }, label = { Text("Secondary muscles (separate with ;)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = defaultRepRange, onValueChange = { defaultRepRange = it }, label = { Text("Typical rep / time range") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            ForgeSectionHeader("Technique")
            OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Instructions") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
            OutlinedTextField(value = keyCues, onValueChange = { keyCues = it }, label = { Text("Key cues (separate with ;)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            OutlinedTextField(value = commonMistakes, onValueChange = { commonMistakes = it }, label = { Text("Common mistakes (separate with ;)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

            ForgeSectionHeader("Optional references")
            OutlinedTextField(value = link, onValueChange = { link = it }, label = { Text("Reference link") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Private notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

            Spacer(Modifier.height(4.dp))
            Text(
                "Exercise demonstrations with Strong Ember are intentionally reserved for v0.7. This version prepares the data needed for them.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ForgeButton(
                text = "Save exercise",
                enabled = name.isNotBlank(),
                onClick = {
                    vm.save(
                        name = name.trim(),
                        muscleGroup = muscleGroup,
                        equipment = equipment.trim(),
                        aliases = aliases.trim(),
                        primaryMuscles = primaryMuscles.trim(),
                        secondaryMuscles = secondaryMuscles.trim(),
                        movementPattern = movementPattern,
                        difficulty = difficulty,
                        defaultRepRange = defaultRepRange.trim(),
                        instructions = instructions.trim(),
                        keyCues = keyCues.trim(),
                        commonMistakes = commonMistakes.trim(),
                        imageUri = imageUri,
                        referenceLink = link.trim().ifBlank { null },
                        notes = notes.trim()
                    ) { onSaved(it) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ChoiceRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option) }
            )
        }
    }
}
