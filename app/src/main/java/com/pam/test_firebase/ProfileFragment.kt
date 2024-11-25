package com.pam.test_firebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pam.test_firebase.adapter.ProfileRecipeAdapter
import com.pam.test_firebase.adapter.RecipeAdapter
import com.pam.test_firebase.data.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var profileRecipeRecyclerView: RecyclerView
    private lateinit var recipeAdapter: ProfileRecipeAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userRecipes: MutableList<Recipe> = mutableListOf()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val profileEmail = view.findViewById<TextView>(R.id.profileEmail)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            profileEmail.text = currentUser.email
        }

        // Initialize RecyclerView
        profileRecipeRecyclerView = view.findViewById(R.id.profileRecipesRecyclerView)
        profileRecipeRecyclerView.layoutManager = LinearLayoutManager(context)

        // Load user recipes
        loadUserRecipes()

        return view
    }

    private fun loadUserRecipes() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            listenerRegistration = firestore.collection("recipes")
                .whereEqualTo("userEmail", currentUser.email)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        if (isAdded) {
                            Toast.makeText(context, "Error loading recipes", Toast.LENGTH_SHORT).show()
                        }
                        return@addSnapshotListener
                    }

                    if (snapshots != null && isAdded) {
                        userRecipes.clear()
                        for (document in snapshots) {
                            val recipe = document.toObject(Recipe::class.java)
                            recipe.id = document.id
                            userRecipes.add(recipe)
                        }

                        // Setup or refresh the adapter
                        recipeAdapter = ProfileRecipeAdapter(
                            requireContext(),
                            userRecipes,
                            onEditClick = { recipe -> editRecipe(recipe) },
                            onDeleteClick = { recipe -> deleteRecipe(recipe) },
                            onItemClick = { recipe -> openRecipeDetail(recipe) }
                        )
                        profileRecipeRecyclerView.adapter = recipeAdapter
                    }
                }
        }
    }

    // Edit recipe functionality
    private fun editRecipe(recipe: Recipe) {
        // Start an activity or show a dialog to edit the recipe
        val intent = Intent(activity, EditRecipeActivity::class.java)
        intent.putExtra("recipeId", recipe.id)
        startActivity(intent)
    }

    // Function to delete the image from Firebase Storage
    private fun deleteRecipeImage(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            // Get a reference to the file to delete
            val storageRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

            // Delete the file
            storageRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Recipe image deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to delete recipe image", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "No image to delete", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openRecipeDetail(recipe: Recipe) {
        val recipeDetailFragment = RecipeDetailFragment.newInstance(recipe)
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, recipeDetailFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    // Function to delete recipe
    private fun deleteRecipe(recipe: Recipe) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Check if the recipe is bookmarked
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("bookmarks")
                .document(recipe.id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Recipe is bookmarked, show a message to unbookmark first
                        Toast.makeText(
                            context,
                            "Cannot delete. Please unbookmark the recipe first.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Recipe is not bookmarked, proceed with delete
                        performDeleteRecipe(recipe)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to check bookmarks", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Helper function to perform the actual delete operation
    private fun performDeleteRecipe(recipe: Recipe) {
        firestore.collection("recipes")
            .document(recipe.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Recipe deleted", Toast.LENGTH_SHORT).show()

                // Remove the recipe from the list and refresh the adapter
                userRecipes.remove(recipe)
                recipeAdapter.notifyDataSetChanged()

                // After deleting recipe from Firestore, delete the image from Firebase Storage
                deleteRecipeImage(recipe.imageUrl)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete recipe", Toast.LENGTH_SHORT).show()
            }
    }

}


