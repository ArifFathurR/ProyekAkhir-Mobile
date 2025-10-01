package com.example.proyekakhir

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.databinding.TambahDokumentasiKegiatanBinding
import com.example.proyekakhir.model.Dokumentasi
import com.example.proyekakhir.model.DropdownDokumentasiResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class TambahDokumentasiActivity : AppCompatActivity() {

    private lateinit var binding: TambahDokumentasiKegiatanBinding
    private var selectedImageUri: Uri? = null
    private var token: String? = null

    private var selectedKegiatanId: Int? = null
    private var selectedUndanganId: Int? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                binding.preview.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TambahDokumentasiKegiatanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil token dari SharedPreferences
        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        token = shared.getString("TOKEN", null)

        binding.btnKembali.setOnClickListener { finish() }

        binding.uploadGambar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnTambah.setOnClickListener {
            submitDokumentasi()
        }

        setupDropdownDinamically()
    }

    private fun setupDropdownDinamically() {
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan!", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.getDropdownDokumentasi("Bearer $token")
            .enqueue(object : Callback<DropdownDokumentasiResponse> {
                override fun onResponse(
                    call: Call<DropdownDokumentasiResponse>,
                    response: Response<DropdownDokumentasiResponse>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body()?.data ?: emptyList()

                        // Mapping kegiatan_id → nama_kegiatan
                        val kegiatanMap = list.map { it.kegiatan_id to it.nama_kegiatan }.distinct()
                        val kegiatanAdapter = ArrayAdapter(
                            this@TambahDokumentasiActivity,
                            android.R.layout.simple_list_item_1,
                            kegiatanMap.map { it.second }
                        )
                        val edtKegiatan = binding.edtKegiatan as AutoCompleteTextView
                        edtKegiatan.setAdapter(kegiatanAdapter)
                        edtKegiatan.setOnItemClickListener { _, _, position, _ ->
                            selectedKegiatanId = kegiatanMap[position].first
                        }
                        edtKegiatan.setOnFocusChangeListener { v, hasFocus ->
                            if (hasFocus) (v as AutoCompleteTextView).showDropDown()
                        }
                        edtKegiatan.setOnClickListener { (it as AutoCompleteTextView).showDropDown() }

                        // Mapping undangan_id → judul
                        val undanganMap = list.map { it.undangan_id to it.judul }
                        val undanganAdapter = ArrayAdapter(
                            this@TambahDokumentasiActivity,
                            android.R.layout.simple_list_item_1,
                            undanganMap.map { it.second }
                        )
                        val edtUndangan = binding.edtUndangan as AutoCompleteTextView
                        edtUndangan.setAdapter(undanganAdapter)
                        edtUndangan.setOnItemClickListener { _, _, position, _ ->
                            selectedUndanganId = undanganMap[position].first
                        }
                        edtUndangan.setOnFocusChangeListener { v, hasFocus ->
                            if (hasFocus) (v as AutoCompleteTextView).showDropDown()
                        }
                        edtUndangan.setOnClickListener { (it as AutoCompleteTextView).showDropDown() }

                    } else {
                        Toast.makeText(
                            this@TambahDokumentasiActivity,
                            "Gagal ambil data dropdown",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DropdownDokumentasiResponse>, t: Throwable) {
                    Toast.makeText(this@TambahDokumentasiActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun submitDokumentasi() {
        val kegiatanId = selectedKegiatanId
        val undanganId = selectedUndanganId
        val deskripsi = binding.edtNotulensi.text.toString()
        val linkZoom = binding.edtJenisGangguan.text.toString()
        val linkMateri = binding.edtlinkMateri.text.toString()

        if (kegiatanId == null || undanganId == null || deskripsi.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua field!", Toast.LENGTH_SHORT).show()
            return
        }

        val kegiatanBody = RequestBody.create("text/plain".toMediaTypeOrNull(), kegiatanId.toString())
        val undanganBody = RequestBody.create("text/plain".toMediaTypeOrNull(), undanganId.toString())
        val deskripsiBody = RequestBody.create("text/plain".toMediaTypeOrNull(), deskripsi)
        val linkZoomBody = RequestBody.create("text/plain".toMediaTypeOrNull(), linkZoom)
        val linkMateriBody = RequestBody.create("text/plain".toMediaTypeOrNull(), linkMateri)

        // Multipart foto
        val fotoParts = mutableListOf<MultipartBody.Part>()
        selectedImageUri?.let { uri ->
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File(cacheDir, "temp_image.jpg")
            inputStream?.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }

            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), tempFile)
            val part = MultipartBody.Part.createFormData("foto_dokumentasi[]", tempFile.name, requestFile)
            fotoParts.add(part)
        }

        val fotoPartsFinal: List<MultipartBody.Part>? = if (fotoParts.isEmpty()) null else fotoParts

        ApiClient.instance.createDokumentasi(
            undanganBody,
            kegiatanBody,
            deskripsiBody,
            fotoPartsFinal,
            "Bearer $token"
        ).enqueue(object : Callback<Dokumentasi> {
            override fun onResponse(call: Call<Dokumentasi>, response: Response<Dokumentasi>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TambahDokumentasiActivity, "Dokumentasi berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    selectedImageUri?.let {
                        val tempFile = File(cacheDir, "temp_image.jpg")
                        if (tempFile.exists()) tempFile.delete()
                    }
                    finish()
                } else {
                    Toast.makeText(this@TambahDokumentasiActivity, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Dokumentasi>, t: Throwable) {
                Toast.makeText(this@TambahDokumentasiActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
