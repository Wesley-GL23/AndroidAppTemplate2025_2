package com.ifpr.androidapptemplate.ui.usuario

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.ui.login.LoginActivity

class ProfileFragment : Fragment() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    private lateinit var txtName: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtAddress: EditText
    private lateinit var btnSalvar: Button
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_perfil_usuario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtName = view.findViewById(R.id.txtUserName)
        txtEmail = view.findViewById(R.id.txtUserEmail)
        txtAddress = view.findViewById(R.id.txtUserAddress)
        btnSalvar = view.findViewById(R.id.salvarButton)
        btnLogout = view.findViewById(R.id.btnLogout)

        val user = auth.currentUser
        txtName.setText(user?.displayName ?: "")
        txtEmail.setText(user?.email ?: "")

        // Carrega endereço salvo (se existir)
        user?.uid?.let { uid ->
            db.child("users").child(uid).get().addOnSuccessListener { snap ->
                val address = snap.child("address").getValue(String::class.java)
                if (!address.isNullOrBlank()) txtAddress.setText(address)
            }
        }

        // Salvar alterações no Realtime Database
        btnSalvar.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val name = txtName.text.toString().trim()
            val email = txtEmail.text.toString().trim()
            val address = txtAddress.text.toString().trim()

            val payload = mapOf(
                "name" to name,
                "email" to email,
                "address" to address
            )

            db.child("users").child(uid).updateChildren(payload).addOnSuccessListener {
                // opcional: Toast
            }
        }

        // Logout (Firebase + Google) e volta pro Login
        btnLogout.setOnClickListener { logout() }
    }

    private fun logout() {
        auth.signOut()

        // Se você usou Google Sign-In em LoginActivity, também encerra a sessão Google:
        /*
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(requireActivity(), gso).signOut()
        */

        val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
