import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Public class to create a connection to MySQL database,
 * start point to run the application by printing the main menu.
 */
public class GroceryStoreApp {

  /**
   * Create a new MySQL database connection.
   * @param sc the scanner to receive user input
   * @return a connection to the database
   * @throws SQLException if failed to establish a connection
   */
  public Connection getConnection(Scanner sc) throws SQLException {

    Connection con;
    String user = "";
    String password = "";

    System.out.print("Please enter MySQL username: ");
    if (sc.hasNext()) {
      user = sc.next();
    }

    System.out.print("Please enter MySQL password: ");
    if (sc.hasNext()) {
      password = sc.next();
    }

    con = DriverManager.getConnection("jdbc:mysql://localhost/grocery_store?"
            + "user=" + user + "&password=" + password);

    return con;
  }


  /**
   * Print main menu in console and then print login menu items based on user type.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void main_menu(Connection con, Scanner sc) throws Exception {

    SharedHelperMethods sharedHelperMethods = new SharedHelperMethods();

    StoreManagerOperations storeManagerOperations = new StoreManagerOperations();
    WarehouseManagerOperations warehouseManagerOperations
                                            = new WarehouseManagerOperations(sharedHelperMethods);
    CashierCleanerOperations cashierCleanerOperations = new CashierCleanerOperations();

    CustomerOperations customerOperations = new CustomerOperations(sharedHelperMethods);

    EmployeeMenuItems employeeMenu = new EmployeeMenuItems(storeManagerOperations,
                                                            warehouseManagerOperations,
                                                            cashierCleanerOperations);

    CustomerMenuItems customerMenu = new CustomerMenuItems(customerOperations, sharedHelperMethods);

    String user_type = "";

    while (true) {
      System.out.println("\nPlease select a user type:\n1. Customer\n2. Employee\n3. Quit");

      if (sc.hasNext()) {
        user_type = sc.next();
      }

      switch (user_type) {
        case "1":
          customerMenu.customer_login_menu(con, sc);

        case "2":
          employeeMenu.employee_login_menu(con, sc);

        case "3":
          System.exit(0);

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Establish a connection to MySQL database, then start the application by calling
   * method to print the main menu, eventually close the database connection and scanner
   * once the operations are done.
   */
  public void run() {

    Scanner sc = new Scanner(System.in);

    // Connect to MySQL
    Connection con = null;
    try {
      con = this.getConnection(sc);
      System.out.println("Connected to database");
    } catch (SQLException e) {
      System.out.println("ERROR: Could not connect to the database");
      e.printStackTrace();
    }

    // Provide user with menu options
    try {
      if (con != null) {
        main_menu(con, sc);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Close the connection to MySQL database
    try {
      if (con != null) {
        con.close();
      }
    } catch (SQLException e) {
      System.out.println("ERROR: Could not close the connection");
      e.printStackTrace();
    }

    // Close the scanner to prevent memory leak
    try {
      sc.close();
    } catch (Exception e) {
      System.out.println("ERROR: Could not close the scanner");
      e.printStackTrace();
    }
  }


  public static void main(String[] args) {
    GroceryStoreApp app = new GroceryStoreApp();
    app.run();
  }
}