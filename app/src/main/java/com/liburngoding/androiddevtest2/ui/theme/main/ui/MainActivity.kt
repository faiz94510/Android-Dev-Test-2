package com.liburngoding.androiddevtest2.ui.theme.main.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.liburngoding.androiddevtest2.R
import com.liburngoding.androiddevtest2.ui.theme.main.ui.fragment.DaftarUangMasukFragment
import com.liburngoding.androiddevtest2.ui.theme.main.ui.fragment.InputUangMasukFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            // Tambahkan fragment ke activity
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, DaftarUangMasukFragment()) // ID container di layout dan instance Fragment
                .commit()
        }
    }
}