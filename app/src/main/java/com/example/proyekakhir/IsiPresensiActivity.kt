package com.example.proyekakhir

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.databinding.AbsensiBinding
import com.example.proyekakhir.model.TtdResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class IsiPresensiActivity : AppCompatActivity() {

    private lateinit var binding: AbsensiBinding
    private var kegiatanId: Int = 0
    private var token: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AbsensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kegiatanId = intent.getIntExtra("KEGIATAN_ID", 0)
        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        token = shared.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Tombol navigasi
        binding.btnKembali.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }

        // Bersihkan tanda tangan
        binding.btnClearSignature.setOnClickListener {
            binding.signaturePad.clear()
        }

        // Ambil lokasi
        binding.btnGetLocation.setOnClickListener {
            ambilLokasi()
        }

        // Simpan tanda tangan
        binding.btnSave.setOnClickListener {
            if (binding.signaturePad.isEmpty) {
                Toast.makeText(this, "Harap isi tanda tangan terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (latitude == null || longitude == null) {
                Toast.makeText(this, "Harap ambil lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bitmap = binding.signaturePad.signatureBitmap
            val base64Ttd = convertBitmapToBase64(bitmap)

            kirimTtdKeServer(base64Ttd)
        }
    }

    private fun ambilLokasi() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        locationManager.requestSingleUpdate(
            LocationManager.GPS_PROVIDER,
            object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    latitude = location.latitude
                    longitude = location.longitude

                    binding.etLatitude.setText(latitude.toString())
                    binding.etLongitude.setText(longitude.toString())
                    Toast.makeText(this@IsiPresensiActivity, "Lokasi berhasil diambil", Toast.LENGTH_SHORT).show()
                }
            },
            null
        )
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) ambilLokasi()
            else Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
        }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun kirimTtdKeServer(base64Ttd: String) {
        ApiClient.instance.storeTtd(
            token = "Bearer $token",
            penerimaId = kegiatanId,
            ttd = base64Ttd,
            latitude = latitude,
            longitude = longitude
        ).enqueue(object : Callback<TtdResponse> {
            override fun onResponse(call: Call<TtdResponse>, response: Response<TtdResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@IsiPresensiActivity, "Presensi berhasil disimpan", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val msg = response.body()?.message ?: response.message()
                    Toast.makeText(this@IsiPresensiActivity, "Gagal: $msg", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TtdResponse>, t: Throwable) {
                Toast.makeText(this@IsiPresensiActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
