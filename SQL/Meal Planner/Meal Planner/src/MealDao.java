import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

@SuppressWarnings("ALL")
public class MealDao {

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
            Statement statement = connection.createStatement();
//            statement.executeUpdate("drop table if exists meals");
//            statement.executeUpdate("drop table if exists ingredients");
            statement.executeUpdate("create table if not exists meals (" +
                    "category varchar(20)," +
                    "meal varchar(20)," +
                    "meal_id integer" +
                    ")");
            statement.executeUpdate("create table if not exists ingredients (" +
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

    public static ArrayList<Meal> load_database() {
        ArrayList<Meal> meals = new ArrayList<>();
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
            return meals;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static ArrayList<Meal> load_database(String category) {
        ArrayList<Meal> meals = new ArrayList<>();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet mealSet = statement.executeQuery("select * from meals where category = '" + category + "'" + "order by meal");

            while (mealSet.next()) {
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
            return meals;

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

//    public static ArrayList<Meal> load_database_by_name(String name) {
//
//    }

    public static void planMeal(ArrayList<Meal> meals, Scanner scanner) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] categories = {"breakfast", "lunch", "dinner"};
        String[] mealChoices = new String[21];
        int mealCount = 0;

        for (String day : days) {
            System.out.println(day);
            for (String category : categories) {
                ArrayList<String> tempMeals = showMealNameByCategory(category, scanner);
                System.out.println("Choose the " + category + " for " + day + " from the list above:");

                while (true) {
                    String mealChoice = scanner.nextLine();
                    if (tempMeals.contains(mealChoice)) {
                        mealChoices[mealCount++] = mealChoice;
                        break;
                    } else {
                        System.out.println("This meal doesn’t exist. Choose a meal from the list above:");
                    }
                }
            }
            System.out.println("Yeah! We planned the meals for " + day + ".\n");
        }

        mealCount = 0;
        for (String day : days) {
            System.out.println(day);
            System.out.println("Breakfast: " + mealChoices[mealCount++]);
            System.out.println("Lunch: " + mealChoices[mealCount++]);
            System.out.println("Dinner: " + mealChoices[mealCount++]);
            System.out.println();
        }

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("drop table if exists plan");
            statement.executeUpdate("create table plan (" +
                    "meal varchar(20)," +
                    "category varchar(20)," +
                    "meal_id integer" +
                    ")");
            for (String mealName : mealChoices) {
                ArrayList
                PreparedStatement ps = connection.prepareStatement("insert into plan values (?, ?, ?)");
                ps.setString(1, meal.getCategory());
                ps.setString(2, meal.getName());
                ps.setInt(3, meal_id);
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addMeal(Scanner scanner) {
        System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");

        String category = inputChecker(scanner);

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

    public static void showMeal(Scanner scanner) {
        System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");
        String category = inputChecker(scanner);

        ArrayList<Meal> mealsFromDB = load_database(category);

        if (mealsFromDB.isEmpty()) {
            System.out.println("No meals found.");
            return;
        }
        System.out.printf("Category: %s%n", category);
        for (Meal meal : mealsFromDB) {
            System.out.printf("%nName: %s%nIngredients:%n", meal.getName());
            String[] ingredientsArray = meal.getIngredientsArray();
            for (String ingredient : ingredientsArray) {
                System.out.println(ingredient);
            }
        }
        System.out.println();
    }

    public static ArrayList<String> showMealNameByCategory(String category, Scanner scanner) {
        ArrayList<Meal> mealsFromDB = load_database(category);
        ArrayList<String> mealNames = new ArrayList<>();

        if (mealsFromDB.isEmpty()) {
//            System.out.println("No meals found.");
            return null;
        }
        for (Meal meal : mealsFromDB) {
            System.out.println(meal.getName());
            mealNames.add(meal.getName());
        }
        return mealNames;
    }

    public static String inputChecker(Scanner scanner) {
        String category;
        while (true) {
            category = scanner.nextLine();
            if (category.equals("breakfast") || category.equals("lunch") || category.equals("dinner")) {
                break;
            } else System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
        }
        return category;
    }
}
