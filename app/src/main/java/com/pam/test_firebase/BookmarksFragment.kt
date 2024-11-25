package com.pam.test_firebase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.pam.test_firebase.adapter.RecipeAdapter
import com.pam.test_firebase.data.Recipe

class BookmarksFragment : Fragment() {

    private lateinit var bookmarkRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var bookmarkedRecipes: MutableList<Recipe> = mutableListOf()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bookmarks, container, false)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        bookmarkRecyclerView = view.findViewById(R.id.bookmarkRecipesRecyclerView)
        bookmarkRecyclerView.layoutManager = LinearLayoutManager(context)

        // Load bookmarked recipes with real-time updates
        loadBookmarkedRecipes()

        return view
    }

    private fun loadBookmarkedRecipes() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            listenerRegistration = firestore.collection("users")
                .document(currentUser.uid)
                .collection("bookmarks")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(context, "Failed to load bookmarks: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        bookmarkedRecipes.clear()
                        for (document in snapshot.documents) {
                            val recipe = document.toObject(Recipe::class.java)
                            recipe?.let {
                                it.id = document.id  // Pastikan ID resep disimpan
                                bookmarkedRecipes.add(it)
                            }
                        }

                        // Update RecyclerView dengan daftar baru
                        recipeAdapter = RecipeAdapter(requireContext(), bookmarkedRecipes) { recipe ->
                            openRecipeDetail(recipe)
                        }
                        bookmarkRecyclerView.adapter = recipeAdapter
                        recipeAdapter.notifyDataSetChanged()
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove() // Remove listener when fragment is destroyed to prevent memory leaks
    }

    private fun openRecipeDetail(recipe: Recipe) {
        val recipeDetailFragment = RecipeDetailFragment.newInstance(recipe)
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, recipeDetailFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}

