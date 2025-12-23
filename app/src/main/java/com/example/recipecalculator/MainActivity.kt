package com.example.recipecalculator

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var ingredientsContainer: LinearLayout
    private lateinit var btnAddIngredient: MaterialButton
    private lateinit var btnCalculate: MaterialButton
    private lateinit var btnClear: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnLoad: MaterialButton
    private lateinit var tvResult: android.widget.TextView

    private lateinit var firstIngredientView: LinearLayout
    private val otherIngredientViews = mutableListOf<LinearLayout>()

    private val PREFS_NAME = "RecipeCalculatorPrefs"
    private val RECIPES_KEY = "saved_recipes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        ingredientsContainer = findViewById(R.id.ingredientsContainer)
        btnAddIngredient = findViewById(R.id.btnAddIngredient)
        btnCalculate = findViewById(R.id.btnCalculate)
        btnClear = findViewById(R.id.btnClear)
        btnSave = findViewById(R.id.btnSave)
        btnLoad = findViewById(R.id.btnLoad)
        tvResult = findViewById(R.id.tvResult)

        // 結果表示部分を長押しでコピーできるようにする
        tvResult.setTextIsSelectable(true)
        tvResult.setOnLongClickListener {
            val resultText = tvResult.text.toString()
            if (resultText.isNotEmpty()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("計算結果", resultText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "結果をコピーしました", Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        }

        // 最初の材料入力欄を保存
        firstIngredientView = ingredientsContainer.getChildAt(0) as LinearLayout

        // 起動時に5個の材料欄を追加（合計6個にする）
        for (i in 1..5) {
            addIngredientRow()
        }
    }

    private fun setupListeners() {
        btnAddIngredient.setOnClickListener {
            addIngredientRow()
        }

        btnCalculate.setOnClickListener {
            calculateIngredients()
        }

        btnClear.setOnClickListener {
            clearAll()
        }

        btnSave.setOnClickListener {
            saveRecipe()
        }

        btnLoad.setOnClickListener {
            loadRecipe()
        }
    }

    private fun addIngredientRow() {
        val inflater = LayoutInflater.from(this)
        val ingredientRow = inflater.inflate(R.layout.ingredient_item, ingredientsContainer, false) as LinearLayout
        ingredientsContainer.addView(ingredientRow)
        otherIngredientViews.add(ingredientRow)
    }

    private fun calculateIngredients() {
        // 1つ目の材料の情報を取得
        val firstNameEdit = firstIngredientView.findViewById<EditText>(R.id.editIngredientName)
        val firstWeightEdit = firstIngredientView.findViewById<EditText>(R.id.editIngredientWeight)
        val firstRatioEdit = firstIngredientView.findViewById<EditText>(R.id.editIngredientRatio)

        val firstName = firstNameEdit.text.toString()
        val firstWeightStr = firstWeightEdit.text.toString()
        val firstRatioStr = firstRatioEdit.text.toString()

        if (firstName.isEmpty() || firstWeightStr.isEmpty() || firstRatioStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_first_weight), Toast.LENGTH_SHORT).show()
            return
        }

        val firstWeight = firstWeightStr.toDoubleOrNull()
        val firstRatio = firstRatioStr.toDoubleOrNull()

        if (firstWeight == null || firstWeight <= 0 || firstRatio == null || firstRatio <= 0) {
            Toast.makeText(this, "有効な値を入力してください", Toast.LENGTH_SHORT).show()
            return
        }

        // 他の材料の情報を取得
        val ingredients = mutableListOf<Ingredient>()
        ingredients.add(Ingredient(firstName, firstRatio))

        for (view in otherIngredientViews) {
            val nameEdit = view.findViewById<EditText>(R.id.editIngredientName)
            val ratioEdit = view.findViewById<EditText>(R.id.editIngredientRatio)

            val name = nameEdit.text.toString()
            val ratioStr = ratioEdit.text.toString()

            if (name.isNotEmpty() && ratioStr.isNotEmpty()) {
                val ratio = ratioStr.toDoubleOrNull()
                if (ratio != null && ratio > 0) {
                    ingredients.add(Ingredient(name, ratio))
                }
            }
        }

        // 比率の合計を計算
        val totalRatio = ingredients.sumOf { it.ratio }

        // 1つ目の材料の比率から単位重量を計算
        val unitWeight = firstWeight / firstRatio

        // 結果を計算して表示
        val resultText = StringBuilder()
        val totalWeight = unitWeight * totalRatio
        resultText.append("合計: ${String.format("%.1f", totalWeight)}g\n\n")

        for (ingredient in ingredients) {
            val weight = ingredient.ratio * unitWeight
            resultText.append("${ingredient.name}: ${String.format("%.1f", weight)}g\n")
        }

        tvResult.text = resultText.toString()
    }

    private fun clearAll() {
        // 最初の行をクリア
        firstIngredientView.findViewById<EditText>(R.id.editIngredientName).text.clear()
        firstIngredientView.findViewById<EditText>(R.id.editIngredientWeight).text.clear()
        firstIngredientView.findViewById<EditText>(R.id.editIngredientRatio).text.clear()

        // 他の材料行をクリア
        for (view in otherIngredientViews) {
            view.findViewById<EditText>(R.id.editIngredientName).text.clear()
            view.findViewById<EditText>(R.id.editIngredientRatio).text.clear()
        }

        tvResult.text = ""
    }

    private fun saveRecipe() {
        val ingredients = getCurrentIngredients() ?: return

        Log.d("RecipeCalc", "Saving ${ingredients.size} ingredients")
        for (i in ingredients.indices) {
            Log.d("RecipeCalc", "Ingredient $i: ${ingredients[i].name}, ratio: ${ingredients[i].ratio}")
        }

        val firstWeightStr = firstIngredientView.findViewById<EditText>(R.id.editIngredientWeight).text.toString()
        val firstWeight = firstWeightStr.toDoubleOrNull()
        if (firstWeight == null || firstWeight <= 0) {
            Toast.makeText(this, "1つ目の材料の重量を入力してください", Toast.LENGTH_SHORT).show()
            return
        }

        // レシピ名入力ダイアログ
        val input = EditText(this)
        input.hint = getString(R.string.recipe_name)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.enter_recipe_name))
            .setView(input)
            .setPositiveButton("保存") { _, _ ->
                val recipeName = input.text.toString()
                if (recipeName.isNotEmpty()) {
                    val recipe = Recipe(recipeName, ingredients, firstWeight)
                    Log.d("RecipeCalc", "Creating recipe with ${recipe.ingredients.size} ingredients")
                    saveRecipeToPrefs(recipe)
                    Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun loadRecipe() {
        val recipes = getSavedRecipes()
        if (recipes.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_saved_recipes), Toast.LENGTH_SHORT).show()
            return
        }

        val recipeNames = recipes.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_recipe))
            .setItems(recipeNames) { _, which ->
                val recipe = recipes[which]
                loadRecipeData(recipe)
                Toast.makeText(this, getString(R.string.loaded), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun getCurrentIngredients(): List<Ingredient>? {
        val ingredients = mutableListOf<Ingredient>()

        Log.d("RecipeCalc", "getCurrentIngredients: otherIngredientViews.size = ${otherIngredientViews.size}")

        // 1つ目の材料
        val firstName = firstIngredientView.findViewById<EditText>(R.id.editIngredientName).text.toString()
        val firstRatioStr = firstIngredientView.findViewById<EditText>(R.id.editIngredientRatio).text.toString()

        Log.d("RecipeCalc", "First ingredient: name='$firstName', ratio='$firstRatioStr'")

        if (firstName.isEmpty() || firstRatioStr.isEmpty()) {
            Toast.makeText(this, "材料を入力してください", Toast.LENGTH_SHORT).show()
            return null
        }

        val firstRatio = firstRatioStr.toDoubleOrNull()
        if (firstRatio == null || firstRatio <= 0) {
            Toast.makeText(this, "有効な比率を入力してください", Toast.LENGTH_SHORT).show()
            return null
        }

        ingredients.add(Ingredient(firstName, firstRatio))

        // 他の材料
        for ((index, view) in otherIngredientViews.withIndex()) {
            val name = view.findViewById<EditText>(R.id.editIngredientName).text.toString()
            val ratioStr = view.findViewById<EditText>(R.id.editIngredientRatio).text.toString()

            Log.d("RecipeCalc", "Other ingredient $index: name='$name', ratio='$ratioStr'")

            if (name.isNotEmpty() && ratioStr.isNotEmpty()) {
                val ratio = ratioStr.toDoubleOrNull()
                if (ratio != null && ratio > 0) {
                    ingredients.add(Ingredient(name, ratio))
                    Log.d("RecipeCalc", "Added ingredient: $name")
                }
            }
        }

        Log.d("RecipeCalc", "Total ingredients collected: ${ingredients.size}")
        return ingredients
    }

    private fun loadRecipeData(recipe: Recipe) {
        clearAll()

        Log.d("RecipeCalc", "Loading recipe: ${recipe.name}")
        Log.d("RecipeCalc", "Total ingredients: ${recipe.ingredients.size}")
        Log.d("RecipeCalc", "otherIngredientViews size: ${otherIngredientViews.size}")

        // 必要な入力欄の数を確保（1つ目の材料は既にあるので、残りの材料の数だけ必要）
        val requiredOtherViews = recipe.ingredients.size - 1
        while (otherIngredientViews.size < requiredOtherViews) {
            addIngredientRow()
            Log.d("RecipeCalc", "Added ingredient row, total now: ${otherIngredientViews.size}")
        }

        // 1つ目の材料
        if (recipe.ingredients.isNotEmpty()) {
            val first = recipe.ingredients[0]
            Log.d("RecipeCalc", "First ingredient: ${first.name}, ratio: ${first.ratio}")
            firstIngredientView.findViewById<EditText>(R.id.editIngredientName).setText(first.name)
            firstIngredientView.findViewById<EditText>(R.id.editIngredientWeight).setText(recipe.firstIngredientWeight.toString())
            firstIngredientView.findViewById<EditText>(R.id.editIngredientRatio).setText(first.ratio.toString())
        }

        // 他の材料
        for (i in 1 until recipe.ingredients.size) {
            val ingredient = recipe.ingredients[i]
            Log.d("RecipeCalc", "Loading ingredient $i: ${ingredient.name}, ratio: ${ingredient.ratio}")

            if (i - 1 < otherIngredientViews.size) {
                val view = otherIngredientViews[i - 1]
                val nameEdit = view.findViewById<EditText>(R.id.editIngredientName)
                val ratioEdit = view.findViewById<EditText>(R.id.editIngredientRatio)

                if (nameEdit != null && ratioEdit != null) {
                    nameEdit.setText(ingredient.name)
                    ratioEdit.setText(ingredient.ratio.toString())
                    Log.d("RecipeCalc", "Set ingredient ${ingredient.name} to view ${i-1}")
                } else {
                    Log.e("RecipeCalc", "EditText not found in view ${i-1}")
                }
            } else {
                Log.w("RecipeCalc", "Not enough ingredient views for ingredient $i")
            }
        }
    }

    private fun saveRecipeToPrefs(recipe: Recipe) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingRecipes = getSavedRecipes().toMutableList()

        // 同名のレシピがあれば削除
        existingRecipes.removeAll { it.name == recipe.name }
        existingRecipes.add(recipe)

        val recipesJson = existingRecipes.joinToString("###RECIPE###") { it.toJson() }
        Log.d("RecipeCalc", "Saving recipes: $recipesJson")
        prefs.edit().putString(RECIPES_KEY, recipesJson).apply()

        // 保存確認
        val saved = prefs.getString(RECIPES_KEY, "")
        Log.d("RecipeCalc", "Saved verification: $saved")
    }

    private fun getSavedRecipes(): List<Recipe> {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val recipesJson = prefs.getString(RECIPES_KEY, "") ?: ""

        Log.d("RecipeCalc", "Loading recipes: $recipesJson")

        if (recipesJson.isEmpty()) return emptyList()

        val recipes = recipesJson.split("###RECIPE###")
            .filter { it.isNotEmpty() }
            .mapNotNull {
                val recipe = Recipe.fromJson(it)
                Log.d("RecipeCalc", "Parsed recipe: $recipe")
                recipe
            }

        Log.d("RecipeCalc", "Total recipes loaded: ${recipes.size}")
        return recipes
    }
}
