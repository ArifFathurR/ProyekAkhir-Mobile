package com.example.proyekakhir.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyekakhir.adapter.PresensiListAdapter
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.auth.LoginActivity
import com.example.proyekakhir.databinding.FragmentProfilBinding
import com.example.proyekakhir.model.PresensiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Profil : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PresensiListAdapter
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shared = requireContext().getSharedPreferences("APP", android.content.Context.MODE_PRIVATE)
        val userName = shared.getString("USER_NAME", "Pengguna")
        token = shared.getString("TOKEN", null)

        binding.tvNama.text = userName

        setupAdapter()
        fetchRiwayatPresensi()

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun setupAdapter() {
        adapter = PresensiListAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun fetchRiwayatPresensi() {
        ApiClient.instance.getRiwayatPresensi("Bearer $token")
            .enqueue(object : Callback<PresensiResponse> {
                override fun onResponse(call: Call<PresensiResponse>, response: Response<PresensiResponse>) {
                    if (response.isSuccessful) {
                        val list = response.body()?.data ?: emptyList()
                        adapter = PresensiListAdapter(list)
                        binding.recyclerView.adapter = adapter
                        if (list.isEmpty()) {
                            Toast.makeText(requireContext(), "Belum ada riwayat presensi", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat data (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PresensiResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun logout() {
        val shared = requireContext().getSharedPreferences("APP", android.content.Context.MODE_PRIVATE)
        ApiClient.instance.logout("Bearer $token")
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    shared.edit().remove("TOKEN").apply()
                    Toast.makeText(requireContext(), "Logout berhasil", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Gagal logout: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}