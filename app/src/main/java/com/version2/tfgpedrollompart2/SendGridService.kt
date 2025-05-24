package com.version2.tfgpedrollompart2

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SendGridService {

    @POST("mail/send")
    fun sendEmail(@Body email: SendGridEmail): Call<Void>
}