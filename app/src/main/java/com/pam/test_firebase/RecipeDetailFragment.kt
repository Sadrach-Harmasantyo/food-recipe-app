package com.pam.test_firebase

import android.content.Context
import android.os.Bundle
import com.pam.test_firebase.data.Recipe
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class RecipeDetailFragment : Fragment() {

    private lateinit var recipeImageView: ImageView
    private lateinit var recipeNameTextView: TextView
    private lateinit var recipeCategoryTextView: TextView
    private lateinit var recipeCaloriesTextView: TextView
    private lateinit var recipeTimeTextView: TextView
    private lateinit var recipeAuthorTextView: TextView
    private lateinit var recipeIngredientsTextView: TextView
    private lateinit var recipeDescriptionTextView: TextView
    private lateinit var recipeStepsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_recipe_detail, container, false)

        // Initialize views
        recipeImageView = view.findViewById(R.id.recipeImage)
        recipeNameTextView = view.findViewById(R.id.recipeNameTextView)
        recipeCategoryTextView = view.findViewById(R.id.categoryRecipeTextView)
        recipeCaloriesTextView = view.findViewById(R.id.caloriesRecipeTextView)
        recipeTimeTextView = view.findViewById(R.id.timeRecipeTextView)
        recipeAuthorTextView = view.findViewById(R.id.authorRecipeTextView)
        recipeIngredientsTextView = view.findViewById(R.id.recipeIngridientsTextView)
        recipeDescriptionTextView = view.findViewById(R.id.recipeDescriptionTextView)
        recipeStepsTextView = view.findViewById(R.id.recipeStepsTextView)

        // Get the passed arguments (recipe details)
        val imageUrl = arguments?.getString("imageUrl")
        val recipeName = arguments?.getString("name")
        val recipeDescription = arguments?.getString("description")
        val recipeSteps = arguments?.getString("steps")
        val recipeCategory = arguments?.getString("category")
        val recipeCalories = arguments?.getString("calories")
        val recipeTime = arguments?.getString("time")
        val recipeAuthor = arguments?.getString("userEmail")
        val recipeIngredients = arguments?.getString("ingredients")

        // Set the recipe details
        // Use Glide to load the image from URL
        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.ic_launcher_foreground)  // Placeholder while loading
            .error(R.drawable.ic_launcher_background)  // Image to display if there's an error
            .into(recipeImageView)

        recipeNameTextView.text = recipeName
        recipeDescriptionTextView.text = recipeDescription
        recipeStepsTextView.text = recipeSteps
        recipeCategoryTextView.text = recipeCategory
        recipeCaloriesTextView.text = recipeCalories
        recipeTimeTextView.text = recipeTime
        recipeAuthorTextView.text = recipeAuthor
        recipeIngredientsTextView.text = recipeIngredients

        return view
    }

    companion object {
        fun newInstance(recipe: Recipe): RecipeDetailFragment {
            val fragment = RecipeDetailFragment()

            // Set arguments to pass data to fragment
            val args = Bundle()
            args.putString("name", recipe.name)
            args.putString("description", recipe.description)
            args.putString("steps", recipe.steps)
            args.putString("imageUrl", recipe.imageUrl)
            args.putString("category", recipe.category)
            args.putString("calories", recipe.calories)
            args.putString("time", recipe.time)
            args.putString("userEmail", recipe.userEmail)
            args.putString("ingredients", recipe.ingredients)
            fragment.arguments = args

            return fragment
        }
    }
}
