package com.example.proyekakhir

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.databinding.DetailDokumentasiKegiatanBinding
import com.example.proyekakhir.databinding.DialogPreviewFotoBinding
import com.example.proyekakhir.model.DetailDokumentasiResponse
import com.example.proyekakhir.model.FotoDetail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailDokumentasiActivity : AppCompatActivity() {
    private lateinit var binding: DetailDokumentasiKegiatanBinding
    private var token: String? = null
    private var fotoList: List<FotoDetail> = emptyList()
    private lateinit var fotoAdapter: FotoAdapterDetail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailDokumentasiKegiatanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil token
        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        token = shared.getString("TOKEN", null)

        // Ambil penerima_id dari intent
        val penerimaId = intent.getIntExtra("PENERIMA_ID", 0)

        if (penerimaId == 0) {
            Toast.makeText(this, "ID tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup RecyclerView untuk foto
        setupRecyclerView()

        // Tombol kembali
        binding.btnKembali.setOnClickListener {
            finish()
        }

        // Tombol download semua foto
        binding.downloadContainer.setOnClickListener {
            downloadAllPhotos()
        }

        // Fetch data dokumentasi
        fetchDetailDokumentasi(penerimaId)
    }

    private fun setupRecyclerView() {
        fotoAdapter = FotoAdapterDetail(emptyList()) { foto, position ->
            showPreviewDialog(position)
        }

        binding.recyclerFoto.apply {
            layoutManager = LinearLayoutManager(
                this@DetailDokumentasiActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = fotoAdapter
        }
    }

    private fun fetchDetailDokumentasi(penerimaId: Int) {
        ApiClient.instance.getDetailDokumentasi("Bearer $token", penerimaId)
            .enqueue(object : Callback<DetailDokumentasiResponse> {
                override fun onResponse(
                    call: Call<DetailDokumentasiResponse>,
                    response: Response<DetailDokumentasiResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        data?.let {
                            displayDokumentasi(it)
                        }
                    } else {
                        Toast.makeText(
                            this@DetailDokumentasiActivity,
                            "Gagal memuat detail dokumentasi: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DetailDokumentasiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@DetailDokumentasiActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun displayDokumentasi(data: DetailDokumentasiResponse) {
        with(binding) {
            // Set notulensi
            notulensi.text = if (data.notulensi.isNotEmpty() && data.notulensi != "-") {
                data.notulensi
            } else {
                "Tidak ada notulensi"
            }

            // Set link zoom
            linkZoom.text = if (data.linkZoom.isNotEmpty() && data.linkZoom != "-") {
                data.linkZoom
            } else {
                "Tidak ada link zoom"
            }

            // Set link materi
            linkMateri.text = if (data.linkMateri.isNotEmpty() && data.linkMateri != "-") {
                data.linkMateri
            } else {
                "Tidak ada link materi"
            }

            // Set foto list
            fotoList = data.foto

            if (fotoList.isNotEmpty()) {
                fotoAdapter.updateData(fotoList)
                fotoInfo.text = "Total ${fotoList.size} foto - Klik untuk preview"
                downloadContainer.visibility = View.VISIBLE
            } else {
                fotoInfo.text = "Tidak ada foto"
                downloadContainer.visibility = View.GONE
            }

            // Make links clickable
            makeLinksClickable()
        }
    }

    private fun makeLinksClickable() {
        binding.linkZoom.setOnClickListener {
            val url = binding.linkZoom.text.toString()
            if (url != "Tidak ada link zoom" && url != "-") {
                openUrl(url)
            }
        }

        binding.linkMateri.setOnClickListener {
            val url = binding.linkMateri.text.toString()
            if (url != "Tidak ada link materi" && url != "-") {
                openUrl(url)
            }
        }
    }

    private fun showPreviewDialog(initialPosition: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Gunakan ViewBinding untuk dialog
        val dialogBinding = DialogPreviewFotoBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        var currentPosition = initialPosition

        // Function to load current photo
        fun loadPhoto() {
            val foto = fotoList[currentPosition]
            val fullImageUrl = ApiClient.BASE_IMAGE_URL + foto.fileFoto

            with(dialogBinding) {
                Glide.with(this@DetailDokumentasiActivity)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(fotoPreview)

                fotoCounter.text = "${currentPosition + 1} / ${fotoList.size}"

                // Enable/disable buttons
                btnPrev.isEnabled = currentPosition > 0
                btnNext.isEnabled = currentPosition < fotoList.size - 1
            }
        }

        // Initial load
        loadPhoto()

        // Button listeners dengan ViewBinding
        with(dialogBinding) {
            btnClose.setOnClickListener {
                dialog.dismiss()
            }

            btnPrev.setOnClickListener {
                if (currentPosition > 0) {
                    currentPosition--
                    loadPhoto()
                }
            }

            btnNext.setOnClickListener {
                if (currentPosition < fotoList.size - 1) {
                    currentPosition++
                    loadPhoto()
                }
            }

            btnDownloadSingle.setOnClickListener {
                val foto = fotoList[currentPosition]
                val fullUrl = ApiClient.BASE_IMAGE_URL + foto.fileFoto
                val fileName = foto.fileFoto.substringAfterLast("/")
                downloadImage(fullUrl, fileName)
            }
        }

        dialog.show()
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun downloadAllPhotos() {
        if (fotoList.isEmpty()) {
            Toast.makeText(this, "Tidak ada foto untuk diunduh", Toast.LENGTH_SHORT).show()
            return
        }

        fotoList.forEachIndexed { index, foto ->
            val fullUrl = ApiClient.BASE_IMAGE_URL + foto.fileFoto
            val fileName = foto.fileFoto.substringAfterLast("/")
            downloadImage(fullUrl, "foto_${index + 1}_$fileName")
        }

        Toast.makeText(
            this,
            "Mengunduh ${fotoList.size} foto...",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun openUrl(url: String) {
        try {
            var finalUrl = url
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                finalUrl = "https://$url"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Tidak dapat membuka link: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun downloadImage(fullUrl: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(fullUrl))
                .setTitle("Download Foto Dokumentasi")
                .setDescription("Mengunduh $fileName...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "dokumentasi_$fileName"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Gagal mengunduh: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}