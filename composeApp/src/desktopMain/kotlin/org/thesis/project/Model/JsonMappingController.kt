package org.thesis.project.Model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class JsonMappingController {
    private val mappings = mutableListOf<FileMappingFull>()
    private val _selectedMappings = MutableStateFlow<List<FileMappingFull>>(emptyList())
    val selectedMappings: StateFlow<List<FileMappingFull>> = _selectedMappings.asStateFlow()

    fun toggleSelectedMapping(mapping: FileMappingFull) {
        _selectedMappings.update { currentList ->
            if (mapping in currentList) {
                currentList - mapping
            } else {
                currentList + mapping
            }
        }
    }
    private val mappingFile = File(PathStrings.SAVED_MAPPING.toString())
    private val json = Json { prettyPrint = true }

    fun loadMappings() {
        if (mappingFile.exists()) {
            if (mappingFile.readText().isNotBlank()) {
                val loaded = json.decodeFromString<List<FileMappingFull>>(mappingFile.readText())
                mappings.clear()
                mappings.addAll(loaded)
                println("Loaded ${mappings.size} mappings from file.")
            } else {
                println("Mapping file was empty. Initializing empty mappings.")
                mappingFile.writeText("[]") // 🔥 Write an empty list into it
                mappings.clear()
            }
        } else {
            println("No existing mappings found. Creating new mapping file.")
            mappingFile.parentFile?.mkdirs()
            mappingFile.writeText("[]") // 🔥 When creating, always put valid empty JSON
            mappings.clear()
        }
    }


    private fun saveMappings() {
        val jsonString = json.encodeToString(mappings)
        mappingFile.writeText(jsonString)
        println("Saved ${mappings.size} mappings to file.")
    }

    fun addMappingAndSave(newMapping: FileMappingFull) {
        loadMappings()
        mappings.add(newMapping)
        saveMappings()
    }

    fun getAllMappings(): List<FileMappingFull> = mappings
}