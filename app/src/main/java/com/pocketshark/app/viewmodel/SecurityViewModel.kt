package com.pocketshark.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketshark.app.PocketSharkApp
import com.pocketshark.app.data.model.SecurityAlert
import com.pocketshark.app.data.model.ThreatCategory
import com.pocketshark.app.data.model.ThreatSeverity
import kotlinx.coroutines.flow.*

/**
 * ViewModel for the Security screen.
 * Provides categorized threat data and severity summaries.
 */
class SecurityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PocketSharkApp).packetRepository

    /** All security alerts */
    val alerts: StateFlow<List<SecurityAlert>> = repository.alerts

    /** Severity counts for the overview card */
    val severityCounts: StateFlow<Map<ThreatSeverity, Int>> = repository.alerts.map { alerts ->
        alerts.groupBy { it.severity }.mapValues { it.value.size }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** Category counts for category cards */
    val categoryCounts: StateFlow<Map<ThreatCategory, Int>> = repository.alerts.map { alerts ->
        alerts.groupBy { it.category }.mapValues { it.value.size }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** Total alert count */
    val totalAlerts = repository.alerts.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Highest severity level present */
    val highestSeverity = repository.alerts.map { alerts ->
        alerts.maxByOrNull { it.severity.weight }?.severity
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
