package com.ifpr.androidapptemplate.baseclasses

data class Item(
    val endereco: String? = null,
    val base64Image: String? = null, // mantém compatibilidade com o que já existia
    val imageUrl: String? = null,    // mantém compatibilidade com o que já existia
    val descricao: String? = null,
    val categoria: String? = null,
    val quantidade: Int? = null,
    val data: String? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "endereco" to endereco,
            "base64Image" to base64Image,
            "imageUrl" to imageUrl,
            "descricao" to descricao,
            "categoria" to categoria,
            "quantidade" to quantidade,
            "data" to data
        )
    }
}
