package com.huntercoles.fatline.portfoliofeature.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.huntercoles.fatline.portfoliofeature.presentation.screen.PortfolioScreen
import com.huntercoles.fatline.core.navigation.NavigationDestination.Portfolio
import com.huntercoles.fatline.core.navigation.NavigationFactory
import javax.inject.Inject

class PortfolioNavigationFactory @Inject constructor() : NavigationFactory {

    override fun create(builder: NavGraphBuilder) {
        builder.composable<Portfolio> {
            PortfolioScreen()
        }
    }
}
