package com.bahadirkaya.puantajgir

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
    private lateinit var textSonGonderim: TextView
    private lateinit var textSecilenTarih: TextView
    private lateinit var editTextAciklama: EditText
    private var secilenTarihSaat: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextPersonelID = findViewById<EditText>(R.id.editTextPersonelID)
        val btnKaydetID = findViewById<Button>(R.id.btnKaydetID)
        val btnGiris = findViewById<Button>(R.id.btnGiris)
        val btnCikis = findViewById<Button>(R.id.btnCikis)
        val btnTarihSec = findViewById<Button>(R.id.btnTarihSec)
        val imageOnay = findViewById<ImageView>(R.id.imageOnay)
        textSonGonderim = findViewById(R.id.textSonGonderim)
        textSecilenTarih = findViewById(R.id.textSecilenTarih)
        editTextAciklama = findViewById(R.id.editTextAciklama)

        val sharedPref = getSharedPreferences("PREF", Context.MODE_PRIVATE)
        val savedID = sharedPref.getString(sharedPrefKey, "")

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

        textSecilenTarih.text = getString(R.string.secilen_tarih, getString(R.string.su_an))

        btnTarihSec.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                TimePickerDialog(this, { _, hour, minute ->
                    secilenTarihSaat.set(y, m, d, hour, minute)
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    textSecilenTarih.text = getString(R.string.secilen_tarih, sdf.format(secilenTarihSaat.time))
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnGiris.setOnClickListener {
            val id = sharedPref.getString(sharedPrefKey, null)
            if (id != null) {
                veriGonder(id, "GİRİŞ", imageOnay)
            } else {
                Toast.makeText(this, getString(R.string.personel_id_bulunamadi), Toast.LENGTH_SHORT).show()
            }
        }

        btnCikis.setOnClickListener {
            val id = sharedPref.getString(sharedPrefKey, null)
            if (id != null) {
                veriGonder(id, "ÇIKIŞ", imageOnay)
            } else {
                Toast.makeText(this, getString(R.string.personel_id_bulunamadi), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun veriGonder(personelID: String, durum: String, imageOnay: ImageView) {
        val tarih = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(secilenTarihSaat.time)
        val kayitTarihi = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val aciklama = editTextAciklama.text.toString().trim()
        val url = "https://script.google.com/macros/s/AKfycbz4xLtfVirU_scp6Zy6LVbAGWezMSxW_Q3VwRGgTLUXIshuJqrDdB5lMQmyymsgdklouw/exec"

        val formBody = FormBody.Builder()
            .add("personelID", personelID)
            .add("tarih", tarih)
            .add("durum", durum)
            .add("kayitTarihi", kayitTarihi)
            .add("aciklama", aciklama)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, getString(R.string.hata, e.message), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val cevap = response.body?.string()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, getString(R.string.sunucu_cevap, cevap), Toast.LENGTH_LONG).show()
                    imageOnay.visibility = View.VISIBLE
                    imageOnay.postDelayed({ imageOnay.visibility = View.GONE }, 2000)
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val tarihStr = sdf.format(secilenTarihSaat.time)
                    textSonGonderim.text = getString(R.string.son_gonderim, tarihStr, durum)
                }
            }
        })
    }
}
