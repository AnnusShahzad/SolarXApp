package io.github.annusshahzad.solarx.data

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException

/** Where the inverter lives on the local network, and the Pocket WiFi serial that unlocks it. */
data class InverterConnection(val host: String, val serial: String)

/**
 * Talks to the local HTTP API of a SolaX Pocket WiFi dongle.
 *
 * Every request is a form POST to the dongle's root URL. All network and protocol failures are
 * reported as [IOException], so callers only need to handle one exception type.
 */
class InverterClient(private val http: OkHttpClient = defaultHttpClient()) {

    /** Returns true when [InverterConnection.serial] is accepted by the dongle. */
    suspend fun verifySerial(connection: InverterConnection): Boolean {
        val body = post(
            connection,
            FormBody.Builder()
                .add("optType", "newParaSetting")
                .add("subOption", "pwd")
                .add("Value", connection.serial)
                .build()
        )
        return body.startsWith("Y")
    }

    suspend fun readRealTimeData(connection: InverterConnection): InverterReading {
        val body = post(
            connection,
            FormBody.Builder()
                .add("optType", "ReadRealTimeData")
                .add("pwd", connection.serial)
                .build()
        )
        return parseRealTimeData(body)
    }

    private suspend fun post(connection: InverterConnection, form: FormBody): String {
        val request = Request.Builder()
            .url(urlFor(connection.host) ?: throw IOException("Invalid inverter address: ${connection.host}"))
            .post(form)
            .build()
        return http.newCall(request).await()
    }

    /** Runs the call on OkHttp's dispatcher and cancels it if the coroutine is cancelled. */
    private suspend fun Call.await(): String = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = continuation.resumeWithException(e)

            override fun onResponse(call: Call, response: Response) {
                val result = runCatching {
                    response.use {
                        if (!it.isSuccessful) throw IOException("Inverter responded with HTTP ${it.code}")
                        it.body.string()
                    }
                }
                continuation.resumeWith(result)
            }
        })
    }

    companion object {
        /** The dongle sits on the LAN, so anything slower than a few seconds means it is offline. */
        fun defaultHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.SECONDS)
            .build()

        /** Returns the dongle's base URL for an address such as `192.168.10.10`, or null if it is not valid. */
        fun urlFor(host: String): HttpUrl? {
            if (host.isBlank() || host.any { it.isWhitespace() || it == '/' }) return null
            return "http://$host/".toHttpUrlOrNull()
        }

        internal fun parseRealTimeData(body: String): InverterReading {
            try {
                val data = JSONObject(body).getJSONArray("Data")
                return InverterReading.fromRealTimeData(List(data.length()) { data.getInt(it) })
            } catch (e: JSONException) {
                throw IOException("Unexpected real-time data response", e)
            }
        }
    }
}
