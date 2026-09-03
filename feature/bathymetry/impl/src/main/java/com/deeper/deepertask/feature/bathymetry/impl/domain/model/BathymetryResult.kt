package com.deeper.deepertask.feature.bathymetry.impl.domain.model

internal sealed interface BathymetryResult {
    data class Success(val data: BathymetryData) : BathymetryResult

    data class Failure(val error: BathymetryError) : BathymetryResult
}

internal sealed interface BathymetryError {
    data object AuthenticationRequired : BathymetryError

    data object Connectivity : BathymetryError

    data object Service : BathymetryError

    data object InvalidData : BathymetryError

    data object Storage : BathymetryError
}
