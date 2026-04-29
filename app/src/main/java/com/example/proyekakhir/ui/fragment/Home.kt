package com.example.proyekakhir.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyekakhir.ui.dokumentasi.DokumentasiSayaActivity
import com.example.proyekakhir.ui.semua_dokumentasi.KegiatanSelesai
import com.example.proyekakhir.ui.semua_dokumentasi.LihatDokumentasi
import com.example.proyekakhir.ui.presensi.Presensi
import com.example.proyekakhir.R
import com.example.proyekakhir.adapter.KegiatanAdapter
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.databinding.FragmentHomeBinding
import com.example.proyekakhir.model.KegiatanResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: KegiatanAdapter
    private var token: String? = null
    private var isTabAkanDatang = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shared = requireContext().getSharedPreferences("APP", android.content.Context.MODE_PRIVATE)
        val userName = shared.getString("USER_NAME", "Pengguna")
        token = shared.getString("TOKEN", null)

        binding.txUsername.text = "$userName"

        setupAdapter()
        setActiveTab(true)
        fetchKegiatan()

        binding.btnLihatDokumentasi.setOnClickListener {
            startActivity(Intent(requireContext(), KegiatanSelesai::class.java))
        }

        binding.btnPresensi.setOnClickListener {
            startActivity(Intent(requireContext(), Presensi::class.java))
        }

        binding.btnBuatDokumentasi.setOnClickListener {
            startActivity(Intent(requireContext(), DokumentasiSayaActivity::class.java))
        }

        binding.tabAkanDatang.setOnClickListener {
            isTabAkanDatang = true
            setActiveTab(true)
            fetchKegiatan()
        }

        binding.tabSelesai.setOnClickListener {
            isTabAkanDatang = false
            setActiveTab(false)
            fetchKegiatanSelesai()
        }

        // SwipeRefresh listener
        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.blue)
        )
        binding.swipeRefresh.setOnRefreshListener {
            if (isTabAkanDatang) fetchKegiatan() else fetchKegiatanSelesai()
        }
    }

    private fun setupAdapter() {
        adapter = KegiatanAdapter(
            emptyList(),
            onUndanganClick = { fileUrl -> openPdf(fileUrl) },
            onDetailClick = { id ->
                val intent = Intent(requireContext(), LihatDokumentasi::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setActiveTab(isAkanDatangActive: Boolean) {
        if (isAkanDatangActive) {
            binding.tabAkanDatang.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tab_active_bg))
            binding.tvAkanDatang.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_active_text))
            binding.tabSelesai.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tab_inactive_bg))
            binding.tvSelesai.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_inactive_text))
        } else {
            binding.tabSelesai.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tab_active_bg))
            binding.tvSelesai.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_active_text))
            binding.tabAkanDatang.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tab_inactive_bg))
            binding.tvAkanDatang.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_inactive_text))
        }
    }

    private fun fetchKegiatan() {
        ApiClient.instance.getKegiatanAkanDatang("Bearer $token")
            .enqueue(object : Callback<KegiatanResponse> {
                override fun onResponse(call: Call<KegiatanResponse>, response: Response<KegiatanResponse>) {
                    binding.swipeRefresh.isRefreshing = false  // stop animasi
                    if (response.isSuccessful) {
                        adapter.updateData(response.body()?.kegiatan ?: emptyList(), false)
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat data: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<KegiatanResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false  // stop animasi
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchKegiatanSelesai() {
        ApiClient.instance.getKegiatanSelesai("Bearer $token")
            .enqueue(object : Callback<KegiatanResponse> {
                override fun onResponse(call: Call<KegiatanResponse>, response: Response<KegiatanResponse>) {
                    binding.swipeRefresh.isRefreshing = false  // stop animasi
                    if (response.isSuccessful) {
                        adapter.updateData(response.body()?.kegiatan ?: emptyList(), true)
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat data: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<KegiatanResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false  // stop animasi
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun openPdf(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(url), "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Tidak ada aplikasi PDF terpasang", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}