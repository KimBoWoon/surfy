package com.cheeke.surfy.network.impl

import com.cheeke.surfy.network.api.SurfyNetworkException
import kotlinx.serialization.Serializable

@Serializable
sealed class ApiResponse<out R> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Failure(
        val code: Int? = null,
        val stringRes: Int? = null,
        val body: String? = null,
        val throwable: SurfyNetworkException = SurfyNetworkException(throwable = Throwable("something wrong..."))
    ) : ApiResponse<Nothing>()
}