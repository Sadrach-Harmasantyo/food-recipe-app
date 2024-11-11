package com.pam.test_firebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class AddFragment : Fragment() {

    private lateinit var recipeNameEditText: EditText
    private lateinit var recipeDescriptionEditText: EditText
    private lateinit var recipeStepsEditText: EditText
    private lateinit var recipeIngredientsEditText: EditText
    private lateinit var recipeTimeEditText: EditText
    private lateinit var recipeCaloriesEditText: EditText
    private lateinit var selectedImageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var addRecipeButton: Button
    private lateinit var selectedCategory: String

    private lateinit var buttonCategoryBreakfast: Button
    private lateinit var buttonCategoryLunch: Button
    private lateinit var buttonCategoryDinner: Button

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_with_image, container, false)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        // Find views
        recipeNameEditText = view.findViewById(R.id.recipeNameEditText)
        recipeDescriptionEditText = view.findViewById(R.id.recipeDescriptionEditText)
        recipeStepsEditText = view.findViewById(R.id.recipeStepsEditText)
        recipeIngredientsEditText = view.findViewById(R.id.recipeIngridientsEditText)
        recipeTimeEditText = view.findViewById(R.id.recipeTimeEditText)
        recipeCaloriesEditText = view.findViewById(R.id.recipeCaloriesEditText)
        selectedImageView = view.findViewById(R.id.selectedImageView)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        addRecipeButton = view.findViewById(R.id.addRecipeButton)
        buttonCategoryBreakfast = view.findViewById(R.id.buttonCategoryBreakfast)
        buttonCategoryLunch = view.findViewById(R.id.buttonCategoryLunch)
        buttonCategoryDinner = view.findViewById(R.id.buttonCategoryDinner)

        // Set click listeners for category buttons
        buttonCategoryBreakfast.setOnClickListener { onCategorySelected("Breakfast") }
        buttonCategoryLunch.setOnClickListener { onCategorySelected("Lunch") }
        buttonCategoryDinner.setOnClickListener { onCategorySelected("Dinner") }

        // Set onClick listener for selecting image
        selectImageButton.setOnClickListener {
            openImagePicker()
        }

        // Set click listener for the button
        addRecipeButton.setOnClickListener {
            uploadRecipeWithImage()
        }

        return view
    }

    private fun onCategorySelected(category: String) {
        selectedCategory = category
        updateButtonStyles2()  // Update button styles to show selected category
    }

    private fun updateButtonStyles(selectedButton: Button) {
        val buttons = listOf(buttonCategoryBreakfast, buttonCategoryLunch, buttonCategoryDinner)
        buttons.forEach {
            it.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray))
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary))
        }

        // Atur warna tombol yang dipilih menjadi lebih menonjol
        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary))
    }

    private fun updateButtonStyles2() {
        // Daftar tombol dan kategorinya
        val buttonMap = mapOf(
            "Breakfast" to buttonCategoryBreakfast,
            "Lunch" to buttonCategoryLunch,
            "Dinner" to buttonCategoryDinner
        )

        // Reset semua warna tombol ke warna default
        val defaultBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.gray)
        val defaultTextColor = ContextCompat.getColor(requireContext(), R.color.secondary)
        buttonMap.values.forEach { button ->
            button.setBackgroundTintList(defaultBackgroundColor)
            button.setTextColor(defaultTextColor)
        }

        // Set warna untuk tombol yang sesuai dengan selectedCategory
        buttonMap[selectedCategory]?.let { selectedButton ->
            selectedButton.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.primary
                )
            )
            selectedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }


    // Open gallery to pick an image
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            selectedImageView.setImageURI(imageUri) // Show the selected image in the ImageView
        }
    }

    // Upload image and save recipe data
    private fun uploadRecipeWithImage() {
        val recipeName = recipeNameEditText.text.toString()
        val recipeDescription = recipeDescriptionEditText.text.toString()
        val recipeSteps = recipeStepsEditText.text.toString()
        val recipeIngredients = recipeIngredientsEditText.text.toString()
        val recipeTime = recipeTimeEditText.text.toString()
        val recipeCalories = recipeCaloriesEditText.text.toString()


        if (recipeName.isEmpty() || recipeDescription.isEmpty() || recipeSteps.isEmpty() ||
            recipeIngredients.isEmpty() || recipeTime.isEmpty() || recipeCalories.isEmpty()
        ) {
            Toast.makeText(context, "Please enter all details", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri == null) {
            Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val storageReference = storage.reference.child("recipes/${UUID.randomUUID()}.jpg")
        imageUri?.let { uri ->
            storageReference.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the download URL of the uploaded image
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveRecipeToFirestore(
                            recipeName,
                            recipeDescription,
                            recipeSteps,
                            recipeIngredients,
                            recipeTime,
                            recipeCalories,
                            downloadUrl.toString()
                        )
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Save recipe data to Firestore
    private fun saveRecipeToFirestore(
        name: String,
        description: String,
        steps: String,
        ingredients: String,
        time: String,
        calories: String,
        imageUrl: String
    ) {
        val currentUser = auth.currentUser
        val recipe = hashMapOf(
            "id" to System.currentTimeMillis().toString(),
            "name" to name,
            "description" to description,
            "steps" to steps,
            "category" to selectedCategory,
            "ingredients" to ingredients,
            "time" to time,
            "calories" to calories,
            "imageUrl" to imageUrl,
            "userEmail" to currentUser?.email,
            "timestamp" to LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        )

        firestore.collection("recipes")
            .add(recipe)
            .addOnSuccessListener {
                Toast.makeText(context, "Recipe added successfully", Toast.LENGTH_SHORT).show()
                // Navigate back to home or recipe list

                // Clear input fields
//                recipeNameEditText.text.clear()
//                recipeDescriptionEditText.text.clear()
//                recipeStepsEditText.text.clear()
//                selectedImageView.setImageURI(null) // Clear the selected image
                clearInputFields()
                redirectToHomeFragment()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add recipe", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearInputFields() {
        recipeNameEditText.text.clear()
        recipeDescriptionEditText.text.clear()
        recipeStepsEditText.text.clear()
        recipeIngredientsEditText.text.clear()
        recipeTimeEditText.text.clear()
        recipeCaloriesEditText.text.clear()
        selectedImageView.setImageURI(null)
    }

    private fun addRecipeToFirestore() {
        // Get the current user
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please login to add a recipe", Toast.LENGTH_SHORT).show()
            return
        }

        // Get input values
        val recipeName = recipeNameEditText.text.toString().trim()
        val recipeDescription = recipeDescriptionEditText.text.toString().trim()
        val recipeSteps = recipeStepsEditText.text.toString().trim()

        // Validate inputs
        if (recipeName.isEmpty() || recipeDescription.isEmpty() || recipeSteps.isEmpty()) {
            Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a recipe object
        val recipe = hashMapOf(
            "id" to System.currentTimeMillis().toString(),
            "name" to recipeName,
            "description" to recipeDescription,
            "steps" to recipeSteps,
            "timestamp" to LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            "userEmail" to currentUser.email
        )

        // Add the recipe to Firestore
        firestore.collection("recipes")
            .add(recipe)
            .addOnSuccessListener {
                Toast.makeText(context, "Recipe added successfully", Toast.LENGTH_SHORT).show()

                recipeNameEditText.text.clear()
                recipeDescriptionEditText.text.clear()
                recipeStepsEditText.text.clear()

                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)

                redirectToHomeFragment()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to add recipe: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun redirectToHomeFragment() {
        val homeFragment = HomeFragment()
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(
            R.id.nav_host_fragment,
            homeFragment
        )  // Replace fragment_container with the correct ID from your layout
        transaction.addToBackStack(null)  // Optional, if you want to add the transaction to the back stack
        transaction.commit()

        // Find and set selected item
        val bottomNav =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation) // Change R.id.bottomNavigationView if the ID in your XML layout is different
        bottomNav.selectedItemId = R.id.nav_home // Replace with your Home fragment's ID
    }
}

