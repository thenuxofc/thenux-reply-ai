package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

@JsonClass(generateAdapter = true)
data class WorkerChatRequest(
    @Json(name = "message") val message: String,
    @Json(name = "mode") val mode: String = "thenux",
    @Json(name = "model") val model: String = "T-Nex 1.0",
    @Json(name = "system_prompt") val systemPrompt: String? = null
)

@JsonClass(generateAdapter = true)
data class WorkerChatResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "response") val response: String? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "error") val error: String? = null
)

interface ApiService {
    @POST("api/chat")
    suspend fun callWorkerChat(
        @Body request: WorkerChatRequest
    ): Response<WorkerChatResponse>

    @GET
    suspend fun callGptEndpoint(
        @Url url: String
    ): Response<String>
}
