package com.deeper.deepertask.feature.bathymetry.impl.presentation

internal sealed interface BathymetryEvent {
    data object NavigateToLogin : BathymetryEvent
}
