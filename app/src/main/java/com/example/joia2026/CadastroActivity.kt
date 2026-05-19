package com.example.joia2026

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CadastroActivity : AppCompatActivity() {

    private lateinit var layoutNome: TextInputLayout
    private lateinit var layoutCpf: TextInputLayout
    private lateinit var layoutEmail: TextInputLayout
    private lateinit var layoutSenha: TextInputLayout
    private lateinit var edtNome: TextInputEditText
    private lateinit var edtCpf: TextInputEditText
    private lateinit var edtEmail: TextInputEditText
    private lateinit var edtSenha: TextInputEditText
    private lateinit var btnCadastrar: MaterialButton
    private lateinit var txtEntrar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        // Mostrar erros nos campos
        layoutNome = findViewById(R.id.layoutNome)
        layoutCpf = findViewById(R.id.layoutCpf)
        layoutEmail = findViewById(R.id.layoutEmail)
        layoutSenha = findViewById(R.id.layoutSenha)

        edtNome = findViewById(R.id.edtNome)
        edtCpf = findViewById(R.id.edtCpf)
        edtEmail = findViewById(R.id.edtEmail)
        edtSenha = findViewById(R.id.edtSenha)
        btnCadastrar = findViewById(R.id.btnCadastrar)
        txtEntrar = findViewById(R.id.txtEntrar)

        btnCadastrar.setOnClickListener {

            val nome = edtNome.text.toString().trim()
            val cpf = edtCpf.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val senha = edtSenha.text.toString().trim()

            if (nome.isEmpty() || cpf.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cpf.length != 11) {
                Toast.makeText(this, "CPF inválido! Deve conter 11 números.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.contains("@")) {
                Toast.makeText(this, "Digite um e-mail válido!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha.length < 6) {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
        }

        txtEntrar.setOnClickListener {
            Toast.makeText(this, "Aqui você pode voltar para Login!", Toast.LENGTH_SHORT).show()
            finish() // fecha essa tela e volta pra anterior
        }

        btnCadastrar.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}