package com.example.foccuss.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.foccuss.databinding.ActivityOverlayBinding

class OverlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOverlayBinding
    private var blockedPackageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOverlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar janela para ficar por cima de outras
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // Obter pacote bloqueado
        blockedPackageName = intent.getStringExtra("PACKAGE_NAME")

        // Configurar botão para voltar à tela inicial
        binding.btnGoHome.setOnClickListener {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
            finish()
        }
    }

    // Impedir que o usuário volte usando o botão back
    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Não fazer nada para impedir o botão back
    }
}