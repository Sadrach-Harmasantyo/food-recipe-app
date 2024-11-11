package com.pam.test_firebase

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pam.test_firebase.data.Recipe
import java.util.UUID

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var recipeNameEditText: EditText
    private lateinit var recipeDescriptionEditText: EditText
    private lateinit var recipeStepsEditText: EditText
    private lateinit var recipeCaloriesEditText: EditText
    private lateinit var recipeTimeEditText: EditText
    private lateinit var recipeIngredientsEditText: EditText

    private lateinit var selectedCategory: String
    private lateinit var buttonCategoryBreakfast: Button
    private lateinit var buttonCategoryLunch: Button
    private lateinit var buttonCategoryDinner: Button

    private lateinit var updateButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var selectedImageView: ImageView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var recipeId: String? = null
    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Get recipeId from the intent
        recipeId = intent.getStringExtra("recipeId")

        // Initialize UI elements
        recipeNameEditText = findViewById(R.id.recipeNameEditText)
        recipeDescriptionEditText = findViewById(R.id.recipeDescriptionEditText)
        recipeStepsEditText = findViewById(R.id.recipeStepsEditText)
        recipeCaloriesEditText = findViewById(R.id.recipeCaloriesEditText)
        recipeTimeEditText = findViewById(R.id.recipeTimeEditText)
        recipeIngredientsEditText = findViewById(R.id.recipeIngridientsEditText)

        buttonCategoryBreakfast = findViewById(R.id.buttonCategoryBreakfast)
        buttonCategoryLunch = findViewById(R.id.buttonCategoryLunch)
        buttonCategoryDinner = findViewById(R.id.buttonCategoryDinner)

        updateButton = findViewById(R.id.updateRecipeButton)
        selectImageButton = findViewById(R.id.selectImageButton)
        selectedImageView = findViewById(R.id.selectedImageView)

        // Atur listener untuk tombol kategori
        buttonCategoryBreakfast.setOnClickListener { setSelectedCategory("Breakfast") }
        buttonCategoryLunch.setOnClickListener { setSelectedCategory("Lunch") }
        buttonCategoryDinner.setOnClickListener { setSelectedCategory("Dinner") }

        // Load the current recipe details
        loadRecipeDetails()

        // Set click listener for selecting image
        selectImageButton.setOnClickListener {
            // Launch gallery to select image
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        // Set click listener for the update button
        updateButton.setOnClickListener {
            updateRecipe()
        }
    }

    // Fungsi untuk mengatur kategori yang dipilih
    private fun setSelectedCategory(category: String) {
        selectedCategory = category
        updateButtonStyles()
    }

    // Update tampilan tombol kategori sesuai kategori yang dipilih
    private fun updateButtonStyles() {
        // Reset warna tombol ke warna default
        val defaultBackground = ContextCompat.getColorStateList(this, R.color.gray)
        val defaultTextColor = ContextCompat.getColor(this, R.color.secondary)
        val selectedBackground = ContextCompat.getColorStateList(this, R.color.primary)
        val selectedTextColor = ContextCompat.getColor(this, R.color.white)

        buttonCategoryBreakfast.setBackgroundTintList(defaultBackground)
        buttonCategoryLunch.setBackgroundTintList(defaultBackground)
        buttonCategoryDinner.setBackgroundTintList(defaultBackground)

        buttonCategoryBreakfast.setTextColor(defaultTextColor)
        buttonCategoryLunch.setTextColor(defaultTextColor)
        buttonCategoryDinner.setTextColor(defaultTextColor)

        // Set warna untuk tombol kategori yang dipilih
        when (selectedCategory) {
            "Breakfast" -> {
                buttonCategoryBreakfast.setBackgroundTintList(selectedBackground)
                buttonCategoryBreakfast.setTextColor(selectedTextColor)
            }
            "Lunch" -> {
                buttonCategoryLunch.setBackgroundTintList(selectedBackground)
                buttonCategoryLunch.setTextColor(selectedTextColor)
            }
            "Dinner" -> {
                buttonCategoryDinner.setBackgroundTintList(selectedBackground)
                buttonCategoryDinner.setTextColor(selectedTextColor)
            }
        }
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    Glide.with(this).load(it).into(selectedImageView)
                }
            }
        }

    private fun loadRecipeDetails() {
        recipeId?.let {
            firestore.collection("recipes")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val recipe = document.toObject(Recipe::class.java)
                    recipe?.let {
                        recipeNameEditText.setText(it.name)
                        recipeDescriptionEditText.setText(it.description)
                        recipeStepsEditText.setText(it.steps)
                        recipeCaloriesEditText.setText(it.calories)
                        recipeTimeEditText.setText(it.time)
                        recipeIngredientsEditText.setText(it.ingredients)
                        currentImageUrl = it.imageUrl
                        setSelectedCategory(it.category ?: "Breakfast") // Set kategori yang tersimpan
                        Glide.with(this).load(it.imageUrl).into(selectedImageView)
                    }
                }
        }
    }


    private fun updateRecipe() {
        val updatedName = recipeNameEditText.text.toString()
        val updatedDescription = recipeDescriptionEditText.text.toString()
        val updatedSteps = recipeStepsEditText.text.toString()
        val updatedCalories = recipeCaloriesEditText.text.toString()
        val updatedTime = recipeTimeEditText.text.toString()
        val updatedIngredients = recipeIngredientsEditText.text.toString()

        if (selectedImageUri != null) {
            // If a new image is selected, upload it to Firebase Storage
            uploadImageToStorageTest { imageUrl ->
                updateRecipeInFirestore(updatedName, updatedDescription, updatedSteps, updatedCalories, updatedTime, updatedIngredients, imageUrl)
            }
        } else {
            // No new image selected, just update the other fields
            updateRecipeInFirestore(updatedName, updatedDescription, updatedSteps, updatedCalories, updatedTime, updatedIngredients, currentImageUrl)
        }
    }

    private fun uploadImageToStorageTest(onSuccess: (imageUrl: String) -> Unit) {
        val imageRef = storage.reference.child("recipes/${UUID.randomUUID()}")
        selectedImageUri?.let { uri ->
            // Delete old image first
            deleteOldImage(currentImageUrl ?: "") {
                // Then upload new image
                imageRef.putFile(uri)
                    .addOnSuccessListener {
                        // Get the download URL of the uploaded image
                        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            onSuccess(downloadUrl.toString())
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun updateRecipeInFirestore(
        name: String,
        description: String,
        steps: String,
        calories: String,
        time: String,
        ingredients: String,
        imageUrl: String?
    ) {
        recipeId?.let {
            val recipeUpdates = hashMapOf(
                "name" to name,
                "description" to description,
                "steps" to steps,
                "calories" to calories,
                "time" to time,
                "ingredients" to ingredients,
                "imageUrl" to imageUrl,
                "category" to selectedCategory // Simpan kategori yang dipilih
            )

            firestore.collection("recipes")
                .document(it)
                .update(recipeUpdates as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Recipe updated", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke layar sebelumnya setelah diperbarui
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update recipe", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun deleteOldImage(imageUrl: String, onSuccess: () -> Unit) {
        if (imageUrl.isNotEmpty()) {
            val oldImageRef = storage.getReferenceFromUrl(imageUrl)
            oldImageRef.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    Toast.makeText(this, "Failed to delete old image", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            onSuccess()
        }
    }
}
