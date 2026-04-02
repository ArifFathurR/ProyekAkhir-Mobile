package com.example.proyekakhir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyekakhir.adapter.KegiatanAdapter
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.databinding.FragmentKalenderBinding
import com.example.proyekakhir.model.KegiatanResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Kalender : Fragment() {

    private var _binding: FragmentKalenderBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: KegiatanAdapter
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKalenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shared = requireContext().getSharedPreferences("APP", android.content.Context.MODE_PRIVATE)
        token = shared.getString("TOKEN", null)

        setupAdapter()
        fetchSemuaKegiatan()
    }

    private fun setupAdapter() {
        adapter = KegiatanAdapter(
            emptyList(),
            onUndanganClick = {},
            onDetailClick = {}
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun fetchSemuaKegiatan() {
        ApiClient.instance.getKegiatan("Bearer $token")
            .enqueue(object : Callback<KegiatanResponse> {
                override fun onResponse(call: Call<KegiatanResponse>, response: Response<KegiatanResponse>) {
                    if (response.isSuccessful) {
                        adapter.updateData(response.body()?.kegiatan ?: emptyList(), false)
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat data: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<KegiatanResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}