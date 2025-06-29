package com.bahadirkaya.puantajgir

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val sharedPrefKey = "personel_id"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextPersonelID = findViewById<EditText>(R.id.editTextPersonelID)
        val btnKaydetID = findViewById<Button>(R.id.btnKaydetID)
        val btnGiris = findViewById<Button>(R.id.btnGiris)
        val btnCikis = findViewById<Button>(R.id.btnCikis)

        val sharedPref = getSharedPreferences("PREF", Context.MODE_PRIVATE)
        val savedID = sharedPref.getString(sharedPrefKey, "")

        // ID kaydedildiyse ID giriş alanlarını gizle
        if (!savedID.isNullOrEmpty()) {
            editTextPersonelID.visibility = View.GONE
            btnKaydetID.visibility = View.GONE
        } else {
            btnKaydetID.setOnClickListener {
                val id = editTextPersonelID.text.toString().trim()
                if (id.isNotEmpty()) {
                    sharedPref.edit().putString(sharedPrefKey, id).apply()
                    Toast.makeText(this, "Personel ID kaydedildi", Toast.LENGTH_SHORT).show()
                    editTextPersonelID.visibility = View.GONE
                    btnKaydetID.visibility = View.GONE
                } else {
                    Toast.makeText(this, "ID boş olamaz", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Giriş butonu
        btnGiris.setOnClickListener {
            val id = sharedPref.getString(sharedPrefKey, null)
            if (id != null) {
                veriGonder(id, "GİRİŞ")
            } else {
                Toast.makeText(this, "Personel ID bulunamadı", Toast.LENGTH_SHORT).show()
            }
        }

        // Çıkış butonu
        btnCikis.setOnClickListener {
            val id = sharedPref.getString(sharedPrefKey, null)
            if (id != null) {
                veriGonder(id, "ÇIKIŞ")
            } else {
                Toast.makeText(this, "Personel ID bulunamadı", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun veriGonder(personelID: String, durum: String) {
        val tarih = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val url= "https://script.google.com/macros/s/AKfycbxeknRqNq-JFaWpC7MJGJgaGKGWtoYQNNWfTA8M5neCg_ZeeMZnawq5jQC_A-orH9H0lA/exec"


        val formBody = FormBody.Builder()
            .add("personelID", personelID)
            .add("tarih", tarih)
            .add("durum", durum)
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
                    Toast.makeText(this@MainActivity, "Sunucu: $cevap", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
