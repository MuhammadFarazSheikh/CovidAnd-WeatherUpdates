package com.montymobile.callsignature.repositories

import com.montymobile.callsignature.networking.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import retrofit2.HttpException


abstract class BaseRepository {
    private lateinit var coroutineScope: CoroutineScope

    suspend fun <T> safeApiCall(apiCall: suspend () -> T): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                if (::coroutineScope.isInitialized) {
                    coroutineScope.cancel()
                }
                coroutineScope = this
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is HttpException -> {
                        Resource.Failure(false, throwable.code(), throwable.response()?.errorBody())
                    }
                    else -> {
                        Resource.Failure(true, null, null)
                    }
                }
            }
        }
    }
}