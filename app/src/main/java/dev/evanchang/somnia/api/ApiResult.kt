package dev.evanchang.somnia.api

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

sealed class ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>()
    data class Err(val message: String) : ApiResult<Nothing>()
}

// Signals that API could not complete because data from datastore was not ready yey
object WaitForDataStore : Throwable() {
    private fun readResolve(): Any = WaitForDataStore
}

fun <T : Any> LazyPagingItems<T>.isWaitForDataStore(): Boolean {
    val refresh = this.loadState.refresh
    return refresh is LoadState.Error && refresh.error is WaitForDataStore
}