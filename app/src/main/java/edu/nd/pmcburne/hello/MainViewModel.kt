package edu.nd.pmcburne.hello

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class Location(
    val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val tags: List<String>
)

data class MainUIState(
    val allLocations: List<Location> = emptyList(),
    val filteredLocations: List<Location> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val selectedTag: String = "core"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState: StateFlow<MainUIState> = _uiState.asStateFlow()

    private val database = AppDatabase.getDatabase(application)

    private val api = Retrofit.Builder()
        .baseUrl("https://www.cs.virginia.edu/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PlacemarkApi::class.java)

    init {
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            try {
                val response = api.getPlacemarks()
                val entities = response.map {
                    LocationEntity(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        latitude = it.visual_center.latitude,
                        longitude = it.visual_center.longitude,
                        tags = it.tag_list.joinToString(",")
                    )
                }
                database.locationDao().insertAll(entities)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loadFromDb()
            }
        }
    }

    private suspend fun loadFromDb() {
        val entities = database.locationDao().getAll()
        val locations = entities.map {
            Location(
                id = it.id,
                name = it.name,
                description = it.description,
                latitude = it.latitude,
                longitude = it.longitude,
                tags = it.tags.split(",")
            )
        }

        val tags = locations.flatMap { it.tags }.distinct().sorted()

        _uiState.update { currentState ->
            currentState.copy(
                allLocations = locations,
                availableTags = tags,
                selectedTag = if (tags.contains("core")) "core" else (tags.firstOrNull() ?: "")
            )
        }
        applyFilter()
    }

    fun selectTag(tag: String) {
        _uiState.update { it.copy(selectedTag = tag) }
        applyFilter()
    }

    private fun applyFilter() {
        _uiState.update { currentState ->
            currentState.copy(
                filteredLocations = currentState.allLocations.filter {
                    it.tags.contains(currentState.selectedTag)
                }
            )
        }
    }
}