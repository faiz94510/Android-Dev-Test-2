package com.liburngoding.androiddevtest2.ui.theme.main.ui.bottomsheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.liburngoding.androiddevtest2.R


class CustomBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.bottom_sheet_layout, container, false)
    }

    override fun getTheme(): Int {
        // Menggunakan style Bottom Sheet dengan Material Design 3
        return R.style.CustomBottomSheetDialogTheme
    }

}