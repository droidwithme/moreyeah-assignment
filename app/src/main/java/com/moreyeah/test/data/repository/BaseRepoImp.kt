package com.moreyeah.test.data.repository

import com.moreyeah.test.data.networkConfig.NetworkResult
import com.moreyeah.test.data.networkConfig.NoConnectivityException
import android.util.Log
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException



open class BaseRepoImp {

    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>, errorContext: String): T? {

        val result: NetworkResult<T> = safeApiResult(call)
        var data: T? = null

        when (result) {
            is NetworkResult.Success ->
                data = result.data
            is NetworkResult.Error ->
                Log.e("BaseRepoImp ", " error context = $errorContext & Exception = ${result.error}")
            is NetworkResult.NoConnection ->
                Log.e("BaseRepoImp ", " error context = $errorContext & Exception = ${result.exception}")
        }

        return data
    }

    private suspend fun <T : Any> safeApiResult(call: suspend () -> Response<T>): NetworkResult<T> {
        var result: NetworkResult<T>;
        try {
            Log.e("BaseRepoImp", "safeApiResult()")
            val response = call.invoke()
            if (response.isSuccessful)
                result = NetworkResult.Success(response.body()!!)
            else
                result = NetworkResult.Error(IOException(setErrorMessage(response)))
        } catch (exception: IOException) {
            if (exception is NoConnectivityException)
                result = NetworkResult.NoConnection(exception)
            else
                result = NetworkResult.Error(exception)
        }
        return result
    }

    private fun <T : Any> setErrorMessage(response: Response<T>): String {
        Log.e("BaseRepoImp", "setErrorMessage($response)")
        val code = response.code().toString()
        val message = try {
            val jObjError = JSONObject(response.errorBody()?.string())
            jObjError.getJSONObject("error").getString("message")
        } catch (e: Exception) {
            e.message
        }
        return if (message.isNullOrEmpty()) " error code = $code " else " error code = $code  & error message = $message "
    }


}