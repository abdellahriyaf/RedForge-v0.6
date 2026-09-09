package com.redforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redforge.app.data.local.entities.Exercise
import com.redforge.app.data.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExerciseLibraryViewModel(private val repository: ExerciseRepository) : ViewModel() {
    val exercises: StateFlow<List<Exercise>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun delete(exercise: Exercise) {
        if (exercise.isCustom) {
            viewModelScope.launch { repository.delete(exercise) }
        }
    }
}

class ExerciseEditorViewModel(
    private val repository: ExerciseRepository,
    private val exerciseId: Long
) : ViewModel() {

    val existing = MutableStateFlow<Exercise?>(null)

    init {
        if (exerciseId != 0L) {
            viewModelScope.launch { existing.value = repository.getById(exerciseId) }
        }
    }

    fun save(
        name: String,
        muscleGroup: String,
        equipment: String,
        aliases: String,
        primaryMuscles: String,
        secondaryMuscles: String,
        movementPattern: String,
        difficulty: String,
        defaultRepRange: String,
        instructions: String,
        keyCues: String,
        commonMistakes: String,
        imageUri: String?,
        referenceLink: String?,
        notes: String,
        onSaved: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val previous = existing.value
            val entity = Exercise(
                id = exerciseId,
                name = name,
                muscleGroup = muscleGroup,
                equipment = equipment,
                imageUri = imageUri,
                referenceLink = referenceLink,
                notes = notes,
                isCustom = previous?.isCustom ?: true,
                createdAt = previous?.createdAt ?: System.currentTimeMillis(),
                aliases = aliases,
                primaryMuscles = primaryMuscles,
                secondaryMuscles = secondaryMuscles,
                movementPattern = movementPattern,
                difficulty = difficulty,
                defaultRepRange = defaultRepRange,
                instructions = instructions,
                keyCues = keyCues,
                commonMistakes = commonMistakes,
                demoAsset = previous?.demoAsset,
                sourceLicense = previous?.sourceLicense ?: "RedForge custom exercise",
                sourceAttribution = previous?.sourceAttribution ?: ""
            )
            val id = repository.save(entity)
            onSaved(if (exerciseId != 0L) exerciseId else id)
        }
    }
}
