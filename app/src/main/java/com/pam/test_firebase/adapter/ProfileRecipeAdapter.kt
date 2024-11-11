package com.pam.test_firebase.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pam.test_firebase.R
import com.pam.test_firebase.data.Recipe

class ProfileRecipeAdapter(
    private val context: Context,
    private val recipeList: List<Recipe>,
    private val onEditClick: (Recipe) -> Unit,  // Callback for edit button
    private val onDeleteClick: (Recipe) -> Unit, // Callback for delete button
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<ProfileRecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImageView: ImageView = itemView.findViewById(R.id.recipeImage)
        val recipeNameTextView: TextView = itemView.findViewById(R.id.recipeNameTextView)
        val recipeCategoryTextView: TextView = itemView.findViewById(R.id.recipeCategoryTextView)
        val recipeCaloriesTextView: TextView = itemView.findViewById(R.id.recipeCaloriesTextView)
        val recipeTimeTextView: TextView = itemView.findViewById(R.id.recipeTimeTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timepstampTextView)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_profile, parent, false)
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
            .placeholder(R.color.black)  // Placeholder while loading
            .error(R.drawable.ic_launcher_background)  // Image to display if there's an error
            .into(holder.recipeImageView)

        // Set click listener for the item view
        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }

        // Set click listeners for edit and delete buttons
        holder.editButton.setOnClickListener {
            onEditClick(recipe)  // Invoke the callback for editing
        }

        holder.deleteButton.setOnClickListener {

            // Inflate the custom layout for the dialog
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null)

            // Create the AlertDialog with the custom view
            val alertDialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            // Get the dialog buttons
            val btnCancel = dialogView.findViewById<Button>(R.id.noButton)
            val btnConfirm = dialogView.findViewById<Button>(R.id.yesButton)

            // Set up button click listeners
            btnCancel.setOnClickListener {
                alertDialog.dismiss()  // Close dialog on "No" button
            }

            btnConfirm.setOnClickListener {
                onDeleteClick(recipe)  // Execute delete callback
                alertDialog.dismiss()  // Close dialog
            }

            // Show the dialog
//            alertDialog.show()
            // Show the dialog
            alertDialog.show()
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        }
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }
}
