package com.example.joia2026

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class CadastroActivity : AppCompatActivity() {

    private lateinit var layoutNome: TextInputLayout
    private lateinit var layoutCpf: TextInputLayout
    private lateinit var layoutTelefone: TextInputLayout
    private lateinit var layoutEmail: TextInputLayout
    private lateinit var layoutCurso: TextInputLayout
    private lateinit var layoutRole: TextInputLayout
    private lateinit var layoutSenha: TextInputLayout

    private lateinit var edtNome: TextInputEditText
    private lateinit var edtCpf: TextInputEditText
    private lateinit var edtTelefone: TextInputEditText
    private lateinit var edtEmail: TextInputEditText
    private lateinit var autoCompleteCurso: AutoCompleteTextView
    private lateinit var autoCompleteRole: AutoCompleteTextView
    private lateinit var edtSenha: TextInputEditText

    private lateinit var btnCadastrar: MaterialButton
    private lateinit var txtEntrar: TextView

    private var listaCursos: List<Curso> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        // Layouts
        layoutNome = findViewById(R.id.layoutNome)
        layoutCpf = findViewById(R.id.layoutCpf)
        layoutTelefone = findViewById(R.id.layoutTelefone)
        layoutEmail = findViewById(R.id.layoutEmail)
        layoutCurso = findViewById(R.id.layoutCurso)
        layoutRole = findViewById(R.id.layoutRole)
        layoutSenha = findViewById(R.id.layoutSenha)

        // Inputs
        edtNome = findViewById(R.id.edtNome)
        edtCpf = findViewById(R.id.edtCpf)
        edtTelefone = findViewById(R.id.edtTelefone)
        edtEmail = findViewById(R.id.edtEmail)
        autoCompleteCurso = findViewById(R.id.autoCompleteCurso)
        autoCompleteRole = findViewById(R.id.autoCompleteRole)
        edtSenha = findViewById(R.id.edtSenha)

        btnCadastrar = findViewById(R.id.btnCadastrar)
        txtEntrar = findViewById(R.id.txtEntrar)

        // Carregar cursos da API
        carregarCursos()
        configurarTiposUsuario()

        btnCadastrar.setOnClickListener {
            limparErros()

            val nome = edtNome.text.toString().trim()
            val cpf = edtCpf.text.toString().trim()
            val telefone = edtTelefone.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val cursoNome = autoCompleteCurso.text.toString().trim()
            val roleNome = autoCompleteRole.text.toString().trim()
            val senha = edtSenha.text.toString().trim()

            val valido = validarCampos(nome, cpf, telefone, email, cursoNome, roleNome, senha)

            if (valido) {
                val cursoSelecionado = listaCursos.find { it.nome == cursoNome }
                if (cursoSelecionado == null) {
                    layoutCurso.error = "Selecione um curso válido"
                    return@setOnClickListener
                }

                val request = RegisterRequest(
                    nome = nome,
                    email = email,
                    senha = senha,
                    cpf = cpf.ifBlank { null },
                    telefone = telefone.ifBlank { null },
                    cursoId = cursoSelecionado.id,
                    role = roleFromLabel(roleNome)
                )

                btnCadastrar.isEnabled = false

                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.instance.register(request)
                        if (response.isSuccessful) {
                            val authResponse = response.body()

                            // Salva os dados, token e marca como logado para entrar direto
                            UserSession.saveToken(this@CadastroActivity, authResponse?.token)
                            UserSession.saveRegisteredUser(
                                context = this@CadastroActivity,
                                nome = authResponse?.user?.nome ?: nome,
                                email = authResponse?.user?.email ?: email,
                                cpf = authResponse?.user?.cpf ?: cpf,
                                telefone = authResponse?.user?.telefone ?: telefone,
                                id = authResponse?.user?.id,
                                cursoId = authResponse?.user?.cursoId ?: cursoSelecionado.id,
                                cursoNome = cursoSelecionado.nome,
                                role = authResponse?.user?.role ?: request.role
                            )
                            UserSession.markLoggedIn(this@CadastroActivity)

                            Toast.makeText(this@CadastroActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()

                            // Navega direto para a MainActivity limpando o histórico de telas
                            val intent = Intent(this@CadastroActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            btnCadastrar.isEnabled = true
                            val errorBody = response.errorBody()?.string()
                            Log.e("Cadastro", "Erro da API (${response.code()}): $errorBody")
                            Toast.makeText(this@CadastroActivity, "Erro ao cadastrar: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        btnCadastrar.isEnabled = true
                        Log.e("Cadastro", "Excecao no cadastro", e)
                        Toast.makeText(this@CadastroActivity, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        txtEntrar.setOnClickListener {
            finish()
        }
    }

    private fun carregarCursos() {
        lifecycleScope.launch {
            try {
                listaCursos = JoiaRepository.getCursos()
                val nomesCursos = listaCursos.map { it.nome }
                val adapter = ArrayAdapter(this@CadastroActivity, android.R.layout.simple_dropdown_item_1line, nomesCursos)
                autoCompleteCurso.setAdapter(adapter)
            } catch (e: Exception) {
                Log.e("Cadastro", "Erro ao carregar cursos: ${e.message}")
            }
        }
    }

    private fun configurarTiposUsuario() {
        val tipos = listOf("Visualizador", "Administrador")
        autoCompleteRole.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipos))
        autoCompleteRole.setText(tipos.first(), false)
    }

    private fun limparErros() {
        layoutNome.error = null
        layoutCpf.error = null
        layoutTelefone.error = null
        layoutEmail.error = null
        layoutCurso.error = null
        layoutRole.error = null
        layoutSenha.error = null
    }

    private fun validarCampos(nome: String, cpf: String, telefone: String, email: String, curso: String, role: String, senha: String): Boolean {
        var ok = true

        if (nome.isEmpty()) {
            layoutNome.error = "Digite seu nome"
            ok = false
        }

        if (cpf.isNotEmpty() && cpf.length != 11) {
            layoutCpf.error = "CPF deve ter 11 dígitos"
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

        if (roleFromLabel(role).isBlank()) {
            layoutRole.error = "Selecione o tipo de usuario"
            ok = false
        }

        if (senha.length < 6) {
            layoutSenha.error = "Senha deve ter no mínimo 6 caracteres"
            ok = false
        }

        return ok
    }

    private fun roleFromLabel(label: String): String {
        return when (label) {
            "Administrador" -> "ADMIN"
            "Visualizador" -> "VIEWER"
            else -> ""
        }
    }
}
