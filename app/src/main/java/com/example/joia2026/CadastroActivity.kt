package com.example.joia2026

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CadastroActivity : AppCompatActivity() {

    private lateinit var layoutNome: TextInputLayout
    private lateinit var layoutCpf: TextInputLayout
    private lateinit var layoutTelefone: TextInputLayout
    private lateinit var layoutEmail: TextInputLayout
    private lateinit var layoutCurso: TextInputLayout
    private lateinit var layoutSenha: TextInputLayout

    private lateinit var edtNome: TextInputEditText
    private lateinit var edtCpf: TextInputEditText
    private lateinit var edtTelefone: TextInputEditText
    private lateinit var edtEmail: TextInputEditText
    private lateinit var autoCompleteCurso: AutoCompleteTextView
    private lateinit var edtSenha: TextInputEditText

    private lateinit var btnCadastrar: MaterialButton
    private lateinit var txtEntrar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        // Layouts
        layoutNome = findViewById(R.id.layoutNome)
        layoutCpf = findViewById(R.id.layoutCpf)
        layoutTelefone = findViewById(R.id.layoutTelefone)
        layoutEmail = findViewById(R.id.layoutEmail)
        layoutCurso = findViewById(R.id.layoutCurso)
        layoutSenha = findViewById(R.id.layoutSenha)

        // Inputs
        edtNome = findViewById(R.id.edtNome)
        edtCpf = findViewById(R.id.edtCpf)
        edtTelefone = findViewById(R.id.edtTelefone)
        edtEmail = findViewById(R.id.edtEmail)
        autoCompleteCurso = findViewById(R.id.autoCompleteCurso)
        edtSenha = findViewById(R.id.edtSenha)

        btnCadastrar = findViewById(R.id.btnCadastrar)
        txtEntrar = findViewById(R.id.txtEntrar)

        // Configurar lista de cursos
        val cursos = arrayOf("Análise e Desenvolvimento de Sistemas", "Engenharia de Software", "Sistemas de Informação", "Ciência da Computação")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cursos)
        autoCompleteCurso.setAdapter(adapter)

        btnCadastrar.setOnClickListener {
            limparErros()

            val nome = edtNome.text.toString().trim()
            val cpf = edtCpf.text.toString().trim()
            val telefone = edtTelefone.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val curso = autoCompleteCurso.text.toString().trim()
            val senha = edtSenha.text.toString().trim()

            val valido = validarCampos(nome, cpf, telefone, email, curso, senha)

            if (valido) {
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        txtEntrar.setOnClickListener {
            finish()
        }
    }

    private fun limparErros() {
        layoutNome.error = null
        layoutCpf.error = null
        layoutTelefone.error = null
        layoutEmail.error = null
        layoutCurso.error = null
        layoutSenha.error = null
    }

    private fun validarCampos(nome: String, cpf: String, telefone: String, email: String, curso: String, senha: String): Boolean {
        var ok = true

        if (nome.isEmpty()) {
            layoutNome.error = "Digite seu nome"
            ok = false
        }

        if (cpf.length != 11) {
            layoutCpf.error = "CPF inválido"
            ok = false
        }

        if (telefone.isEmpty()) {
            layoutTelefone.error = "Digite seu telefone"
            ok = false
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.error = "E-mail inválido"
            ok = false
        }

        if (curso.isEmpty()) {
            layoutCurso.error = "Selecione um curso"
            ok = false
        }

        if (senha.length < 6) {
            layoutSenha.error = "Senha muito curta"
            ok = false
        }

        return ok
    }
}