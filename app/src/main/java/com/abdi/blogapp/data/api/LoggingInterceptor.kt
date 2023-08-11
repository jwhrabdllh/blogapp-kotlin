package com.abdi.blogapp.data.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.nio.charset.Charset

class LoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Cetak respons dari server sebelum diproses
        val responseBody = response.body
        val source = responseBody?.source()
        source?.request(Long.MAX_VALUE) // Buffer the entire body
        val buffer = source?.buffer

        val charset: Charset = Charset.forName("UTF-8")
        val contentType = responseBody?.contentType()
        val bodyString = buffer?.clone()?.readString(charset)

        println("RESPONSE FROM SERVER: $bodyString")

        return response
    }
}