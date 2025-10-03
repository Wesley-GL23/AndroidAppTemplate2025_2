package com.ifpr.androidapptemplate.ui.usuario

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ifpr.androidapptemplate.databinding.FragmentPerfilUsuarioBinding
import com.ifpr.androidapptemplate.ui.login.LoginActivity

class PerfilUsuarioFragment : Fragment() {

    private var _binding: FragmentPerfilUsuarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilUsuarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        val user = auth.currentUser
        if (user != null) {
            // Preenche nome e email do FirebaseAuth
            binding.txtUserName.setText(user.displayName ?: "")
            binding.txtUserEmail.setText(user.email ?: "")

            // Puxa endereço e telefone do Realtime Database
            dbRef.child("users").child(user.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val endereco = snapshot.child("endereco").getValue(String::class.java) ?: ""
                            val telefone = snapshot.child("telefone").getValue(String::class.java) ?: ""

                            binding.txtUserAddress.setText(endereco)
                            binding.txtUserPhone.setText(telefone)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // Botão salvar
        binding.salvarButton.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            val nome = binding.txtUserName.text.toString()
            val email = binding.txtUserEmail.text.toString()
            val endereco = binding.txtUserAddress.text.toString()
            val telefone = binding.txtUserPhone.text.toString()
            val senha = binding.txtPassword.text.toString()
            val confirmarSenha = binding.txtConfirmPassword.text.toString()

            // Validação de senha
            if (senha.isNotEmpty()) {
                if (senha == confirmarSenha) {
                    user?.updatePassword(senha)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Senha atualizada!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Erro ao atualizar senha", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Atualiza dados no Realtime Database
            val dados = mapOf(
                "nome" to nome,
                "email" to email,
                "endereco" to endereco,
                "telefone" to telefone
            )

            dbRef.child("users").child(uid)
                .updateChildren(dados)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Erro ao salvar dados", Toast.LENGTH_SHORT).show()
                }
        }

        // Botão logout
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(
                Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
