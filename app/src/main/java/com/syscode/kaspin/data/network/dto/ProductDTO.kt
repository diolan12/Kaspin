package com.syscode.kaspin.data.network.dto

import com.google.gson.annotations.SerializedName


data class ProductDTO(
    val category: String,
    val description: String,
    val id: Int,
    val image: String,
    val price: Double,
//    @SerializedName("rating")
    val rating: RatingDTO,
    val title: String
)