package com.tempo.tempoapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tempo.tempoapp.ui.navigation.TempoNavHost

@Composable
fun TempoApp(navController: NavHostController = rememberNavController()){
    TempoNavHost(navController = navController)
}