package com.example.joia2026

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.joia2026.R.id
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Usando TextInputEditText para ser compatível com o TextInputLayout no XML
        val etEmail = findViewById<TextInputEditText>(id.etEmail)
        val etSenha = findViewById<TextInputEditText>(id.etSenha)
        val btnLogin = findViewById<Button>(id.btnLogin)
        // Corrigido: No layout XML, btnIrCadastro é um TextView, não um Button
        val btnIrCadastro = findViewById<TextView>(id.btnIrCadastro)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val senha = etSenha.text.toString()

            if (email == "admin@email.com" && senha == "123456") {
                Toast.makeText(this, "Login realizado!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Email ou senha inválidos", Toast.LENGTH_SHORT).show()
            }
        }

        btnIrCadastro.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }
    }
}
