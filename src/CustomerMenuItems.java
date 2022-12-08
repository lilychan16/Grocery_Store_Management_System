import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Public class to hold all menu items on customer's end.
 */
public class CustomerMenuItems {

  CustomerOperations customerOperations;
  SharedHelperMethods sharedHelperMethods;

  /**
   * Constructor for CustomerMenuItems class.
   * @param customerOperations an object from CustomerOperations class
   * @param sharedHelperMethods an object from SharedHelperMethods class
   */
  public CustomerMenuItems(CustomerOperations customerOperations,
                           SharedHelperMethods sharedHelperMethods) {

    this.customerOperations = customerOperations;
    this.sharedHelperMethods = sharedHelperMethods;
  }


  /**
   * Print customer-end login menu in console and then print more customer menu items
   * based on user input.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void customer_login_menu(Connection con, Scanner sc) throws Exception {

    GroceryStoreApp groceryStoreApp = new GroceryStoreApp();

    String customer_login_input = "";

    while (true) {
      System.out.println("\nCustomer Login Page:");
      System.out.println("Please select an option:\n1. Customer Login"
              + "\n2. Back to User Type Menu\n3. Quit");

      if (sc.hasNext()) {
        customer_login_input = sc.next();
      }

      switch (customer_login_input) {
        case "1":
          this.validate_customer_login(con, sc);

        case "2":
          groceryStoreApp.main_menu(con, sc);

        case "3":
          System.exit(0);

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Check if the customer login inputs are valid, then print customer information
   * in console and direct to customer menu options.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void validate_customer_login(Connection con, Scanner sc) throws Exception {

    ArrayList<String> customer_id_list = new ArrayList<>();

    String customer_id = "";
    String customer_password = "";

    CallableStatement cs_customer_all = con.prepareCall(
            "{call queryCustomerAll()}"
    );

    ResultSet rs_customer_all = cs_customer_all.executeQuery();

    while (rs_customer_all.next()) {
      customer_id_list.add(Integer.toString(rs_customer_all.getInt(1)));
    }

    System.out.print("Please enter customer id: ");
    if (sc.hasNext()) {
      customer_id = sc.next();
    }

    while (!customer_id_list.contains(customer_id)) {
      System.out.println("You entered invalid input. Please re-enter customer id or 0 to quit");
      System.out.print("Please enter customer id: ");
      if (sc.hasNext()) {
        customer_id = sc.next();

        if (customer_id.equals("0")) {
          System.exit(0);
        }
      }
    }

    System.out.print("Please enter customer password: ");
    if (sc.hasNext()) {
      customer_password = sc.next();
    }

    // Default password for customer login
    while (!customer_password.equals("123456")) {
      System.out.println("You entered invalid input. Please re-enter customer password "
              + "or 0 to quit");
      System.out.print("Please enter customer password: ");
      if (sc.hasNext()) {
        customer_password = sc.next();

        if (customer_password.equals("0")) {
          System.exit(0);
        }
      }
    }

    System.out.println("\nLogin successfully.");
    System.out.println("\nCustomer Information:");

    CallableStatement cs_customer_id = con.prepareCall(
            "{call queryCustomerById(?)}"
    );

    cs_customer_id.setInt(1, Integer.parseInt(customer_id));

    ResultSet rs_customer_id = cs_customer_id.executeQuery();

    sharedHelperMethods.print_formatted_customer_table(rs_customer_id);

    this.customer_menu(con, sc, customer_id);

    rs_customer_all.close();
    cs_customer_all.close();

    rs_customer_id.close();
    cs_customer_id.close();
  }


  /**
   * Print customer menu and then perform an operation in console based on user input.
   * The allowed operations for a customer are:
   *    (1) Look up product info by product id
   *    (2) Look up product info by product name
   *    (3) Redeem points to grocery dollars
   *    (4) Check orders
   *    (5) Check cart & Check out
   *    (6) Go back to customer login menu
   *    (7) Quit
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void customer_menu(Connection con, Scanner sc, String customer_id) throws Exception {

    String customer_menu_input = "";

    while (true) {
      System.out.println("\nPlease select an option:"
              + "\n1. Look up product info by product id & Add to cart"
              + "\n2. Look up product info by product name & Add to cart"
              + "\n3. Redeem all reward points to grocery dollars\n4. Check orders"
              + "\n5. Check cart & Check out & Update cart\n6. Go back to customer login menu"
              + "\n7. Quit");

      if (sc.hasNext()) {
        customer_menu_input = sc.next();
      }

      switch(customer_menu_input) {
        case "1":
          customerOperations.customer_look_up_product_by_id(con, sc);
          this.customer_after_result_menu(con, sc, customer_id);

        case "2":
          customerOperations.customer_look_up_product_by_name(con, sc);
          this.customer_after_result_menu(con, sc, customer_id);

        case "3":
          customerOperations.redeem_points(con, sc, customer_id);
          this.customer_after_result_menu(con, sc, customer_id);

        case "4":
          customerOperations.check_orders(con, sc, customer_id);
          this.customer_after_result_menu(con, sc, customer_id);

        case "5":
          customerOperations.cart_operations(con, sc, customer_id);
          this.customer_after_result_menu(con, sc, customer_id);

        case "6":
          this.customer_login_menu(con, sc);

        case "7":
          System.exit(0);

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * If a customer's menu selection involves calling a SQL procedure, then once it's done,
   * a prompt will show up in console to ask the customer to either go back to the previous menu
   * (which is customer's main menu) or quit.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void customer_after_result_menu(Connection con, Scanner sc, String customer_id)
                                                                                throws Exception {

    String customer_after_result_input = "";

    while (true) {
      System.out.print("\nPlease select 1 to go back to customer main menu, or 0 to quit: ");

      if (sc.hasNext()) {
        customer_after_result_input = sc.next();
      }

      switch (customer_after_result_input) {
        case "1":
          this.customer_menu(con, sc, customer_id);

        case "0":
          System.exit(0);

        default:
          System.out.print("\nInvalid input, please re-enter");
      }
    }
  }
}
