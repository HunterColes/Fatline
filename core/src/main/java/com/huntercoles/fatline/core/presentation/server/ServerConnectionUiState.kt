package com.huntercoles.fatline.core.presentation.server

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class ServerConnectionUiState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val showLoginDialog: Boolean = false,
    val needsServerSetup: Boolean = true,
) : Parcelable {

    sealed class PartialState {
        data object Connecting : PartialState()
        data class Connected(val serverUrl: String, val username: String) : PartialState()
        data class ConnectionFailed(val error: String) : PartialState()
        data class ServerUrlChanged(val url: String) : PartialState()
        data class UsernameChanged(val username: String) : PartialState()
        data class PasswordChanged(val password: String) : PartialState()
        data object ShowLogin : PartialState()
        data object HideLogin : PartialState()
        data object ClearError : PartialState()
    }
}
