import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static ArrayList<Meal> meals = new ArrayList<>();

    public static void main(String[] args) {
        MealDao.initialize_database();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            meals = MealDao.load_database();
            System.out.println("What would you like to do (add, show, exit)?");
            String option = scanner.nextLine();
            switch (option) {
                case "add" -> MealDao.addMeal(scanner);
                case "show" -> {
                    if (meals.isEmpty()) {
                        System.out.println("No meals saved. Add a meal first.");
                        continue;
                    }
                    MealDao.showMeal(meals, scanner);
                }
                case "exit" -> {
                    System.out.println("Bye!");
                    return;
                }
            }
        }
    }
}