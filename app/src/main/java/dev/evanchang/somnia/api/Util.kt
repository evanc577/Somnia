package dev.evanchang.somnia.api

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

suspend inline fun <reified T> doRequest(request: () -> HttpResponse): ApiResult<T> {
    val response = try {
        request()
    } catch (e: Exception) {
        return ApiResult.Err(e.toString())
    }

    if (!response.status.isSuccess()) {
        return ApiResult.Err("bad status: ${response.status.value}")
    }

    val body: T = try {
        val text = response.bodyAsText()
        response.body()
    } catch (e: Exception) {
        return ApiResult.Err(e.toString())
    }

    return ApiResult.Ok(body)
}
