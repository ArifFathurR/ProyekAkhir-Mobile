package com.example.proyekakhir

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.databinding.EditDokumentasiKegiatanBinding
import com.example.proyekakhir.model.Dokumentasi
import com.example.proyekakhir.model.DokumentasiResponse
import com.example.proyekakhir.model.DropdownDokumentasiResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class EditDokumentasiActivity : AppCompatActivity() {

    private lateinit var binding: EditDokumentasiKegiatanBinding
    private lateinit var fotoAdapter: FotoAdapter
    private val listFoto = mutableListOf<Any>()
    private val deletedFotoIds = mutableListOf<Int>()
    private var dokumentasiId: Int = 0
    private var kegiatanId: Int? = null
    private var undanganId: Int? = null
    private var token: String = ""

    private val pilihGambar =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                listFoto.addAll(uris)
                fotoAdapter.notifyDataSetChanged()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditDokumentasiKegiatanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = getSharedPreferences("APP", MODE_PRIVATE).getString("TOKEN", null) ?: ""
        dokumentasiId = intent.getIntExtra("id", 0)

        fotoAdapter = FotoAdapter(listFoto) { pos, item ->
            // jika foto lama (String berisi id#path), simpan ID-nya untuk dihapus
            if (item is String && item.contains("#")) {
                val fotoId = item.substringBefore("#").toInt()
                deletedFotoIds.add(fotoId)
            }
            fotoAdapter.removeAt(pos)
        }

        binding.recyclerFoto.apply {
            layoutManager = LinearLayoutManager(this@EditDokumentasiActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = fotoAdapter
        }

        binding.btnKembali.setOnClickListener { finish() }
        binding.btnPilihGambar.setOnClickListener { pilihGambar.launch("image/*") }
        binding.btnTambah.setOnClickListener { updateDokumentasi() }

        setupDropdownDinamically()
    }

    private fun setupDropdownDinamically() {
        ApiClient.instance.getDropdownDokumentasi("Bearer $token")
            .enqueue(object : Callback<DropdownDokumentasiResponse> {
                override fun onResponse(
                    call: Call<DropdownDokumentasiResponse>,
                    response: Response<DropdownDokumentasiResponse>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body()?.data ?: emptyList()
                        val kegiatanMap = list.map { it.kegiatan_id to it.nama_kegiatan }.distinct()
                        val undanganMap = list.map { it.undangan_id to it.judul }

                        val kegiatanAdapter = ArrayAdapter(
                            this@EditDokumentasiActivity,
                            android.R.layout.simple_list_item_1,
                            kegiatanMap.map { it.second }
                        )
                        (binding.edtKegiatan as AutoCompleteTextView).apply {
                            setAdapter(kegiatanAdapter)
                            setOnItemClickListener { _, _, pos, _ ->
                                kegiatanId = kegiatanMap[pos].first
                            }
                        }

                        val undanganAdapter = ArrayAdapter(
                            this@EditDokumentasiActivity,
                            android.R.layout.simple_list_item_1,
                            undanganMap.map { it.second }
                        )
                        (binding.edtUndangan as AutoCompleteTextView).apply {
                            setAdapter(undanganAdapter)
                            setOnItemClickListener { _, _, pos, _ ->
                                undanganId = undanganMap[pos].first
                            }
                        }

                        getDetailDokumentasi(kegiatanMap, undanganMap)
                    }
                }

                override fun onFailure(call: Call<DropdownDokumentasiResponse>, t: Throwable) {
                    Toast.makeText(this@EditDokumentasiActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getDetailDokumentasi(
        kegiatanMap: List<Pair<Int, String>>,
        undanganMap: List<Pair<Int, String>>
    ) {
        ApiClient.instance.getDokumentasiSaya("Bearer $token")
            .enqueue(object : Callback<DokumentasiResponse> {
                override fun onResponse(
                    call: Call<DokumentasiResponse>,
                    response: Response<DokumentasiResponse>
                ) {
                    if (response.isSuccessful) {
                        val dok = response.body()?.dokumentasi?.find { it.id == dokumentasiId }
                        if (dok != null) tampilkanData(dok, kegiatanMap, undanganMap)
                    }
                }

                override fun onFailure(call: Call<DokumentasiResponse>, t: Throwable) {
                    Toast.makeText(this@EditDokumentasiActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun tampilkanData(
        dok: Dokumentasi,
        kegiatanMap: List<Pair<Int, String>>,
        undanganMap: List<Pair<Int, String>>
    ) {
        binding.edtNotulensi.setText(dok.notulensi ?: "")
        binding.edtLinkZoom.setText(dok.linkZoom ?: "")
        binding.edtlinkMateri.setText(dok.linkMateri ?: "")

        kegiatanId = dok.kegiatanId
        undanganId = dok.undanganId

        binding.edtKegiatan.setText(kegiatanMap.find { it.first == kegiatanId }?.second ?: "", false)
        binding.edtUndangan.setText(undanganMap.find { it.first == undanganId }?.second ?: "", false)

        dok.fotoDokumentasi?.forEach { foto ->
            fotoAdapter.addFotoUrl("${foto.id}#${foto.foto}")
        }
    }

    private fun updateDokumentasi() {
        val notulensiBody = binding.edtNotulensi.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val linkZoomBody = binding.edtLinkZoom.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val linkMateriBody = binding.edtlinkMateri.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val kegiatanIdBody = (kegiatanId ?: 0).toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val undanganIdBody = (undanganId ?: 0).toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val deletedFotoBody = deletedFotoIds.joinToString(",").toRequestBody("text/plain".toMediaTypeOrNull())

        val fotoParts = mutableListOf<MultipartBody.Part>()
        listFoto.filterIsInstance<Uri>().forEach { uri ->
            val file = uriToFile(uri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            fotoParts.add(MultipartBody.Part.createFormData("foto_dokumentasi[]", file.name, requestFile))
        }

        ApiClient.instance.updateDokumentasi(
            id = dokumentasiId,
            undanganId = undanganIdBody,
            kegiatanId = kegiatanIdBody,
            deskripsi = notulensiBody,
            link_zoom = linkZoomBody,
            link_materi = linkMateriBody,
            deleted_foto_ids = deletedFotoBody,
            foto = if (fotoParts.isEmpty()) null else fotoParts,
            token = "Bearer $token"
        ).enqueue(object : Callback<Dokumentasi> {
            override fun onResponse(call: Call<Dokumentasi>, response: Response<Dokumentasi>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditDokumentasiActivity, "Berhasil update dokumentasi", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@EditDokumentasiActivity, "Gagal update data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Dokumentasi>, t: Throwable) {
                Toast.makeText(this@EditDokumentasiActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uriToFile(uri: Uri): File {
        val file = File(cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }
}
