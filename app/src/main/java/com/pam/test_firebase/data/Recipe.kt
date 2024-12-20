package com.pam.test_firebase.data

data class Recipe(
    val name: String = "",
    val description: String = "",
    val steps: String = "",
    val ingredients: String = "",
    val category: String = "",
    val calories: String = "",
    val time: String = "",
    val imageUrl: String = "",
    var id: String = "",
    val userEmail: String = "",
    val timestamp: String = ""
)
