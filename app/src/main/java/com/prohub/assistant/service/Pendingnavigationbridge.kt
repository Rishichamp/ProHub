package com.prohub.assistant.service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
/**
 * Lets Sage's background "Hey Sage" listener (running outside any screen's
 * context) request in-app navigation. FloatingBubbleService writes the
 * target route and brings MainActivity to the front; ProHubApp's NavHost
 * observes this and performs the actual navigation once it's visible.
 */
object PendingNavigationBridge {
    private val _pendingRoute = MutableStateFlow<String?>(null)
    val pendingRoute = _pendingRoute.asStateFlow()

    fun requestNavigation(route: String) {
        _pendingRoute.value = route
    }

    fun consume() {
        _pendingRoute.value = null
    }
}