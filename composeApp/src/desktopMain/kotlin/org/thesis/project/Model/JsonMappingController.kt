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
    private val _mappings = MutableStateFlow<List<FileMappingFull>>(emptyList())
    val mappings: StateFlow<List<FileMappingFull>> = _mappings.asStateFlow()

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
                _mappings.value = loaded
                //println("Loaded ${loaded.size} mappings from file.")
            } else {
                //println("Mapping file was empty. Initializing empty mappings.")
                mappingFile.writeText("[]")
                _mappings.value = emptyList()
            }
        } else {
            //println("No existing mappings found. Creating new mapping file.")
            mappingFile.parentFile?.mkdirs()
            mappingFile.writeText("[]")
            _mappings.value = emptyList()
        }
    }


    private fun saveMappings() {
        val jsonString = json.encodeToString(_mappings.value)
        mappingFile.writeText(jsonString)
        //println("Saved ${_mappings.value.size} mappings to file.")
    }

    fun addMappingAndSave(newMapping: FileMappingFull) {
        loadMappings()
        _mappings.update { it + newMapping }
        saveMappings()
    }

    fun removeMapping(mappingToRemove: FileMappingFull) {
        loadMappings()
        _mappings.update { it.filterNot { it.title == mappingToRemove.title } }
        saveMappings()
        _selectedMappings.update { it.filterNot { it.title == mappingToRemove.title } }
        //println("Removed mapping: ${mappingToRemove.title}")
    }
}