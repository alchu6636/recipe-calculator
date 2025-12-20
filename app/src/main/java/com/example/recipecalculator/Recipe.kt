package com.example.recipecalculator

data class Recipe(
    val name: String,
    val ingredients: List<Ingredient>,
    val firstIngredientWeight: Double
) {
    fun toJson(): String {
        val ingredientsJson = ingredients.joinToString("|||") {
            "${it.name}~~~${it.ratio}"
        }
        return "$name@@@$firstIngredientWeight@@@$ingredientsJson"
    }

    companion object {
        fun fromJson(json: String): Recipe? {
            try {
                val parts = json.split("@@@")
                if (parts.size < 3) return null

                val name = parts[0]
                val firstWeight = parts[1].toDouble()

                val ingredients = mutableListOf<Ingredient>()
                if (parts[2].isNotEmpty()) {
                    val ingredientParts = parts[2].split("|||")
                    for (ingredientStr in ingredientParts) {
                        val ingredientData = ingredientStr.split("~~~")
                        if (ingredientData.size == 2) {
                            ingredients.add(Ingredient(ingredientData[0], ingredientData[1].toDouble()))
                        }
                    }
                }

                return Recipe(name, ingredients, firstWeight)
            } catch (e: Exception) {
                return null
            }
        }
    }
}
