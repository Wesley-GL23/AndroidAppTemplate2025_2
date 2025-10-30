package com.ifpr.androidapptemplate.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ifpr.androidapptemplate.MainActivity
import com.ifpr.androidapptemplate.R

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var btnGoogle: com.google.android.gms.common.SignInButton

    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient

    // Activity Result para o Google Sign-In
    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    // Sucesso: vai para a MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Falha no login: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Cancelado/erro: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Views
        emailEditText = findViewById(R.id.edit_text_email)
        passwordEditText = findViewById(R.id.edit_text_password)
        loginButton = findViewById(R.id.button_login)
        registerLink = findViewById(R.id.registerLink)
        btnGoogle = findViewById(R.id.btnGoogle)

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Se já estiver logado, pula direto
        auth.currentUser?.let {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Configuração do Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // gerado pelo google-services.json
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        // Clique do botão Google
        btnGoogle.setOnClickListener {
            googleLauncher.launch(googleClient.signInIntent)
        }

        // (Opcional) Login com email/senha
        loginButton.setOnClickListener {
            val email = emailEditText.text?.toString().orEmpty()
            val pass = passwordEditText.text?.toString().orEmpty()
            if (email.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Falha: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // (Opcional) Link de cadastro
        registerLink.setOnClickListener {
            // abrir tela de cadastro se tiver
        }
    }
}
