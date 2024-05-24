package com.tempo.tempoapp.ui.navigation

/**
 * Interface representing a navigation destination.
 * Defines properties for the route and title resource ID.
 */
interface NavigationDestination {

    // Unique identifier for the navigation destination
    val route: String

    // Resource ID for the title of the navigation destination
    val titleRes: Int
}