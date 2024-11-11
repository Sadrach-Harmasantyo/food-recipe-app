package com.pam.test_firebase

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pam.test_firebase.adapter.RecipeAdapter
import com.pam.test_firebase.data.Recipe

class HomeFragment : Fragment() {
    private lateinit var buttonCategoryAll: Button
    private lateinit var buttonCategoryBreakfast: Button
    private lateinit var buttonCategoryLunch: Button
    private lateinit var buttonCategoryDinner: Button
    private var selectedCategory: String = "All"

    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var firestore: FirebaseFirestore
    private var recipeList: MutableList<Recipe> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize category buttons
        buttonCategoryAll = view.findViewById(R.id.buttonCategoryAll)
        buttonCategoryBreakfast = view.findViewById(R.id.buttonCategoryBreakfast)
        buttonCategoryLunch = view.findViewById(R.id.buttonCategoryLunch)
        buttonCategoryDinner = view.findViewById(R.id.buttonCategoryDinner)

        // Set click listeners for category buttons
        buttonCategoryAll.setOnClickListener { onCategorySelected("All") }
        buttonCategoryBreakfast.setOnClickListener { onCategorySelected("Breakfast") }
        buttonCategoryLunch.setOnClickListener { onCategorySelected("Lunch") }
        buttonCategoryDinner.setOnClickListener { onCategorySelected("Dinner") }

        // Ambil email dari arguments
        val email = arguments?.getString("userEmail")

        // Temukan TextView dan set email di sana
        val emailTextView: TextView = view.findViewById(R.id.userEmailTextView)
        emailTextView.text = email

        val signOutButton = view.findViewById<Button>(R.id.signOutButton)

        signOutButton.setOnClickListener {
            signOutUser()
        }

        // Initialize RecyclerView
        recipesRecyclerView = view.findViewById(R.id.recipesRecyclerView)
        recipesRecyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Load recipes from Firestore
        loadRecipes(selectedCategory)

        return view
    }

    private fun onCategorySelected(category: String) {
        selectedCategory = category
        updateButtonStyles()  // Update button styles to show selected category
        loadRecipes(selectedCategory) // Load recipes based on selected category
    }

    private fun updateButtonStyles() {
        // Daftar tombol dan kategorinya
        val buttonMap = mapOf(
            "All" to buttonCategoryAll,
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


    private fun signOutUser() {
        auth.signOut()
        val intent =
            Intent(requireActivity(), LoginRegisterActivity::class.java) // Use requireActivity()
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun loadRecipes(category: String) {
        val query = if (category == "All") {
            firestore.collection("recipes")
        } else {
            firestore.collection("recipes").whereEqualTo("category", category)
        }

        query.get()
            .addOnSuccessListener { result ->
                recipeList.clear()
                for (document in result) {
                    val recipe = document.toObject(Recipe::class.java)
                    recipeList.add(recipe)
                }
                recipeList.sortByDescending { it.timestamp }

                // Set adapter and notify changes
                recipeAdapter = RecipeAdapter(requireContext(), recipeList) { recipe ->
                    openRecipeDetail(recipe)
                }
                recipesRecyclerView.adapter = recipeAdapter
                recipeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Error loading recipes: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun openRecipeDetail(recipe: Recipe) {
        val recipeDetailFragment = RecipeDetailFragment.newInstance(recipe)
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, recipeDetailFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}

