package com.ifpr.androidapptemplate.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // Campos do layout
    private lateinit var enderecoEditText: EditText
    private lateinit var descricaoEditText: EditText
    private lateinit var categoriaEditText: EditText
    private lateinit var quantidadeEditText: EditText
    private lateinit var dataEditText: EditText
    private lateinit var horaEditText: EditText

    private lateinit var itemImageView: ImageView
    private var imageUri: Uri? = null

    private lateinit var salvarButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Ligação dos campos do layout
        itemImageView = view.findViewById(R.id.image_item)
        salvarButton = view.findViewById(R.id.salvarItemButton)
        selectImageButton = view.findViewById(R.id.button_select_image)
        enderecoEditText = view.findViewById(R.id.enderecoItemEditText)
        descricaoEditText = view.findViewById(R.id.etDescricao)
        categoriaEditText = view.findViewById(R.id.etCategoria)
        quantidadeEditText = view.findViewById(R.id.etQuantidade)
        dataEditText = view.findViewById(R.id.etData)
        horaEditText = view.findViewById(R.id.etHora)

        auth = FirebaseAuth.getInstance()

        selectImageButton.setOnClickListener { openFileChooser() }
        salvarButton.setOnClickListener { salvarItem() }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun salvarItem() {
        val endereco = enderecoEditText.text.toString().trim()
        val descricao = descricaoEditText.text.toString().trim()
        val categoria = categoriaEditText.text.toString().trim()
        val quantidade = quantidadeEditText.text.toString().trim()
        val data = dataEditText.text.toString().trim()
        val hora = horaEditText.text.toString().trim()

        if (endereco.isEmpty() || descricao.isEmpty() || categoria.isEmpty() ||
            quantidade.isEmpty() || data.isEmpty() || hora.isEmpty() || imageUri == null
        ) {
            Toast.makeText(context, "Por favor, preencha todos os campos e selecione uma imagem", Toast.LENGTH_SHORT).show()
            return
        }

        uploadImageToFirestore(descricao, categoria, quantidade, data, hora, endereco)
    }

    private fun uploadImageToFirestore(
        descricao: String,
        categoria: String,
        quantidade: String,
        data: String,
        hora: String,
        endereco: String
    ) {
        if (imageUri != null) {
            val inputStream = context?.contentResolver?.openInputStream(imageUri!!)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

                val item = Item(
                    endereco = endereco,
                    base64Image = base64Image,
                    descricao = descricao,
                    categoria = categoria,
                    quantidade = quantidade.toIntOrNull(),
                    data = "$data $hora" // une data e hora
                )

                saveItemIntoDatabase(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(itemImageView)
        }
    }

    private fun saveItemIntoDatabase(item: Item) {
        databaseReference = FirebaseDatabase.getInstance().getReference("itens")
        val itemId = databaseReference.push().key

        if (itemId != null) {
            databaseReference.child(auth.uid.toString()).child(itemId).setValue(item)
                .addOnSuccessListener {
                    Toast.makeText(context, "Item cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Falha ao cadastrar o item", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Erro ao gerar o ID do item", Toast.LENGTH_SHORT).show()
        }
    }
}
