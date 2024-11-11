package com.pam.test_firebase.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pam.test_firebase.R
import com.pam.test_firebase.data.Recipe

class RecipeAdapter(
    private val context: Context,
    private var recipeList: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImageView: ImageView = itemView.findViewById(R.id.recipeImage)
        val recipeNameTextView: TextView = itemView.findViewById(R.id.recipeNameTextView)
        val recipeCategoryTextView: TextView = itemView.findViewById(R.id.recipeCategoryTextView)
        val recipeCaloriesTextView: TextView = itemView.findViewById(R.id.recipeCaloriesTextView)
        val recipeTimeTextView: TextView = itemView.findViewById(R.id.recipeTimeTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timepstampTextView)
        val bookmarkButton: ImageButton = itemView.findViewById(R.id.bookmarkButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe_with_image, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]

        holder.recipeNameTextView.text = recipe.name
        holder.recipeCategoryTextView.text = recipe.category
        holder.recipeCaloriesTextView.text = recipe.calories
        holder.recipeTimeTextView.text = recipe.time
        holder.emailTextView.text = recipe.userEmail
        holder.timestampTextView.text = recipe.timestamp

        // Use Glide to load the image from URL
        Glide.with(context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.ic_launcher_foreground)  // Placeholder while loading
            .error(R.drawable.ic_launcher_background)  // Image to display if there's an error
            .into(holder.recipeImageView)

        // Check if the recipe is already bookmarked and update the icon accordingly
        checkIfBookmarked(holder, recipe)

        // Set click listener on the itemView
        holder.itemView.setOnClickListener {
            onItemClick(recipe)  // Trigger the callback when item is clicked
        }

        // Set click listener on the bookmark button
        holder.bookmarkButton.setOnClickListener {
            toggleBookmark(holder, recipe)  // Add or remove bookmark
        }
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    // Method to update the list and refresh the RecyclerView
    fun updateList(newList: List<Recipe>) {
        recipeList = newList
        notifyDataSetChanged()
    }

    private fun checkIfBookmarked(holder: RecipeViewHolder, recipe: Recipe) {
        val currentUser = auth.currentUser
        if (currentUser != null && !recipe.id.isNullOrEmpty()) {
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("bookmarks")
                .document(recipe.id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Recipe is already bookmarked, show filled bookmark icon
                        holder.bookmarkButton.setImageResource(R.drawable.ic_bookmarks)
                    } else {
                        // Recipe is not bookmarked, show border bookmark icon
                        holder.bookmarkButton.setImageResource(R.drawable.ic_bookmark_border)
                    }
                }
                .addOnFailureListener {
                    // Handle failure, maybe default to not bookmarked
                    holder.bookmarkButton.setImageResource(R.drawable.ic_bookmark_border)
                }
        }
    }

    private fun toggleBookmark(holder: RecipeViewHolder, recipe: Recipe) {
        val currentUser = auth.currentUser
        if (currentUser != null && !recipe.id.isNullOrEmpty()) {
            val bookmarkRef = firestore.collection("users")
                .document(currentUser.uid)
                .collection("bookmarks")
                .document(recipe.id)

            bookmarkRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // If the recipe is already bookmarked, remove it
                    bookmarkRef.delete()
                        .addOnSuccessListener {
                            holder.bookmarkButton.setImageResource(R.drawable.ic_bookmark_border)
                        }
                        .addOnFailureListener {
                            // Handle failure, e.g., show error message
                            Toast.makeText(context, "Failed to remove bookmark", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // If the recipe is not bookmarked, add it
                    bookmarkRef.set(recipe)
                        .addOnSuccessListener {
                            holder.bookmarkButton.setImageResource(R.drawable.ic_bookmarks)
                        }
                        .addOnFailureListener {
                            // Handle failure, e.g., show error message
                            Toast.makeText(context, "Failed to add bookmark", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}
