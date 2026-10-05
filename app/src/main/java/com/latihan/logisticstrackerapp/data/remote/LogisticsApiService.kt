package com.latihan.logisticstrackerapp.data.remote

import com.latihan.logisticstrackerapp.data.model.TrackingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LogisticsApiService {
    @GET("api/v1/tracking/{resi}")
    suspend fun getTrackingDetail(
        @Path("resi") resiNumber: String
    ): Response<TrackingResponse>

    @GET("api/v1/tracking/search")
    suspend fun searchTracking(
        @Query("query") keyword: String
    ): Response<List<TrackingResponse>>
}
