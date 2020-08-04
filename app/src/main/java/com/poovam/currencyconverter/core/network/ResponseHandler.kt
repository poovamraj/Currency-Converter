package com.poovam.currencyconverter.core.network

import retrofit2.HttpException
import java.net.SocketTimeoutException

class ResponseHandler {
    companion object {
        fun <T : Any> handleSuccess(data: T): Resource<T> {
            return Resource.success(data)
        }

        fun <T : Any> handleException(e: Exception): Resource<T> {
            return when (e) {
                is HttpException -> Resource.error(getErrorMessage(e.code()), null)
                is SocketTimeoutException -> Resource.error(getErrorMessage(408), null)
                else -> Resource.error(getErrorMessage(Int.MAX_VALUE), null)
            }
        }

        private fun getErrorMessage(code: Int): String {
            return when (code) {
                408 -> "Timeout"
                401 -> "Unauthorised"
                404 -> "Not found"
                else -> "Something went wrong"
            }
        }
    }
}