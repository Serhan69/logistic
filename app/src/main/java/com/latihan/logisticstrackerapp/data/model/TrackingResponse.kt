package com.latihan.logisticstrackerapp.data.model

import com.google.gson.annotations.SerializedName

data class TrackingResponse(
    @SerializedName("tracking_number") val trackingNumber: String? = null,
    @SerializedName("courier_name") val courierName: String? = null,
    @SerializedName("service_type") val serviceType: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("last_location") val lastLocation: String? = null,
    @SerializedName("estimated_delivery") val estimatedDelivery: String? = null,
    @SerializedName("recipient_name") val recipientName: String? = null
)
