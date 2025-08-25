package com.huntercoles.fatline.core.presentation.server

sealed class ServerConnectionIntent {
    data class SetServerUrl(val url: String) : ServerConnectionIntent()
    data class SetUsername(val username: String) : ServerConnectionIntent()
    data class SetPassword(val password: String) : ServerConnectionIntent()
    data object ConnectToServer : ServerConnectionIntent()
    data object ShowLoginDialog : ServerConnectionIntent()
    data object HideLoginDialog : ServerConnectionIntent()
    data object ClearError : ServerConnectionIntent()
    data object TestConnection : ServerConnectionIntent()
    data object Login : ServerConnectionIntent()
    data object Disconnect : ServerConnectionIntent()
}

sealed class ServerConnectionEvent {
    data class ShowMessage(val message: String) : ServerConnectionEvent()
    data object NavigateToMain : ServerConnectionEvent()
}
