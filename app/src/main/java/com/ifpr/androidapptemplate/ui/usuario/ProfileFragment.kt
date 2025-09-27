package com.ifpr.androidapptemplate.ui.usuario

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.ui.login.LoginActivity

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_perfil_usuario, container, false)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val txtUserName = view.findViewById<TextView>(R.id.txtUserName)
        val txtUserEmail = view.findViewById<TextView>(R.id.txtUserEmail)
        val btnLogout   = view.findViewById<Button>(R.id.btnLogout)

        txtUserName.text = user?.displayName ?: "Usuário"
        txtUserEmail.text = user?.email ?: ""

        btnLogout.setOnClickListener { logout() }

        return view
    }

    private fun logout() {
        // Firebase
        auth.signOut()

        // Google (se entrou com Google)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(requireActivity(), gso).signOut()

        // Voltar para o Login
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
