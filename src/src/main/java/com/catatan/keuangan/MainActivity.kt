package com.catatan.keuangan

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var tvSaldo: TextView
    private lateinit var tvPemasukan: TextView
    private lateinit var tvPengeluaran: TextView
    private lateinit var listContainer: LinearLayout
    private val transaksiList = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadData()

        val rootScroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#F4F6F9"))
            isFillViewport = true
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 48)
        }

        val tvTitle = TextView(this).apply {
            text = "Catatan Keuangan"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1E293B"))
        }
        mainLayout.addView(tvTitle)

        // Kartu Ringkasan Saldo
        val summaryCard = createCard("#1E293B").apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 32 }
        }

        val lblSaldo = TextView(this).apply {
            text = "Total Saldo"
            textSize = 14f
            setTextColor(Color.parseColor("#94A3B8"))
        }
        tvSaldo = TextView(this).apply {
            text = "Rp 0"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
        }

        val rowDetail = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 24 }
        }

        tvPemasukan = TextView(this).apply {
            text = "Masuk: Rp 0"
            textSize = 13f
            setTextColor(Color.parseColor("#4ADE80"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        tvPengeluaran = TextView(this).apply {
            text = "Keluar: Rp 0"
            textSize = 13f
            setTextColor(Color.parseColor("#F87171"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        rowDetail.addView(tvPemasukan)
        rowDetail.addView(tvPengeluaran)

        summaryCard.addView(lblSaldo)
        summaryCard.addView(tvSaldo)
        summaryCard.addView(rowDetail)
        mainLayout.addView(summaryCard)

        // Kartu Input Transaksi
        val inputCard = createCard("#FFFFFF").apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 32 }
        }

        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
        }
        val rbKeluar = RadioButton(this).apply {
            id = 1001
            text = "Pengeluaran (-)"
            isChecked = true
            setTextColor(Color.parseColor("#1E293B"))
        }
        val rbMasuk = RadioButton(this).apply {
            id = 1002
            text = "Pemasukan (+)"
            setTextColor(Color.parseColor("#1E293B"))
            setPadding(32, 0, 0, 0)
        }
        radioGroup.addView(rbKeluar)
        radioGroup.addView(rbMasuk)

        val etNominal = EditText(this).apply {
            hint = "Nominal (contoh: 25000)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.parseColor("#1E293B"))
            setHintTextColor(Color.parseColor("#94A3B8"))
        }

        val etKeterangan = EditText(this).apply {
            hint = "Keterangan (contoh: Makan siang)"
            inputType = InputType.TYPE_CLASS_TEXT
            setTextColor(Color.parseColor("#1E293B"))
            setHintTextColor(Color.parseColor("#94A3B8"))
        }

        val btnSimpan = Button(this).apply {
            text = "Simpan Transaksi"
            setBackgroundColor(Color.parseColor("#2563EB"))
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 24 }
        }

        btnSimpan.setOnClickListener {
            val nominalText = etNominal.text.toString().trim()
            val ketText = etKeterangan.text.toString().trim()

            if (nominalText.isEmpty()) {
                Toast.makeText(this, "Isi nominal dulu ya!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nominal = nominalText.toLongOrNull() ?: 0L
            if (nominal <= 0L) {
                Toast.makeText(this, "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tipe = if (radioGroup.checkedRadioButtonId == 1002) "MASUK" else "KELUAR"
            val ketFinal = if (ketText.isEmpty()) (if (tipe == "MASUK") "Pemasukan" else "Pengeluaran") else ketText
            val waktu = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(Date())

            val item = JSONObject().apply {
                put("tipe", tipe)
                put("nominal", nominal)
                put("keterangan", ketFinal)
                put("waktu", waktu)
            }

            val temp = JSONArray()
            temp.put(item)
            for (i in 0 until transaksiList.length()) {
                temp.put(transaksiList.getJSONObject(i))
            }
            while (transaksiList.length() > 0) {
                transaksiList.remove(0)
            }
            for (i in 0 until temp.length()) {
                transaksiList.put(temp.getJSONObject(i))
            }

            saveData()
            refreshUI()

            etNominal.text.clear()
            etKeterangan.text.clear()
            Toast.makeText(this, "Tersimpan!", Toast.LENGTH_SHORT).show()
        }

        inputCard.addView(radioGroup)
        inputCard.addView(etNominal)
        inputCard.addView(etKeterangan)
        inputCard.addView(btnSimpan)
        mainLayout.addView(inputCard)

        val tvRiwayatTitle = TextView(this).apply {
            text = "Riwayat Transaksi"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1E293B"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 40; bottomMargin = 16 }
        }
        mainLayout.addView(tvRiwayatTitle)

        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        mainLayout.addView(listContainer)

        rootScroll.addView(mainLayout)
        setContentView(rootScroll)

        refreshUI()
    }

    private fun createCard(hexColor: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 36, 36, 36)
            background = GradientDrawable().apply {
                setColor(Color.parseColor(hexColor))
                cornerRadius = 28f
            }
        }
    }

    private fun formatRupiah(angka: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        return "Rp ${formatter.format(angka)}"
    }

    private fun refreshUI() {
        var totalMasuk = 0L
        var totalKeluar = 0L
        listContainer.removeAllViews()

        if (transaksiList.length() == 0) {
            val emptyText = TextView(this).apply {
                text = "Belum ada catatan transaksi."
                textSize = 14f
                setTextColor(Color.parseColor("#64748B"))
                gravity = Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            listContainer.addView(emptyText)
        }

        for (i in 0 until transaksiList.length()) {
            val obj = transaksiList.getJSONObject(i)
            val tipe = obj.getString("tipe")
            val nominal = obj.getLong("nominal")
            val ket = obj.getString("keterangan")
            val waktu = obj.getString("waktu")

            if (tipe == "MASUK") totalMasuk += nominal else totalKeluar += nominal

            val itemCard = createCard("#FFFFFF").apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 20 }
            }

            val infoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvKet = TextView(this).apply {
                text = ket
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#1E293B"))
            }
            val tvWaktu = TextView(this).apply {
                text = waktu
                textSize = 12f
                setTextColor(Color.parseColor("#64748B"))
            }
            val tvNominalItem = TextView(this).apply {
                text = (if (tipe == "MASUK") "+ " else "- ") + formatRupiah(nominal)
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor(if (tipe == "MASUK") "#16A34A" else "#DC2626"))
                setPadding(0, 8, 0, 0)
            }

            infoLayout.addView(tvKet)
            infoLayout.addView(tvWaktu)
            infoLayout.addView(tvNominalItem)

            val btnHapus = Button(this).apply {
                text = "Hapus"
                textSize = 12f
                setBackgroundColor(Color.parseColor("#FEE2E2"))
                setTextColor(Color.parseColor("#DC2626"))
                setOnClickListener {
                    transaksiList.remove(i)
                    saveData()
                    refreshUI()
                }
            }

            itemCard.addView(infoLayout)
            itemCard.addView(btnHapus)
            listContainer.addView(itemCard)
        }

        val saldo = totalMasuk - totalKeluar
        tvSaldo.text = formatRupiah(saldo)
        tvPemasukan.text = "Masuk: ${formatRupiah(totalMasuk)}"
        tvPengeluaran.text = "Keluar: ${formatRupiah(totalKeluar)}"
    }

    private fun saveData() {
        val prefs = getSharedPreferences("keuangan_db", Context.MODE_PRIVATE)
        prefs.edit().putString("data_transaksi", transaksiList.toString()).apply()
    }

    private fun loadData() {
        val prefs = getSharedPreferences("keuangan_db", Context.MODE_PRIVATE)
        val raw = prefs.getString("data_transaksi", "[]") ?: "[]"
        val loaded = JSONArray(raw)
        while (transaksiList.length() > 0) {
            transaksiList.remove(0)
        }
        for (i in 0 until loaded.length()) {
            transaksiList.put(loaded.getJSONObject(i))
        }
    }
}
