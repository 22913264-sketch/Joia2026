package com.example.joia2026

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class PerfilFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        val txtNome = view.findViewById<TextView>(R.id.txtNomeUsuario)
        val txtEmail = view.findViewById<TextView>(R.id.txtEmailUsuario)
        val txtCpf = view.findViewById<TextView>(R.id.txtCpfValue)
        val btnSair = view.findViewById<MaterialButton>(R.id.btnSair)

        // Dados estáticos para exemplo (idealmente viriam de um SharedPreferences ou API)
        txtNome.text = "Administrador"
        txtEmail.text = "admin@email.com"
        txtCpf.text = "000.000.000-00"

        btnSair.setOnClickListener {
            // Volta para a tela de login
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }
}
