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

//    companion object {
//        private const val TAG = "ProfileFragment"
//    }

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
        loadUserRecipesReal()

        return view
    }

    private fun loadUserRecipesReal() {
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

    // Delete recipe functionality
    private fun deleteRecipe(recipe: Recipe) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
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
                    Toast.makeText(context, "Failed to delete recipe", Toast.LENGTH_SHORT)
                        .show()
                }

            firestore.collection("users")
                .document(currentUser.uid)
                .collection("bookmarks")
                .document(recipe.id)
                .delete()
                .addOnSuccessListener {
                    //
                    Toast.makeText(context, "Recipe from bookmarks deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    //
                    Toast.makeText(context, "Failed to delete recipe from bookmarks", Toast.LENGTH_SHORT).show()
                }
        }
    }

//    private fun deleteRecipeFromBookmarks(recipeId: String, onComplete: () -> Unit) {
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            firestore.collection("users")
//                .document(currentUser.uid)
//                .collection("bookmarks")
//                .document(recipeId)
//                .delete()
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        onComplete()
//                    } else {
//                        Toast.makeText(context, "Failed to remove from bookmarks", Toast.LENGTH_SHORT).show()
//                        onComplete()
//                    }
//                }
//        } else {
//            onComplete()
//        }
//    }


//    private fun deleteRecipeTest(recipe: Recipe) {
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            // Start a batch write
//            val batch = firestore.batch()
//
//            // Delete the recipe from the 'recipes' collection
//            val recipeRef = firestore.collection("recipes").document(recipe.id)
//            batch.delete(recipeRef)
//
//            // Delete the recipe from all users' bookmarks
//            firestore.collection("users").get().addOnSuccessListener { userSnapshots ->
//                for (userDoc in userSnapshots) {
//                    val bookmarkRef = userDoc.reference.collection("bookmarks").document(recipe.id)
//                    batch.delete(bookmarkRef)
//                }
//
//                // Commit the batch
//                batch.commit().addOnSuccessListener {
//                    Toast.makeText(context, "Recipe deleted and removed from all bookmarks", Toast.LENGTH_SHORT).show()
//                    deleteRecipeImage(recipe.imageUrl)
//
//                    // Remove the recipe from the local list and refresh the adapter
//                    userRecipes.remove(recipe)
//                    recipeAdapter.notifyDataSetChanged()
//                }.addOnFailureListener { e ->
//                    Toast.makeText(context, "Failed to delete recipe: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }.addOnFailureListener { e ->
//                Toast.makeText(context, "Failed to access users: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

//    private fun deleteRecipeTest(recipe: Recipe) {
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            Log.d(TAG, "Starting deletion process for recipe: ${recipe.id}")
//
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    // Periksa apakah pengguna ada di koleksi 'users'
//                    val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
//                    if (!userDoc.exists()) {
//                        // Jika tidak ada, buat dokumen pengguna
//                        firestore.collection("users").document(currentUser.uid).set(mapOf("email" to currentUser.email)).await()
//                        Log.d(TAG, "Created user document for ${currentUser.email}")
//                    }
//
//                    // Lanjutkan dengan proses penghapusan
//                    deleteRecipeProcess(recipe)
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error in deleteRecipe", e)
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Error deleting recipe: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        } else {
//            Log.e(TAG, "No current user found")
//            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
//        }
//    }

//    private suspend fun deleteRecipeProcess(recipe: Recipe) {
//        // Hapus dari koleksi 'recipes'
//        firestore.collection("recipes").document(recipe.id).delete().await()
//        Log.d(TAG, "Recipe successfully deleted from 'recipes' collection")
//
//        // Hapus dari semua bookmark
//        deleteFromAllBookmarks(recipe.id)
//
//        // Hapus gambar resep
//        deleteRecipeImage(recipe.imageUrl)
//
//        // Perbarui UI
//        withContext(Dispatchers.Main) {
//            userRecipes.remove(recipe)
//            recipeAdapter.notifyDataSetChanged()
//            Toast.makeText(context, "Recipe deleted successfully", Toast.LENGTH_SHORT).show()
//        }
//
//        // Verifikasi penghapusan
//        verifyDeletion(recipe.id)
//    }

//    private suspend fun deleteFromAllBookmarks(recipeId: String) {
//        Log.d(TAG, "Starting to delete recipe $recipeId from all bookmarks")
//
//        try {
//            val userSnapshots = firestore.collection("users").get().await()
//            Log.d(TAG, "Retrieved ${userSnapshots.size()} users")
//
//            if (userSnapshots.isEmpty) {
//                Log.w(TAG, "No users found in the 'users' collection")
//            } else {
//                userSnapshots.documents.forEach { userDoc ->
//                    val bookmarkRef = userDoc.reference.collection("bookmarks").document(recipeId)
//                    bookmarkRef.delete().await() // Hapus tanpa memeriksa keberadaan
//                    Log.d(TAG, "Attempted to delete bookmark for user ${userDoc.id}")
//                }
//            }
//
//            Log.d(TAG, "Finished deleting from all bookmarks")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in deleteFromAllBookmarks", e)
//            throw e
//        }
//    }

//    private suspend fun verifyDeletion(recipeId: String) {
//        Log.d(TAG, "Starting verification of deletion for recipe: $recipeId")
//
//        try {
//            val recipeDoc = firestore.collection("recipes").document(recipeId).get().await()
//            if (recipeDoc.exists()) {
//                Log.e(TAG, "Recipe still exists in 'recipes' collection after deletion attempt")
//            } else {
//                Log.d(TAG, "Confirmed: Recipe does not exist in 'recipes' collection")
//            }
//
//            val userSnapshots = firestore.collection("users").get().await()
//            val bookmarkExists = userSnapshots.documents.any { userDoc ->
//                userDoc.reference.collection("bookmarks").document(recipeId).get().await().exists()
//            }
//
//            if (bookmarkExists) {
//                Log.e(TAG, "Recipe still exists in at least one user's bookmarks")
//            } else {
//                Log.d(TAG, "Confirmed: Recipe does not exist in any user's bookmarks")
//            }
//
//            Log.d(TAG, "Verification complete")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in verifyDeletion", e)
//        }
//    }
//
//
//    private suspend fun deleteRecipeImage(imageUrl: String?) {
//        if (!imageUrl.isNullOrEmpty()) {
//            try {
//                val storageRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
//                storageRef.delete().await()
//                Log.d(TAG, "Recipe image deleted successfully")
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to delete recipe image", e)
//                // Don't throw this exception as image deletion is not critical
//            }
//        }
//    }

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
}


