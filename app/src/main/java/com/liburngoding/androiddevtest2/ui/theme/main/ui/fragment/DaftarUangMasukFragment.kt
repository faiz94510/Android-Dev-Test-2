package com.liburngoding.androiddevtest2.ui.theme.main.ui.fragment

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.liburngoding.androiddevtest2.R
import com.liburngoding.androiddevtest2.databinding.FragmentDaftarUangMasukBinding
import com.liburngoding.androiddevtest2.ui.theme.main.data.model.Transaction
import com.liburngoding.androiddevtest2.ui.theme.main.data.model.TransaksiItem
import com.liburngoding.androiddevtest2.ui.theme.main.ui.adapter.AdapterTransaction
import com.liburngoding.androiddevtest2.ui.theme.main.ui.viewmodel.TransactionViewModel
import com.liburngoding.androiddevtest2.ui.theme.main.utilitas.DatePickerUtils
import java.text.SimpleDateFormat
import java.util.Locale


class DaftarUangMasukFragment : Fragment(), AdapterTransaction.OnTransactionDeleteListener, AdapterTransaction.OnTransactionUpdateListener {

    private var _binding: FragmentDaftarUangMasukBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionViewModel : TransactionViewModel
    private lateinit var adapter: AdapterTransaction
    var getStartDate: String = ""
    var getEndDate : String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDaftarUangMasukBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AdapterTransaction(mutableListOf(), this, this) // Inisialisasi adapter dengan list kosong
        binding.recyclerView.adapter = adapter

        transactionViewModel.allTransaction.observe(viewLifecycleOwner) { transaksiList ->
            updateRecyclerView(transaksiList)
        }
        transactionViewModel.filteredTransactions.observe(viewLifecycleOwner) { transaksiList ->
            updateRecyclerView(transaksiList)
        }

        binding.btnCreateTransaction.setOnClickListener {
            val editTransactionFragment = InputUangMasukFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment, editTransactionFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnCalender?.setOnClickListener {
            DatePickerUtils.showDateRangePicker(requireContext()) { startDate, endDate ->
                binding.rangeDate!!.text = "$startDate -    $endDate"

                transactionViewModel.updateTransactionsByDateRange(startDate, endDate)
            }
        }
        binding.icBackActivity?.setOnClickListener{
            parentFragmentManager.popBackStack()
        }
    }

    private fun updateRecyclerView(transaksiList: List<Transaction>) {
        val transaksiItems = getGroupedTransaksi(transaksiList)
        adapter.updateData(transaksiItems)
    }

    private fun getGroupedTransaksi(transaksiList: List<Transaction>): MutableList<TransaksiItem> {
        val groupedData = mutableListOf<TransaksiItem>()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val groupedMap = transaksiList.groupBy {
            dateFormat.format(it.created_at)
        }


        for ((tanggal, transaksiByTanggal) in groupedMap) {
            val total = transaksiByTanggal.sumBy {
                it.income_amount.toIntOrNull() ?: 0
            }
            groupedData.add(TransaksiItem.Header(tanggal.toString(), total))

            groupedData.addAll(transaksiByTanggal.map { transaksi ->
                TransaksiItem.Item(timeFormat.format(transaksi.created_at), transaksi.id.toString(), transaksi.destination_data, transaksi.source_data, transaksi.description, transaksi.income_amount.toInt(), transaksi.files)
            })
            groupedData.add(TransaksiItem.Footer(total))
        }

        return groupedData
    }

    override fun onUpdateTransaction(transactionId: Int) {
        val editTransactionFragment = InputUangMasukFragment.newInstance(transactionId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment, editTransactionFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onViewPhoto(path: String) {
        showCustomDialog(requireContext(), path)
    }
    fun showCustomDialog(context: Context, path : String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_view_photo, null)

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val viewPhoto : ImageView = dialogView.findViewById(R.id.viewPhoto)

        val bitmap = BitmapFactory.decodeFile(path)
        viewPhoto.setImageBitmap(bitmap)

        dialog.show()
    }

    override fun onDeleteTransaction(transactionId: Int) {
        transactionViewModel.deleteTransaction(transactionId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    


}