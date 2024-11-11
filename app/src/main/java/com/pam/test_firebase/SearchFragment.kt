package com.pam.test_firebase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.pam.test_firebase.adapter.RecipeAdapter
import com.pam.test_firebase.data.Recipe

class SearchFragment : Fragment() {
    private lateinit var buttonCategoryAll: Button
    private lateinit var buttonCategoryBreakfast: Button
    private lateinit var buttonCategoryLunch: Button
    private lateinit var buttonCategoryDinner: Button
    private var selectedCategory: String = "All"

    private lateinit var searchView: SearchView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var firestore: FirebaseFirestore
    private var recipeList: MutableList<Recipe> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

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

        firestore = FirebaseFirestore.getInstance()

        searchView = view.findViewById(R.id.recipeSearchView)
        searchRecyclerView = view.findViewById(R.id.searchRecipesRecyclerView)
        searchRecyclerView.layoutManager = LinearLayoutManager(context)

        // Load all recipes initially
        loadRecipes(selectedCategory)

        // Set up search query listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchRecipes(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    searchRecipes(newText)
                }
                return false
            }
        })

        return view
    }

    private fun onCategorySelected(category: String) {
        selectedCategory = category
        updateButtonStyles()
        loadRecipes(selectedCategory)
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

    private fun loadRecipes(category: String) {
        firestore.collection("recipes")
            .get()
            .addOnSuccessListener { result ->
                recipeList.clear()
                for (document in result) {
                    val recipe = document.toObject(Recipe::class.java)
                    if (category == "All" || recipe.category == category) {
                        recipeList.add(recipe)
                    }
                }
                applySearchFilter(searchView.query.toString())
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

    private fun searchRecipes(query: String) {
        applySearchFilter(query)
    }

    private fun applySearchFilter(query: String) {
        val filteredList = recipeList.filter {
            it.name.contains(query, ignoreCase = true)
        }
        recipeAdapter = RecipeAdapter(requireContext(), filteredList) { recipe ->
            openRecipeDetail(recipe)
        }
        searchRecyclerView.adapter = recipeAdapter
    }

    private fun openRecipeDetail(recipe: Recipe) {
        val recipeDetailFragment = RecipeDetailFragment.newInstance(recipe)
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, recipeDetailFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
