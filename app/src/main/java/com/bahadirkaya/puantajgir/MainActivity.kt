package com.bahadirkaya.puantajgir

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val sharedPrefKey = "personel_id"
    private lateinit var textSonGonderim: TextView
    private lateinit var textSecilenTarih: TextView
    private lateinit var editTextAciklama: EditText
    private lateinit var btnGiris: Button
    private lateinit var btnCikis: Button
    private lateinit var textVersion: TextView
    private var secilenTarihSaat: Calendar? = null

    private val webAppUrl = "https://script.google.com/macros/s/AKfycbyJbYFJz0VjFAfy93gcwG7XoiAcTXv8tIJX76GaujhQ7_mbPDg8KhLSceqcMKguK5VVRg/exec"
    private val currentVersionCode = 5
    private val currentVersionName = "0.5"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextPersonelID = findViewById<EditText>(R.id.editTextPersonelID)
        val btnKaydetID = findViewById<Button>(R.id.btnKaydetID)
        btnGiris = findViewById(R.id.btnGiris)
        btnCikis = findViewById(R.id.btnCikis)
        val btnTarihSec = findViewById<Button>(R.id.btnTarihSec)
        val btnTopluSecim = findViewById<Button>(R.id.btnTopluSecim)
        val imageOnay = findViewById<ImageView>(R.id.imageOnay)
        textSonGonderim = findViewById(R.id.textSonGonderim)
        textSecilenTarih = findViewById(R.id.textSecilenTarih)
        editTextAciklama = findViewById(R.id.editTextAciklama)
        textVersion = findViewById(R.id.textVersion)

        textVersion.text = "Sürüm: v$currentVersionName"

        val sharedPref = getSharedPreferences("PREF", Context.MODE_PRIVATE)
        val savedID = sharedPref.getString(sharedPrefKey, "")
        val sonDurum = sharedPref.getString("sonDurum", "")
        val sonTarih = sharedPref.getString("sonTarih", "")

        btnGiris.isEnabled = false
        btnCikis.isEnabled = false
        textSecilenTarih.text = ""

        if (!sonDurum.isNullOrEmpty() && !sonTarih.isNullOrEmpty()) {
            textSonGonderim.text = "$sonTarih - $sonDurum"
        } else {
            textSonGonderim.text = getString(R.string.lutfen_tarih_seciniz)
            textSonGonderim.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        }

        if (!savedID.isNullOrEmpty()) {
            editTextPersonelID.visibility = View.GONE
            btnKaydetID.visibility = View.GONE
        } else {
            btnKaydetID.setOnClickListener {
                val id = editTextPersonelID.text.toString().trim()
                if (id.isNotEmpty()) {
                    with(sharedPref.edit()) {
                        putString(sharedPrefKey, id)
                        apply()
                    }
                    Toast.makeText(this, getString(R.string.personel_id_kaydedildi), Toast.LENGTH_SHORT).show()
                    editTextPersonelID.visibility = View.GONE
                    btnKaydetID.visibility = View.GONE
                } else {
                    Toast.makeText(this, getString(R.string.id_bos_olamaz), Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnTarihSec.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                TimePickerDialog(this, { _, hour, minute ->
                    secilenTarihSaat = Calendar.getInstance().apply {
                        set(y, m, d, hour, minute)
                    }

                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val tarihStr = sdf.format(secilenTarihSaat!!.time)
                    textSecilenTarih.text = getString(R.string.secilen_tarih, tarihStr)
                    textSonGonderim.text = ""

                    when (sonDurum) {
                        "GİRİŞ" -> {
                            btnGiris.isEnabled = false
                            btnCikis.isEnabled = true
                        }
                        "ÇIKIŞ" -> {
                            btnGiris.isEnabled = true
                            btnCikis.isEnabled = false
                        }
                        else -> {
                            btnGiris.isEnabled = true
                            btnCikis.isEnabled = true
                        }
                    }



                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnTopluSecim.setOnClickListener {
            val intent = Intent(this, TopluSecimActivity::class.java)
            startActivity(intent)
        }

        btnGiris.setOnClickListener {
            val id = sharedPref.getString(sharedPrefKey, null)
            if (id != null && secilenTarihSaat != null) {
                veriGonder(id, "GİRİŞ", imageOnay)
            } else {
                Toast.makeText(this, "Lütfen tarih seçiniz", Toast.LENGTH_SHORT).show()
            }
        }

        btnCikis.setOnClickListener {
            val id = sharedPref.getString(sharedPrefKey, null)
            if (id != null && secilenTarihSaat != null) {
                veriGonder(id, "ÇIKIŞ", imageOnay)
            } else {
                Toast.makeText(this, "Lütfen tarih seçiniz", Toast.LENGTH_SHORT).show()
            }
        }

        textSecilenTarih.setOnLongClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                TimePickerDialog(this, { _, hour, minute ->
                    secilenTarihSaat = Calendar.getInstance().apply {
                        set(y, m, d, hour, minute)
                    }
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val tarihStr = sdf.format(secilenTarihSaat!!.time)
                    textSecilenTarih.text = getString(R.string.secilen_tarih, tarihStr)
                    Toast.makeText(this, "Toplu seçim yapıldı", Toast.LENGTH_SHORT).show()
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
            true
        }

        checkForUpdate()
    }
    private fun checkForUpdate() {
        val request = Request.Builder()
            .url(webAppUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: return
                val json = JSONObject(responseBody)

                val latestVersionCode: Int
                val latestApkUrl: String

                if (json.has("data")) {
                    val dataArray = json.getJSONArray("data")
                    val last = dataArray.getJSONObject(dataArray.length() - 1)
                    latestVersionCode = last.getInt("versionCode")
                    latestApkUrl = last.getString("apkUrl")
                } else {
                    latestVersionCode = json.getInt("versionCode")
                    latestApkUrl = json.getString("apkUrl")
                }

                if (latestVersionCode > currentVersionCode) {
                    runOnUiThread {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Yeni Güncelleme Mevcut")
                            .setMessage("Yeni sürüm indirilsin mi?")
                            .setPositiveButton("Evet") { _, _ ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(latestApkUrl))
                                startActivity(intent)
                            }
                            .setNegativeButton("Hayır", null)
                            .show()
                    }
                }
            }
        })
    }
    private fun veriGonder(personelID: String, durum: String, imageOnay: ImageView) {
        val tarih = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(secilenTarihSaat!!.time)
        val kayitTarihi = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val aciklama = editTextAciklama.text.toString().trim()

        val formBody = FormBody.Builder()
            .add("personelID", personelID)
            .add("tarih", tarih)
            .add("durum", durum)
            .add("kayitTarihi", kayitTarihi)
            .add("aciklama", aciklama)
            .build()

        val request = Request.Builder().url(webAppUrl).post(formBody).build()

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
                    imageOnay.visibility = View.VISIBLE
                    imageOnay.postDelayed({ imageOnay.visibility = View.GONE }, 2000)

                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val tarihStr = sdf.format(secilenTarihSaat!!.time)
                    textSonGonderim.text = "$tarihStr - $durum"

                    val sharedPref = getSharedPreferences("PREF", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("sonDurum", durum)
                        putString("sonTarih", tarihStr)
                        apply()
                    }

                    if (durum == "GİRİŞ") {
                        btnGiris.isEnabled = false
                        btnCikis.isEnabled = true
                    } else if (durum == "ÇIKIŞ") {
                        btnCikis.isEnabled = false
                        btnGiris.isEnabled = true
                    }
                }
            }
        })
    }

}
