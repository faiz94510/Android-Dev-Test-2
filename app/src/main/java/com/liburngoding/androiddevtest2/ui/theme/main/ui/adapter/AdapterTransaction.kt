package com.liburngoding.androiddevtest2.ui.theme.main.ui.adapter


import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liburngoding.androiddevtest2.R
import com.liburngoding.androiddevtest2.ui.theme.main.data.model.Transaction
import com.liburngoding.androiddevtest2.ui.theme.main.data.model.TransaksiItem
import com.liburngoding.androiddevtest2.ui.theme.main.ui.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class AdapterTransaction(private var transaksiItems : MutableList<TransaksiItem>, private val deleteListener: OnTransactionDeleteListener, private val updateListener: OnTransactionUpdateListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_FOOTER = 2
    }
    interface OnTransactionDeleteListener {
        fun onDeleteTransaction(transactionId: Int)
    }
    interface OnTransactionUpdateListener{
        fun onUpdateTransaction(transactionId: Int)
        fun onViewPhoto(path : String)
    }
    fun removeItem(position: Int) {
        transaksiItems.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, transaksiItems.size)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newTransaksiItems: List<TransaksiItem>) {
        transaksiItems.clear() // Kosongkan list lama
        transaksiItems.addAll(newTransaksiItems) // Tambahkan data baru
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (transaksiItems[position]) {
            is TransaksiItem.Header -> VIEW_TYPE_HEADER
            is TransaksiItem.Item -> VIEW_TYPE_ITEM
            is TransaksiItem.Footer -> VIEW_TYPE_FOOTER
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fetch_total, parent, false)
            HeaderViewHolder(view)
        } else if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fetch_item, parent, false)
            ItemViewHolder(view)
        }else if (viewType == VIEW_TYPE_FOOTER){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fetch_total_footer, parent, false)
            FooterViewHolder(view)
        }else{
            throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = transaksiItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when {
            holder is HeaderViewHolder && transaksiItems[position] is TransaksiItem.Header -> {
                val header = transaksiItems[position] as TransaksiItem.Header
                holder.bind(header)
            }
            holder is ItemViewHolder && transaksiItems[position] is TransaksiItem.Item -> {
                val item = transaksiItems[position] as TransaksiItem.Item
                holder.bind(item)
            }
            holder is FooterViewHolder && transaksiItems[position] is TransaksiItem.Footer -> {
                val footer = transaksiItems[position] as TransaksiItem.Footer
                holder.bind(footer)
            }
            else -> throw IllegalArgumentException("Invalid type of data or ViewHolder at position $position")
        }
    }

    class FooterViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val textTotal: TextView? = itemView.findViewById(R.id.total)

        fun bind(footer: TransaksiItem.Footer) {
            textTotal?.text = "Total: Rp ${footer.total}"
        }
    }
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textDate: TextView = itemView.findViewById(R.id.date)
        private val textTotal: TextView? = itemView.findViewById(R.id.total)

        fun bind(header: TransaksiItem.Header) {
            textDate.text = header.tanggal
            textTotal?.text = "Total: Rp ${header.total}"
        }
    }
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textSource : TextView = itemView.findViewById(R.id.source)
        private val textDestination : TextView = itemView.findViewById(R.id.destination)
        private val textTime : TextView = itemView.findViewById(R.id.time)
        private val textDescription : TextView = itemView.findViewById(R.id.description)
        private val textTotal : TextView = itemView.findViewById(R.id.total)
        private val btnDelete : LinearLayout? = itemView.findViewById(R.id.btnDelete)
        private val btnEdit : LinearLayout? = itemView.findViewById(R.id.btnEdit)
        private val btnViewPhoto : TextView? = itemView.findViewById(R.id.btnViewPhoto)

        fun bind(item: TransaksiItem.Item) {
            textSource.text = "Dari ${item.source}"
            textDestination.text = "Ke ${item.destination}"
            textTotal.text = "Rp ${item.total}"
            textDescription.text = item.description
            textTime.text = item.time

            if (item.file.isEmpty()){
                btnViewPhoto?.visibility = View.INVISIBLE
            }

            btnDelete?.setOnClickListener {
                deleteListener.onDeleteTransaction(item.id.toInt())
                removeItem(adapterPosition)
            }

            btnEdit?.setOnClickListener {
                updateListener.onUpdateTransaction(item.id.toInt())
            }
            btnViewPhoto?.setOnClickListener {
                updateListener.onViewPhoto(item.file)
            }
        }
    }

}



