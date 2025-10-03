package com.ifpr.androidapptemplate.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var btnGoogle: com.google.android.gms.common.SignInButton

    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient
    private lateinit var googleLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Views
        emailEditText = findViewById(R.id.edit_text_email)
        passwordEditText = findViewById(R.id.edit_text_password)
        loginButton = findViewById(R.id.button_login)
        registerLink = findViewById(R.id.registerLink)
        btnGoogle = findViewById(R.id.btnGoogle)

        // Firebase
        auth = FirebaseAuth.getInstance()

        // Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        // Activity Result
        googleLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { signIn ->
                    if (signIn.isSuccessful) {
                        Toast.makeText(this, "Login com Google OK!", Toast.LENGTH_SHORT).show()
                        goToMain()
                    } else {
                        Toast.makeText(this, "Falha no FirebaseAuth", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In cancelado/erro", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoogle.setOnClickListener {
            googleLauncher.launch(googleClient.signInIntent)
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val pass = passwordEditText.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login OK!", Toast.LENGTH_SHORT).show()
                        goToMain()
                    } else {
                        Toast.makeText(this, "Email/Senha inválidos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        registerLink.setOnClickListener {
            Toast.makeText(this, "Abrir tela de cadastro…", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        // Se já está logado, pula o login
        if (FirebaseAuth.getInstance().currentUser != null) {
            goToMain()
        }
    }

    private fun goToMain() {
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        // finish() é opcional aqui (a task anterior já foi limpa)
        // finish()
    }
}
