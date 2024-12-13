import java.io.*;
import java.util.*;

class User {
    private String userId;
    private String name;
    private String password;
    private String role; // Admin or Customer

    public User(String userId, String name, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return userId + "," + name + "," + password + "," + role;
    }

    public static User fromString(String line) {
        String[] parts = line.split(",");
        return new User(parts[0], parts[1], parts[2], parts[3]);
    }
}

class Product {
    private int productId;
    private String name;
    private double price;
    private int stock;

    public Product(int productId, String name, double price, int stock) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public void reduceStock(int quantity) {
        this.stock -= quantity;
    }

    @Override
    public String toString() {
        return productId + "," + name + "," + price + "," + stock;
    }

    public static Product fromString(String line) {
        String[] parts = line.split(",");
        return new Product(Integer.parseInt(parts[0]), parts[1], Double.parseDouble(parts[2]), Integer.parseInt(parts[3]));
    }
}

public class ShoppingManagementSystem {
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<User> users = new ArrayList<>();
    private static final List<Product> products = new ArrayList<>();
    private static final Map<String, List<String>> orders = new HashMap<>();
    private static final String USERS_FILE = "users.txt";
    private static final String PRODUCTS_FILE = "products.txt";
    private static final String ORDERS_FILE = "orders.txt";

    public static void main(String[] args) {
        loadUsersFromFile();
        loadProductsFromFile();
        loadOrdersFromFile();

        System.out.println("Welcome to the Shopping Management System!");
        while (true) {
            System.out.println("\n1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> {
                    saveUsersToFile();
                    saveProductsToFile();
                    saveOrdersToFile();
                    System.out.println("Thank you for using the system!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void register() {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();
        if (getUserById(userId) != null) {
            System.out.println("User ID already exists. Try a different one.");
            return;
        }

        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        System.out.print("Enter Role (Admin/Customer): ");
        String role = scanner.nextLine();

        if (!role.equalsIgnoreCase("Admin") && !role.equalsIgnoreCase("Customer")) {
            System.out.println("Invalid role. Registration failed.");
            return;
        }

        users.add(new User(userId, name, password, role));
        System.out.println("Registration successful!");
    }

    private static void login() {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        User user = getUserById(userId);
        if (user == null || !user.getPassword().equals(password)) {
            System.out.println("Invalid credentials. Try again.");
            return;
        }

        if (user.getRole().equalsIgnoreCase("Admin")) {
            adminMenu();
        } else {
            customerMenu(userId);
        }
    }

    private static void adminMenu() {
        System.out.println("Admin Login Successful!");
        while (true) {
            System.out.println("\n1. Add Product");
            System.out.println("2. View Products");
            System.out.println("3. Logout");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> addProduct();
                case 2 -> viewProducts();
                case 3 -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void customerMenu(String userId) {
        System.out.println("Customer Login Successful!");
        while (true) {
            System.out.println("\n1. View Products");
            System.out.println("2. Buy Product");
            System.out.println("3. View Purchase History");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> viewProducts();
                case 2 -> buyProduct(userId);
                case 3 -> viewPurchaseHistory(userId);
                case 4 -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void addProduct() {
        System.out.print("Enter Product Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Product Price: ");
        double price = scanner.nextDouble();
        System.out.print("Enter Product Stock: ");
        int stock = scanner.nextInt();

        int productId = products.size() + 1;
        products.add(new Product(productId, name, price, stock));
        System.out.println("Product added successfully!");
    }

    private static void viewProducts() {
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }
        System.out.println("Available Products:");
        for (Product product : products) {
            System.out.println("ID: " + product.getProductId() + ", Name: " + product.getName() +
                               ", Price: " + product.getPrice() + ", Stock: " + product.getStock());
        }
    }

    private static void buyProduct(String userId) {
        System.out.print("Enter Product ID: ");
        int productId = scanner.nextInt();
        System.out.print("Enter Quantity: ");
        int quantity = scanner.nextInt();

        Product product = getProductById(productId);
        if (product == null || product.getStock() < quantity) {
            System.out.println("Invalid product or insufficient stock.");
            return;
        }

        product.reduceStock(quantity);
        orders.computeIfAbsent(userId, k -> new ArrayList<>()).add(product.getName() + " x " + quantity);
        System.out.println("Purchase successful!");
    }

    private static void viewPurchaseHistory(String userId) {
        List<String> userOrders = orders.get(userId);
        if (userOrders == null || userOrders.isEmpty()) {
            System.out.println("No purchase history found.");
            return;
        }
        System.out.println("Your Purchase History:");
        for (String order : userOrders) {
            System.out.println(order);
        }
    }

    private static User getUserById(String userId) {
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    private static Product getProductById(int productId) {
        for (Product product : products) {
            if (product.getProductId() == productId) {
                return product;
            }
        }
        return null;
    }

    private static void loadUsersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                users.add(User.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    private static void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User user : users) {
                writer.write(user.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private static void loadProductsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(PRODUCTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                products.add(Product.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("Error loading products: " + e.getMessage());
        }
    }

    private static void saveProductsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRODUCTS_FILE))) {
            for (Product product : products) {
                writer.write(product.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving products: " + e.getMessage());
        }
    }

    private static void loadOrdersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ORDERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                orders.put(parts[0], Arrays.asList(parts[1].split(",")));
            }
        } catch (IOException e) {
            System.out.println("Error loading orders: " + e.getMessage());
        }
    }

    private static void saveOrdersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ORDERS_FILE))) {
            for (Map.Entry<String, List<String>> entry : orders.entrySet()) {
                writer.write(entry.getKey() + ":" + String.join(",", entry.getValue()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving orders: " + e.getMessage());
        }
    }
}

