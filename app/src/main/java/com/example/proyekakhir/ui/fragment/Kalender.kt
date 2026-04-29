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
import com.example.proyekakhir.R
import com.example.proyekakhir.adapter.KegiatanAdapter
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.databinding.FragmentKalenderBinding
import com.example.proyekakhir.model.Kegiatan
import com.example.proyekakhir.model.KegiatanResponse
import com.example.proyekakhir.ui.semua_dokumentasi.LihatDokumentasi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Kalender : Fragment() {

    private var _binding: FragmentKalenderBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: KegiatanAdapter
    private var token: String? = null

    private var allKegiatan: List<Kegiatan> = emptyList()
    private val eventDates = mutableSetOf<String>()

    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))

    // Tab state
    private var isTabAkanDatang = true
    private var currentFilterDate: String? = null

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
        setupCalendar()
        setupTabs()
        fetchSemuaKegiatan()
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

    private fun setupTabs() {
        setActiveTab(true)

        binding.tabAkanDatang.setOnClickListener {
            isTabAkanDatang = true
            setActiveTab(true)
            applyFilter()
        }

        binding.tabSelesai.setOnClickListener {
            isTabAkanDatang = false
            setActiveTab(false)
            applyFilter()
        }
    }

    private fun setActiveTab(isAkanDatang: Boolean) {
        if (isAkanDatang) {
            binding.tabAkanDatang.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.blue)
            )
            binding.tvAkanDatang.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            binding.tabSelesai.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            binding.tvSelesai.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.blue)
            )
        } else {
            binding.tabSelesai.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.blue)
            )
            binding.tvSelesai.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            binding.tabAkanDatang.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            binding.tvAkanDatang.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.blue)
            )
        }
    }

    private fun setupCalendar() {
        updateMonthLabel()

        binding.btnPrevMonth.setOnClickListener {
            binding.customCalendarView.displayCalendar.add(Calendar.MONTH, -1)
            updateMonthLabel()
            binding.customCalendarView.refresh()
        }

        binding.btnNextMonth.setOnClickListener {
            binding.customCalendarView.displayCalendar.add(Calendar.MONTH, 1)
            updateMonthLabel()
            binding.customCalendarView.refresh()
        }

        binding.customCalendarView.onDateClick = { dateStr ->
            binding.customCalendarView.selectedDateStr = dateStr
            binding.customCalendarView.invalidate()

            val cal = Calendar.getInstance()
            cal.time = apiDateFormat.parse(dateStr)!!
            binding.tvSelectedDate.text = "Kegiatan: ${displayDateFormat.format(cal.time)}"
            binding.tvReset.visibility = View.VISIBLE

            currentFilterDate = dateStr
            applyFilter()
        }

        binding.tvReset.setOnClickListener {
            binding.customCalendarView.selectedDateStr = null
            binding.customCalendarView.invalidate()
            binding.tvSelectedDate.text = "Semua Kegiatan"
            binding.tvReset.visibility = View.GONE
            currentFilterDate = null
            applyFilter()
        }
    }

    private fun applyFilter() {
        val today = apiDateFormat.format(Calendar.getInstance().time)

        var filtered = allKegiatan.filter { kegiatan ->
            val tgl = kegiatan.tanggal?.substring(0, 10) ?: return@filter false
            if (isTabAkanDatang) tgl >= today else tgl < today
        }

        currentFilterDate?.let { dateStr ->
            filtered = filtered.filter { kegiatan ->
                kegiatan.tanggal?.substring(0, 10) == dateStr
            }
        }

        adapter.updateData(filtered, false)

        if (filtered.isEmpty() && currentFilterDate != null) {
            Toast.makeText(requireContext(), "Tidak ada kegiatan pada tanggal ini", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMonthLabel() {
        binding.tvMonthYear.text = monthYearFormat.format(
            binding.customCalendarView.displayCalendar.time
        )
    }

    private fun buildEventDates() {
        eventDates.clear()
        allKegiatan.forEach { kegiatan ->
            try {
                val dateStr = kegiatan.tanggal?.substring(0, 10)
                if (!dateStr.isNullOrEmpty()) eventDates.add(dateStr)
            } catch (e: Exception) { e.printStackTrace() }
        }
        binding.customCalendarView.eventDateSet = eventDates
        binding.customCalendarView.refresh()
    }

    private fun fetchSemuaKegiatan() {
        ApiClient.instance.getKegiatan("Bearer $token")
            .enqueue(object : Callback<KegiatanResponse> {
                override fun onResponse(call: Call<KegiatanResponse>, response: Response<KegiatanResponse>) {
                    if (response.isSuccessful) {
                        allKegiatan = response.body()?.kegiatan ?: emptyList()
                        buildEventDates()
                        applyFilter()
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat data: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<KegiatanResponse>, t: Throwable) {
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