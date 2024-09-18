package com.liburngoding.androiddevtest2.ui.theme.main.data.model

sealed class TransaksiItem {
    data class Header(val tanggal: String, val total: Int) : TransaksiItem()
    data class Item(val time: String, val id : String, val destination: String, val source: String, val description: String, val total: Int, val file : String) : TransaksiItem()
    data class Footer(val total: Int) : TransaksiItem()
}