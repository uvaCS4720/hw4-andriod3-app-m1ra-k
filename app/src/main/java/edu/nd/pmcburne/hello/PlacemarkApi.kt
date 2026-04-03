package edu.nd.pmcburne.hello

import retrofit2.http.GET

data class VisualCenter(
    val latitude: Double,
    val longitude: Double
)

data class PlacemarkResponse(
    val id: Int,
    val name: String,
    val tag_list: List<String>,
    val description: String,
    val visual_center: VisualCenter
)

interface PlacemarkApi {
    @GET("~wxt4gm/placemarks.json")
    suspend fun getPlacemarks(): List<PlacemarkResponse>
}