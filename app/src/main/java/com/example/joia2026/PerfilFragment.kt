package com.example.joia2026

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {
    private lateinit var imgAvatar: ShapeableImageView
    private lateinit var edtNomePerfil: TextInputEditText
    private lateinit var autoCompleteCursoPerfil: AutoCompleteTextView
    private lateinit var txtEmailUsuario: TextView
    private lateinit var txtCpfValue: TextView
    private lateinit var txtTelefoneValue: TextView

    private var listaCursos: List<Curso> = emptyList()

    private val escolherFoto = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult

        requireContext().contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        UserSession.saveProfilePhoto(requireContext(), uri.toString())
        imgAvatar.setImageURI(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        imgAvatar = view.findViewById(R.id.imgAvatar)
        edtNomePerfil = view.findViewById(R.id.edtNomePerfil)
        autoCompleteCursoPerfil = view.findViewById(R.id.autoCompleteCursoPerfil)
        txtEmailUsuario = view.findViewById(R.id.txtEmailUsuario)
        txtCpfValue = view.findViewById(R.id.txtCpfValue)
        txtTelefoneValue = view.findViewById(R.id.txtTelefoneValue)
        val btnAlterarFoto = view.findViewById<MaterialButton>(R.id.btnAlterarFoto)
        val btnSalvarPerfil = view.findViewById<MaterialButton>(R.id.btnSalvarPerfil)
        val btnSair = view.findViewById<MaterialButton>(R.id.btnSair)

        preencherDados()
        carregarCursos()

        btnAlterarFoto.setOnClickListener {
            escolherFoto.launch(arrayOf("image/*"))
        }

        btnSalvarPerfil.setOnClickListener {
            salvarPerfil()
        }

        btnSair.setOnClickListener {
            UserSession.clear(requireContext())
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    private fun preencherDados() {
        val userData = UserSession.getUserData(requireContext())
        edtNomePerfil.setText(userData.nome ?: "Usuario JOIA")
        autoCompleteCursoPerfil.setText(userData.cursoNome.orEmpty(), false)
        txtEmailUsuario.text = userData.email ?: "usuario@email.com"
        txtCpfValue.text = userData.cpf ?: "000.000.000-00"
        txtTelefoneValue.text = userData.telefone ?: "-"

        userData.fotoPerfilUri?.let { uri ->
            imgAvatar.setImageURI(Uri.parse(uri))
        }
    }

    private fun carregarCursos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCursos()
                if (response.isSuccessful) {
                    listaCursos = response.body() ?: emptyList()
                    val nomesCursos = listaCursos.map { it.nome }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        nomesCursos
                    )
                    autoCompleteCursoPerfil.setAdapter(adapter)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao carregar cursos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun salvarPerfil() {
        val nome = edtNomePerfil.text.toString().trim()
        val cursoNome = autoCompleteCursoPerfil.text.toString().trim()
        val cursoSelecionado = listaCursos.find { it.nome == cursoNome }

        if (nome.isEmpty()) {
            edtNomePerfil.error = "Digite seu nome"
            return
        }

        if (cursoNome.isNotEmpty() && cursoSelecionado == null) {
            autoCompleteCursoPerfil.error = "Selecione um curso valido"
            return
        }

        UserSession.updateProfile(
            context = requireContext(),
            nome = nome,
            cursoId = cursoSelecionado?.id,
            cursoNome = cursoSelecionado?.nome ?: cursoNome.takeIf { it.isNotEmpty() }
        )

        Toast.makeText(requireContext(), "Perfil atualizado", Toast.LENGTH_SHORT).show()
    }
}
