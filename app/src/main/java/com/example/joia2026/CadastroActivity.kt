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

        // Layouts (para mostrar erro no campo)
        layoutNome = findViewById(R.id.layoutNome)
        layoutCpf = findViewById(R.id.layoutCpf)
        layoutEmail = findViewById(R.id.layoutEmail)
        layoutSenha = findViewById(R.id.layoutSenha)

        // Inputs
        edtNome = findViewById(R.id.edtNome)
        edtCpf = findViewById(R.id.edtCpf)
        edtEmail = findViewById(R.id.edtEmail)
        edtSenha = findViewById(R.id.edtSenha)

        btnCadastrar = findViewById(R.id.btnCadastrar)
        txtEntrar = findViewById(R.id.txtEntrar)

        btnCadastrar.setOnClickListener {

            // limpa erros antigos
            limparErros()

            val nome = edtNome.text.toString().trim()
            val cpf = edtCpf.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val senha = edtSenha.text.toString().trim()

            val valido = validarCampos(nome, cpf, email, senha)

            if (valido) {
                Toast.makeText(this, "Dados válidos! Pronto para enviar para API.", Toast.LENGTH_SHORT).show()

                // Aqui futuramente vocês chamam a API do professor
                // cadastrarUsuarioNaApi(nome, cpf, email, senha)
            }

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        txtEntrar.setOnClickListener {
            finish()
        }
    }

    private fun limparErros() {
        layoutNome.error = null
        layoutCpf.error = null
        layoutEmail.error = null
        layoutSenha.error = null
    }

    private fun validarCampos(nome: String, cpf: String, email: String, senha: String): Boolean {
        var ok = true

        // Nome
        if (nome.isEmpty()) {
            layoutNome.error = "Digite seu nome"
            ok = false
        } else if (nome.length < 3) {
            layoutNome.error = "Nome muito curto"
            ok = false
        }

        // CPF
        if (cpf.isEmpty()) {
            layoutCpf.error = "Digite seu CPF"
            ok = false
        } else if (cpf.length != 11) {
            layoutCpf.error = "CPF deve ter 11 números"
            ok = false
        } else if (!cpf.all { it.isDigit() }) {
            layoutCpf.error = "CPF deve conter apenas números"
            ok = false
        } else if (!validarCpf(cpf)) {
            layoutCpf.error = "CPF inválido"
            ok = false
        }

        // Email
        if (email.isEmpty()) {
            layoutEmail.error = "Digite seu e-mail"
            ok = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.error = "E-mail inválido"
            ok = false
        }

        // Senha
        if (senha.isEmpty()) {
            layoutSenha.error = "Digite uma senha"
            ok = false
        } else if (senha.length < 6) {
            layoutSenha.error = "Senha deve ter no mínimo 6 caracteres"
            ok = false
        } else if (!senha.any { it.isUpperCase() }) {
            layoutSenha.error = "A senha deve ter pelo menos 1 letra maiúscula"
            ok = false
        } else if (!senha.any { it.isDigit() }) {
            layoutSenha.error = "A senha deve ter pelo menos 1 número"
            ok = false
        }

        return ok
    }

    // Validação real de CPF (cálculo dos dígitos verificadores)
    private fun validarCpf(cpf: String): Boolean {

        // elimina CPFs repetidos tipo 11111111111
        if (cpf.all { it == cpf[0] }) return false

        try {
            val nums = cpf.map { it.toString().toInt() }

            // primeiro dígito
            var soma = 0
            for (i in 0..8) {
                soma += nums[i] * (10 - i)
            }
            var resto = (soma * 10) % 11
            if (resto == 10) resto = 0
            if (resto != nums[9]) return false

            // segundo dígito
            soma = 0
            for (i in 0..9) {
                soma += nums[i] * (11 - i)
            }
            resto = (soma * 10) % 11
            if (resto == 10) resto = 0
            if (resto != nums[10]) return false

            return true

        } catch (e: Exception) {
            return false
        }
    }
}