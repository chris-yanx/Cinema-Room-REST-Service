class Meal {
    private final String category;
    private final String name;
    private final String ingredients;

    public Meal(String category, String name, String ingredients) {
        this.category = category;
        this.name = name;
        this.ingredients = ingredients;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String[] getIngredientsArray() {
        String[] ingredientsArray = ingredients.split(",");
        for (int i = 0; i < ingredientsArray.length; i++) {
            ingredientsArray[i] = ingredientsArray[i].trim();
        }
        return ingredientsArray;
    }
}