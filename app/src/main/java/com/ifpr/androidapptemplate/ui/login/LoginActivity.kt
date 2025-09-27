package com.ifpr.androidapptemplate.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ifpr.androidapptemplate.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.ifpr.androidapptemplate.MainActivity
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.ui.usuario.CadastroUsuarioActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var btnGoogleSignIn: SignInButton
    private lateinit var googleSignInClient: GoogleSignInClient
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
    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "signInWithEmail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(this)

        // Inicializa o Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Views
        emailEditText = findViewById(R.id.edit_text_email)
        passwordEditText = findViewById(R.id.edit_text_password)
        loginButton = findViewById(R.id.button_login)
        registerLink = findViewById(R.id.registerLink)
        btnGoogleSignIn = findViewById<SignInButton>(R.id.btnGoogleSignIn)

        val registerLink: TextView = findViewById(R.id.registerLink)
        registerLink.setOnClickListener {
            val intent: Intent = Intent(
                applicationContext,
                CadastroUsuarioActivity::class.java
            )
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            signIn(email, password)
        }

        // Configuration do Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up the sign-in button click handler
        btnGoogleSignIn.setOnClickListener {
            signInGoogle()
        }
    }

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    updateUI(firebaseAuth.currentUser)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Navegue para a proxima atividade
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(
                applicationContext,
                "Email ou senha incorretos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login bem-sucedido, navegar para a atividade principal ou atualizar UI
                    Log.d(TAG, "signInWithGoogle:success")
                    updateUI(firebaseAuth.currentUser)
                } else {
                    // Tratar falha de login
                    Log.w(TAG, "signInWithGoogle:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Tratar falha de login
            }
        }
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
