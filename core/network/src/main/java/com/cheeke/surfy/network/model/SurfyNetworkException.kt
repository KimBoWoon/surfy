package com.cheeke.surfy.network.model

import androidx.annotation.StringRes
import com.cheeke.surfy.core.network.R

data class SurfyNetworkException(
    val code: Int? = null,
    val throwable: Throwable? = null,
    val stringRes: Int? = null,
    override val message: String? = null
) : Exception(message)

enum class SurfyNetworkErrorMessage(@param:StringRes val stringRes: Int) {
    UN_KNOWN_HOST(stringRes = R.string.un_known_host),
    SOCKET_TIMEOUT(stringRes = R.string.soket_timeout),
    CONNECT_EXCEPTION(stringRes = R.string.connect_exception),
    SSL_HAND_SHAKE_EXCEPTION(stringRes = R.string.ssl_hand_shake_exception),
    UNKNOWN(stringRes = R.string.network_error_unknown)
}

enum class SurfyResponseErrorMessage(@param:StringRes val stringRes: Int) {
    EMPTY_BODY(stringRes = R.string.empty_body),
    USER_ERROR(stringRes = R.string.user_error),
    SERVER_ERROR(stringRes = R.string.server_error),
    UNKNOWN(stringRes = R.string.response_error_unknown)
}

fun Throwable.toSurfyNetworkException(): SurfyNetworkException =
    SurfyNetworkException(throwable = this)