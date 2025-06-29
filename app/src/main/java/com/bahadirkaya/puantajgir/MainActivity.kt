package com.bahadirkaya.puantajgir

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextAd = findViewById<EditText>(R.id.editTextAd)
        val btnGonder = findViewById<Button>(R.id.btnGonder)

        btnGonder.setOnClickListener {
            val ad = editTextAd.text.toString().trim()
            val tarih = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (ad.isNotEmpty()) {
                veriGonder(ad, tarih)
            } else {
                Toast.makeText(this, "Lütfen adınızı girin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun veriGonder(ad: String, tarih: String) {
        val url = "https://script.google.com/macros/s/AKfycbz9KOjXWHFspZfssQAehhNUjrYMMbpFCi05fnwzpNyg8whdaQgA486j-j6yiBSIkGslew/exec"


        val formBody = FormBody.Builder()
            .add("AdSoyad", ad)
            .add("Tarih", tarih)
            .add("Durum", "Geldi")
            .add("Aciklama", "BlueStacks testi")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val cevap = response.body?.string()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Sunucu cevabı: $cevap", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
