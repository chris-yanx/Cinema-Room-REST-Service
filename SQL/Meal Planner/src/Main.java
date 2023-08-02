import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static ArrayList<Meal> meals = new ArrayList<>();

    public static void main(String[] args) {
        initialize_database();
//        load_database();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            load_database();
            System.out.println("What would you like to do (add, show, exit)?");
            String option = scanner.nextLine();
            switch (option) {
                case "add" -> addMeal();
                case "show" -> {
                    if (meals.isEmpty()) {
                        System.out.println("No meals saved. Add a meal first.");
                        continue;
                    }
                    showMeal();
                }
                case "exit" -> {
                    System.out.println("Bye!");
                    return;
                }
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        String DB_URL = "jdbc:postgresql://localhost:5432/meals_db";
        String USER = "postgres";
        String PASS = "1111";
        Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
        connection.setAutoCommit(true);
        return connection;
    }

    public static void initialize_database() {
        try (Connection connection = getConnection()) {
            System.out.println("Connected to the PostgresSQL server successfully.");

            Statement statement = connection.createStatement();
            statement.executeUpdate("drop table if exists meals");
            statement.executeUpdate("drop table if exists ingredients");
            statement.executeUpdate("create table meals (" +
                    "category varchar(20)," +
                    "meal varchar(20)," +
                    "meal_id integer" +
                    ")");
            statement.executeUpdate("create table ingredients (" +
                    "ingredient varchar(100)," +
                    "ingredient_id integer," +
                    "meal_id integer" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to initialize the database.");
            System.exit(0);
        }
    }

    public static void load_database() {
        meals = new ArrayList<>();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet mealSet = statement.executeQuery("select * from meals");

            while (mealSet.next()) {
                String category = mealSet.getString("category");
                String meal = mealSet.getString("meal");
                int meal_id = mealSet.getInt("meal_id");

                PreparedStatement ps = connection.prepareStatement("select ingredient from ingredients where meal_id = ?");
                ps.setInt(1, meal_id);
                ResultSet ingredientSet = ps.executeQuery();
                while (ingredientSet.next()) {
                    String ingredients = ingredientSet.getString("ingredient");
                    Meal newMeal = new Meal(category, meal, ingredients);
                    meals.add(newMeal);
                }
            }
        } catch (SQLException ex) {
//            System.out.println("Failed to load data");
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static void addMeal() {
        Scanner scanner = new Scanner(System.in);
        String category;

        System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");
        while (true) {
            category = scanner.nextLine();
            if (category.equals("breakfast") || category.equals("lunch") || category.equals("dinner")) {
                break;
            } else System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
        }

        String name;
        System.out.println("Input the meal's name:");
        while (true) {
            name = scanner.nextLine();
            if (name.matches("[a-zA-Z ]+")) {
                break;
            } else System.out.println("Wrong format. Use letters only!");
        }

        String[] ingredientsArray;
        String ingredients;
        System.out.println("Input the ingredients:");

        while (true) {
            boolean incorrectFormat = false;
            ingredients = scanner.nextLine();
            ingredientsArray = ingredients.split(",");

            for (int i = 0; i < ingredientsArray.length; i++) {
                ingredientsArray[i] = ingredientsArray[i].trim();
                if (ingredientsArray[i].isEmpty() || !ingredientsArray[i].matches("[a-zA-Z ]*")) {
                    System.out.println("Wrong format. Use letters only!");
                    incorrectFormat = true;
                    break;
                }
            }
            if (!incorrectFormat) {
                break;
            }
        }

        String insertIntoMeals = "INSERT INTO meals (category, meal, meal_id) VALUES (?, ?, ?)";
        String insertIntoIngredients = "INSERT INTO ingredients (ingredient, ingredient_id, meal_id) VALUES (?, ?, ?)";

        try (Connection connection = getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) AS count FROM meals");

            int meal_id = 1;
            int ingredient_id = 1;

            if (rs1.next()) {
                meal_id = rs1.getInt("count") + 1;
            }
            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) AS count FROM ingredients");
            if (rs2.next()) {
                ingredient_id = rs2.getInt("count") + 1;
            }

            Meal meal = new Meal(category, name, ingredients);
//            meals.add(meal);
            PreparedStatement psMeals = connection.prepareStatement(insertIntoMeals);
            psMeals.setString(1, meal.getCategory());
            psMeals.setString(2, meal.getName());
            psMeals.setInt(3, meal_id);

            PreparedStatement psIngredients = connection.prepareStatement(insertIntoIngredients);

            psIngredients.setString(1, ingredients);
            psIngredients.setInt(2, ingredient_id);
            psIngredients.setInt(3, meal_id);

            psIngredients.executeUpdate();
            psMeals.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to add meal to the database.");
        }

        System.out.println("The meal has been added!");
    }

    public static void showMeal() {
        for (Meal meal : meals) {
            System.out.printf("%nCategory: %s%nName: %s%nIngredients:%n", meal.getCategory(), meal.getName());
            String[] ingredientsArray = meal.getIngredientsArray();
            for (String ingredient : ingredientsArray) {
                System.out.println(ingredient);
            }
        }
        System.out.println();
    }

    public static String[] ingredientsToArray(String ingredients){
        String[] ingredientsArray = ingredients.split(",");
        for (int i = 0; i < ingredientsArray.length; i++) {
            ingredientsArray[i] = ingredientsArray[i].trim();
        }
        return ingredientsArray;
    }
}

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