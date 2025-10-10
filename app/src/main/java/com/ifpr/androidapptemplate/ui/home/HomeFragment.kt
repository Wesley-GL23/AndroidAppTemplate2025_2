package com.ifpr.androidapptemplate.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.util.Base64
import android.widget.*
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val container = view.findViewById<LinearLayout>(R.id.itemContainer)
        carregarItensMarketplace(container)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun carregarItensMarketplace(container: LinearLayout) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(container.context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("itens").child(userId)

        databaseRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                container.removeAllViews()

                if (!snapshot.exists()) {
                    Toast.makeText(container.context, "Nenhum item cadastrado", Toast.LENGTH_SHORT).show()
                    return
                }

                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(com.ifpr.androidapptemplate.baseclasses.Item::class.java) ?: continue

                    val itemView = LayoutInflater.from(container.context)
                        .inflate(R.layout.item_template, container, false)

                    val imageView = itemView.findViewById<ImageView>(R.id.item_image)
                    val enderecoView = itemView.findViewById<TextView>(R.id.item_endereco)
                    val descricaoView = itemView.findViewById<TextView>(R.id.item_descricao)
                    val categoriaView = itemView.findViewById<TextView>(R.id.item_categoria)

                    enderecoView.text = "Endereço: ${item.endereco ?: "Não informado"}"
                    descricaoView.text = "Descrição: ${item.descricao ?: "Sem descrição"}"
                    categoriaView.text = "Categoria: ${item.categoria ?: "Não informada"}"

                    if (!item.imageUrl.isNullOrEmpty()) {
                        Glide.with(container.context).load(item.imageUrl).into(imageView)
                    } else if (!item.base64Image.isNullOrEmpty()) {
                        try {
                            val bytes = Base64.decode(item.base64Image, Base64.DEFAULT)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            imageView.setImageBitmap(bitmap)
                        } catch (_: Exception) {}
                    }

                    container.addView(itemView)
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(container.context, "Erro ao carregar dados: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}