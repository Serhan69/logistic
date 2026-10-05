package com.latihan.logisticstrackerapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.latihan.logisticstrackerapp.data.model.TrackingResponse
import com.latihan.logisticstrackerapp.data.remote.ApiClient
import com.latihan.logisticstrackerapp.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class MainActivity : AppCompatActivity() {

    private lateinit var editTextTrackingNumber: EditText
    private lateinit var buttonTrack: Button
    private lateinit var progressBarLoading: ProgressBar
    private lateinit var textViewErrorMessage: TextView

    private lateinit var cardResult: MaterialCardView
    private lateinit var textViewResiTitle: TextView
    private lateinit var textViewStatusBadge: TextView
    private lateinit var textViewCourier: TextView
    private lateinit var textViewLocation: TextView
    private lateinit var textViewRecipient: TextView
    private lateinit var textViewEta: TextView

    private lateinit var editTextSearchKeyword: EditText
    private lateinit var buttonSearch: Button
    private lateinit var progressBarSearchLoading: ProgressBar
    private lateinit var textViewSearchErrorMessage: TextView
    private lateinit var textViewSearchResultCount: TextView
    private lateinit var layoutSearchResultsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()

        buttonTrack.setOnClickListener {
            performTracking()
        }

        buttonSearch.setOnClickListener {
            performSearch()
        }
    }

    private fun initViews() {
        editTextTrackingNumber = findViewById(R.id.editTextTrackingNumber)
        buttonTrack = findViewById(R.id.buttonTrack)
        progressBarLoading = findViewById(R.id.progressBarLoading)
        textViewErrorMessage = findViewById(R.id.textViewErrorMessage)

        cardResult = findViewById(R.id.cardResult)
        textViewResiTitle = findViewById(R.id.textViewResiTitle)
        textViewStatusBadge = findViewById(R.id.textViewStatusBadge)
        textViewCourier = findViewById(R.id.textViewCourier)
        textViewLocation = findViewById(R.id.textViewLocation)
        textViewRecipient = findViewById(R.id.textViewRecipient)
        textViewEta = findViewById(R.id.textViewEta)

        editTextSearchKeyword = findViewById(R.id.editTextSearchKeyword)
        buttonSearch = findViewById(R.id.buttonSearch)
        progressBarSearchLoading = findViewById(R.id.progressBarSearchLoading)
        textViewSearchErrorMessage = findViewById(R.id.textViewSearchErrorMessage)
        textViewSearchResultCount = findViewById(R.id.textViewSearchResultCount)
        layoutSearchResultsContainer = findViewById(R.id.layoutSearchResultsContainer)
    }

    private fun performTracking() {
        val resi = editTextTrackingNumber.text.toString().trim()
        if (resi.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_empty_resi), Toast.LENGTH_SHORT).show()
            return
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showTrackingOfflineError()
            return
        }

        showTrackingLoading()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getTrackingDetail(resi)
                }

                if (response.isSuccessful) {
                    val trackingData = response.body()
                    if (trackingData != null) {
                        showTrackingSuccess(trackingData)
                    } else {
                        showTrackingError(getString(R.string.error_network_failure))
                    }
                } else {
                    val statusCode = response.code()
                    if (statusCode == 404) {
                        showTrackingError(getString(R.string.error_404_format, resi))
                    } else {
                        showTrackingError(getString(R.string.error_http_other_format, statusCode))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                showTrackingError(getString(R.string.error_network_failure))
            } finally {
                progressBarLoading.visibility = View.GONE
                buttonTrack.isEnabled = true
            }
        }
    }

    private fun showTrackingLoading() {
        progressBarLoading.visibility = View.VISIBLE
        cardResult.visibility = View.GONE
        textViewErrorMessage.visibility = View.GONE
        buttonTrack.isEnabled = false
    }

    private fun showTrackingSuccess(data: TrackingResponse) {
        cardResult.visibility = View.VISIBLE
        textViewErrorMessage.visibility = View.GONE

        val trackingNum = data.trackingNumber.orEmpty().ifEmpty { "-" }
        val courier = listOfNotNull(data.courierName, data.serviceType)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
            .ifEmpty { "-" }

        textViewResiTitle.text = trackingNum
        textViewCourier.text = courier
        textViewLocation.text = data.lastLocation.orEmpty().ifEmpty { "-" }
        textViewRecipient.text = data.recipientName.orEmpty().ifEmpty { "-" }
        textViewEta.text = data.estimatedDelivery.orEmpty().ifEmpty { "-" }

        val status = data.status.orEmpty()
        applyStatusBadge(textViewStatusBadge, status)
    }

    private fun showTrackingError(message: String) {
        cardResult.visibility = View.GONE
        textViewErrorMessage.text = message
        textViewErrorMessage.visibility = View.VISIBLE
    }

    private fun showTrackingOfflineError() {
        cardResult.visibility = View.GONE
        progressBarLoading.visibility = View.GONE
        buttonTrack.isEnabled = true
        textViewErrorMessage.text = getString(R.string.error_offline)
        textViewErrorMessage.visibility = View.VISIBLE
    }

    private fun performSearch() {
        val keyword = editTextSearchKeyword.text.toString().trim()
        if (keyword.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_empty_keyword), Toast.LENGTH_SHORT).show()
            return
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showSearchOfflineError()
            return
        }

        showSearchLoading()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.searchTracking(keyword)
                }

                if (response.isSuccessful) {
                    val searchList = response.body().orEmpty()
                    showSearchSuccess(searchList)
                } else {
                    val statusCode = response.code()
                    showSearchError(getString(R.string.error_http_other_format, statusCode))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                showSearchError(getString(R.string.error_network_failure))
            } finally {
                progressBarSearchLoading.visibility = View.GONE
                buttonSearch.isEnabled = true
            }
        }
    }

    private fun showSearchLoading() {
        progressBarSearchLoading.visibility = View.VISIBLE
        textViewSearchErrorMessage.visibility = View.GONE
        layoutSearchResultsContainer.removeAllViews()
        buttonSearch.isEnabled = false
    }

    private fun showSearchSuccess(results: List<TrackingResponse>) {
        textViewSearchErrorMessage.visibility = View.GONE
        textViewSearchResultCount.text = getString(R.string.label_search_result_count_format, results.size)
        layoutSearchResultsContainer.removeAllViews()

        val inflater = LayoutInflater.from(this)
        for (item in results) {
            val itemView = inflater.inflate(R.layout.item_search_result, layoutSearchResultsContainer, false)

            val textViewItemResi = itemView.findViewById<TextView>(R.id.textViewItemResi)
            val textViewItemStatusBadge = itemView.findViewById<TextView>(R.id.textViewItemStatusBadge)
            val textViewItemCourier = itemView.findViewById<TextView>(R.id.textViewItemCourier)
            val textViewItemRecipient = itemView.findViewById<TextView>(R.id.textViewItemRecipient)
            val textViewItemLocation = itemView.findViewById<TextView>(R.id.textViewItemLocation)

            val trackingNum = item.trackingNumber.orEmpty().ifEmpty { "-" }
            val courier = listOfNotNull(item.courierName, item.serviceType)
                .filter { it.isNotBlank() }
                .joinToString(" - ")
                .ifEmpty { "-" }

            textViewItemResi.text = trackingNum
            textViewItemCourier.text = getString(R.string.label_item_courier_format, courier)
            textViewItemRecipient.text = getString(R.string.label_item_recipient_format, item.recipientName.orEmpty().ifEmpty { "-" })
            textViewItemLocation.text = getString(R.string.label_item_location_format, item.lastLocation.orEmpty().ifEmpty { "-" })

            applyStatusBadge(textViewItemStatusBadge, item.status.orEmpty())

            layoutSearchResultsContainer.addView(itemView)
        }
    }

    private fun showSearchError(message: String) {
        textViewSearchErrorMessage.text = message
        textViewSearchErrorMessage.visibility = View.VISIBLE
    }

    private fun showSearchOfflineError() {
        progressBarSearchLoading.visibility = View.GONE
        buttonSearch.isEnabled = true
        textViewSearchErrorMessage.text = getString(R.string.error_offline)
        textViewSearchErrorMessage.visibility = View.VISIBLE
    }

    private fun applyStatusBadge(badgeTextView: TextView, status: String) {
        val upperStatus = status.uppercase()
        badgeTextView.text = upperStatus.ifEmpty { "PENDING" }

        when (upperStatus) {
            "DELIVERED" -> {
                badgeTextView.background = ContextCompat.getDrawable(this, R.drawable.bg_badge_delivered)
                badgeTextView.setTextColor(ContextCompat.getColor(this, R.color.badge_delivered_text))
            }
            "IN_TRANSIT" -> {
                badgeTextView.background = ContextCompat.getDrawable(this, R.drawable.bg_badge_in_transit)
                badgeTextView.setTextColor(ContextCompat.getColor(this, R.color.badge_in_transit_text))
            }
            else -> {
                badgeTextView.background = ContextCompat.getDrawable(this, R.drawable.bg_badge_pending)
                badgeTextView.setTextColor(ContextCompat.getColor(this, R.color.badge_pending_text))
            }
        }
    }
}
