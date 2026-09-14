package com.cheeke.surfy.network.impl

import com.cheeke.surfy.network.api.SurfyNetworkErrorMessage
import com.cheeke.surfy.network.api.SurfyNetworkException
import com.cheeke.surfy.network.api.SurfyResponseErrorMessage
import com.cheeke.surfy.network.api.toSurfyNetworkException
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

internal class ApiResultCall<R>(
    private val delegate: Call<R>,
    private val successType: Type
) : Call<ApiResponse<R>> {
    override fun enqueue(callback: Callback<ApiResponse<R>>) {
        delegate.enqueue(object : Callback<R> {
            override fun onResponse(call: Call<R>, response: Response<R>) {
                callback.onResponse(this@ApiResultCall, Response.success(response.toApiResponse()))
            }

            override fun onFailure(call: Call<R?>, throwable: Throwable) {
                val stringRes = when (throwable) {
                    is UnknownHostException -> SurfyNetworkErrorMessage.UN_KNOWN_HOST.stringRes
                    is SocketTimeoutException -> SurfyNetworkErrorMessage.SOCKET_TIMEOUT.stringRes
                    is ConnectException -> SurfyNetworkErrorMessage.CONNECT_EXCEPTION.stringRes
                    is SSLHandshakeException -> SurfyNetworkErrorMessage.SSL_HAND_SHAKE_EXCEPTION.stringRes
                    else -> SurfyNetworkErrorMessage.UNKNOWN.stringRes
                }
                callback.onResponse(this@ApiResultCall, Response.success(ApiResponse.Failure(throwable = throwable.toSurfyNetworkException(), stringRes = stringRes)))
            }
        })
    }

    override fun execute(): Response<ApiResponse<R>> = try {
        delegate.execute().let { response ->
            Response.success(response.toApiResponse())
        }
    } catch (e: Exception) {
        Response.success(ApiResponse.Failure(throwable = e.toSurfyNetworkException()))
    }
    override fun clone(): Call<ApiResponse<R>> = ApiResultCall(delegate, successType)
    override fun isExecuted(): Boolean = delegate.isExecuted
    override fun cancel() = delegate.cancel()
    override fun isCanceled(): Boolean = delegate.isCanceled
    override fun request(): Request = delegate.request()
    override fun timeout(): Timeout = delegate.timeout()

    private fun Response<R>.toApiResponse(): ApiResponse<R> = when (code()) {
        in 200..299 -> body()
            ?.let { ApiResponse.Success(data = it) }
            ?: ApiResponse.Failure(throwable = SurfyNetworkException(stringRes = SurfyResponseErrorMessage.EMPTY_BODY.stringRes))
        in 400..499 -> ApiResponse.Failure(
            code = code(),
            stringRes = SurfyResponseErrorMessage.USER_ERROR.stringRes,
            body = errorBody()?.string()
        )
        in 500..599 -> ApiResponse.Failure(
            code = code(),
            stringRes = SurfyResponseErrorMessage.SERVER_ERROR.stringRes,
            body = errorBody()?.string()
        )
        else -> ApiResponse.Failure(
            code = code(),
            stringRes = SurfyResponseErrorMessage.UNKNOWN.stringRes,
            body = errorBody()?.string()
        )
    }
}