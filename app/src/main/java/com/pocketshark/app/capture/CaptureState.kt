package com.pocketshark.app.capture

/**
 * Sealed class representing the lifecycle states of the packet capture engine.
 */
sealed class CaptureState {
    /** Engine is idle, ready to start */
    data object Idle : CaptureState()

    /** VPN consent is being requested from the user */
    data object Preparing : CaptureState()

    /** Actively capturing packets */
    data object Running : CaptureState()

    /** Capture was stopped gracefully */
    data object Stopped : CaptureState()

    /** An error occurred during capture */
    data class Error(val message: String) : CaptureState()
}
