/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lunchtray.datasource.DataSource
import com.example.lunchtray.ui.AccompanimentMenuScreen
import com.example.lunchtray.ui.CheckoutScreen
import com.example.lunchtray.ui.EntreeMenuScreen
import com.example.lunchtray.ui.OrderViewModel
import com.example.lunchtray.ui.SideDishMenuScreen
import com.example.lunchtray.ui.StartOrderScreen

enum class DMRutasPantallas(@StringRes val titulo: Int) {
    inicio(titulo = R.string.start_order),
    platosPrincipales(titulo = R.string.choose_entree),
    plastosGuarnicion(titulo = R.string.choose_side_dish),
    platosAconpañamiento(titulo = R.string.choose_accompaniment),
    confirmarCompra(titulo = R.string.order_summary)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appBarPersonalizada(
    pantallaActual: DMRutasPantallas,
    navigateUp: () -> Unit,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = pantallaActual.titulo)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_button)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayApp(
    viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val pantallaActual =
        DMRutasPantallas.valueOf(backStackEntry?.destination?.route ?: DMRutasPantallas.inicio.name)

    Scaffold(
        topBar = {
            appBarPersonalizada(
                pantallaActual = pantallaActual,
                navigateUp = { navController.navigateUp() },
                canNavigateBack = navController.previousBackStackEntry != null,
            )

        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = DMRutasPantallas.inicio.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = DMRutasPantallas.inicio.name) {
                StartOrderScreen(
                    onStartOrderButtonClicked = { navController.navigate(DMRutasPantallas.platosPrincipales.name) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(route = DMRutasPantallas.platosPrincipales.name) {
                EntreeMenuScreen(
                    options = DataSource.entreeMenuItems,
                    onCancelButtonClicked = {
                        viewModel.resetOrder()
                        navController.popBackStack(DMRutasPantallas.inicio.name, inclusive = false)
                    },
                    onNextButtonClicked = { navController.navigate(DMRutasPantallas.plastosGuarnicion.name) },
                    onSelectionChanged = { item -> viewModel.updateEntree(item) },
                    modifier = Modifier.fillMaxHeight()
                )
            }

            composable(route = DMRutasPantallas.plastosGuarnicion.name) {
                SideDishMenuScreen(
                    options = DataSource.sideDishMenuItems,
                    onCancelButtonClicked = {
                        viewModel.resetOrder()
                        navController.popBackStack(DMRutasPantallas.inicio.name, inclusive = false)
                    },
                    onNextButtonClicked = { navController.navigate(DMRutasPantallas.platosAconpañamiento.name) },
                    onSelectionChanged = { item -> viewModel.updateSideDish(item) },
                    modifier = Modifier.fillMaxHeight()
                )
            }

            composable(route = DMRutasPantallas.platosAconpañamiento.name) {
                AccompanimentMenuScreen(
                    options = DataSource.accompanimentMenuItems,
                    onCancelButtonClicked = {
                        viewModel.resetOrder()
                        navController.navigate(DMRutasPantallas.inicio.name)
                    },
                    onNextButtonClicked = { navController.navigate(DMRutasPantallas.confirmarCompra.name) },
                    onSelectionChanged = { item -> viewModel.updateAccompaniment(item) },
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = DMRutasPantallas.confirmarCompra.name) {
                CheckoutScreen(
                    orderUiState = uiState,
                    onNextButtonClicked = { viewModel.resetOrder()
                        navController.navigate(DMRutasPantallas.inicio.name) },
                    onCancelButtonClicked = {
                        viewModel.resetOrder()
                        navController.popBackStack(DMRutasPantallas.inicio.name, inclusive = false)
                    },
                )
            }
        }
    }
}
