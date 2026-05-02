package com.mm.astraisandroid.ui.components

import androidx.lifecycle.ViewModel

import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.channels.Channel

import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.receiveAsFlow

import javax.inject.Inject

import javax.inject.Singleton

data class SnackbarEvent(
    val message: String,
    val actionLabel: String? = null
)

interface SnackbarManager {
    val messages: Flow<SnackbarEvent>
    suspend fun showMessage(message: String, actionLabel: String? = null)
}

@Singleton
class SnackbarManagerImpl @Inject constructor() : SnackbarManager {
    private val _messages = Channel<SnackbarEvent>(Channel.BUFFERED)
    override val messages = _messages.receiveAsFlow()
    override suspend fun showMessage(message: String, actionLabel: String?) {
        _messages.send(SnackbarEvent(message, actionLabel))
    }
}

@HiltViewModel
class GlobalSnackbarViewModel @Inject constructor(
    snackbarManager: SnackbarManager
) : ViewModel() {
    val snackbarEvents = snackbarManager.messages
}