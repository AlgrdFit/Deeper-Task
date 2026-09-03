package com.deeper.deepertask.feature.bathymetry.impl.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

internal interface BathymetryApi {
    @GET("geoData")
    suspend fun getBathymetry(
        @Query("grid") grid: String,
        @Query("generator") generator: String,
        @Query("scanIds") scanId: Long,
        @Query("token") token: String,
    ): GeoDataResponseDto
}
