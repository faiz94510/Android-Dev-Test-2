package com.liburngoding.androiddevtest2.ui.theme.main.utilitas

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import java.text.SimpleDateFormat
import java.util.Locale

object DatePickerUtils {
    fun showDateRangePicker(context: Context, onDateSelected: (startDate: String, endDate: String) -> Unit) {


        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .build()

        // Tampilkan DatePicker
        dateRangePicker.show((context as AppCompatActivity).supportFragmentManager, "DateRangePicker")

        // Set listener untuk mendapatkan rentang tanggal yang dipilih
        dateRangePicker.addOnPositiveButtonClickListener(
            MaterialPickerOnPositiveButtonClickListener { selection ->
                val startDate = selection.first
                val endDate = selection.second

                // Format tanggal yang dipilih
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                val formattedStartDate = dateFormat.format(startDate)
                val formattedEndDate = dateFormat.format(endDate)

                // Callback dengan hasil
                onDateSelected(formattedStartDate, formattedEndDate)
            })
    }
}