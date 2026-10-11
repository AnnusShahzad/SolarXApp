package com.example.solarx.pages.viewM

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.Socket
import java.net.SocketException

fun stringToNumberArray(numberString: String): Array<Int> {
    return numberString
        .removeSurrounding("[", "]")  // Remove the surrounding brackets
        .split(",")                    // Split by commas
        .map { it.trim() }             // Trim any whitespace around the numbers
        .map { it.toInt() }            // Convert each string to an Int
        .toTypedArray()                // Convert List<Int> to Array<Int>
}

class responseViewModel(SharedIPAddress:String,SharedPocketWIFISN:String): ViewModel() {

    val client = OkHttpClient()

    val url = "http://${SharedIPAddress}/"
    val formBody = FormBody.Builder()
        .add("optType", "ReadRealTimeData")
        .add("pwd", SharedPocketWIFISN)
        .build()

    val request = Request.Builder()
        .url(url)
        .post(formBody)
        .build()

    fun getRealTimeData(onError: (Exception) -> Unit = {}, callback: (Array<Int>) -> Unit) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                val data = response.body!!.string()
                val org = JSONObject(data)
                callback(stringToNumberArray(org["Data"].toString()))
            }
            catch (e: Exception)
            {
                onError(e)
            }
        }
    }
    fun getRealTimeData(IP: String, SN: String,callback: (Any) -> Unit) {
        val url = "http://${IP}/"
        val formBody = FormBody.Builder()
            .add("optType", "ReadRealTimeData")
            .add("pwd", SN)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()
        viewModelScope.launch(Dispatchers.IO) {
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            val data = response.body!!.string()
            val org = JSONObject(data)
            callback(org["Data"])
        }
    }
}