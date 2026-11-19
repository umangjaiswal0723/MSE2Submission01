package com.kiet.inventory.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class OrderJdbcApp {

    private static final String URL =
            "jdbc:mysql://localhost:3306/ecommerce_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Umangjaiswal123@";

    private static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n1. Insert order");
            System.out.println("2. Search orders by status");
            System.out.println("3. Add quantity to order");
            System.out.println("4. Update order status");
            System.out.println("5. Show total value and delete cancelled order");
            System.out.println("6. Exit");

            int choice = readInt("Choose: ");

            try {
                switch (choice) {
                    case 1:
                        insertOrder();
                        break;
                    case 2:
                        searchOrders();
                        break;
                    case 3:
                        addQuantity();
                        break;
                    case 4:
                        updateStatus();
                        break;
                    case 5:
                        totalAndDelete();
                        break;
                    case 6:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static int readInt(String message) {
        System.out.print(message);
        return Integer.parseInt(input.nextLine());
    }

    private static double readDouble(String message) {
        System.out.print(message);
        return Double.parseDouble(input.nextLine());
    }

    private static String readText(String message) {
        System.out.print(message);
        return input.nextLine().trim();
    }

    private static String validStatus(String value) {
        String[] statuses = {
                "Pending", "Shipped", "Delivered", "Cancelled"
        };

        for (String status : statuses) {
            if (status.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }

    private static void insertOrder() throws SQLException {
        int id = readInt("Order ID: ");
        String customer = readText("Customer name: ");
        String product = readText("Product name: ");
        int quantity = readInt("Quantity: ");
        double price = readDouble("Unit price: ");

        String enteredStatus = readText(
                "Status (Pending/Shipped/Delivered/Cancelled): ");
        String status = validStatus(enteredStatus);

        if (quantity <= 0 || price <= 0 || status == null) {
            System.out.println("Enter positive quantity/price and valid status.");
            return;
        }

        try (Connection con = connect()) {
            String checkSql =
                    "SELECT order_id FROM `Orders` WHERE order_id = ?";

            try (PreparedStatement check = con.prepareStatement(checkSql)) {
                check.setInt(1, id);

                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Duplicate order ID.");
                        return;
                    }
                }
            }

            String sql = "INSERT INTO `Orders` "
                    + "(order_id, customer_name, product_name, "
                    + "quantity, unit_price, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setString(2, customer);
                ps.setString(3, product);
                ps.setInt(4, quantity);
                ps.setDouble(5, price);
                ps.setString(6, status);

                ps.executeUpdate();
                System.out.println("Order inserted.");
            }
        }
    }

    private static void searchOrders() throws SQLException {
        String status = validStatus(readText("Enter status: "));

        if (status == null) {
            System.out.println("Invalid status.");
            return;
        }

        String sql = "SELECT * FROM `Orders` "
                + "WHERE status = ? AND quantity > 2 "
                + "ORDER BY unit_price DESC";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    printOrder(rs);
                }
            }
        }
    }

    private static void addQuantity() throws SQLException {
        int id = readInt("Order ID: ");
        int extraQuantity = readInt("Additional quantity: ");

        if (extraQuantity <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }

        String sql = "UPDATE `Orders` "
                + "SET quantity = quantity + ? WHERE order_id = ?";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, extraQuantity);
            ps.setInt(2, id);

            if (ps.executeUpdate() > 0) {
                System.out.println("Quantity updated.");
            } else {
                System.out.println("Order not found.");
            }
        }
    }

    private static void updateStatus() throws SQLException {
        int id = readInt("Order ID: ");
        String status = validStatus(readText(
                "New status (Pending/Shipped/Delivered/Cancelled): "));

        if (status == null) {
            System.out.println("Invalid status.");
            return;
        }

        try (Connection con = connect()) {
            String selectSql =
                    "SELECT * FROM `Orders` WHERE order_id = ?";

            try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Order not found.");
                        return;
                    }
                    System.out.println("Existing order:");
                    printOrder(rs);
                }
            }

            String updateSql =
                    "UPDATE `Orders` SET status = ? WHERE order_id = ?";

            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setString(1, status);
                ps.setInt(2, id);
                ps.executeUpdate();
            }

            System.out.println("Updated order:");
            try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        printOrder(rs);
                    }
                }
            }
        }
    }

    private static void totalAndDelete() throws SQLException {
        String totalSql =
                "SELECT COALESCE(SUM(quantity * unit_price), 0) AS total_value "
              + "FROM `Orders`";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(totalSql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println("Total order value: "
                        + rs.getDouble("total_value"));
            }
        }

        int id = readInt("Order ID to delete: ");

        String deleteSql = "DELETE FROM `Orders` "
                + "WHERE order_id = ? AND status = 'Cancelled'";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(deleteSql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() > 0) {
                System.out.println("Cancelled order deleted.");
            } else {
                System.out.println(
                        "Cannot delete: order missing or not Cancelled.");
            }
        }
    }

    private static void printOrder(ResultSet rs) throws SQLException {
        System.out.println(
                rs.getInt("order_id") + " | "
              + rs.getString("customer_name") + " | "
              + rs.getString("product_name") + " | "
              + rs.getInt("quantity") + " | "
              + rs.getDouble("unit_price") + " | "
              + rs.getString("status"));
    }
}