import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Scanner;
public class OrderManagement {
	 static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
	    static final String USER = "root";
	    static final String PASS = "Naman.jain@2907";

	    static Scanner sc = new Scanner(System.in);

	    public static void main(String[] args) {
	        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
	            int choice;

	            do {
	                System.out.println("\n--- ORDER MANAGEMENT SYSTEM ---");
	                System.out.println("1. Insert Orders");
	                System.out.println("2. Search Orders by Status");
	                System.out.println("3. Increase Order Quantity");
	                System.out.println("4. Update Order Status");
	                System.out.println("5. Display Total Order Value and Delete Cancelled Order");
	                System.out.println("0. Exit");
	                System.out.print("Enter choice: ");
	                choice = sc.nextInt();
	                sc.nextLine();

	                switch (choice) {
	                    case 1:
	                        insertOrders(con);
	                        break;
	                    case 2:
	                        searchOrders(con);
	                        break;
	                    case 3:
	                        increaseQuantity(con);
	                        break;
	                    case 4:
	                        updateStatus(con);
	                        break;
	                    case 5:
	                        totalAndDelete(con);
	                        break;
	                    case 0:
	                        System.out.println("Exiting...");
	                        break;
	                    default:
	                        System.out.println("Invalid choice!");
	                }
	            } while (choice != 0);

	        } catch (SQLException e) {
	            System.out.println("Database error: " + e.getMessage());
	        }
	    }


	    static void insertOrders(Connection con) {
	        String sql = "INSERT INTO Orders VALUES (?, ?, ?, ?, ?, ?)";

	        try (PreparedStatement ps = con.prepareStatement(sql)) {

	            System.out.print("Enter Order ID: ");
	            int id = sc.nextInt();
	            sc.nextLine();

	            System.out.print("Customer Name: ");
	            String customer = sc.nextLine();

	            System.out.print("Product Name: ");
	            String product = sc.nextLine();

	            System.out.print("Quantity: ");
	            int quantity = sc.nextInt();

	            System.out.print("Unit Price: ");
	            double price = sc.nextDouble();
	            sc.nextLine();

	            System.out.print("Status: ");
	            String status = sc.nextLine();

	            ps.setInt(1, id);
	            ps.setString(2, customer);
	            ps.setString(3, product);
	            ps.setInt(4, quantity);
	            ps.setDouble(5, price);
	            ps.setString(6, status);

	            ps.executeUpdate();

	            System.out.println("Order inserted successfully!");

	        } catch (SQLIntegrityConstraintViolationException e) {
	            System.out.println("Duplicate Order ID! Record not inserted.");

	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	    }

	
	    static void searchOrders(Connection con) {
	        System.out.print("Enter order status: ");
	        String status = sc.nextLine();

	        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 " +
	                     "ORDER BY unit_price DESC";

	        try (PreparedStatement ps = con.prepareStatement(sql)) {
	            ps.setString(1, status);

	            try (ResultSet rs = ps.executeQuery()) {
	                while (rs.next()) {
	                    displayOrder(rs);
	                }
	            }
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	    }

	
	    static void increaseQuantity(Connection con) {
	        System.out.print("Enter Order ID: ");
	        int id = sc.nextInt();

	        System.out.print("Enter additional quantity: ");
	        int amount = sc.nextInt();
	        sc.nextLine();

	        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";

	        try (PreparedStatement ps = con.prepareStatement(sql)) {
	            ps.setInt(1, amount);
	            ps.setInt(2, id);

	            int rows = ps.executeUpdate();

	            if (rows > 0)
	                System.out.println("Order quantity updated successfully!");
	            else
	                System.out.println("Order does not exist.");
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	    }

	  
	    static void updateStatus(Connection con) {
	        System.out.print("Enter Order ID: ");
	        int id = sc.nextInt();
	        sc.nextLine();

	        System.out.print("Enter new status: ");
	        String status = sc.nextLine();

	        if (!(status.equalsIgnoreCase("Pending") ||
	              status.equalsIgnoreCase("Shipped") ||
	              status.equalsIgnoreCase("Delivered"))) {
	            System.out.println("Invalid status!");
	            return;
	        }

	        String check = "SELECT * FROM Orders WHERE order_id = ?";
	        String update = "UPDATE Orders SET status = ? WHERE order_id = ?";

	        try (PreparedStatement ps = con.prepareStatement(check)) {
	            ps.setInt(1, id);

	            try (ResultSet rs = ps.executeQuery()) {
	                if (rs.next()) {
	                    try (PreparedStatement ups = con.prepareStatement(update)) {
	                        ups.setString(1, status);
	                        ups.setInt(2, id);
	                        ups.executeUpdate();
	                    }

	                    try (PreparedStatement show = con.prepareStatement(check)) {
	                        show.setInt(1, id);
	                        try (ResultSet updated = show.executeQuery()) {
	                            if (updated.next()) {
	                                System.out.println("Updated order details:");
	                                displayOrder(updated);
	                            }
	                        }
	                    }
	                } else {
	                    System.out.println("Order does not exist.");
	                }
	            }
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	    }

	    static void totalAndDelete(Connection con) {
	        String totalSql = "SELECT SUM(quantity * unit_price) AS total FROM Orders";

	        try (Statement st = con.createStatement();
	             ResultSet rs = st.executeQuery(totalSql)) {
	            if (rs.next()) {
	                System.out.println("Total Order Value: " + rs.getDouble("total"));
	            }
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }

	        System.out.print("Enter Order ID to delete: ");
	        int id = sc.nextInt();
	        sc.nextLine();

	        String sql = "DELETE FROM Orders WHERE order_id = ? AND status = 'Cancelled'";

	        try (PreparedStatement ps = con.prepareStatement(sql)) {
	            ps.setInt(1, id);

	            int rows = ps.executeUpdate();

	            if (rows > 0)
	                System.out.println("Cancelled order deleted successfully!");
	            else
	                System.out.println("Order cannot be deleted. It may not exist or may not be Cancelled.");
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	    }

	 
	    static void displayOrder(ResultSet rs) throws SQLException {
	        System.out.println(
	            "ID: " + rs.getInt("order_id") +
	            " | Customer: " + rs.getString("customer_name") +
	            " | Product: " + rs.getString("product_name") +
	            " | Quantity: " + rs.getInt("quantity") +
	            " | Price: " + rs.getDouble("unit_price") +
	            " | Status: " + rs.getString("status")
	        );
	    }

}
