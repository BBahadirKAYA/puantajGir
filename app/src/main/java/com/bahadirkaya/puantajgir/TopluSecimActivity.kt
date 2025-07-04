package com.bahadirkaya.puantajgir

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class TopluSecimActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val tarihListesi = mutableListOf<String>()
    private var secilenSaatDakika: Pair<Int, Int>? = null
    private lateinit var listAdapter: ArrayAdapter<String>

    private val webAppUrl = "https://script.google.com/macros/s/AKfycbyJbYFJz0VjFAfy93gcwG7XoiAcTXv8tIJX76GaujhQ7_mbPDg8KhLSceqcMKguK5VVRg/exec"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toplu_secim)

        val btnTarihEkle = findViewById<Button>(R.id.btnTarihEkle)
        val listViewTarihler = findViewById<ListView>(R.id.listViewTarihler)
        val btnSaatSec = findViewById<Button>(R.id.btnSaatSec)
        val textSaatSecim = findViewById<TextView>(R.id.textSaatSecim)
        val radioDurum = findViewById<RadioGroup>(R.id.radioDurum)
        val editAciklama = findViewById<EditText>(R.id.editAciklamaToplu)
        val btnGonder = findViewById<Button>(R.id.btnGonder)

        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tarihListesi)
        listViewTarihler.adapter = listAdapter

        btnTarihEkle.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val cal = Calendar.getInstance()
                cal.set(y, m, d)
                val tarih = sdf.format(cal.time)
                if (!tarihListesi.contains(tarih)) {
                    tarihListesi.add(tarih)
                    listAdapter.notifyDataSetChanged()
                }
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnSaatSec.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(this, { _, saat, dakika ->
                secilenSaatDakika = Pair(saat, dakika)
                textSaatSecim.text = String.format("%02d:%02d", saat, dakika)
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
        }

        btnGonder.setOnClickListener {
            val sharedPref = getSharedPreferences("PREF", Context.MODE_PRIVATE)
            val personelID = sharedPref.getString("personel_id", null)
            val aciklama = editAciklama.text.toString().trim()
            val seciliDurum = if (radioDurum.checkedRadioButtonId == R.id.radioGiris) "GİRİŞ" else "ÇIKIŞ"

            if (personelID.isNullOrEmpty()) {
                Toast.makeText(this, "Personel ID bulunamadı", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tarihListesi.isEmpty()) {
                Toast.makeText(this, "Lütfen en az bir tarih seçin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (secilenSaatDakika == null) {
                Toast.makeText(this, "Lütfen saat seçin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val saat = secilenSaatDakika!!.first
            val dakika = secilenSaatDakika!!.second

            val sdfFull = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val now = Date()

            for (tarihStr in tarihListesi) {
                val cal = Calendar.getInstance()
                val parcalar = tarihStr.split("-")
                cal.set(parcalar[0].toInt(), parcalar[1].toInt() - 1, parcalar[2].toInt(), saat, dakika)

                val tamTarih = sdfFull.format(cal.time)
                val kayitTarihi = sdfFull.format(now)

                val formBody = FormBody.Builder()
                    .add("personelID", personelID)
                    .add("tarih", tamTarih)
                    .add("durum", seciliDurum)
                    .add("kayitTarihi", kayitTarihi)
                    .add("aciklama", aciklama)
                    .build()

                val request = Request.Builder().url(webAppUrl).post(formBody).build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this@TopluSecimActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        runOnUiThread {
                            Toast.makeText(this@TopluSecimActivity, "Gönderildi: $tarihStr", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }
    }
}
